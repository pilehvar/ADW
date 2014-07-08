package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class WeightedOverlap implements SignatureComparison
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
		List<Integer> v2Keys = new ArrayList<Integer>(v2.keySet());
		List<Integer> v1Keys = new ArrayList<Integer>(v1.keySet());
		
		HashMap<Integer,Integer> map = SemSigComparator.ListToMap(v2Keys);
		
		Set<Integer> v2KeysSet = v2.keySet();
		
		int index = 0;
		for(Integer s : v1Keys)
		{
			if(v2KeysSet.contains(s))
			{
				overlaps += 1.0/((index+1)+(map.get(s)+1));	//linear
				normalization += 1.0/(2*(index+1));
				index++;
			}
			
			
		}
		
		if(overlaps == 0 || normalization == 0)
			return 0;
		
		return overlaps/normalization;
	}
	
}
