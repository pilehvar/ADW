package it.uniroma1.lcl.adw.textual.similarity;

import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.jlt.pipeline.stanford.DataProcessor;
import it.uniroma1.lcl.jlt.pipeline.stanford.StanfordTokenizer;
import it.uniroma1.lcl.jlt.util.EnglishLemmatizer;
import it.uniroma1.lcl.jlt.util.Files;
import it.uniroma1.lcl.jlt.util.Maps;
import it.uniroma1.lcl.jlt.util.Maps.SortingOrder;
import it.uniroma1.lcl.jlt.util.Pair;
import it.uniroma1.lcl.jlt.util.Strings;
import it.uniroma1.lcl.jlt.wordnet.WordNet;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.WordLemmaTag;

/**
 * a class for preprocessing a surface-level lexical item
 * @author pilehvar
 *
 */
public class Preprocess 
{
	static Set<String> allWNWords = null;
	
	
	/**
	 * removes hyphen only if the word cannot be found in WN 
	 * replace by space or blank according to existence of the word in WN
	 * @param sentence
	 * @return
	 */
	public static String removeHyphens(String sentence)
	{
		if(allWNWords == null)
			allWNWords = WordNet.getInstance().getAllWords();
		
		String cleanedSentence = "";
		
		for(String word : StanfordTokenizer.getInstance().tokenizeString(sentence))
		{
			word = word.replaceAll("\\\\", "");
			if(!word.contains("-") && !word.contains("/") && !word.contains("<") && !word.contains(">"))
				cleanedSentence += word;
			else
			{
				if(WordNet.getInstance().containsLemma(WordNet.getInstance().getSingularOf(word)))
					cleanedSentence += word;
				else
				{
					String replacement = word.replaceAll("[-/<>]", "").trim();
				
					if(WordNet.getInstance().containsLemma(WordNet.getInstance().getSingularOf(replacement)))
						cleanedSentence += replacement;
					else
						cleanedSentence += word.replaceAll("[-/><]", " ").trim();
				}
			}
			
			cleanedSentence += " ";
		}
		
		return cleanedSentence.trim();
	}
	
	public static Pair<List<String>,List<String>> mirrorCompounder(List<Pair<String,String>> pairs)
	{
		TextualSimilarity TS = new TextualSimilarity();
		
		WordNet wn = WordNet.getInstance();
		Set<String> allWNWords = wn.getAllWords();
		
//		int index = 1;
		
		for(Pair<String,String> aPair : pairs)
		{
			//System.out.println("[working on "+ index++ +"]");
			List<String> sentA = TS.cookSentence(aPair.getFirst(), true).getFirst();
			List<String> sentB = TS.cookSentence(aPair.getSecond(), true).getFirst();
			
			//System.out.println(sentA+"\t"+sentB);
			
			List<String> strippedA = TS.stripSentence(sentA);
			List<String> strippedB = TS.stripSentence(sentB);

			for(String a : strippedA)
			{
				///
				
				if(!allWNWords.contains(WordNet.getInstance().getSingularOf(a)))
					System.out.println(a);
				
				
				///
								
				
				int indexb = 0;
				for(String b : strippedB)
				{
					if(a.contains(b))
					{
						String aSecond = a.replace(b, "");
						
						if(strippedB.size() > indexb+1)
							if(aSecond.equals(strippedB.get(indexb+1)))
							{
								System.out.println(aSecond+" "+strippedB.get(indexb+1));
								System.out.println("Sentences are potentially mirrorable: "+ strippedA+"\t"+strippedB);
							}
					}
					
					indexb++;
				}
			}
			
			List<String> temp = strippedA;
			strippedA = strippedB;
			strippedB = temp;
					
			for(String a : strippedA)
			{
				int indexb = 0;
				for(String b : strippedB)
				{
					if(a.contains(b))
					{
						String aSecond = a.replace(b, "");
						
						if(strippedB.size() > indexb+1)
							if(aSecond.equals(strippedB.get(indexb+1)))
							{
								System.out.println(aSecond+" "+strippedB.get(indexb+1));
								System.out.println("Sentences are potentially mirrorable: "+ strippedA+"\t"+strippedB);
							}
					}
					
					indexb++;
				}
			}
		}
			
		return null;
	}
	
	/**
	 * returns <stanfordSentence, remaining non-WordNet words>
	 * @param sentence
	 * @return
	 */
	public static Pair<String,String> spellCorrect(Pair<String,String> sentencePair)
	{
		List<HashMap<String,String>> replacements = new ArrayList<HashMap<String,String>>();
		List<String> tags = Arrays.asList("n","v","r","a");
		
		List<Pair<String,String>> tempList = new ArrayList<Pair<String,String>>();
		
		tempList.add(sentencePair);
		tempList.add(new Pair<String,String>(sentencePair.getSecond(),sentencePair.getFirst()));
		
		int direction = 0;
		for(Pair<String,String> aPair : tempList)
		{
			HashMap<String,String> replacement = new HashMap<String,String>();
			List<WordLemmaTag> wlts = DataProcessor.getInstance().processSentence(aPair.getFirst(), false);
			
			try
			{
				EnglishLemmatizer el = new EnglishLemmatizer();
				
				for(WordLemmaTag wlt : wlts)
				{
					String tg = GeneralUtils.shortenTagString(wlt.tag());
					
					String lemma = WordNet.getInstance().getSingularOf(wlt.lemma()).toLowerCase();
					
					if(tags.contains(tg) && !WordNet.getInstance().containsLemma(lemma))
					{
						String wnLemma = el.getWordNetLemma(wlt.word(), tg);
						
						
						if(wnLemma == null)
						{
							for(String t : tags)
								if(WordNet.getInstance().containsLemma(lemma, GeneralUtils.getTagfromTag(t)))
								{
									tg = t;
									break;
								}
						}
						
						String alternative = getAlternative(wlt.lemma(),aPair.getSecond());
						if(tags.contains(tg) && !WordNet.getInstance().containsLemma(WordNet.getInstance().getSingularOf(lemma.toLowerCase())))
							if(!aPair.getSecond().contains(wlt.lemma()))
							{
								if(!alternative.equals(wlt.lemma()))
								{
									replacement.put(wlt.lemma(), alternative);
									System.out.println(wlt.lemma()+"\t"+alternative+"\t"+direction);
									System.out.println("\t"+aPair.getFirst());
									System.out.println("\t"+aPair.getSecond());
								}
							}
								
					}
				}
				
				direction++;
				replacements.add(replacement);
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return fixSpellErrors(sentencePair, replacements.get(0),replacements.get(1));
	}
	
	public static Pair<String,String> fixSpellErrors(Pair<String,String> pair, HashMap<String,String> replacementA, HashMap<String,String> replacementB)
	{
		
		List<String> sentenceA = StanfordTokenizer.getInstance().tokenizeString(pair.getFirst());
		List<String> sentenceB = StanfordTokenizer.getInstance().tokenizeString(pair.getSecond());
		
		List<String> newSentenceA = new ArrayList<String>();
		List<String> newSentenceB = new ArrayList<String>();
		
		Set<String> done = new HashSet<String>();
		for(String s : sentenceA)
		{
			if(replacementA.containsKey(s))
			{
				newSentenceA.add(replacementA.get(s));
				done.add(replacementA.get(s));
			}
			else
				newSentenceA.add(s);
		}
		
		for(String s : sentenceB)
		{
			if(replacementB.containsKey(s) && !done.contains(s))
				newSentenceB.add(replacementB.get(s));
			else
				newSentenceB.add(s);
		}
		
		System.out.println("\t\t"+Strings.join(newSentenceA," "));
		System.out.println("\t\t"+Strings.join(newSentenceB," ")+"\n");
		return new Pair<String,String>(Strings.join(newSentenceA," "),Strings.join(newSentenceB," "));
	}
	
	public static Pair<String,String> caseFixer(Pair<String,String> pair)
	{
		List<String> sentenceA = StanfordTokenizer.getInstance().tokenizeString(pair.getFirst());
		List<String> sentenceB = StanfordTokenizer.getInstance().tokenizeString(pair.getSecond());
		
		HashMap<String,String> replacementsA = new HashMap<String,String>();
		HashMap<String,String> replacementsB = new HashMap<String,String>();
		
		List<String> newSentenceA = new ArrayList<String>();
		List<String> newSentenceB = new ArrayList<String>();
		
		for(String w : sentenceA)
			if(sentenceB.contains(w.toLowerCase()) && !sentenceB.contains(w))
				replacementsA.put(w.toLowerCase(), w);
					
		for(String w : sentenceB)
			if(sentenceA.contains(w.toLowerCase()) && !sentenceA.contains(w))
				replacementsB.put(w.toLowerCase(), w);

		Set<String> done = new HashSet<String>();
		for(String s : sentenceA)
		{
			if(replacementsA.containsKey(s))
			{
				newSentenceA.add(replacementsA.get(s));
				done.add(replacementsA.get(s));
			}
			else
				newSentenceA.add(s);
		}
		
		for(String s : sentenceB)
		{
			if(replacementsB.containsKey(s) && !done.contains(s))
			{
				newSentenceB.add(replacementsB.get(s));
				done.add(s);
			}
			else
				newSentenceB.add(s);
		}
		
		return new Pair<String,String>(Strings.join(newSentenceA," "),Strings.join(newSentenceB," "));
	}
	
	
	public static String getAlternative(String word, String sentence)
	{
		List<String> wordChars = Arrays.asList(word.toLowerCase().split(""));

		HashMap<String,Integer> candidates = new HashMap<String,Integer>();
		
		for(String w : sentence.split(" "))
		{
			String wLowered = w.toLowerCase();
			List<String> wChars = Arrays.asList(wLowered.split(""));
			
			//System.out.println(wordChars+"\t"+wChars);
			if(Math.abs(wChars.size() - wordChars.size()) > 1) continue;
		
			int overlaps = getNumberOfOverlapChars(wordChars,wChars);
			
			if(overlaps >= wordChars.size() - 1)
				candidates.put(w,overlaps);
		}
			
		List<String> cands = new ArrayList<String>(Maps.sortByValue(candidates, SortingOrder.DESCENDING).keySet());
		
		if(cands.size() > 0)
			return cands.get(0);
		else
			return word;
	}
	
	public static int getNumberOfOverlapChars(List<String> aList, List<String> bList)
	{
		int over = 0;
		
		for(String a : aList)
			if(bList.contains(a))
				over++;
		
		return over;
	}
	
	public static String fixCurrency(String sentence)
	{
		return sentence.replaceAll("\\$US", "\\$");
	}
	
	
	public static String fixAbbrev(String sentence)
	{
		return sentence.replaceAll("I'm", "I am").replaceAll("n't", " not");
	}
	
	public static void pipeline(List<Pair<String,String>> pairs, String outPath)
	{
		try
		{
			BufferedWriter bw = Files.getBufferedWriter(outPath);
			
			int i = 1;
			for(Pair<String,String> aPair : pairs)
			{
				System.out.println("[working on "+ i++ +"]");
				String first = aPair.getFirst();
				String second = aPair.getSecond();

				//n't & 'm => not and am
				first = fixAbbrev(first);
				second = fixAbbrev(second);
				
				//remove hyphens
				first = removeHyphens(first);
				second = removeHyphens(second);
				
				//fixCurrency
				first = fixCurrency(first);
				second = fixCurrency(second);
				
				bw.write(first.replace(" .", ".")+"\t"+second.replace(" .", ".")+"\n");
			}
		
			bw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void manualCheck(List<Pair<String,String>> pairs, boolean flag, String filePath)
	{
		
		if(flag)
		{
			//mirror-compounding
			mirrorCompounder(pairs);
		}
		else
		{
			try
			{
				BufferedWriter bw = Files.getBufferedWriter(filePath);
				
				//spellchecking
				for(Pair<String,String> aPair : pairs)
				{
					Pair<String,String> fixedPair = spellCorrect(aPair);
					bw.write(fixedPair.getFirst()+"\t"+fixedPair.getSecond()+"\n");
				}
				
				bw.close();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
	}
	
	public static void fixAllCasings(List<Pair<String,String>> pairs, String path)
	{
		try
		{
			BufferedWriter bw = Files.getBufferedWriter(path);
			
			for(Pair<String,String> aPair : pairs)
			{
				Pair<String,String> fixedPair = caseFixer(aPair);
				
				bw.write(fixedPair.getFirst()+"\t"+fixedPair.getSecond()+"\n");
			}
			
			bw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		
		//System.out.println(removeHyphens("A man is holding up and talking about a gray You Tube T-shirt"));
			
		TextualSimilarity TS = new TextualSimilarity();
		
		System.out.println(TS.cookSentence("an approximated amount of something", true));
		System.exit(0);
		
//		boolean training = false;
//		boolean processed = false;
//		List<Pair<String,String>> pairs =  TS.getSentencePairs(PairSource.headlines13, training, processed);
		//PHASE [1]
		//pipeline(pairs, "/home/pilehvar/data/Semeval2012/TextualSimilarity/test-gold/STS13.input.headlines.processed.txt");
		
		//System.exit(0);
		
		//2.1 fix all
		//TODO: a better spell checking can improve many cases: motorcyle, tomatoe, potatoe...
		//2.2 spell correct only if they have the correct form on the other side (does not fix tomatoe, potatoe,...)
//		processed = true;
//		pairs =  TS.getSentencePairs(PairSource.headlines13, training, processed);
		
		//PHASE [2] spell checking
		//manualCheck(pairs, true,"/home/pilehvar/data/Semeval2012/TextualSimilarity/test-gold/STS13.input.headlines.processed.txt");
		
	}
}
