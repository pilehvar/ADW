package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;

import java.util.LinkedHashMap;
import java.util.Map;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntFloatIterator;
import gnu.trove.map.TIntFloatMap;


public class Cosine implements SignatureComparison 
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
		//sorted or not, it does not change the comparison procedure
		return cosineSimilarity(v1, v2);
	}
	
	public static double norm2(TIntFloatMap vector)
	{		
		double norm = 0.0;
                TFloatIterator iter = vector.valueCollection().iterator();
                while (iter.hasNext())
                {
                    float f = iter.next();
                    norm += f * f;
                }
                    
		norm = Math.sqrt(norm);
		
		return norm;
	}
	
	public static double cosineSimilarity(TIntFloatMap u, TIntFloatMap v)
	{
		double u_norm = norm2(u);
		if( u_norm == 0 ) return 0;
		
		double v_norm = norm2(v);
		if( v_norm == 0 ) return 0;

		return dotProduct(u, v)/(u_norm * v_norm);
	}
	
	public static double dotProduct(TIntFloatMap vector1, TIntFloatMap vector2)
	{
		double dotProduct = 0.0;
                
		TIntFloatMap temp = null;
		
		if (vector1.size() > vector2.size())
		{
			temp = vector1;
			vector1 = vector2;
			vector2 = temp;
		}

                TIntFloatIterator iter = vector1.iterator();
                while (iter.hasNext()) 
		{
                    iter.advance();
                    int key = iter.key();
                    float f = vector2.get(key);
                    if (f == 0)
                        continue;
                    dotProduct += iter.value() * f;
		}
		
		return dotProduct;
	}

}
