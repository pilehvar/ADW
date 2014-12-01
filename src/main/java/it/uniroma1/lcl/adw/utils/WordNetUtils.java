package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.adw.ADWConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultimap;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IExceptionEntry;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.SynsetID;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordNetUtils
{
	private IDictionary dictionary;
	
	private static final Pattern wordSenseFormat = Pattern.compile("([^ ]*)[\\.#]([anvr])[\\.#](\\d+)");
	
	private static final Log log = LogFactory.getLog(WordNetUtils.class);
	
	private static WordNetUtils singleton = null;
	
	private static Set<String> allWNWords = null;
	
	private static HashMultimap<POS,String> allWNPOSWords = null; 
	
	private IStemmer wnStemmer;
	
	public static WordNetUtils getInstance()
	{
		if (singleton == null)
		{
			singleton = new WordNetUtils();
		}
		
		return singleton;
	}
	
	private WordNetUtils()
	{
		try
		{
			final String location = ADWConfiguration.getInstance().getWordNetData();
			final Dictionary dict = new Dictionary(new URL("file", null, location));
			dict.getCache().setMaximumCapacity(Integer.MAX_VALUE);
			this.dictionary = dict;

			this.dictionary.open();
			
			wnStemmer = new WordnetStemmer(dict);
		}
		catch (Exception mue)
		{
			mue.printStackTrace();
		}
		
	}
	
	private void setAllWordNetWords()
	{
		if(allWNWords != null) return;
		
		allWNWords = new HashSet<String>();
		allWNPOSWords = HashMultimap.create();
		
		for(POS pos : Arrays.asList(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB))
		{
			Set<String> current = WordNetUtils.getInstance().getAllWords(pos);
			
			allWNWords.addAll(current);
			allWNPOSWords.putAll(pos, current);
		}
		
	}
	
	public String mapOffsetToReadableForm(String offset, String word, POS tag)
	{
		List<IWord> senses = getSenses(word, tag);
		ISynset syn = getSynsetFromOffset(offset);
			
		int senseRank = 1;
		for(IWord sense : senses)
		{
			if(sense.getSynset() == syn)
				break;
			
			senseRank++;
		}
		
		if(senseRank > senses.size())
		{
			log.warn("[ERROR: could not generate the readable form for "+word+" "+offset);
			return "null";
		}

		return word+"."+senseRank;
	}
	
	public IWord mapWordSenseToIWord(String wordSense)
	{
		Matcher m = wordSenseFormat.matcher(wordSense);
		
		if(m.find())
		{
			String word = m.group(1);
			POS tag = GeneralUtils.getTagfromTag(m.group(2));
			int sense = Integer.parseInt(m.group(3));
			
			List<IWord> senses = getSenses(word, tag);
			
			if(senses.size() < sense)
				return null;
			
			return senses.get(sense-1);			
			
		}
		else
		{
//			log.warn("[ERROR: non matching regular expression at "+ wordSense+"]");
//			System.exit(0);
			return null;
		}			
	}
	
	
	public static String fixOffset(int offset, POS tag)
	{
		String foff = Integer.toString(offset);
		
		while(foff.length() < 8)
			foff = "0"+foff;
		
		if(tag.equals(POS.ADJECTIVE))
			foff += "-a";
		else
			foff += "-"+tag.getTag();
		
		return foff;
	}
	
	public String getOffsetFromIWord(String senseKey)
	{
		IWord sense = getSenseFromSenseKey(senseKey);
		return fixOffset(sense.getSynset().getOffset(), sense.getPOS());
	}
	
	public String getOffsetFromWordSense(String wordSense)
	{
		IWord sense = mapWordSenseToIWord(wordSense);
		return fixOffset(sense.getSynset().getOffset(), sense.getPOS());
	}
	
	public static void main(String args[])
	{
		WordNetUtils utils = WordNetUtils.getInstance();
		System.out.println(utils.mapWordSenseToIWord("mouse.n.4"));
		
		System.out.println(utils.getSynsetFromOffset("15300051-n").getGloss());
		
	}
	
	public IWord getSenseFromSenseKey(final String sensekey)
	{
		final String lemma = sensekey.split("%")[0];
		final Set<IWord> senses = new HashSet<IWord>();
		for (POS pos : POS.values())
			senses.addAll(getSenses(lemma, pos));

		for (IWord sense : senses)
			if (sense.getSenseKey().toString().equals(sensekey))
				return sense;

		return null;
	}
	
	public boolean inVocabulary(String word)
	{
		if(allWNWords == null)
			setAllWordNetWords();

		return allWNWords.contains(word);
	}
	
	public boolean inVocabulary(String word, POS pos)
	{
		if(allWNPOSWords == null)
			setAllWordNetWords();

		return allWNPOSWords.containsKey(word);
	}
	
	public List<IWord> getSenses(String word, POS pos)
	{
		return getSenses(getIndexWord(word, pos));
	}
	
	public List<IWord> getSenses(IIndexWord idxWord)
	{
		List<IWord> senses = new ArrayList<IWord>();
		if (idxWord != null)
		{
			for (IWordID senseID : idxWord.getWordIDs())
				senses.add(dictionary.getWord(senseID));
		}
		return senses;
	}
	
	public IIndexWord getIndexWord(String word, POS pos)
	{
		return dictionary.getIndexWord(word, pos);
	}
	
	public ISynset getSynsetFromOffset(String value)
	{
		return dictionary.getSynset(getOffset(value));
	}
	
	public static SynsetID getOffset(String value)
	{
		if (value == null) return null;

		String[] fields = value.split("-");
		
		if (fields.length > 1)
			value = fields[0]+fields[1];
		
		if (value.length() != 9) return null;

		// get offset
		int offset = 0;
		try
		{
			offset = Integer.parseInt(value.substring(0, 8));
		}
		catch (Exception e)
		{
			return null;
		}

		// get pos
		char tag = Character.toLowerCase(value.charAt(8));
		POS pos = null;
		try
		{
			pos = POS.getPartOfSpeech(tag);
		}
		catch (Exception e)
		{
			return null;
		}

		if (pos != null) return new SynsetID(offset, pos);
		return null;
	}
	
	public static String trimOffset(String value)
	{


		return value;
	}

	 public Set<String> getAllWords(POS pos)
	 {
		 Set<String> words = new HashSet<String>();

		 Iterator<IIndexWord> i = dictionary.getIndexWordIterator(pos);
		 while(i.hasNext())
		 {
			 IIndexWord iw = (IIndexWord)i.next();
			 words.add(iw.getLemma());
		 }

		 return words;
	 }
	 
	 public String getSingularOf(String word, POS pos)
	 {
		// prima cerca tra le eccezioni
		IExceptionEntry exc = dictionary.getExceptionEntry(word, pos);
		if (exc != null) return exc.getRootForms().get(0);

		String suffix = "";
		if (pos == POS.NOUN)
		{
			// e.g., handsful
			if (word.endsWith("ful"))
			{
				word = word.substring(0, word.length()-3);
				suffix = "ful";
			}
			// e.g., kiss, s
			else if (word.endsWith("ss") || word.length() <= 2) return word;
		}

		String[] suffixes = null;
		String[] replacements = null;

		switch(pos)
		{
			case NOUN:
				suffixes = new String[] { "s", "ses", "shes", "ches", "zes", "xes", "men", "ies"};
				replacements = new String[] { "", "s", "sh", "ch", "z", "x", "man", "y" };
				break;

			case VERB:
				suffixes = new String[] { "s", "ies", "es", "es", "ed", "ed", "ing", "ing" };
				replacements = new String[] { "", "y", "e", "", "e", "", "e", "" };
				break;

			case ADJECTIVE:
				suffixes = new String[] { "er", "est", "er", "est" };
				replacements = new String[] { "", "", "e", "e" };
				break;

			case ADVERB:
			 	suffixes = new String[] {};
			 	break;
		 }

		for (int k = 0; k < suffixes.length; k++)
		{
			String ret;
			if (word.endsWith(suffixes[k]))
			{
				ret = word.substring(0, word.length()-suffixes[k].length())+replacements[k];

				// se esiste un senso della parola in WordNet
				if (getSenses(ret, pos).size() > 0) return ret+suffix;
			}
		}

		return word+suffix;
	 }
	 
	 public Set<String> getWordNetStems(String word)
	 {
		 Set<String> stems = new HashSet<String>();

		 for (POS pos : POS.values())
			 stems.addAll(wnStemmer.findStems(word, pos));

		 return stems;
	 }
}
