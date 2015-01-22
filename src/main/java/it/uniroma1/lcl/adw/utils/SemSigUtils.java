package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.adw.semsig.SemSig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SemSigUtils 
{
	
	public static LinkedHashMap<Integer,Float> sortVector(Map<Integer,Float> vector)
	{
		LinkedHashMap<Integer,Float> sortedMap = new LinkedHashMap<Integer,Float>();
		
		for(int key : sortByValue(vector).keySet())
			sortedMap.put(key, vector.get(key));
		
		return sortedMap;
	}
	
	/**
	 * Truncates a vector to the top-n elements
	 * @param vector
	 * @param size
	 * @param normalize
	 * @return truncated vector
	 */	
	public static LinkedHashMap<Integer,Float> truncateVector(Map<Integer,Float> vector, boolean sorted, int size, boolean normalize)
	{
		LinkedHashMap<Integer,Float> truncatedMap = new LinkedHashMap<Integer,Float>();
		
		List<Integer> keys = (sorted)? new ArrayList<Integer>(vector.keySet()) : new ArrayList<Integer>(sortVector(vector).keySet());
		
		int i = 0;
		for(int key : keys)
		{
			truncatedMap.put(key, vector.get(key));
			
			if(i++ >= size) break;
		}
		
		if(normalize)
		{
			truncatedMap = new LinkedHashMap<Integer,Float>(normalizeVector(truncatedMap));
		}
		
		return truncatedMap;
	}
	
	/**
	 * Truncates a vector to the top-n elements (assumes that the input vector is already sorted)
	 * @param vector
	 * @param size
	 * @param normalize
	 * @return truncated vector
	 */
	public static LinkedHashMap<Integer,Float> truncateSortedVector(LinkedHashMap<Integer,Float> vector, int size)
	{
		LinkedHashMap<Integer,Float> sortedMap = new LinkedHashMap<Integer,Float>();
		
		int i = 1;
		for(int key : vector.keySet())
		{
			sortedMap.put(key, vector.get(key));
			if(i++ > size) break;
		}
		
		return new LinkedHashMap<Integer,Float>(normalizeVector(sortedMap));
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
			if(vector != null)
				vectorsKeyset.addAll(vector.getVector().keySet());
		}

		for(int key : vectorsKeyset)
		{
			float thisKeyValue = 0;
			
			for(SemSig vector : vectors)
			{
				HashMap<Integer, Float> currentV = (vector == null)? new HashMap<Integer, Float>() : vector.getVector();
				
				if(currentV.containsKey(key))
					thisKeyValue += currentV.get(key);
			}
			
			if(thisKeyValue == 1)
				thisKeyValue = 0;
			
			thisKeyValue /= size; 
			overallVector.put(key, thisKeyValue);
		}

		SemSig overallSig = new SemSig();
		overallSig.setVector(overallVector);
		
		return overallSig;
	}

	
	/**
	 * Normalizes the probability values in a vector so that to sum to 1.0
	 * @param vector
	 * @return
	 */
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

	public static <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map)
	{
		Comparator<Map.Entry<K, V>> valueComparator = null;
		
		valueComparator = new Comparator<Map.Entry<K, V>>()
		{
			public int compare(Entry<K, V> o1, Entry<K, V> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		};

		final List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());

		Collections.sort(entries, valueComparator);
		
		final Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : entries)
			result.put(entry.getKey(), entry.getValue());
		return result;
	}
	
}

