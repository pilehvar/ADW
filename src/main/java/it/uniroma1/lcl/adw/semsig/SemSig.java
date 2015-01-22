package it.uniroma1.lcl.adw.semsig;

import java.util.LinkedHashMap;

import edu.mit.jwi.item.POS;

/**
 * A Semantic Signature
 * 
 * Semantic signatures in the current build are obtained by running the 
 * Personalized PageRank algorithm on the {@link WordNetGloss} graph.
 * 
 * The original size of these signatures is equal to the number of connected nodes
 * in the graph which is about 118K. Due to space constraints, however, a compressed
 * version of the signatures are provided with this package.
 * 
 * A semantic signature is essentially represented as a LinkedHashMap<Integer,Float>
 * which is a map of dimension-IDs (synsets) and their assigned weight.
 *  
 * Compression:
 * Signatures are truncated to their top-5000 dimensions and normalized.
 * They are also stored in the sorted format so as to allow faster processing speed. 
 * Also, to reduce the signatures' size, the dimensions are represented as integer 
 * keys  (and not the WordNet offsets). This mapping is included in the resources directory.
 * 
 *  
 * @author pilehvar
 *
 */
public class SemSig
{
	private LinkedHashMap<Integer,Float> vector = new LinkedHashMap<Integer,Float>();
	private String offset = "null";
	private POS tag = null;
	private LKB lkb = null;
	
	public void setVector(LinkedHashMap<Integer,Float> v)
	{
		this.vector = v;
	}
	
	public void setOffset(String o)
	{
		this.offset = o;
	}
	
	public void setTag(POS tag)
	{
		this.tag = tag;
	}
	
	public void setLKB(LKB l)
	{
		this.lkb = l;
	}
	
	public void addDimension(int offset, float prob)
	{
		this.vector.put(offset, prob);
	}
	
	public LinkedHashMap<Integer,Float> getVector()
	{
		return this.vector;
	}

	public LinkedHashMap<Integer,Float> getVector(int size)
	{
		return this.vector;
	}
	
	public String getOffset()
	{
		return this.offset;
	}
	
	public POS getTag()
	{
		return this.tag;
	}
	
	public LKB getLKB()
	{
		return this.lkb;
	}
}
