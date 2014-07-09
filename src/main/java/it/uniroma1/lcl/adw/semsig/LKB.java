package it.uniroma1.lcl.adw.semsig;

/**
 * the LKB used for generating semantic signatures.
 * In the current build, the semantic signatures are created on
 * the WordNet 3.0 semantic network that was further enriched with the 
 * help of the disambiguated glosses ({@link WordNetGloss}).
 * 
 * Signatures are generated using the Personalized PageRank implementation
 * in the UKB package (http://ixa2.si.ehu.es/ukb/).
 * 
 * @author pilehvar
 *
 */
public enum LKB
{
	WordNet,
	WordNetGloss;
}
