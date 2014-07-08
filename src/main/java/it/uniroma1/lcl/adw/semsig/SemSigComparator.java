package it.uniroma1.lcl.adw.semsig;

import it.uniroma1.lcl.adw.comparison.KLDivergence;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.jlt.wordnet.WordNet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;


public class SemSigComparator
{
	public static Double compare(SemSig v1, SemSig v2, SignatureComparison measure, int size, double oovScore)
	{
		if(v1 == null || v2 == null)
			return oovScore;
		
		return compareSortedNormalizedMaps(v1.getVector(), v2.getVector(), measure, size);
	}
	
	public static Double compare(
			SemSig v1, 
			SemSig v2, 
			SignatureComparison measure, 
			int size)
	{
		if(v1 == null || v2 == null)
			return 0.0;
		
		return compareSortedNormalizedMaps(v1.getVector(), v2.getVector(), measure, size);
	}
	
	public static Double compareSortedNormalizedMaps(
			LinkedHashMap<Integer,Float> vec1, 
			LinkedHashMap<Integer,Float> vec2, 
			SignatureComparison measure, 
			int size)
	{
		return compare(vec1, vec2, measure, size, true);
	}
	
	public static Double compare(
			LinkedHashMap<Integer,Float> vec1, 
			LinkedHashMap<Integer,Float> vec2, 
			SignatureComparison measure, int size, 
			boolean sortedNormalized)
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
		
		return measure.compare(vec1, vec2);
		
	}

	public static HashMap<Integer,Integer> ListToMap(List<Integer> list)
	{
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		int index = 0;
		
		for(Integer s : list)
			map.put(s,index++);
		
		return map;
	}
	
	public void getClosestSenses(String w1, POS tag1, String w2, POS tag2, LKB lkb, SignatureComparison measure, int size)
	{
		double maxSim = 0;
		IWord src = null;
		IWord trg = null;
		
		for(IWord sense1 : WordNet.getInstance().getSenses(w1, tag1))
		{
			SemSig v1 = SemSigProcess.getInstance().getSemSigFromOffset(GeneralUtils.fixOffset(sense1.getSynset().getOffset(), tag1), lkb, size);
		    
			for(IWord sense2 : WordNet.getInstance().getSenses(w2, tag2))
			{
				SemSig v2 = SemSigProcess.getInstance().getSemSigFromOffset(GeneralUtils.fixOffset(sense2.getSynset().getOffset(), tag2), lkb, size);	
				
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
	    
		vc.getClosestSenses("fire", POS.VERB, "probe", POS.NOUN, LKB.WordNetGloss, new WeightedOverlap(), 0);
		
		System.exit(0);
	      
		SemSig v1 = SemSigProcess.getInstance().getSemSigFromOffset("03519981-n", LKB.WordNetGloss, 0);	//highway
		SemSig v2 = SemSigProcess.getInstance().getSemSigFromOffset("03691459-n", LKB.WordNetGloss, 0);	//road
		
		System.out.println(v1.getVector().size()+"\t"+v2.getVector().size());
		
		System.out.println(SemSigComparator.compare(v1, v2, new KLDivergence(), 0) );
	}
}
