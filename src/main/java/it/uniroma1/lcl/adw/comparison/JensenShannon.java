package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import gnu.trove.iterator.TIntFloatIterator;
import gnu.trove.map.TIntFloatMap;

public class JensenShannon implements SignatureComparison
{

	public double compare(SemSig v1, SemSig v2, boolean sorted) 
	{
		return compare(v1.getVector(),v2.getVector(), sorted);
	}

	public double compare(
			TIntFloatMap v1,
			TIntFloatMap v2,
			boolean sorted) 
	{
		
		//it does not matter if vectors are sorted or not
		
		double JS = 0.0;
		
		if(v1.size() == 0 || v2.size() == 0)
			return JS;

                TIntFloatIterator iter = v1.iterator();
                while (iter.hasNext())
                {
                    iter.advance();
                    int key = iter.key();
                    double P = iter.value();
                    // if v2 doesn't have the key, Q is 0
                    double Q = v2.get(key);
                    double M = (P + Q) / 2;

                    JS += Math.log(P/M) * P;
                }

                iter = v2.iterator();
                while (iter.hasNext())
                {
                    iter.advance();
                    int key = iter.key();
                    double P = iter.value();
                    // if v1 doesn't have the key, Q is 0
                    double Q = v1.get(key);
                    double M = (P + Q) / 2;

                    JS += Math.log(P/M) * P;
                }
                		
		return JS;
	}

}
