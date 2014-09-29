package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cosine implements SignatureComparison 
{

	public double compare(SemSig v1, SemSig v2, boolean sorted) 
	{
		return compare(v1.getVector(),v2.getVector(), sorted);
	}

	public double compare(
			LinkedHashMap<Integer, Float> v1,
			LinkedHashMap<Integer, Float> v2,
			boolean sorted) 
	{
		//sorted or not, it does not change the comparison procedure
		return cosineSimilarity(v1, v2);
	}
	
	public static double norm2(Map<Integer, Float> vector)
	{		
		double norm = 0.0;
		for(Number value : vector.values()) norm += value.doubleValue() * value.doubleValue();		
		norm = Math.sqrt(norm);
		
		return norm;
	}
	
	public static double cosineSimilarity(Map<Integer, Float> u, Map<Integer, Float> v)
	{
		double u_norm = norm2(u);
		if( u_norm == 0 ) return 0;
		
		double v_norm = norm2(v);
		if( v_norm == 0 ) return 0;
		
		return dotProduct(u, v)/(u_norm * v_norm);
	}
	
	public static double dotProduct(Map<Integer, Float> vector1, Map<Integer, Float> vector2)
	{
		double dotProduct = 0.0;
		
		Map<Integer, Float> temp = null;
		
		if (vector1.size() > vector2.size())
		{
			temp = vector1;
			vector1 = vector2;
			vector2 = temp;
		}
		
		for(int key : vector1.keySet())
		{
			Number value = vector2.get(key);
			if( value == null ) continue;
			
			dotProduct += vector1.get(key).doubleValue() * value.doubleValue(); 
		}
		
		return dotProduct;
	}

}
