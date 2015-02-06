package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.adw.semsig.SemSig;

import gnu.trove.iterator.TIntFloatIterator;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;

import java.util.Arrays;
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

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;


public class SemSigUtils 
{
	
	// public static TIntFloatMap sortVector(Map<Integer,Float> vector)
	// {
	// 	TIntFloatMap sortedMap = new TIntFloatMap();
		
	// 	for(int key : sortByValue(vector).keySet())
	// 		sortedMap.put(key, vector.get(key));
		
	// 	return sortedMap;
	// }

    public static int[] getSortedIndices(TIntFloatMap vector)
    {
        // NOTE: it's probably possible to do this using purely primitive
        // operations without having to resort to pushing things into an
        // Index[].  However, this code is much cleaner to have and since we
        // sort at most once per vector and the result is memoized, we don't
        // lose too much from the Object-based sorting.
        Index[] keyValPairs = new Index[vector.size()];
        TIntFloatIterator iter = vector.iterator();
        int i = 0;
        while (iter.hasNext())
        {
                iter.advance();
                keyValPairs[i++] = new Index(iter.key(), iter.value());
        }

        Arrays.sort(keyValPairs);
        int[] sortedIndices = new int[keyValPairs.length];
        for (i = 0; i < keyValPairs.length; ++i)
            sortedIndices[i] = keyValPairs[i].key;
        
        return sortedIndices;
    }

	/**
	 * Truncates a vector to the top-n elements
	 * @param vector
	 * @param size
	 * @param normalize
	 * @return truncated vector
	 */	
	public static TIntFloatMap truncateVector(TIntFloatMap vector, boolean sorted, int size, boolean normalize)
	{
		TIntFloatMap truncatedMap = new TIntFloatHashMap();

                int[] sortedIndices = getSortedIndices(vector);

                float valSum = 0f;

                for (int i = 0; i < size && i < sortedIndices.length; ++i)
                {
                    int index = sortedIndices[i];
                    float val = vector.get(index);
                    truncatedMap.put(index, val);
                    valSum += val;
                }

		if(normalize)
		{
                    // Iterate over all the value again and normalize.  We do
                    // this here (in-place0 to avoid having to create another
                    // Map instance of the truncated data.
                    for (int i = 0; i < size && i < sortedIndices.length; ++i)
                    {
                            int index = sortedIndices[i];
                            float val = vector.get(index);
                            truncatedMap.put(index, val / valSum);
                    }
                    
		}

		return truncatedMap;
	}
	
	// /**
	//  * Truncates a vector to the top-n elements (assumes that the input vector is already sorted)
	//  * @param vector
	//  * @param size
	//  * @param normalize
	//  * @return truncated vector
	//  */
	// public static TIntFloatMap truncateSortedVector(TIntFloatMap vector, int size)
	// {
	// 	TIntFloatMap sortedMap = new TIntFloatMap();
		
	// 	int i = 1;
	// 	for(int key : vector.keySet())
	// 	{
	// 		sortedMap.put(key, vector.get(key));
	// 		if(i++ > size) break;
	// 	}
		
	// 	return new TIntFloatMap(normalizeVector(sortedMap));
	// }

	/**
	 * Averages a set of semantic signatures, equivalent to obtaining a
	 * semantic signature by initializing the PPR from all the corresponding
	 * nodes
         *
	 * @param vectors
	 * 			a list of vectors
	 * @return
	 * 			the averaged semantic signature
	 */
	public static SemSig averageSemSigs(List<SemSig> vectors)
	{
		int size = vectors.size();
		TIntFloatMap overallVector = new TIntFloatHashMap();

                // Sum the vectors
                for (SemSig ss : vectors) {
                    TIntFloatIterator iter = ss.getVector().iterator();
                    while (iter.hasNext()) {
                        iter.advance();
                        int key = iter.key();
                        float curVal = overallVector.get(key);
                        overallVector.put(key, curVal + iter.value());
                    }
                }

                // Normalize by the number of vectors
                TIntFloatIterator iter = overallVector.iterator();
                while (iter.hasNext()) {
                    iter.advance();
                    iter.setValue(iter.value() / size);
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
	public static TIntFloatMap normalizeVector(TIntFloatMap vector)
	{
		float total = 0;

                TFloatIterator iter = vector.valueCollection().iterator();
                while (iter.hasNext())
                       total += iter.next();
                
                TIntFloatMap normalized = new TIntFloatHashMap(vector.size());
		
                TIntFloatIterator iter2 = vector.iterator();
                while (iter2.hasNext())
                {
                        iter2.advance();
                        normalized.put(iter2.key(), iter2.value() / total);
                }		
		return normalized;
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

