package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import gnu.trove.iterator.TIntFloatIterator;
import gnu.trove.map.TIntFloatMap;


public class KLDivergence implements SignatureComparison
{

	public double compare(SemSig v1, SemSig v2, boolean sorted) 
	{
		return compare(v1.getVector(), v2.getVector(), sorted);
	}

	public double compare(
			TIntFloatMap v1,
			TIntFloatMap v2,
			boolean sorted) 
	{
		//it does not matter if the vectors are sorted or not
		
		double DKL = 0.0;

                TIntFloatIterator iter = v1.iterator();
                while (iter.hasNext())
                {
                    iter.advance();
                    int key = iter.key();
                    if (!v2.containsKey(key))
                    {
                        continue;
                    }
                    double P = iter.value();
                    double Q = v2.get(key);
                    DKL += Math.log(P/Q) * P;
                }
	
		return DKL;

	}
	

}
