package it.uniroma1.lcl.adw.textual.similarity;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.item.IWord;

import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.ADWConfiguration;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.LexicalItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.semsig.LKB;
import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigComparator;
import it.uniroma1.lcl.adw.semsig.SemSigProcess;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.adw.utils.WordNetUtils;
import it.uniroma1.lcl.jlt.util.Pair;
import it.uniroma1.lcl.jlt.wordnet.WordNet;
import it.uniroma1.lcl.jlt.wordnet.WordNetVersion;

public class PairSimilarity 
{
	private SignatureComparison alignmentMeasure;
	private int alignmentVecSize;
	private int testedVectorSize;
	private static final WordNetVersion WORDNET_VERSION = WordNetVersion.WN_30;
	private static boolean discardStopwords = ADWConfiguration.getInstance().getDiscardStopwordsCondition();
	private static boolean mirrorPOStagging = ADWConfiguration.getInstance().getMirrorPOSTaggingCondition();
	
	static private PairSimilarity instance;
	
	public PairSimilarity()
	{
		//the signature comparison measure used during similarity-based disambiguation
	     try
	        {
	            String p = "it.uniroma1.lcl.adw.comparison."
	            			+ ADWConfiguration.getInstance().getAlignmentSimilarityMeasure();
	            
				@SuppressWarnings("unchecked")
				Constructor<SignatureComparison> ct = 
	            		(Constructor<SignatureComparison>) Class.forName(p).getConstructor(new Class[] {});
	            alignmentMeasure = ct.newInstance();
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
	     
		//the size of the semantic signatures used during similarity-based disambiguation
		alignmentVecSize = ADWConfiguration.getInstance().getAlignmentVectorSize();
		//the size of the semantic signatures used during the main comparison
		testedVectorSize = ADWConfiguration.getInstance().getTestVectorSize();
	}
	
	/**
	 * Used to access {@link ADW}
	 * 
	 * @return an instance of {@link ADW}
	 */
	public static PairSimilarity getInstance()
	{
		try
		{
			if (instance == null) instance = new PairSimilarity();
			return instance;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not init TSPipeline: " + e.getMessage());
		}
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
			
			for(String item : Arrays.asList(text.split(" ")))
			{
				if(item.trim().length() == 0) continue;
				
				switch (textType)
				{
					case SENSE_OFFSETS:
					
						cookedSentence.add(item);
						break;
					
					case SENSE_KEYS:
						
						IWord sense = WordNet.getInstance().getSenseFromSenseKey(item);
						cookedSentence.add(GeneralUtils.fixOffset(sense.getSynset().getOffset(), sense.getPOS()));
						break;
				
					case WORD_SENSE:
						
						IWord snse = WordNetUtils.mapWordSenseToIWord(WORDNET_VERSION, item);
						cookedSentence.add(GeneralUtils.fixOffset(snse.getSynset().getOffset(), 
								snse.getPOS()));
						break;
				
					case SURFACE:
						
						out = TextualSimilarity.getInstance().cookSentence(text);
						cookedSentence = out.getFirst();	
						break;
			
					case SURFACE_TAGGED:
						for(String word : text.split(" "))
							cookedSentence.add(word);
				
						break;
				
				}
				
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
	
	private Set<SemSig> convertToSet(List<List<SemSig>> vs) 
	{
		Set<SemSig> vectors = new HashSet<SemSig>();
		
		for(List<SemSig> aList : vs)
			for(SemSig a : aList)
				vectors.add(a);
			
		return vectors;
	}
	
	public Pair<List<SemSig>,List<SemSig>> 
	DisambiguateCookedSentence(
			List<String> cookedSentence1, 
			List<String> cookedSentence2, 
			LexicalItemType srcTextType,
			LexicalItemType trgTextType,
			LKB lkb, 
			SignatureComparison measure, 
			int vectorSize, 
			boolean restrictedByPOS, 
			boolean verbose)
	{

		try
		{
		
			LinkedHashMap<Pair<SemSig,SemSig>,Double> alignments;
			LinkedHashMap<Pair<SemSig,SemSig>,Double> alignmentsRev;
			
			List<List<SemSig>> firstVectors = convertToVectors(cookedSentence1, srcTextType, lkb, vectorSize);
			Set<SemSig> firstVectorSet = convertToSet(firstVectors);
		
			List<List<SemSig>> secondVectors = convertToVectors(cookedSentence2, trgTextType, lkb, vectorSize);
			Set<SemSig> secondVectorSet = convertToSet(secondVectors);
			
			
			alignments = TextualSimilarity.getInstance().alignmentBasedDisambiguation(firstVectors, secondVectorSet, alignmentMeasure, alignmentVecSize, new HashSet<SemSig>());
			
			//of there is any alignment with 1.0 score, make sure that the target is selected on the reverse disambiguation
			Set<SemSig> toBeTakens = new HashSet<SemSig>();
			for(Pair<SemSig,SemSig> sig : alignments.keySet())
				if(Math.abs(alignments.get(sig) - 1) < 0.0001)
					toBeTakens.add(sig.getSecond());
			
			alignmentsRev = TextualSimilarity.getInstance().alignmentBasedDisambiguation(secondVectors, firstVectorSet, alignmentMeasure, alignmentVecSize, toBeTakens); 

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
	
	public LexicalItemType guessLexicalItemType(String input)
	{
		String firstWord = input.split(" ")[0];
		
		if(firstWord.matches("[0-9]*\\-[anvr]"))
			return LexicalItemType.SENSE_OFFSETS;
		
		if(firstWord.matches("[^ ]*%[0-9]*:[^ ]*"))
			return LexicalItemType.SENSE_KEYS;
		
		if(WordNetUtils.mapWordSenseToIWord(WORDNET_VERSION, firstWord) != null)
			return LexicalItemType.WORD_SENSE;
		
		if(firstWord.matches("[^ ]*#[nvra]"))
			return LexicalItemType.SURFACE_TAGGED;
		
		return LexicalItemType.SURFACE;
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
	
	public double getSimilarity(
			String text1, String text2, 
			DisambiguationMethod disMethod,
			SignatureComparison measure,
			LexicalItemType srcTextType,
			LexicalItemType trgTextType)
	{

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
				srcSemSigs = SemSigProcess.getInstance().getAllSemSigsFromWordPosList(cookedSentence1, srcTextType, alignmentVecSize);
				trgSemSigs = SemSigProcess.getInstance().getAllSemSigsFromWordPosList(cookedSentence2, trgTextType, alignmentVecSize);
				break;
				
				//alignment-based disambiguation
				//should disambiguate the two texts and return the disambiguated SemSigs				
			case ALIGNMENT_BASED:
				Pair<List<SemSig>,List<SemSig>>	disambiguatedPair =
						DisambiguateCookedSentence(cookedSentence1, cookedSentence2, 
						srcTextType, trgTextType, LKB.WordNetGloss, alignmentMeasure, alignmentVecSize, 
						true, true);
				
				srcSemSigs = disambiguatedPair.getFirst();
				trgSemSigs = disambiguatedPair.getSecond();
				
				break;
				
		}
			
		SemSig srcSemSig = (srcSemSigs.size() == 1)?
				srcSemSigs.get(0) : SemSigUtils.averageSemSigs(srcSemSigs);
		
		SemSig trgSemSig = (trgSemSigs.size() == 1)?
						trgSemSigs.get(0) : SemSigUtils.averageSemSigs(trgSemSigs);
		
		return SemSigComparator.compare(srcSemSig.getVector(), trgSemSig.getVector(), measure, testedVectorSize, false, true);
	}
	
	public List<List<SemSig>> convertToVectors(List<String> sentence, LexicalItemType type, LKB lkb, int vSize)
	{
		List<List<SemSig>> firstVectors = new ArrayList<List<SemSig>>();
		Set<SemSig> firstVectorSet = new HashSet<SemSig>();
		
		if(type.equals(LexicalItemType.SENSE_OFFSETS) 
				|| type.equals(LexicalItemType.SENSE_KEYS)
				|| type.equals(LexicalItemType.WORD_SENSE))
		{
			firstVectorSet = new HashSet<SemSig>(TextualSimilarity.getInstance().getSenseVectorsFromOffsetSentence(sentence, type, lkb, vSize));
		
			for(SemSig s : firstVectorSet)
				firstVectors.add(Arrays.asList(s));				
		}
		else
		{
			firstVectors = TextualSimilarity.getInstance().getSenseVectorsFromCookedSentence(sentence, lkb, vSize);
		}
		
		return firstVectors;
	}
	
	
}
