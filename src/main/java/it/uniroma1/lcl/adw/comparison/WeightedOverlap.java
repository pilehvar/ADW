package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.utils.SemSigUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
	
	//old implementation, not suitable for truncated vectors with small overlaps
//	public double compare(List<Integer> v1,
//						  List<Integer> v2) 
//	{
//
//		double overlaps = 0;
//		double normalization = 0;
//		
//		HashMap<Integer,Integer> map = SemSigComparator.ListToMap(v2);
//		
//		int index = 0;
//		
//		for(Integer s : v1)
//		{
//			//works only on the overlapping dimensions of v1 and v2
//			if(v2.contains(s))
//			{
//				overlaps += 1.0/((index+1)+(map.get(s)+1));	
//				normalization += 1.0/(2*(index+1));
//				index++;
//			}
//		}
//		
//		//if the two signatures have no dimension in common
//		if(overlaps == 0 || normalization == 0)
//			return 0;
//		
//		return overlaps/normalization;
//	}
	
	/**
	 * New implementation of Weighted Overlap that better suits the case when only a small part of two vectors overlap,
	 * which is also the case for the provided vectors that are truncated to top-5000 elements (of the original 117,500) 
	 * Also, to have a smoother distribution of scores, a square-root function has been added to the formula.
	 * @param v1 sorted list of dimensions in the first vector (smaller)
	 * @param v2 sorted list of dimensions in the second vector (larger)
	 * @return
	 */
	public static double compareSmallerWithBigger(List<Integer> v1, List<Integer> v2) 
	{
		double nominator = 0;
		double normalization = 0;
		
		Set<Integer> overlaps = getOverlap(v1, v2);
		
		for(Integer overlap : overlaps)
		{
			nominator += 1.0/((v1.indexOf(overlap)+1)+(v2.indexOf(overlap)+1));
//			nominator += 1.0/(Math.sqrt((v1.indexOf(overlap)+1)+(v2.indexOf(overlap)+1)));	
		}
		
		for(int i=1; i<=overlaps.size(); i++)
		{
			normalization += 1.0/((2*i));
//			normalization += 1.0/(Math.sqrt(2*i));
		}
		
		//if the two signatures have no dimension in common
		if(nominator == 0 || normalization == 0)
			return 0;
		
		return ((double)nominator/normalization);
	}
	
	private static Set<Integer> getOverlap(List<Integer> v1, List<Integer> v2)
	{
		Set<Integer> overlap = new HashSet<Integer>();
		
		for(int a : v1)
			if(v2.contains(a))
				overlap.add(a);
		
		return overlap;
	}
	
	public static double compare(List<Integer> v1, List<Integer> v2)
	{
		//in order to normalize by the smaller vector
		if(v1.size() > v2.size())
			return compareSmallerWithBigger(v2, v1);
		else
			return compareSmallerWithBigger(v1, v2);
	}
	
}
