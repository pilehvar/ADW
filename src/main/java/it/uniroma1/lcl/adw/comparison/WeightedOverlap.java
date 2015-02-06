package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigComparator;
import it.uniroma1.lcl.adw.utils.SemSigUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;


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
            TIntSet overlap = new TIntHashSet(v1.getVector().keySet());
            overlap.retainAll(v2.getVector().keySet());
            return compare(overlap,
                           v1.getSortedIndices(),
                           v2.getSortedIndices());
	}

	public double compare(
			TIntFloatMap v1,
			TIntFloatMap v2,
			boolean sorted) 
	{
            TIntSet overlap = new TIntHashSet(v1.keySet());
            overlap.retainAll(v2.keySet());

            return compare(overlap,
                           SemSigUtils.getSortedIndices(v1),
                           SemSigUtils.getSortedIndices(v2));
	}
	
	/**
	 * New implementation of Weighted Overlap that better suits the case
	 * when only a small part of two vectors overlap, which is also the case
	 * for the provided vectors that are truncated to top-5000 elements (of
	 * the original 117,500) Also, to have a smoother distribution of
	 * scores, a square-root function has been added to the formula.
         *
	 * @param v1 sorted list of dimensions in the first vector (smaller)
	 * @param v2 sorted list of dimensions in the second vector (larger)
	 * @return
	 */
        public static double compareSmallerWithBigger(TIntSet overlaps, int[] v1, int[] v2) 
	{
		double nominator = 0;
		double normalization = 0;

                //if the two signatures have no dimension in common
                if (overlaps.isEmpty())
                    return 0;
                
		TIntIntMap indexToPosition1 = new TIntIntHashMap(v1.length);
                TIntIntMap indexToPosition2 = new TIntIntHashMap(v2.length);
                for (int i = 0; i < v1.length; ++i)
                    indexToPosition1.put(v1[i], i);
                for (int i = 0; i < v2.length; ++i)
                    indexToPosition2.put(v2[i], i);	
               
		int i = 1;
                TIntIterator iter = overlaps.iterator();
                while (iter.hasNext()) 
		{
                    int overlap = iter.next();
//			System.out.println(i+"\t"+map1.get(overlap)+1+"\t"+map2.get(overlap)+1);

                    nominator += 1.0 /
                        ((indexToPosition1.get(overlap)+1) + (indexToPosition2.get(overlap)+1));
//			nominator += 1.0/(Math.sqrt((map1.get(overlap)+1)+(map2.get(overlap)+1)));
			
                    normalization += 1.0/((2*i));
//			normalization += 1.0/(Math.sqrt(2*i));
                    i++;
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
	
        public static double compare(TIntSet overlaps, int[] v1, int[] v2)
	{
		//in order to normalize by the smaller vector
		if(v1.length > v2.length)
                        return compareSmallerWithBigger(overlaps, v2, v1);
		else
                        return compareSmallerWithBigger(overlaps, v1, v2);
	}
	
}
