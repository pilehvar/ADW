package it.uniroma1.lcl.adw.textual.similarity;

import it.uniroma1.lcl.adw.SimilarityMeasure;
import it.uniroma1.lcl.adw.semsig.LKB;
import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigProcess;
import it.uniroma1.lcl.adw.semsig.comparison.SemSigComparator;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.jlt.pipeline.stanford.DataProcessor;
import it.uniroma1.lcl.jlt.pipeline.stanford.StanfordSentence;
import it.uniroma1.lcl.jlt.pipeline.stanford.StanfordSentence.CompoundingParameter;
import it.uniroma1.lcl.jlt.pipeline.stanford.StanfordSentence.MultiwordBelongingTo;
import it.uniroma1.lcl.jlt.util.EnglishLemmatizer;
import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.jlt.util.Pair;
import it.uniroma1.lcl.jlt.util.Stopwords;
import it.uniroma1.lcl.jlt.util.Strings;
import it.uniroma1.lcl.jlt.wordnet.WordNet;

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

/**
 * a class for computing semantic similarity of a pair of lexical items
 * @author pilehvar
 *
 */
public class TextualSimilarity
{
	private static final Log log = LogFactory.getLog(TextualSimilarity.class);
	
	static private TextualSimilarity instance;
	
	
	List<Character> TAGS = Arrays.asList(new Character[]{'V','R','J','N'});
	
	static MultiHashMap<String,String> allWordNetEntries = new MultiHashMap<String,String>();
	
	
	static WordNet WN = WordNet.getInstance();
	EnglishLemmatizer el = null;
	
	public TextualSimilarity()
	{
		for(String tag : Arrays.asList("n","v","r","a"))
			allWordNetEntries.putAll(tag, WordNet.getInstance().getAllWords(GeneralUtils.getTagfromTag(tag)));	
	
		try
		{
			el = new EnglishLemmatizer();	
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
			throw new RuntimeException("Could not init TextualSimilarity: " + e.getMessage());
		}
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
	public Pair<List<String>, List<String>> getStanfordSentence(String sentence, boolean discardStopWords)
	{
		List<WordLemmaTag> wlts = DataProcessor.getInstance().processSentence(sentence, false);
		
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

		//discards OOVs, and tried to map incorrect pos-tags to the correct ones
		return fixTerms(terms, discardStopWords);
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
				String lemma = WordNet.getInstance().getSingularOf(comps[0]);
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
							for(String l : WordNet.getInstance().getWordNetStems(lemma))
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
		if(tag == null || tag.equals("?"))
			return true;
		
		if(allWordNetEntries.get(tag).contains(word))
			return false;
		else
			return true;
	}
	
	public boolean isOOV(String wordpos)
	{
		String comps[] = wordpos.split("#");
		String word = comps[0];
		String tag = comps[1];
		
		if(tag == null || tag.equals("?"))
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
	 * @return Pair<List of cooked sentences, List of non-formatted remainings (non-wordnet words)>
	 */
	public Pair<List<String>,List<String>> cookSentence(String inSentence, boolean discardStopwods)
	{
		try
		{
			
			Pair<List<String>, List<String>> sent = getStanfordSentence(inSentence, discardStopwods);
			
			List<String> sentence = sent.getFirst();
			List<String> remainings = sent.getSecond();
			
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
	
	public List<String> stripSentence(List<String> ins)
	{
		List<String> stripped = new ArrayList<String>();
		
		if(ins == null)
			return stripped;
		
		for(String s : ins)
			stripped.add(s.split("#")[0]);
		
		return stripped;
	}
	
	
	//TODO continue deleting here and fixing the preprocess, make the functions to average the vectors
	//make eveything ready just the database part!
	
	/**
	 * gets the average vector of words in a sentence
	 * @param sentence
	 * @param vectorSize
	 * @param discardStopwords
	 * @param babelnet
	 * @return
	 */
	public List<List<SemSig>> getSenseVectorsFromCookedSentence(List<String> words, LKB lkb)
	{
		/*
		 * Disk-based retrieval 
		 */
		
		List<List<SemSig>> vectors = new ArrayList<List<SemSig>>();
		
		for(String w : words)
		{
			String word = w.split("#")[0];
			String tag = w.split("#")[1];
			
			SemSig v = null;
			
			List<SemSig> thisVectors = new ArrayList<SemSig>();
			
			//if it is a synset offset
			if(w.matches("[0-9]*-[arvn]"))
			{
				v = SemSigProcess.getInstance().getSemSigFromOffset(word, lkb, 0);
			}
			else
			{
				for(IWord sense : WordNet.getInstance().getSenses(word, GeneralUtils.getTagfromTag(tag)))
				{
					v = SemSigProcess.getInstance().getSemSigFromIWord(sense, lkb, 0);
					
					thisVectors.add(v);
				}
			}
			
			vectors.add(thisVectors);
		}
		
		return vectors;
		
		
		
		/*
		 * Index-based retrieval
		 */
		/*
		IndexVectoring IV = new IndexVectoring(lkb);
		
		List<List<SemSig>> vectors = new ArrayList<List<SemSig>>();
		
		for(String w : words)
		{
			String word = w.split("#")[0];
			String tag = w.split("#")[1];
			
			List<SemSig> thisVectors = new ArrayList<SemSig>();
			for(IWord sense : WordNet.getInstance().getSenses(word,general.getPOSfromString(tag)))
				thisVectors.add(IV.getVectorFromIWord(sense));
			
			vectors.add(thisVectors);
		}
		
		return vectors;
		
		*/
		
	}
	
	
	public List<SemSig> getSenseVectorsFromOffsetSentence(List<String> offsets, LKB lkb)
	{
		List<SemSig> vectors = new ArrayList<SemSig>();
		
		for(String offset : offsets)
		{
			SemSig v = SemSigProcess.getInstance().getSemSigFromOffset(offset, lkb, 0);
				
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
	
	public Set<String> getWordNetOffsets(String wordPos)
	{
		Set<String> offsets = new HashSet<String>();

		wordPos = wordPos.trim();
		
		if(wordPos.matches("[0-9]*-[nrva]"))
		{
			offsets.add(wordPos);
		}
		else
		{
			String comps[] = wordPos.split("#");
			
			if(comps.length != 2)
			{
				log.error("mal-formatted word-pos: "+wordPos);
				return null;
			}
			
			String word = comps[0];
			POS pos = GeneralUtils.getTagfromTag(comps[1]);
			
			List<IWord> senses = WordNet.getInstance().getSenses(word, pos);
			
			if(senses == null)
				return null;
			
			for(IWord sense : senses)
			{
				offsets.add(GeneralUtils.fixOffset(sense.getSynset().getOffset(), pos));
			}
		}
		
		return offsets;
	}
	
	/**
	 * 
	 * @param firstSenses
	 * 			list of list of all the senses of the words in the first sentence
	 * @param secondSenses
	 * 			the set of all senses of the words in the second sentence
	 * @param vectorSize
	 * 			the alignment measure
	 * @param mirror
	 * 			false if all the words of the two sides to be considered while alignment
	 * @param toBeTakens
	 * 			synsets that are to be taken if encountered among senses of a word
	 * @return
	 * 			a LinkedHashMap of disambiguated src sense and the aligned target sense, as well as their similarity score 
	 */
	public static LinkedHashMap<Pair<SemSig,SemSig>,Double> semanticAlignerBySense(
			List<List<SemSig>> firstSenses, 
			Set<SemSig> secondSenses, 
			SimilarityMeasure measure, 
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
						
						//System.out.println("[working on "+ aFSense.getOffset() + " and "+ secondSense.getOffset()+"]");
						
						//getting vectors from index, they are already sorted
						//double thisSimilarity = VectorComparator.compare(aFSense.getVector(), secondSense.getVector(), vectorSize, measure, true);
						
						//if(counter++ %1000==0)
							//System.out.println("comparing "+aFSense.getOffset()+" and "+secondSense.getOffset() +"\t"+ counter);
						
						double thisSimilarity = SemSigComparator.compare(aFSense, secondSense, measure, vectorSize);
						
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
		
		//TS.runDirectComparison();
		
//		System.out.println(TS.getStanfordSentence(
//				"Slovakia has not been condemned to the second division and it is logical that it should like to join together with the Czech Republic."
//				, true).getFirst());
				
		System.out.println(TS.cookSentence("Slovakia has not been condemned to the second division and it is logical that it should like to join together with the Czech Republic.", true));
		
//		System.out.println(TS.revisedCookSentence("A person who takes on a motor vehicle such as a car or a bus bmw.", 
//				true, true, true, null, true, true));
		//TS.runDatasetGenerator();
		//TS.runDirectComparison();
		
		
//		System.out.println(TS.fixPOSmirroring(Arrays.asList("15215844-n mouse#v jumped#v over#r table#n".split(" ")), Arrays.asList("mouse#n jumped#v over#r table#n".split(" "))));	
		
	}
	
}
