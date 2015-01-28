package it.uniroma1.lcl.adw;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.stanford.nlp.util.Pair;

import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import it.uniroma1.lcl.adw.textual.similarity.PairSimilarity;
import it.uniroma1.lcl.adw.utils.WordNetUtils;

/**
 * A class to compute the semantic similarity of arbitrary pairs of lexical items
 * 
 * @author pilehvar
 *
 */
public class ADW 
{
	private static final Log log = LogFactory.getLog(ADW.class);
	
	static private ADW instance;
	private PairSimilarity PS = PairSimilarity.getInstance();
	
	/**
	 * Used to access {@link ADW}
	 * 
	 * @return an instance of {@link ADW}
	 */
	public static ADW getInstance()
	{
		try
		{
			if (instance == null) instance = new ADW();
			return instance;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not init TSPipeline: " + e.getMessage());
		}
	}
	
	public boolean checkType(String input, ItemType type)
	{
		if(input.trim().length() == 0)
			return false;
		
		for(String s : input.split(" "))
		{
			if(s.trim().length() == 0) continue;
				
			switch(type)
			{
				case SENSE_OFFSETS:
					if(!s.matches("[0-9]*-[nrva]"))
						return false;
					break;
					
				case SURFACE:
					if(s.trim().length() == 0)
						return false;
					break;
					
				case SURFACE_TAGGED:
					if(!s.matches("[^ ]*\\#[nrva]"))
						return false;
					break;
						
				case SENSE_KEYS:
					if(WordNetUtils.getInstance().getSenseFromSenseKey(s) == null)
						return false;
					break;
					
				case WORD_SENSE:
					if(WordNetUtils.getInstance().mapWordSenseToIWord(s) == null)
						return false;
					break;
			}
		}
		
		return true;
	}
	
	public Pair<Boolean,String> evaluateInputType(String input, ItemType type)
	{
		boolean passed = checkType(input, type);
		
		if(!passed)
		{
			log.warn("Invalid input type for "+ type +" and string \""+ input +"\"! Please check the input type.");
			return new Pair<Boolean,String>(false,"Invalid input type for "+ type +" and string \""+ input +"\"! Please check the input type.");
		}
		else
		{
			return new Pair<Boolean,String>(true,"Valid input type for "+ type +" and string \""+ input +"\".");
		}
	}
	
	public double getPairSimilarity(
			String text1, String text2, 
			DisambiguationMethod disMethod,
			SignatureComparison measure,
			ItemType srcTextType,
			ItemType trgTextType)
	{

		if(!evaluateInputType(text1, srcTextType).first)
		{
			return 0;
//			System.exit(0);
		}
		
		if(!evaluateInputType(text2, trgTextType).first)
		{
			return 0;
//			System.exit(0);
		}
		
		return	PS.getSimilarity(text1, text2, disMethod, measure, srcTextType, trgTextType);
	}
	
	public static void demo()
	{
        ADW pipeLine = new ADW();
        
        String text1 = "a# mill that is powered by the wind";
        ItemType text1Type = ItemType.SURFACE;
        
        String text2 = "c#n rotate#v wind#n";
        ItemType text2Type = ItemType.SURFACE_TAGGED;
        
        String text3 = "windmill.n.1 wind.n.1 rotate.v.1";	//or windmill#n#1
        ItemType text3Type = ItemType.WORD_SENSE;
        
        String text4 = "windmill%1:06:01::  windmill%1:06:01::";
        ItemType text4Type = ItemType.SENSE_KEYS;
        
        String text5 = "terminate";
        ItemType text5Type = ItemType.SURFACE;
        
        String text6 = "fire#v";
        ItemType text6Type = ItemType.SURFACE_TAGGED;
        
        //if lexical items has to be disambiguated
        DisambiguationMethod disMethod = DisambiguationMethod.ALIGNMENT_BASED;
        
        //measure for comparing semantic signatures
        SignatureComparison measure = new WeightedOverlap(); 

        double score1 = pipeLine.getPairSimilarity(
                text1, text2,
                disMethod, 
                measure,
                text1Type, text2Type);
        System.out.println(score1+"\t"+text1+"\t"+text2);
        
        double score2 = pipeLine.getPairSimilarity(
                text1, text3,
                disMethod, 
                measure,
                text1Type, text3Type);
        System.out.println(score2+"\t"+text1+"\t"+text3);
        
        double score3 = pipeLine.getPairSimilarity(
                text1, text4,
                disMethod, 
                measure,
                text1Type, text4Type);
        System.out.println(score3+"\t"+text1+"\t"+text4);
        
        double score4 = pipeLine.getPairSimilarity(
                text2, text3,
                disMethod, 
                measure,
                text2Type, text3Type);
        System.out.println(score4+"\t"+text2+"\t"+text3);
        
        double score5 = pipeLine.getPairSimilarity(
                text3, text4,
                disMethod, 
                measure,
                text3Type, text4Type);
        System.out.println(score5+"\t"+text3+"\t"+text4);

        double score6 = pipeLine.getPairSimilarity(
                text5, text6,
                disMethod, 
                measure,
                text5Type, text6Type);
        System.out.println(score6+"\t"+text5+"\t"+text6);
        
	}
	
	public static void main(String args[])
	{
		demo();
		
        ADW pipeLine = new ADW();
        
        String text1 = "04381183-n";
        ItemType text1Type = ItemType.SENSE_OFFSETS;

        String text2 = "give_up#v";
        ItemType text2Type = ItemType.SURFACE_TAGGED;
        
        //if lexical items has to be disambiguated
        DisambiguationMethod disMethod = DisambiguationMethod.NONE;
        
        //measure for comparing semantic signatures
        SignatureComparison measure = new WeightedOverlap(); 

        double score1 = pipeLine.getPairSimilarity(
                text1, text2,
                disMethod, 
                measure,
                text1Type, text2Type);
        System.out.println(score1+"\t"+text1+"\t"+text2);
        
	}


}
