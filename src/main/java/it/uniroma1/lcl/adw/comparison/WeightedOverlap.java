package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigComparator;
import it.uniroma1.lcl.adw.utils.SemSigUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A non-parametric approach for comparing two multinomial distributions proposed in:
 * 
 * M. T. Pilehvar, D. Jurgens and R. Navigli. 
 * Align, Disambiguate and Walk: A Unified Approach for Measuring Semantic Similarity. ACL 2013.
 * 
 * The approach compares the ranking of the dimensions across the two distributions.
 * The approach penalizes the differences in the top-ranking dimensions more than it 
 * does for the lower ones.
 *  
 * @author pilehvar
 *
 */
public class WeightedOverlap implements SignatureComparison
{
	public double compare(SemSig v1, SemSig v2, boolean sortedNormalized) 
	{
		return compare(v1.getVector(),v2.getVector(),sortedNormalized);
	}

	public double compare(
			LinkedHashMap<Integer, Float> v1,
			LinkedHashMap<Integer, Float> v2,
			boolean sorted) 
	{
		
		if(!sorted)   
		{
			v1 = SemSigUtils.sortVector(v1);
			v2 = SemSigUtils.sortVector(v2);
		}
		
		List<Integer> v2Keys = new ArrayList<Integer>(v2.keySet());
		List<Integer> v1Keys = new ArrayList<Integer>(v1.keySet());
		
		return compare(v1Keys,v2Keys);
	}
	
	public double compare(List<Integer> v1,
						  List<Integer> v2) 
	{

		double overlaps = 0;
		double normalization = 0;
		
		HashMap<Integer,Integer> map = SemSigComparator.ListToMap(v2);
		
		int index = 0;
		
		for(Integer s : v1)
		{
			//works only on the overlapping dimensions of v1 and v2
			if(v2.contains(s))
			{
				overlaps += 1.0/((index+1)+(map.get(s)+1));	
				normalization += 1.0/(2*(index+1));
				index++;
			}
		}
		
		//if the two signatures have no dimension in common
		if(overlaps == 0 || normalization == 0)
			return 0;
		
		return overlaps/normalization;
	}
}
