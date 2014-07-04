package it.uniroma1.lcl.adw.semsig;

import it.uniroma1.lcl.adw.SimilarityMeasure;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.jlt.util.IntegerCounter;
import it.uniroma1.lcl.jlt.util.Maths;
import it.uniroma1.lcl.jlt.wordnet.WordNet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;


public class SemSigComparator
{
	public static Double compare(SemSig v1, SemSig v2, SimilarityMeasure measure, int size, double oovScore)
	{
		if(v1 == null || v2 == null)
			return oovScore;
		
		return compareSortedNormalizedMaps(v1.getVector(), v2.getVector(), measure, size);
	}
	
	public static Double compare(SemSig v1, SemSig v2, SimilarityMeasure measure, int size)
	{
		if(v1 == null || v2 == null)
			return 0.0;
		
		return compareSortedNormalizedMaps(v1.getVector(), v2.getVector(), measure, size);
	}
	
	public static Double compareSortedNormalizedMaps(LinkedHashMap<Integer,Float> vec1, LinkedHashMap<Integer,Float> vec2, SimilarityMeasure measure, int size)
	{
		return compare(vec1, vec2, measure, size, true);
	}
	
	public static Double compare(LinkedHashMap<Integer,Float> vec1, LinkedHashMap<Integer,Float> vec2, 
			SimilarityMeasure measure, int size, boolean sortedNormalized)
	{
		int newSize = size;
		
		if(size == 0)
		{
			newSize = (vec1.size() < vec2.size()) ? vec1.size() : vec2.size(); 
		}
		
		if(size > vec1.size())
			newSize = vec1.size();
		
		if(size > vec2.size())
			newSize = vec2.size();
		
		if(!sortedNormalized || newSize != size || size != vec1.size() || size != vec2.size())  
		{
			vec1 = SemSigUtils.truncateSemSig(vec1, newSize, true);
			vec2 = SemSigUtils.truncateSemSig(vec2, newSize, true);
		}
		
		size = newSize;
		
		switch(measure)
		{
			case COSINE:
				return Maths.cosineSimilarity(vec1, vec2);
				
			case WEIGHTED_OVERLAP:
				return getNumberOfOverlaps(vec1, vec2, true);
				
			case OVERLAP:
				return getNumberOfOverlaps(vec1, vec2, false);
				
			case KL_DIVERGENCE:
				return calculateKLDiv(vec1, vec2);
				
			case JENSEN_SHANNON:
				return calculateJS(vec1, vec2);
				
		}
		
		return null;
	}
	
	private static Double calculateKLDiv(LinkedHashMap<Integer,Float> vec1, LinkedHashMap<Integer,Float> vec2) 
	{
		double DKL = 0.0;
		
		for(Integer key : vec1.keySet())
		{
			double P = vec1.get(key);

			if(!vec2.containsKey(key))
			{
				//System.out.println("There is no key "+key+" in the vector!");
				//System.exit(0);
				continue;
			}

			double Q = vec2.get(key);
			
			DKL += Math.log(P/Q) * P;
		}
	
		return DKL;
	}

	private static Double calculateJS(LinkedHashMap<Integer, Float> vec1, LinkedHashMap<Integer, Float> vec2) 
	{
		double JS = 0.0;
		
		if(vec1.keySet().size() == 0 || vec2.keySet().size() == 0)
			return JS;
		
		for(Integer key : vec1.keySet())
		{
			double P = vec1.get(key);
			double Q = 0;
			
			if(vec2.containsKey(key))
			{
				Q = vec2.get(key);
			}

			Q = (P+Q)/2;
			JS += Math.log(P/Q) * P;
		}
	
		for(Integer key : vec2.keySet())
		{
			double P = vec2.get(key);
			double Q = 0;
			
			if(vec1.containsKey(key))
			{
				Q = vec1.get(key);
			}

			Q = (P+Q)/2;
			JS += Math.log(P/Q) * P;
		}
		
		return JS;
	}
	
	public static HashMap<Integer,Integer> ListToMap(List<Integer> list)
	{
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		int index = 0;
		
		for(Integer s : list)
			map.put(s,index++);
		
		return map;
	}
	
	public static double getNumberOfOverlaps(Map<Integer,Float> v1, Map<Integer,Float> v2, boolean weighted)
	{
		//assumes that the given vectors are sorted
		double overlaps = 0;
		double normalization = 0;
		List<Integer> v2Keys = new ArrayList<Integer>(v2.keySet());
		List<Integer> v1Keys = new ArrayList<Integer>(v1.keySet());
		
		HashMap<Integer,Integer> map = ListToMap(v2Keys);
		
		Set<Integer> v2KeysSet = v2.keySet();
		
		int index = 0;
		for(Integer s : v1Keys)
		{
			if(v2KeysSet.contains(s))
			{
				if(weighted)
				{
					//overlaps += 1.0/((index+1)+(v2Keys.indexOf(s)+1));	//linear
					//overlaps += (double)1/Math.exp(v1Keys.indexOf(s)+v2Keys.indexOf(s));	//exponential
					overlaps += 1.0/((index+1)+(map.get(s)+1));	//linear
					normalization += 1.0/(2*(index+1));
				}
				else
				{
					overlaps++;
					normalization++;
				}
			
				index++;
			}
			
			
		}
		
		if(overlaps == 0 || normalization == 0)
			return 0;
		
		return overlaps/normalization;
	}

	public static double getSynsetWordOverlapScore(Set<String> wordsInA, Set<String> wordsInB)
	{
		double sizeA = wordsInA.size();
		double sizeB = wordsInB.size();
		
		wordsInA.retainAll(wordsInB);
		double overlap = wordsInA.size();
		
		//formula from TakeLab 2012
		return (  2.0  /  ((sizeA/overlap) + (sizeB/overlap)) );
	}
	
	public static String manualCorrection(String in)
	{
		return in.replaceAll(",", "");
	}
	
	public static IntegerCounter<String> getSynsetFrequency(String path)
	{
		return GeneralUtils.counterReader(path);
	}
	
	public static Set<Integer> getOverlaps(List<SemSig> vectors, int topN)
	{
		Set<Integer> overlap = new HashSet<Integer>();
		
		overlap = SemSigUtils.truncateSortedVector(vectors.get(0).getVector(),topN).keySet();
		
		for(int i=1; i<vectors.size(); i++)
			overlap.retainAll(SemSigUtils.truncateSortedVector(vectors.get(i).getVector(),topN).keySet());
		
		return overlap;
	}
	
	public void getClosestSenses(String w1, POS tag1, String w2, POS tag2, LKB lkb, SimilarityMeasure measure, int size)
	{
		double maxSim = 0;
		IWord src = null;
		IWord trg = null;
		
		//IndexVectoring IV = new IndexVectoring(LKB.WordNetGloss);
		
		for(IWord sense1 : WordNet.getInstance().getSenses(w1, tag1))
		{
			SemSig v1 = SemSigProcess.getInstance().getSemSigFromOffset(GeneralUtils.fixOffset(sense1.getSynset().getOffset(), tag1), lkb, size);
			//Vector v1 = IV.getVectorFromOffset(general.fixOffset(sense1.getSynset().getOffset(), tag1));
		    
			for(IWord sense2 : WordNet.getInstance().getSenses(w2, tag2))
			{
				SemSig v2 = SemSigProcess.getInstance().getSemSigFromOffset(GeneralUtils.fixOffset(sense2.getSynset().getOffset(), tag2), lkb, size);	
				//Vector v2 = IV.getVectorFromOffset(general.fixOffset(sense2.getSynset().getOffset(), tag2));
				
				double currentSim = SemSigComparator.compareSortedNormalizedMaps(v1.getVector(), v2.getVector(), measure, size);
				
				
				System.out.print(sense1+"\t");
				System.out.print(sense2+"\t");
				System.out.println(currentSim);
				
				
				if(maxSim < currentSim)
				{
					src = sense1;
					trg = sense2;
					maxSim = currentSim;
				}
			}
		}
		
		System.out.println(src);
		System.out.println(trg);
		System.out.println(maxSim);
	}
	
	public static void main(String args[])
	{
		
		SemSigComparator vc = new SemSigComparator();
	    
		vc.getClosestSenses("fire", POS.VERB, "probe", POS.NOUN, LKB.WordNetGloss, SimilarityMeasure.WEIGHTED_OVERLAP, 0);
		
		System.exit(0);
	      
		/*
		boolean normalizeByStatic = false;
		SimilarityMeasure measure = SimilarityMeasure.WEIGHTED_OVERLAP;
		int vectorSize = 100;
		String vectorPath1 = "/home/pilehvar//UKB/ukb-2.0/fingerprint/TS/test/SMTnews/vectors/side.a.379.ppv";
		//String vectorPath2 = "/home/pilehvar//UKB/ukb-2.0/fingerprint/TS/test/MSRpar.babel/vectors/side.b.34.ppv";
		String vectorPath2 = "/home/pilehvar//UKB/ukb-2.0/fingerprint/TS/test/SMTnews/vectors/side.b.379.ppv";
		double sim = compare(vectorPath2, vectorPath1, vectorSize, measure, normalizeByStatic, LKB.WordNet);
		System.out.println(sim);
		*/
		
		/*
		SemSig v1 = Vectoring.getWordVector("ride", POS.VERB, LKB.WordNet);
		SemSig v2 = Vectoring.getWordVector("drive", POS.VERB, LKB.WordNet);
		SemSig v3 = Vectoring.getWordVector("motorcycle", POS.NOUN, LKB.WordNet);
		
		*/
		SemSig v1 = SemSigProcess.getInstance().getSemSigFromOffset("03519981-n", LKB.WordNetGloss, 0);	//highway
		SemSig v2 = SemSigProcess.getInstance().getSemSigFromOffset("03691459-n", LKB.WordNetGloss, 0);	//road
		//SemSig v3 = Vectoring.getVectorFromOffset("03790512-n", LKB.WordNetGloss, 1000);	//motorcycle
		
		
		//SemSig v1 = Vectoring.getCustomVectorFrom("/media/backup/fromSDA/ppvs.30g.full/02574977-v.ppv", false);
		//SemSig v2 = Vectoring.getCustomVectorFromCompressed("/home/pilehvar/UKB/ukb-2.0/ppvs.30g.full/02574977-v.ppv", 0, false);
		
		System.out.println(v1.getVector().size()+"\t"+v2.getVector().size());
		
//		long prev = System.currentTimeMillis();
//		for(int i=0; i<=100; i++)

		System.out.println(SemSigComparator.compare(v1, v2, SimilarityMeasure.KL_DIVERGENCE, 0) );
		
//		long curr = System.currentTimeMillis();
//		System.out.println(curr - prev);
		
		/*
		IntegerCounter<String> counter = getSynsetFrequency("/home/pilehvar/UKB/ukb-2.0/fingerprint/TS/test/MSRpar/frequencies.100");
		
		Vector v1 = cg.getCustomVectorFrom(vectorPath1, false);
		Vector v2 = cg.getCustomVectorFrom(vectorPath2, false);

		LinkedHashMap<String,Double> wv1 = vc.vectorReWeighter(v1,counter, vectorSize);
		LinkedHashMap<String,Double> wv2 = vc.vectorReWeighter(v2,counter, vectorSize);
		
		sim = compare(wv1, wv2, measure, vectorSize);
		
		System.out.println(sim);
		*/		
	}
}
