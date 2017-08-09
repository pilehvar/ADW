package it.uniroma1.lcl.adw;

import static org.junit.Assert.*;
import it.uniroma1.lcl.adw.comparison.Cosine;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;

import org.junit.Test;

public class ADWTest 
{
	private static final double epsilon = 0.01;
	
	@Test
	public void testWeightedOverlap() 
	{
        ADW pipeLine = new ADW();

        String text1 = "a mill that is powered by the wind";
        ItemType text1Type = ItemType.SURFACE;
        
        String text2 = "windmill#n rotate#v wind#n";
        ItemType text2Type = ItemType.SURFACE_TAGGED;
        
        String text3 = "windmill.n.1";
        ItemType text3Type = ItemType.WORD_SENSE;
        
        String text4 = "windmill%1:06:01::";
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
        
        double score2 = pipeLine.getPairSimilarity(
                text1, text3,
                disMethod, 
                measure,
                text1Type, text3Type);
        
        double score3 = pipeLine.getPairSimilarity(
                text1, text4,
                disMethod, 
                measure,
                text1Type, text4Type);
        
        double score4 = pipeLine.getPairSimilarity(
                text2, text3,
                disMethod, 
                measure,
                text2Type, text3Type);
        
        double score5 = pipeLine.getPairSimilarity(
                text3, text4,
                disMethod, 
                measure,
                text3Type, text4Type);
        
        double score6 = pipeLine.getPairSimilarity(
                text5, text6,
                disMethod, 
                measure,
                text5Type, text6Type);
        
        assertEquals(0.5735, score1, epsilon);
        assertEquals(0.5029, score2, epsilon);
        assertEquals(0.5029, score3, epsilon);
        assertEquals(0.7777, score4, epsilon);
        assertEquals(1.0, score5, epsilon);
        assertEquals(1.0, score6, epsilon);
        
	}
	
	
	@Test
	public void testCosine() 
	{
        ADW pipeLine = new ADW();

        String text1 = "a mill that is powered by the wind";
        ItemType text1Type = ItemType.SURFACE;
        
        String text2 = "windmill#n rotate#v wind#n";
        ItemType text2Type = ItemType.SURFACE_TAGGED;
        
        String text3 = "windmill.n.1";
        ItemType text3Type = ItemType.WORD_SENSE;
        
        String text4 = "windmill%1:06:01::";
        ItemType text4Type = ItemType.SENSE_KEYS;
        
        String text5 = "terminate";
        ItemType text5Type = ItemType.SURFACE;
        
        String text6 = "fire#v";
        ItemType text6Type = ItemType.SURFACE_TAGGED;
        
        //if lexical items has to be disambiguated
        DisambiguationMethod disMethod = DisambiguationMethod.ALIGNMENT_BASED;
        
        //measure for comparing semantic signatures
        SignatureComparison measure = new Cosine(); 

        double score1 = pipeLine.getPairSimilarity(
                text1, text2,
                disMethod, 
                measure,
                text1Type, text2Type);
        
        double score2 = pipeLine.getPairSimilarity(
                text1, text3,
                disMethod, 
                measure,
                text1Type, text3Type);
        
        double score3 = pipeLine.getPairSimilarity(
                text1, text4,
                disMethod, 
                measure,
                text1Type, text4Type);
        
        double score4 = pipeLine.getPairSimilarity(
                text2, text3,
                disMethod, 
                measure,
                text2Type, text3Type);
        
        double score5 = pipeLine.getPairSimilarity(
                text3, text4,
                disMethod, 
                measure,
                text3Type, text4Type);
        
        double score6 = pipeLine.getPairSimilarity(
                text5, text6,
                disMethod, 
                measure,
                text5Type, text6Type);
        
        assertEquals(0.5078, score1, epsilon);
        assertEquals(0.2251, score2, epsilon);
        assertEquals(0.2251, score3, epsilon);
        assertEquals(0.6910, score4, epsilon);
        assertEquals(1.0, score5, epsilon);
        assertEquals(1.0, score6, epsilon);
        	
	}
	
//	@Test
	public void testWeightedOverlapWithNoDisambiguation() 
	{
        ADW pipeLine = new ADW();

        String text1 = "a mill that is powered by the wind";
        ItemType text1Type = ItemType.SURFACE;
        
        String text2 = "windmill#n rotate#v wind#n";
        ItemType text2Type = ItemType.SURFACE_TAGGED;
        
        String text3 = "windmill.n.1";
        ItemType text3Type = ItemType.WORD_SENSE;
        
        String text4 = "windmill%1:06:01::";
        ItemType text4Type = ItemType.SENSE_KEYS;
        
        String text5 = "terminate";
        ItemType text5Type = ItemType.SURFACE;
        
        String text6 = "fire#v";
        ItemType text6Type = ItemType.SURFACE_TAGGED;

        
        //if lexical items has to be disambiguated
        DisambiguationMethod disMethod = DisambiguationMethod.NONE;
        
        //measure for comparing semantic signatures
        SignatureComparison measure = new WeightedOverlap(); 

        double score1 = pipeLine.getPairSimilarity(
                text1, text2,
                disMethod, 
                measure,
                text1Type, text2Type);
        
        double score2 = pipeLine.getPairSimilarity(
                text1, text3,
                disMethod, 
                measure,
                text1Type, text3Type);
        
        double score3 = pipeLine.getPairSimilarity(
                text1, text4,
                disMethod, 
                measure,
                text1Type, text4Type);
        
        double score4 = pipeLine.getPairSimilarity(
                text2, text3,
                disMethod, 
                measure,
                text2Type, text3Type);
        
        double score5 = pipeLine.getPairSimilarity(
                text3, text4,
                disMethod, 
                measure,
                text3Type, text4Type);
        
        double score6 = pipeLine.getPairSimilarity(
                text5, text6,
                disMethod, 
                measure,
                text5Type, text6Type);
        
        assertEquals(0.76289, score1, epsilon);
        assertEquals(0.48140, score2, epsilon);
        assertEquals(0.48140, score3, epsilon);
        assertEquals(0.59101, score4, epsilon);
        assertEquals(1.0, score5, epsilon);
        assertEquals(0.42288, score6, epsilon);
        
	}
	
}
