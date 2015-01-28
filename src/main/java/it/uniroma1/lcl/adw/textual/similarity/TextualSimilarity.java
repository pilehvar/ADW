package it.uniroma1.lcl.adw.textual.similarity;

import it.uniroma1.lcl.adw.ADWConfiguration;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.semsig.LKB;
import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigComparator;
import it.uniroma1.lcl.adw.semsig.SemSigProcess;
import it.uniroma1.lcl.adw.utils.SentenceProcessor;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.WordNetUtils;
import it.uniroma1.lcl.jlt.pipeline.stanford.StanfordSentence;
import it.uniroma1.lcl.jlt.pipeline.stanford.StanfordSentence.CompoundingParameter;
import it.uniroma1.lcl.jlt.pipeline.stanford.StanfordSentence.MultiwordBelongingTo;
import it.uniroma1.lcl.jlt.util.EnglishLemmatizer;
import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.jlt.util.Stopwords;
import it.uniroma1.lcl.jlt.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.util.Pair;

/**
 * a class for computing semantic similarity of a pair of lexical items
 * @author pilehvar
 *
 */
public class TextualSimilarity
{
	private static final Log log = LogFactory.getLog(TextualSimilarity.class);
	
	private static TextualSimilarity instance;
	
	private static List<Character> TAGS = Arrays.asList(new Character[]{'V','R','J','N'});
	
	private static MultiHashMap<String,String> allWordNetEntries = null;
	
	private static boolean discardStopwords = ADWConfiguration.getInstance().getDiscardStopwordsCondition();
	
	private static EnglishLemmatizer el = null;
	
	public TextualSimilarity()
	{
		if(allWordNetEntries == null)
		{
			allWordNetEntries = new MultiHashMap<String,String>();
		
			for(String tag : Arrays.asList("n","v","r","a"))
				allWordNetEntries.putAll(tag, WordNetUtils.getInstance().getAllWords(GeneralUtils.getTagfromTag(tag)));	
		
		}
		
		if(el == null)
		{
			try
			{
				el = new EnglishLemmatizer();	
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Used to access {@link TextualSimilarity}
	 * 
	 * @return an instance of {@link TextualSimilarity}
	 */
	public static TextualSimilarity getInstance()
	{
		try
		{
			if (instance == null) instance = new TextualSimilarity();
			return instance;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * returns 
	 * @param sentence
	 * 			input sentence, space delimited
	 * @param discardStopWords
	 * 			true if stopwords are to be discarded from the sentence 			
	 * @return
	 * 		a pair containing <list of word-pos, remaining not-handled terms>  
	 * 		
	 */
	public Pair<List<String>, List<String>> getStanfordSentence(String sentence)
	{
		List<WordLemmaTag> wlts = SentenceProcessor.getInstance().processSentence(sentence, false);
		
		List<String> terms = null;
		StanfordSentence sSentence = StanfordSentence.fromLine(Strings.join(wlts," "));
		
		try
		{
			 terms = sSentence.getTerms(TAGS, 
					 Language.EN, 
					 null, 
					 MultiwordBelongingTo.WORDNET, 
					 CompoundingParameter.ALLOW_MULTIWORD_EXPRESSIONS,
					 CompoundingParameter.APPEND_POS);	 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		//discards OOVs, and tries to map incorrect pos-tags to the correct ones
		return fixTerms(terms, discardStopwords);
	}
	
	/**
	 * @param terms
	 * 			a list of word-pos delimited by "#" 
	 * @param discardStopWords
	 * 			true if stopwords are to be discarded from the sentence
	 * @return
	 * 			a pair containing <list of fixed word-pos, remaining not-handled terms>
	 * 
	 */
	private Pair<List<String>,List<String>> fixTerms(List<String> terms, boolean discardStopWords)
	{
		List<String> fixedTerms = new ArrayList<String>();
		List<String> remainings = new ArrayList<String>();
		List<String> tags = Arrays.asList("n","v","r","a");
		
		try
		{
			for(String term: terms)
			{
				String comps[] = term.split("#");
				String tg = comps[1];
				String lemma = WordNetUtils.getInstance().getSingularOf(comps[0], POS.NOUN);
				String fixedTag = tg;
				
				if(!isOOV(lemma, tg))
				{
					if(!discardStopWords || (discardStopWords && !Stopwords.getInstance().isStopword(lemma)))
					{
						fixedTerms.add(lemma+"#"+tg);
						continue;
					}
				}
				
				//the word is OOV, try to fix the lemma or the pos tag
				for(String tag : tags)
				{
					//if the word belongs to another POS category
					if(allWordNetEntries.get(tag).contains(lemma))
					{
						fixedTag = tag;
						break;
					}
					else	
					{
						//try to fix the lemma in this case
						String wnLemma = el.getWordNetLemma(lemma, tag);
						
						if(wnLemma == null)
						{
							for(String l : WordNetUtils.getInstance().getWordNetStems(lemma))
								if(allWordNetEntries.get(tag).contains(l))
									lemma = l;
						}
						else
						{
							lemma = wnLemma;
							break;
						}
					}
				}

				if(tags.contains(fixedTag))
				{
					if(!isOOV(lemma, fixedTag))
					{
						if(!discardStopWords || (discardStopWords && !Stopwords.getInstance().isStopword(lemma)))
							fixedTerms.add(lemma+"#"+fixedTag);

						continue;
					}
				}
				
				//gather whatever non-punctuation thing remaining
				if(!discardStopWords || (discardStopWords && !Stopwords.getInstance().isStopword(lemma)))
					if(!lemma.matches("\\W"))
						remainings.add(lemma);
			}
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return new Pair<List<String>,List<String>>(fixedTerms,remainings);
	}
	
	/**
	 * Checks if a word is WordNet OOV
	 * @param word
	 * @param tag
	 * @return
	 */
	public boolean isOOV(String word, String tag)
	{
		if(tag == null  || tag.trim().length() == 0 || tag.equals("?"))
			return true;
		
		if(allWordNetEntries.get(tag).contains(word))
			return false;
		else
			return true;
	}
	
	/**
	 * 
	 * @param inSentence
	 * @param discardStopwords
	 * @return Pair<List of cooked sentences, List of non-formatted remaining (non-wordnet words)>
	 */
	public Pair<List<String>,List<String>> cookSentence(String inSentence)
	{
		try
		{
			
			Pair<List<String>, List<String>> sent = getStanfordSentence(inSentence);
			
			List<String> sentence = sent.first;
			List<String> remainings = sent.second;
			
			 //discard duplicates
			 Set<String> covered = new HashSet<String>();
			 
			 StringBuffer sb = new StringBuffer();
			 
			 for(String word : sentence)
			 {
				 String lemma = word.split("#")[0];
				 String tag = word.split("#")[1];
				 
				 if(covered.contains(word)) 
					 continue;
				 
				 sb.append(lemma);
				 sb.append("#");
				 sb.append(tag);
				 sb.append(" ");
				 
				 covered.add(word);
			 }	
			 
			
			if(sb.toString().trim().length() == 0)
				return new Pair<List<String>,List<String>>(null,remainings);
			else
				return new Pair<List<String>,List<String>>(Arrays.asList(sb.toString().split(" ")),remainings);	
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Strips the sentence from pos tags attached to words
	 * @param ins
	 * @return
	 */
	public List<String> stripSentence(List<String> ins)
	{
		List<String> stripped = new ArrayList<String>();
		
		if(ins == null)
			return stripped;
		
		for(String s : ins)
			stripped.add(s.split("#")[0]);
		
		return stripped;
	}
	
	
	/**
	 * gets the average vector of words in a sentence
	 * @param sentence
	 * @param vectorSize
	 * @param discardStopwords
	 * @param babelnet
	 * @return
	 */
	public List<List<SemSig>> getSenseVectorsFromCookedSentence(List<String> words, LKB lkb, int vectorSize)
	{
		List<List<SemSig>> vectors = new ArrayList<List<SemSig>>();
		
		for(String w : words)
		{
			String word = w.split("#")[0];
			String tag = w.split("#")[1];
			
			List<SemSig> thisVectors = new ArrayList<SemSig>();
			
			//if it is a synset offset
			if(w.matches("[0-9]*-[arvn]"))
			{
				thisVectors.add(SemSigProcess.getInstance().getSemSigFromOffset(word, lkb, vectorSize));
			}
			else
			{
				for(IWord sense : WordNetUtils.getInstance().getSenses(word, GeneralUtils.getTagfromTag(tag)))
				{
					thisVectors.add(SemSigProcess.getInstance().getSemSigFromIWord(sense, lkb, vectorSize));
				}
			}
			
			vectors.add(thisVectors);
		}
		
		return vectors;
		
	}
	
	
	public List<SemSig> getSenseVectorsFromOffsetSentence(List<String> offsets, ItemType type, LKB lkb, int vecSize)
	{
		List<SemSig> vectors = new ArrayList<SemSig>();

		for(String offset : offsets)
		{
			SemSig v = SemSigProcess.getInstance().getSemSigFromOffset(offset, lkb, vecSize);
				
			vectors.add(v);
		}
		
		return vectors;
	}
	
	/**
	 * Greedily fixed the pos tags so that the same word appearing on both sides always gets the same POS  	
	 * @param first
	 * 			first lexical item, space-delimited
	 * @param second
	 * 			second lexical item, space-delimited
	 * @return
	 * 			a pair consisting of the two fixed sentences, in the same order
	 */
	public static Pair<List<String>,List<String>> fixPOSmirroring(List<String> first, List<String> second)
	{
		List<String> fixedA = new ArrayList<String>();
		
		HashMap<String,String> mapA = new HashMap<String,String>();
		HashMap<String,String> mapB = new HashMap<String,String>();
	
		for(String s : first)
		{
			if(s.matches("[0-9]*-[arvn]")) continue;		//discard direct concepts in the context
			mapA.put(s.split("#")[0], s.split("#")[1]);
		}
		
		for(String s : second)
		{
			if(s.matches("[0-9]*-[arvn]")) continue;		//discard direct concepts in the context
			mapB.put(s.split("#")[0], s.split("#")[1]);
		}
		
		for(String s : first)
		{
			String term = s.split("#")[0];
			if(mapB.containsKey(term))
				fixedA.add(s.replace("#"+mapA.get(term),"#"+mapB.get(term)));
			else
				fixedA.add(s);
		}
		
		return new Pair<List<String>,List<String>>(fixedA,second);
		
	}
	
	/**
	 * 
	 * @param firstSenses
	 * 			list of list of all the senses of the words in the first sentence,
	 * 			assumes the {@link SemSig}} to be sorted and normalized
	 * @param secondSenses
	 * 			the set of all senses of the words in the second sentence
	 * 			assumes the {@link SemSig}} to be sorted and normalized
	 * @param vectorSize
	 * 			the alignment measure
	 * @param mirror
	 * 			false if all the words of the two sides to be considered while alignment
	 * @param toBeTakens
	 * 			synsets that are to be taken if encountered among senses of a word
	 * @return
	 * 			a LinkedHashMap of disambiguated src sense and the aligned target sense, as well as their similarity score 
	 */
	public LinkedHashMap<Pair<SemSig,SemSig>,Double> alignmentBasedDisambiguation(
			List<List<SemSig>> firstSenses, 
			Set<SemSig> secondSenses, 
			SignatureComparison measure, 
			int vectorSize,
			Set<SemSig> toBeTakens)
	{
		LinkedHashMap<Pair<SemSig,SemSig>,Double> alignments = new  LinkedHashMap<Pair<SemSig,SemSig>,Double>();
		 
		for(List<SemSig> firstSense : firstSenses)
		{
			double overallMax = 0;
			SemSig bestSrcSense = null;
			SemSig bestTrgSense = null;
			
			boolean done = false;
			
			//check if any of the senses are in the toBeTakens list
			for(SemSig fSense : firstSense)
			{
				if(toBeTakens.contains(fSense))
				{
					done = true;
					bestSrcSense = fSense;
					bestTrgSense = fSense;
				}
			}

			//check if any of the senses exists on the target side
			for(SemSig sense :  firstSense)
			{
				if(secondSenses.contains(sense))
				{
					done = true;
					bestSrcSense = sense;
					bestTrgSense = sense;
				}
			}
			
			//for senses of each word in src sentence
			if(!done)
			{
				for(SemSig aFSense : firstSense)
				{
					//if maximum has already been reached, no need to further investigate
					if(overallMax == 1) continue;
						
					double maxSimilarity = 0;
					SemSig closest = null;
					
					//compare to all senses of the words in target sentence and keep the closest sense
					for(SemSig secondSense : secondSenses)
					{
						//if maximum has already been reached, no need to further investigate
						if(maxSimilarity == 1) continue;
						
						double thisSimilarity = SemSigComparator.compare(aFSense, secondSense, measure, vectorSize, true, true);
						
						if(thisSimilarity > maxSimilarity)
						{
							maxSimilarity = thisSimilarity;
							closest = secondSense;
						}
						
					}
					
					if(maxSimilarity > overallMax)
					{
						overallMax = maxSimilarity;
						bestSrcSense = aFSense;
						bestTrgSense = closest;
					}
					
				}
			}
			
			if (bestSrcSense == null)
			{
				bestSrcSense = new SemSig();
				bestSrcSense.setOffset("null");
			}
			
			if (bestTrgSense == null)
			{
				bestTrgSense = new SemSig();
				bestTrgSense.setOffset("null");
			}
				
			alignments.put(new Pair<SemSig,SemSig>(bestSrcSense, bestTrgSense), overallMax);
		}
		
		return alignments;
	}
	
	
	
	public static void main(String args[]) throws IOException
	{
		TextualSimilarity TS = new TextualSimilarity();
		
		log.info(TS.getStanfordSentence(
				"Slovakia has not been condemned to the second division and it is logical that it should like to join together with the Czech Republic."
				).second);
				
		System.out.println(TS.getSenseVectorsFromCookedSentence(TS.cookSentence("Slovakia has not been condemned to the second division and it is logical that it should like to join together with the Czech Republic.").first, LKB.WordNetGloss,0));
	}
	
}
