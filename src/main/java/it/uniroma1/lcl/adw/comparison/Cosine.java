package it.uniroma1.lcl.adw.comparison;

import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.jlt.util.Maths;

import java.util.LinkedHashMap;

public class Cosine implements SignatureComparison 
{

	public double compare(SemSig v1, SemSig v2) 
	{
		return compare(v1.getVector(),v2.getVector());
	}

	public double compare(
			LinkedHashMap<Integer, Float> v1,
			LinkedHashMap<Integer, Float> v2) 
	{
		return Maths.cosineSimilarity(v1, v2);
	}

}
