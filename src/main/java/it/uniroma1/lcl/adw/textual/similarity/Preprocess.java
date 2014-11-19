package it.uniroma1.lcl.adw.textual.similarity;

import it.uniroma1.lcl.adw.ADWConfiguration;
import it.uniroma1.lcl.adw.utils.DataProcessor;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.adw.utils.StanfordTokenizer;
import it.uniroma1.lcl.adw.utils.WordNetUtils;
import it.uniroma1.lcl.jlt.util.EnglishLemmatizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.util.Pair;

/**
 * a class for preprocessing a surface-level lexical item
 * @author pilehvar
 *
 */
public class Preprocess 
{
	private static Preprocess instance;

	private Preprocess() { }

	public static synchronized Preprocess getInstance()
	{
		if (instance == null) instance = new Preprocess();
		return instance;
	}

	
	/**
	 * removes hyphen only if the word cannot be found in WN 
	 * replace by space or blank according to existence of the word in WN
	 * @param sentence
	 * @return
	 */
	public static String removeHyphens(String sentence)
	{
		String cleanedSentence = "";
		
		for(String word : StanfordTokenizer.getInstance().tokenizeString(sentence))
		{
			word = word.replaceAll("\\\\", "");
			if(!word.contains("-") && !word.contains("/") && !word.contains("<") && !word.contains(">"))
				cleanedSentence += word;
			else
			{
				if(WordNetUtils.getInstance().inVocabulary(WordNetUtils.getInstance().getSingularOf(word, POS.NOUN)))
					cleanedSentence += word;
				else
				{
					String replacement = word.replaceAll("[-/<>]", "").trim();
				
					if(WordNetUtils.getInstance().inVocabulary(WordNetUtils.getInstance().getSingularOf(replacement, POS.NOUN)))
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
		
//		int index = 1;
		
		for(Pair<String,String> aPair : pairs)
		{
			//System.out.println("[working on "+ index++ +"]");
			List<String> sentA = TS.cookSentence(aPair.first).first;
			List<String> sentB = TS.cookSentence(aPair.second).first;
			
			//System.out.println(sentA+"\t"+sentB);
			
			List<String> strippedA = TS.stripSentence(sentA);
			List<String> strippedB = TS.stripSentence(sentB);

			for(String a : strippedA)
			{
				///
				
				if(!WordNetUtils.getInstance().inVocabulary(WordNetUtils.getInstance().getSingularOf(a, POS.NOUN)))
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
		tempList.add(new Pair<String,String>(sentencePair.second,sentencePair.first));
		
		int direction = 0;
		for(Pair<String,String> aPair : tempList)
		{
			HashMap<String,String> replacement = new HashMap<String,String>();
			List<WordLemmaTag> wlts = DataProcessor.getInstance().processSentence(aPair.first, false);
			
			try
			{
				EnglishLemmatizer el = new EnglishLemmatizer();
				
				for(WordLemmaTag wlt : wlts)
				{
					String tg = GeneralUtils.shortenTagString(wlt.tag());
					
					String lemma = WordNetUtils.getInstance().getSingularOf(wlt.lemma(), POS.NOUN).toLowerCase();
					
					if(tags.contains(tg) && ! WordNetUtils.getInstance().inVocabulary(lemma))
					{
						String wnLemma = el.getWordNetLemma(wlt.word(), tg);
						
						
						if(wnLemma == null)
						{
							for(String t : tags)
								if(WordNetUtils.getInstance().inVocabulary(lemma, GeneralUtils.getTagfromTag(t)))
								{
									tg = t;
									break;
								}
						}
						
						String alternative = getAlternative(wlt.lemma(),aPair.second);
						if(tags.contains(tg) && !WordNetUtils.getInstance().inVocabulary(WordNetUtils.getInstance().getSingularOf(lemma.toLowerCase(), POS.NOUN)))
							if(!aPair.second.contains(wlt.lemma()))
							{
								if(!alternative.equals(wlt.lemma()))
								{
									replacement.put(wlt.lemma(), alternative);
									System.out.println(wlt.lemma()+"\t"+alternative+"\t"+direction);
									System.out.println("\t"+aPair.first);
									System.out.println("\t"+aPair.second);
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
		
		List<String> sentenceA = StanfordTokenizer.getInstance().tokenizeString(pair.first);
		List<String> sentenceB = StanfordTokenizer.getInstance().tokenizeString(pair.second);
		
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
		
		System.out.println("\t\t"+String.join(" ", newSentenceA));
		System.out.println("\t\t"+String.join(" ", newSentenceB)+"\n");
		return new Pair<String,String>(String.join(" ", newSentenceA), String.join(" ", newSentenceB));
	}
	
	public static Pair<String,String> caseFixer(Pair<String,String> pair)
	{
		List<String> sentenceA = StanfordTokenizer.getInstance().tokenizeString(pair.first);
		List<String> sentenceB = StanfordTokenizer.getInstance().tokenizeString(pair.second);
		
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
		
		return new Pair<String,String>(String.join(" ",newSentenceA),String.join(" ", newSentenceB));
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
			
		List<String> cands = new ArrayList<String>(SemSigUtils.sortByValue(candidates).keySet());
		
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
			BufferedWriter bw = new BufferedWriter(new FileWriter(ADWConfiguration.getInstance().getOffsetMapPath(), false)); 
					
			int i = 1;
			for(Pair<String,String> aPair : pairs)
			{
				System.out.println("[working on "+ i++ +"]");
				String first = aPair.first;
				String second = aPair.second;

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
				BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false)); 
				
				//spellchecking
				for(Pair<String,String> aPair : pairs)
				{
					Pair<String,String> fixedPair = spellCorrect(aPair);
					bw.write(fixedPair.first+"\t"+fixedPair.second+"\n");
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
			BufferedWriter bw = new BufferedWriter(new FileWriter(path, false)); 
			
			for(Pair<String,String> aPair : pairs)
			{
				Pair<String,String> fixedPair = caseFixer(aPair);
				
				bw.write(fixedPair.first+"\t"+fixedPair.second+"\n");
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
		
		System.out.println(removeHyphens("A man is holding up and talking about a gray You Tube T-shirt"));
			
		TextualSimilarity TS = new TextualSimilarity();
		
		System.out.println(TS.cookSentence("an approximated amount of something"));
		
	}
	

}
