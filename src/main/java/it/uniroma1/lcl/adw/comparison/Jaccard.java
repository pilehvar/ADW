package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Counts the number of overlapping dimensions across the two signatures
 * and normalizes that by the total number of 
 * 
 * @author pilehvar
 *
 */
public class Jaccard implements SignatureComparison 
{
	public double compare(SemSig v1, SemSig v2) 
	{
		return compare(v1.getVector(),v2.getVector());
	}

	public double compare(
			LinkedHashMap<Integer, Float> v1,
			LinkedHashMap<Integer, Float> v2) 
	{

		double overlaps = 0;
		List<Integer> v1Keys = new ArrayList<Integer>(v1.keySet());
		
		Set<Integer> v2KeysSet = v2.keySet();
		
		for(Integer s : v1Keys)
		{
			if(v2KeysSet.contains(s))
			{
				overlaps++;
			}
		}
		
		if(overlaps == 0)
			return 0;
		
		return 2.0*overlaps/(v1.size()+v2.size());
	}

}
