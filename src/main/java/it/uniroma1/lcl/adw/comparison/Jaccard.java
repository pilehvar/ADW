package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.utils.SemSigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntFloatMap;

/**
 * Counts the number of overlapping dimensions across the two signatures
 * and normalizes that by the total number of 
 * 
 * @author pilehvar
 *
 */
public class Jaccard implements SignatureComparison 
{
	public double compare(SemSig v1, SemSig v2, boolean sorted) 
	{
		return compare(v1.getVector(),v2.getVector(),sorted);
	}

	public double compare(
			TIntFloatMap v1,
			TIntFloatMap v2,
			boolean sorted) 
	{
            int overlaps = 0;
		
            TIntIterator iter = v1.keySet().iterator();
            while (iter.hasNext())
            {
                int key = iter.next();
                if (v2.containsKey(key))
                    overlaps++;
            }
            
            return overlaps / (double)(v1.size() + v2.size() - overlaps);
	}

}
