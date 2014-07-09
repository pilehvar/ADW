package it.uniroma1.lcl.adw.comparison;

import java.util.LinkedHashMap;

import it.uniroma1.lcl.adw.semsig.SemSig;

/**
 * An interface for comparing two {@link SemSig}s
 * 
 * @author pilehvar
 *
 */
public interface SignatureComparison 
{
	double compare(SemSig v1, SemSig v2);
	double compare(LinkedHashMap<Integer,Float> v1, LinkedHashMap<Integer,Float> v2);
}
