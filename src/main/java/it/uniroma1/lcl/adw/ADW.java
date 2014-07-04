package it.uniroma1.lcl.adw;

import it.uniroma1.lcl.adw.semsig.LKB;
import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigComparator;
import it.uniroma1.lcl.adw.semsig.SemSigProcess;
import it.uniroma1.lcl.adw.textual.similarity.TextualSimilarity;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.adw.utils.WordNetUtils;
import it.uniroma1.lcl.jlt.util.Pair;
import it.uniroma1.lcl.jlt.wordnet.WordNet;
import it.uniroma1.lcl.jlt.wordnet.WordNetVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;

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
	TextualSimilarity TS = TextualSimilarity.getInstance();
	
	SimilarityMeasure alignmentMeasure;
	int alignmentVecSize;
	int generatedVectorSize;
	String STSWorkingDirectory;
	int testedVectorSize;
	WordNetVersion wordnetVersion;
	
	Set<String> WordNetWords = null; 
	
	private ADW()
	{
		//the signature comparison measure used during similarity-based disambiguation
		alignmentMeasure =  ADWConfiguration.getInstance().getAlignmentSimilarityMeasure();
		//the size of the semantic signatures used during similarity-based disambiguation
		alignmentVecSize = ADWConfiguration.getInstance().getAlignmentVectorSize();
		//the size of the semantic signatures used during the main comparison
		testedVectorSize = ADWConfiguration.getInstance().getTestVectorSize();
		
		wordnetVersion = WordNetVersion.WN_30;
	}
	
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
	
	public double getFastSimilarity(
			String text1, String text2, 
			DisambiguationMethod disMethod,
			SimilarityMeasure measure,
			LexicalItemType srcTextType,
			LexicalItemType trgTextType)
	{
		boolean discardStopwords = true;
		boolean mirrorPOStagging = true;
		
		if(!checkType(text1, srcTextType))
		{
			log.error("Invalid input type for "+ srcTextType +" and string \""+ text1 +"\"! Please check the input");
			System.exit(0);
		}
		
		if(!checkType(text2, trgTextType))
		{
			log.error("Invalid input type for "+ trgTextType +" and string \""+ text2 +"\"! Please check the input");
			System.exit(0);
		}
	
		//pre-process sentence pair
		List<String> cookedSentence1 = cookLexicalItem(text1, srcTextType, discardStopwords).getFirst();
		List<String> cookedSentence2 = cookLexicalItem(text2, trgTextType, discardStopwords).getFirst();
		
		//Mirror pos tagging
		if(mirrorPOStagging && 
				srcTextType.equals(LexicalItemType.SURFACE) ||
				trgTextType.equals(LexicalItemType.SURFACE))
		{
			Pair<List<String>, List<String>> aPair  = mirrorPosTags(cookedSentence1, cookedSentence2);
			
			cookedSentence1 = aPair.getFirst();
			cookedSentence2 = aPair.getSecond();	
		}
		
		List<SemSig> srcSemSigs = new ArrayList<SemSig>();
		List<SemSig> trgSemSigs = new ArrayList<SemSig>();
		
		switch(disMethod)
		{
			case NONE:
				//take all the synsets (or Semsigs to be consistent with others) of all the words in the two sides
				srcSemSigs = SemSigProcess.getInstance().getAllOffsetsFromWordPosList(cookedSentence1, srcTextType);
				trgSemSigs = SemSigProcess.getInstance().getAllOffsetsFromWordPosList(cookedSentence2, trgTextType);
				break;
				
				//alignment-based disambiguation
				//should disambiguate the two texts and return the disambiguated SemSigs				
			case MIRROR:
				Pair<List<SemSig>,List<SemSig>>	disambiguatedPair =
						DisambiguateCookedSentence(cookedSentence1, cookedSentence2, 
						srcTextType, trgTextType, LKB.WordNetGloss, alignmentMeasure, alignmentVecSize, 
						true, true);
				
				srcSemSigs = disambiguatedPair.getFirst();
				trgSemSigs = disambiguatedPair.getSecond();
				
				break;
				
		}
			
		//TODO: disambiguation does not need averaging
		SemSig srcSemSig = SemSigUtils.averageSemSigs(srcSemSigs);
		SemSig trgSemSig = SemSigUtils.averageSemSigs(trgSemSigs);
		
		return SemSigComparator.compare(srcSemSig.getVector(), trgSemSig.getVector(), measure, testedVectorSize, false);
	}
	
	public Pair<List<String>,List<String>> mirrorPosTags(List<String> firstCookedSentence, List<String> secondCookedSentence) 
	{
		/*
		if(secondCookedSentence.size() == 0)
		{
			System.out.println("[ERROR: Set mirror pos tagging off!]");
			System.exit(0);
		}
		*/
		
		return TextualSimilarity.fixPOSmirroring(firstCookedSentence, secondCookedSentence);
	}

	
	public Pair<List<String>,List<String>> cookLexicalItem(
			String text, 
			LexicalItemType textType,
			boolean discardStopwords)
	{
		try
		{
			
			List<String> cookedSentence = new ArrayList<String>();
			Pair<List<String>,List<String>> out = new Pair<List<String>,List<String>>(null,null);
			
			switch (textType)
			{
				case SENSE_OFFSETS:
					for(String offset : Arrays.asList(text.split(" ")))
						cookedSentence.add(offset);

					break;
					
				case SENSE_KEYS:
					for(String senseKey : Arrays.asList(text.split(" ")))
					{
						IWord sense = WordNet.getInstance().getSenseFromSenseKey(senseKey);
						String offset = GeneralUtils.fixOffset(sense.getSynset().getOffset(), sense.getPOS());
						cookedSentence.add(offset);
					}
					
					break;
					
				case SURFACE:
					out = TS.cookSentence(text, discardStopwords);
					cookedSentence = out.getFirst();	
					
					break;
				
				case SURFACE_TAGGED:
					for(String word : text.split(" "))
					cookedSentence.add(word);
					
					break;
			}
				
			if(cookedSentence == null)
				cookedSentence = new ArrayList<String>();
			
			List<String> newCS = new ArrayList<String>();
			
			for(String s : cookedSentence)
			{
				//if it is a synset
				if(s.matches("[0-9]*\\-[anvr]"))
				{
					newCS.add(s);
					continue;
				}
				
				String comps[] = s.split("#");
				String word = comps[0];
				String ps = comps[1];
				
				//otherwise check word exists in WordNet
				if(!TextualSimilarity.getInstance().isOOV(word,ps))
					newCS.add(word+"#"+ps);
			}
			
			cookedSentence = newCS;

			return new Pair<List<String>,List<String>> (cookedSentence,out.getSecond());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean checkType(String input, LexicalItemType type)
	{
		if(input.trim().length() == 0)
			return false;
		
		for(String s : input.split(" "))
		{
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
					if(!s.endsWith("#n") && 
						!s.endsWith("#r") &&
						!s.endsWith("#v") &&
						!s.endsWith("#a"))
						return false;
					break;
						
				case SENSE_KEYS:
					if(WordNet.getInstance().getSenseFromSenseKey(s) == null)
						return false;
					break;
					
				case WORD_SENSE:
					if(WordNetUtils.mapWordSenseToIWord(wordnetVersion, s) == null)
						return false;
					break;
			}
		}
		
		return true;
	}
	
	
	public Pair<List<SemSig>,List<SemSig>> 
	DisambiguateCookedSentence(
			List<String> cookedSentence1, 
			List<String> cookedSentence2, 
			LexicalItemType srcTextType,
			LexicalItemType trgTextType,
			LKB lkb, 
			SimilarityMeasure measure, 
			int vectorSize, 
			boolean restrictedByPOS, 
			boolean verbose)
	{

		try
		{
		
			LinkedHashMap<Pair<SemSig,SemSig>,Double> alignments;
			LinkedHashMap<Pair<SemSig,SemSig>,Double> alignmentsRev;
			
			List<List<SemSig>> firstVectors = new ArrayList<List<SemSig>>();
			Set<SemSig> firstVectorSet = new HashSet<SemSig>();
			
			if(srcTextType.equals(LexicalItemType.SENSE_OFFSETS) || srcTextType.equals(LexicalItemType.SENSE_KEYS))
			{
				firstVectorSet = new HashSet<SemSig>(TS.getSenseVectorsFromOffsetSentence(cookedSentence1, lkb));
			
				for(SemSig s : firstVectorSet)
					firstVectors.add(Arrays.asList(s));				
			}
			else
			{
				firstVectors = TS.getSenseVectorsFromCookedSentence(cookedSentence1, lkb);
				firstVectorSet = convertToSet(firstVectors);
			}
			
			
			List<List<SemSig>> secondVectors = new ArrayList<List<SemSig>>();
			Set<SemSig> secondVectorSet = new HashSet<SemSig>();
					
			if(trgTextType.equals(LexicalItemType.SENSE_OFFSETS) || trgTextType.equals(LexicalItemType.SENSE_KEYS))
			{
				secondVectorSet = new HashSet<SemSig>(TS.getSenseVectorsFromOffsetSentence(cookedSentence2, lkb));
				
				for(SemSig s : secondVectorSet)
					secondVectors.add(Arrays.asList(s));				
			}
			else
			{
				secondVectors = TS.getSenseVectorsFromCookedSentence(cookedSentence2, lkb);
				secondVectorSet = convertToSet(secondVectors); 	
			}
			
			
			alignments = TextualSimilarity.semanticAlignerBySense(firstVectors, secondVectorSet, alignmentMeasure, alignmentVecSize, new HashSet<SemSig>());
			
			//of there is any alignment with 1.0 score, make sure that the target is selected on the reverse disambiguation
			Set<SemSig> toBeTakens = new HashSet<SemSig>();
			for(Pair<SemSig,SemSig> sig : alignments.keySet())
				if(alignments.get(sig) == 1)
					toBeTakens.add(sig.getSecond());
			
				
			alignmentsRev = TextualSimilarity.semanticAlignerBySense(secondVectors, firstVectorSet, alignmentMeasure, alignmentVecSize, toBeTakens); 

			//To avoid non-identical disambiguation pairs in case of word pairs that share multiple sysnsets
			//TODO
			//need to check if two synsets align with 1.0 score, they should be taken with priority
			
			List<SemSig> srcSigs = new ArrayList<SemSig>();
			List<SemSig> trgSigs = new ArrayList<SemSig>();
			
			for(Pair<SemSig,SemSig> sig : alignments.keySet())
				srcSigs.add(sig.getFirst());
				
			for(Pair<SemSig,SemSig> sig : alignmentsRev.keySet())
				trgSigs.add(sig.getFirst());
			
			return new Pair<List<SemSig>,List<SemSig>>(srcSigs,trgSigs);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
	
	public List<SemSig> getVectorsForSynsets(List<ISynset> synsets, LKB lkb)
	{
		List<SemSig> vectors = new ArrayList<SemSig>();
	
		for(ISynset synset : synsets)
		{
			String offset = WordNetUtils.fixOffset(synset.getOffset(), synset.getPOS());
			vectors.add(SemSigProcess.getInstance().getSemSigFromOffset(offset, lkb, 0));
		}
		
		return vectors;
	}
	
	
	private Set<SemSig> convertToSet(List<List<SemSig>> vs) 
	{
		Set<SemSig> vectors = new HashSet<SemSig>();
		
		for(List<SemSig> aList : vs)
			for(SemSig a : aList)
				vectors.add(a);
			
		return vectors;
	}
	
	//TODO: automatic detection of text types
	
	public static void main(String args[])
	{
		ADW pipeLine = new ADW();

//		this has a problem , it should be 1.0
		String text1 = "windmill is very powerfull";	
		String text2 = "this is a test";

		//if disambiguation by pair-specific alignment is intended
		DisambiguationMethod disMethod = DisambiguationMethod.MIRROR;		
		
		SimilarityMeasure measure = SimilarityMeasure.WEIGHTED_OVERLAP;	//measure for comparing resulting vectors
		LexicalItemType srcTextType = LexicalItemType.SURFACE;	//0: text pair, 1:pos-tagged lemmas pair, 2: sense pair
		LexicalItemType trgTextType = LexicalItemType.SURFACE;
		
		double score = pipeLine.getFastSimilarity(
				text1, text2,
				disMethod, 
				measure,
				srcTextType, trgTextType); 
		
		System.out.println(score);
		
	}


}
