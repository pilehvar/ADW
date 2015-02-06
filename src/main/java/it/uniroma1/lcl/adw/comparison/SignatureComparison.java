package it.uniroma1.lcl.adw.comparison;

import gnu.trove.map.TIntFloatMap;

import it.uniroma1.lcl.adw.semsig.SemSig;

/**
 * An interface for comparing two {@link SemSig}s
 * 
 * @author pilehvar
 *
 */
public interface SignatureComparison 
{
	double compare(SemSig v1, SemSig v2, boolean sortedNormalized);
	double compare(TIntFloatMap v1, TIntFloatMap v2, boolean sortedNormalized);
}
