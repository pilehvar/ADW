package it.uniroma1.lcl.adw.comparison;

import static org.junit.Assert.*;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;

import org.junit.Test;

import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;

public class WeightedOverlapTest
{
	
	@Test
	public void testGetSortedIndices() 
	{
    	WeightedOverlap WO = new WeightedOverlap();
    	
    	TIntFloatMap map1 = new TIntFloatHashMap();
    	TIntFloatMap map2 = new TIntFloatHashMap();
    	TIntFloatMap map3 = new TIntFloatHashMap();
    	TIntFloatMap map4 = new TIntFloatHashMap();
    	TIntFloatMap map5 = new TIntFloatHashMap();
    	
    	map1.put(1, 1f);
    	map1.put(2, 2f);
    	map1.put(3, 3f);
    	map1.put(4, 4f);
    	map1.put(5, 5f);
    	map1.put(6, 6f);
    	
    	map2.putAll(map1);
    	
    	map3.put(4, 4f);
    	map3.put(5, 5f);
    	map3.put(6, 6f);
    	map3.put(7, 1f);
    	map3.put(8, 2f);
    	map3.put(9, 3f);
    	
    	map4.put(1, 6f);
    	map4.put(4, 5f);
    	map4.put(2, 4f);
    	map4.put(5, 3f);
    	map4.put(3, 2f);
    	map4.put(6, 1f);
    	
    	map5.put(7, 6f);
    	map5.put(8, 5f);
    	
    	double score1 = WO.compare(map1, map2, true);
    	double score2 = WO.compare(map1, map2, true);
    	double score3 = WO.compare(map1, map4, true);
    	double score4 = WO.compare(map1, map5, true);
    	
        assertEquals(1, score1, 0.01);
        assertEquals(1, score2, 0.01);
        assertEquals(0.725, score3, 0.01);
        assertEquals(0, score4, 0.0001);
        
        
	}
}
            
