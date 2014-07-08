package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Jaccard implements SignatureComparison 
{
	@Override
	public double compare(SemSig v1, SemSig v2) 
	{
		return compare(v1.getVector(),v2.getVector());
	}

	@Override
	public double compare(
			LinkedHashMap<Integer, Float> v1,
			LinkedHashMap<Integer, Float> v2) 
	{

		double overlaps = 0;
		double normalization = 0;
		List<Integer> v1Keys = new ArrayList<Integer>(v1.keySet());
		
		Set<Integer> v2KeysSet = v2.keySet();
		
		for(Integer s : v1Keys)
		{
			if(v2KeysSet.contains(s))
			{
				overlaps++;
				normalization++;
			}
			
			
		}
		
		if(overlaps == 0 || normalization == 0)
			return 0;
		
		return overlaps/normalization;
	}


}
