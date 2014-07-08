package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import java.util.LinkedHashMap;

public class KLDivergence implements SignatureComparison
{

	@Override
	public double compare(SemSig v1, SemSig v2) 
	{
		return compare(v1.getVector(), v2.getVector()) ;
	}

	@Override
	public double compare(
			LinkedHashMap<Integer, Float> v1,
			LinkedHashMap<Integer, Float> v2) 
	{
		double DKL = 0.0;
		
		for(Integer key : v1.keySet())
		{
			double P = v1.get(key);

			if(!v2.containsKey(key))
			{
				//System.out.println("There is no key "+key+" in the vector!");
				//System.exit(0);
				continue;
			}

			double Q = v2.get(key);
			
			DKL += Math.log(P/Q) * P;
		}
	
		return DKL;

	}
	

}
