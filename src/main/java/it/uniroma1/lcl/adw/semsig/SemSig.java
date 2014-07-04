package it.uniroma1.lcl.adw.semsig;

import java.util.LinkedHashMap;

import edu.mit.jwi.item.POS;

/**
 * a semantic signature
 * @author pilehvar
 *
 */
public class SemSig
{
	private LinkedHashMap<Integer,Float> vector = new LinkedHashMap<Integer,Float>();
	
	private String offset;
	private POS tag;
	private LKB lkb;
	
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
