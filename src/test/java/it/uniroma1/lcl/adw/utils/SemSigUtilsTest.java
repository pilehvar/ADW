package it.uniroma1.lcl.adw.utils;

import static org.junit.Assert.*;
import it.uniroma1.lcl.adw.comparison.Cosine;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;

import org.junit.Test;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;


public class SemSigUtilsTest
{
	
	@Test
	public void testGetSortedIndices() 
	{
            TIntFloatMap m = new TIntFloatHashMap();
            m.put(0, 1f);
            m.put(1, 10f);
            m.put(2, 5f);
            m.put(3, 2f);

            int[] sorted = SemSigUtils.getSortedIndices(m);
            assertEquals(4, sorted.length);
            assertEquals(1, sorted[0]);
            assertEquals(2, sorted[1]);
            assertEquals(3, sorted[2]);
            assertEquals(0, sorted[3]);
        }

    	@Test
	public void testTruncateVector() 
	{
            TIntFloatMap m = new TIntFloatHashMap();
            m.put(0, 1f);
            m.put(1, 10f);
            m.put(2, 5f);
            m.put(3, 2f);

            TIntFloatMap truncated = SemSigUtils.truncateVector(m, false, 2, false);
            assertEquals(2, truncated.size());
            assertEquals(10f, truncated.get(1), 0.1f);
            assertEquals(5f, truncated.get(2), 0.1f);
        }

    	@Test
	public void testTruncateVectorNormalized() 
	{
            TIntFloatMap m = new TIntFloatHashMap();
            m.put(0, 1f);
            m.put(1, 10f);
            m.put(2, 5f);
            m.put(3, 2f);

            TIntFloatMap truncated = SemSigUtils.truncateVector(m, false, 2, true);
            assertEquals(2, truncated.size());
            assertEquals(10f / 15f, truncated.get(1), 0.1f);
            assertEquals(5f / 15f, truncated.get(2), 0.1f);            
        }
    
}
            
