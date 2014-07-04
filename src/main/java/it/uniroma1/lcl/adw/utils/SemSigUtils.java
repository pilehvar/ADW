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
	
	public static SemSig normalizeSemSig(SemSig v, SemSig normalization)
	{
		LinkedHashMap<Integer,Float> normalizedVector = new LinkedHashMap<Integer,Float>();
		SemSig normalizedSemSig = new SemSig();
		
		for(int offset : v.getVector().keySet())
			normalizedVector.put(offset, v.getVector().get(offset) / normalization.getVector().get(offset));
		
		normalizedSemSig.setOffset(v.getOffset());
		normalizedSemSig.setTag(v.getTag());
		normalizedSemSig.setLKB(v.getLKB());
		
		normalizedSemSig.setVector(truncateSemSig(normalizedVector, normalizedVector.size(), true));
		
		return normalizedSemSig;
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
	
	public static LinkedHashMap<Integer,Float> truncateSemSig(Map<Integer,Float> vector, int size, boolean normalize)
	{
		LinkedHashMap<Integer,Float> sortedMap = new LinkedHashMap<Integer,Float>();
		
		int i = 1;
		for(int key : Maps.sortByValue(vector, SortingOrder.DESCENDING).keySet())
		{
			sortedMap.put(key, vector.get(key));
			
			if(i++ >= size) break;
		}
		
		if(normalize)
		{
			sortedMap = normalizeSemSigs(sortedMap);
		}
		
		return sortedMap;
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
				if(vector.getVector().containsKey(key))
					thisKeyValue += vector.getVector().get(key);
			}
			
			thisKeyValue /= size; 
			overallVector.put(key, thisKeyValue);
		}

		SemSig overallSig = new SemSig();
		overallSig.setVector(overallVector);
		
		return overallSig;
	}

}
