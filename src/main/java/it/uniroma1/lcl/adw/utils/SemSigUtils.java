package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.jlt.util.Maps;
import it.uniroma1.lcl.jlt.util.Maps.SortingOrder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SemSigUtils 
{
	
	public static LinkedHashMap<Integer,Float> truncateSortedVector(LinkedHashMap<Integer,Float> vector, int size)
	{
		LinkedHashMap<Integer,Float> sortedMap = new LinkedHashMap<Integer,Float>();
		
		int i = 1;
		for(int key : vector.keySet())
		{
			sortedMap.put(key, vector.get(key));
			if(i++ > size) break;
		}
		
		return normalizeSemSigs(sortedMap);
	}
	
	public static Map<Integer,Float> normalizeVector(Map<Integer,Float> vector)
	{
		float total = 0;
		for(int s : vector.keySet())
			total += vector.get(s);
		
		Map<Integer,Float> normalizedVector = new HashMap<Integer,Float>();
		
		for(int s : vector.keySet())
			normalizedVector.put(s, vector.get(s)/total);
		
		return normalizedVector;
	}
	
	
	public static LinkedHashMap<Integer,Float> normalizeSemSigs(LinkedHashMap<Integer,Float> vector)
	{
		float total = 0;
		for(int s : vector.keySet())
			total += vector.get(s);
		
		LinkedHashMap<Integer,Float> normalizedVector = new LinkedHashMap<Integer,Float>();
		
		for(int s : vector.keySet())
			normalizedVector.put(s, vector.get(s)/total);
		
		return normalizedVector;
	}
	
	public static LinkedHashMap<Integer,Float> sortSemSig(Map<Integer,Float> vector)
	{
		LinkedHashMap<Integer,Float> sortedMap = new LinkedHashMap<Integer,Float>();
		
		for(int key : Maps.sortByValue(vector, SortingOrder.DESCENDING).keySet())
			sortedMap.put(key, vector.get(key));
		
		return sortedMap;
	}
	
	public static LinkedHashMap<Integer,Float> truncateSemSig(Map<Integer,Float> vector, int size)
	{
		LinkedHashMap<Integer,Float> truncatedMap = new LinkedHashMap<Integer,Float>();
		
		int i = 1;
		for(int key : vector.keySet())
		{
			truncatedMap.put(key, vector.get(key));
			
			if(i++ >= size) break;
		}
		
		return normalizeSemSigs(truncatedMap);
	}

	/**
	 * Averages a set of semantic signatures, 
	 * equivalent to obtaining a semantic signature by initializing the PPR from all the corresponding nodes
	 * @param vectors
	 * 			a list of vectors
	 * @return
	 * 			the averaged semantic signature
	 */
	public static SemSig averageSemSigs(List<SemSig> vectors)
	{
		int size = vectors.size();
		LinkedHashMap<Integer,Float> overallVector = new LinkedHashMap<Integer,Float>();
		
		Set<Integer> vectorsKeyset = new HashSet<Integer>();
		
		for(SemSig vector : vectors)
		{
			vectorsKeyset.addAll(vector.getVector().keySet());
		}

		for(int key : vectorsKeyset)
		{
			float thisKeyValue = 0;
			
			for(SemSig vector : vectors)
			{
				HashMap<Integer, Float> currentV = vector.getVector();
				
				if(currentV.containsKey(key))
					thisKeyValue += currentV.get(key);
			}
			
			thisKeyValue /= size; 
			overallVector.put(key, thisKeyValue);
		}

		SemSig overallSig = new SemSig();
		overallSig.setVector(overallVector);
		
		return overallSig;
	}

}
