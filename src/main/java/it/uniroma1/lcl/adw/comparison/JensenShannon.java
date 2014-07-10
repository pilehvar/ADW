package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import java.util.LinkedHashMap;

public class JensenShannon implements SignatureComparison
{

	public double compare(SemSig v1, SemSig v2) 
	{
		return compare(v1.getVector(),v2.getVector());
	}

	public double compare(
			LinkedHashMap<Integer, Float> v1,
			LinkedHashMap<Integer, Float> v2) 
	{
		double JS = 0.0;
		
		if(v1.keySet().size() == 0 || v2.keySet().size() == 0)
			return JS;
		
		for(Integer key : v1.keySet())
		{
			double P = v1.get(key);
			double Q = 0;
			
			if(v2.containsKey(key))
			{
				Q = v2.get(key);
			}

			Q = (P+Q)/2;
			JS += Math.log(P/Q) * P;
		}
	
		for(Integer key : v2.keySet())
		{
			double P = v2.get(key);
			double Q = 0;
			
			if(v1.containsKey(key))
			{
				Q = v1.get(key);
			}

			Q = (P+Q)/2;
			JS += Math.log(P/Q) * P;
		}
		
		return JS;
	}

}
