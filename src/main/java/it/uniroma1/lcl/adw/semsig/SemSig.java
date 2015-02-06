package it.uniroma1.lcl.adw.semsig;

import it.uniroma1.lcl.adw.utils.SemSigUtils;

import gnu.trove.iterator.TIntFloatIterator;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;

import java.util.Collections;

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
	private TIntFloatMap vector = new TIntFloatHashMap();
	private String offset = "null";
	private POS tag = null;
	private LKB lkb = null;
        private int[] sortedIndices = null;
    
	public void setVector(TIntFloatMap v)
	{
		this.vector = v;
                this.sortedIndices = null;
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
	
	public TIntFloatMap getVector()
	{
		return this.vector;
	}

	public TIntFloatMap getVector(int size)
	{
		return this.vector;
	}

        public int[] getSortedIndices()
        {
            if (sortedIndices == null && vector != null)
            {
                sortedIndices = SemSigUtils.getSortedIndices(vector);
            }
            return sortedIndices;
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
