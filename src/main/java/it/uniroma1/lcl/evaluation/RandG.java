package it.uniroma1.lcl.evaluation;

import it.uniroma1.lcl.adw.comparison.Cosine;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import it.uniroma1.lcl.adw.semsig.LKB;
import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigProcess;
import it.uniroma1.lcl.adw.utils.CorrelationCalculator;
import it.uniroma1.lcl.jlt.util.Files;
import it.uniroma1.lcl.jlt.util.Pair;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import edu.mit.jwi.item.POS;

public class RandG 
{
	
	public static void main(String args[])
	{
		int vSize = 0;
		
		LinkedHashMap<Pair<String,String>,Double> pairs = readPairs("resources/datasets/dataset.tsv");
		boolean smallerBetter = false;
		
		List<Double> givenScores = new ArrayList<Double>();
		List<Double> goldScores = new ArrayList<Double>();
		
		try
		{
			for(Pair<String,String> aPair : pairs.keySet())
			{
				String word1 = aPair.getFirst();
				String word2 = aPair.getSecond();
				
				double score = getClosestPairScore(word1, word2, POS.NOUN, vSize, smallerBetter);
					
				System.out.println(word1+"\t"+word2+"\t"+score);
				
				givenScores.add(score);
				goldScores.add(pairs.get(aPair));
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		double p = CorrelationCalculator.getPearson(givenScores, goldScores);
		double s = CorrelationCalculator.getSpearman(givenScores, goldScores);
		
		System.out.println("==================================");
		System.out.println("Pearson:\t"+ p);
		System.out.println("Spearman:\t"+ s);
	}
	
	public static double getClosestPairScore(
			String word1, String word2, 
			POS tag, 
			int size, 
			boolean smallerBetter)
	{
	
		List<SemSig> v1s = SemSigProcess.getInstance().getWordSemSigs(word1, tag, LKB.WordNetGloss, size, true);
		List<SemSig> v2s = SemSigProcess.getInstance().getWordSemSigs(word2, tag, LKB.WordNetGloss, size, true);
		
		WeightedOverlap WO = new WeightedOverlap();
		
		double maxSim = (smallerBetter)? 1000 : 0;
		
		for(SemSig v1 : v1s)
		{
			for(SemSig v2 : v2s)
			{
				double currentSim = WO.compare(v1.getVector(size), v2.getVector(size), false);
//				double currentSim = Cosine.cosineSimilarity(v1.getVector(size), v2.getVector(size));
				
				if(smallerBetter)
				{
					if(currentSim < maxSim)
					{
						maxSim = currentSim;
					}
				}
				else
				{
					if(currentSim > maxSim)
					{
						maxSim = currentSim;
					}
				}
			}
		}
			
		return maxSim;
	}

	public static LinkedHashMap<Pair<String,String>,Double> readPairs(String path)
	{
		LinkedHashMap<Pair<String,String>,Double> pairList = new LinkedHashMap<Pair<String,String>,Double>();
		
		try
		{
			BufferedReader br = Files.getBufferedReader(path);
			
			while(br.ready())
			{
				String line = br.readLine();
				
				String source = line.split("\t")[0].trim();
				String target = line.split("\t")[1].trim();
				double score = Double.parseDouble(line.split("\t")[2].trim());
				
				pairList.put(new Pair<String,String>(source,target), score);
			}
			
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return pairList;
	}
	
}

