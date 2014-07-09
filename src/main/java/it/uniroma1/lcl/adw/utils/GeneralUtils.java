package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.adw.ADWConfiguration;
import it.uniroma1.lcl.jlt.util.Files;
import it.uniroma1.lcl.jlt.util.IntegerCounter;
import it.uniroma1.lcl.jlt.util.Maps;
import it.uniroma1.lcl.jlt.util.Maps.SortingOrder;
import it.uniroma1.lcl.jlt.wordnet.WordNet;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;

public class GeneralUtils
{
	/**
	 * converts {@link POS} to character
	 * @param tag
	 * @return
	 */
	public static char getTagfromTag(POS tag)
	{
		switch(tag)
		{
			case NOUN:
				return 'n';
				
			case VERB:
				return 'v';
				
			case ADJECTIVE:
				return 'a';
				
			case ADVERB:
				return 'r';
				
			default:
				return 'x';
		}
	}
	
	public static POS getTagfromTag(String tag)
	{
		if(tag.toLowerCase().startsWith("n"))
			return POS.NOUN;
		
		else
			if(tag.toLowerCase().startsWith("v"))
				return POS.VERB;
		
		else
			if(tag.toLowerCase().startsWith("r") || tag.toLowerCase().startsWith("adv"))
				return POS.ADVERB;
			
		else
			if(tag.toLowerCase().startsWith("j") 
					|| tag.toLowerCase().startsWith("adj") 
					|| tag.toLowerCase().startsWith("a"))
				return POS.ADJECTIVE;
			
		return null;
	}
	
	
	/**
	 * transforms character into part of speech ({@link POS})
	 * @param tag
	 * @return
	 */
	public static POS getTagfromTag(char tag)
	{
		switch(tag)
		{
			case 'n':
				return POS.NOUN;
			
			case 'v':
				return POS.VERB;
				
			case 'a':
				return POS.ADJECTIVE;
				
			case 'r':
				return POS.ADVERB;
				
			default:
				return null;
		}
	}
	
	/**
	 * gets an offset in integer form and returns it in full 8-letter string form with tag
	 * @param intOffset
	 * @param tag
	 * @return offset in full-form
	 */
	public static String fixOffset(int intOffset, POS tag)
	{
		String offset = Integer.toString(intOffset);
		
		while(offset.length() < 8)
			offset = "0" + offset;
		
		offset = offset + "-" + getTagfromTag(tag);
		
		return offset;
	}
	
	/**
	 * Normalizes the probability values in a vector so that to sum to 1.0
	 * @param vector
	 * @return
	 */
	public static Map<Integer,Float> normalizeVector(Map<Integer,Float> vector)
	{
		float total = 0;
		
		for(int s : vector.keySet())
			total += vector.get(s);
		
		Map<Integer,Float> normalizedVector = new HashMap<Integer,Float>();
		
		for(int s : vector.keySet())
			normalizedVector.put(s, vector.get(s)/total);
		
		return normalizedVector;
	}
	
	
	public static LinkedHashMap<Integer,Float> normalizeVector(LinkedHashMap<Integer,Float> vector)
	{
		float total = 0;
		for(int s : vector.keySet())
			total += vector.get(s);
		
		LinkedHashMap<Integer,Float> normalizedVector = new LinkedHashMap<Integer,Float>();
		
		for(int s : vector.keySet())
			normalizedVector.put(s, vector.get(s)/total);
		
		return normalizedVector;
	}
	
	/**
	 * return short (single letter) form of the tag, covers only nouns, verbs, adjectives, and adverbs 
	 * @param longTag
	 * @return
	 */
	public static String shortenTagString(String longTag)
	{
		longTag = longTag.toLowerCase();
		
		if(longTag.startsWith("n"))
			return "n";
		else
		if(longTag.startsWith("v"))
			return "v";
		else
		if(longTag.startsWith("adj") || longTag.startsWith("j"))
			return "a";
		else
		if(longTag.startsWith("adv") || longTag.startsWith("r"))
			return "r";
		else
			return null;
	}
	
	/**
	 * Truncates a vector to the top-n elements
	 * @param vector
	 * @param size
	 * @param normalize
	 * @return truncated vector
	 */
	public static LinkedHashMap<Integer,Float> truncateVector(Map<Integer,Float> vector, int size, boolean normalize)
	{
		LinkedHashMap<Integer,Float> sortedMap = new LinkedHashMap<Integer,Float>();
		
		int i = 0;
		for(int key : Maps.sortByValue(vector, SortingOrder.DESCENDING).keySet())
		{
			sortedMap.put(key, vector.get(key));
			
			if(i++ >= size) break;
		}
		
		if(normalize)
		{
			sortedMap = normalizeVector(sortedMap);
		}
		
		return sortedMap;
	}
	
	/**
	 * Truncates a vector to the top-n elements (assumes that the input vector is already sorted)
	 * @param vector
	 * @param size
	 * @param normalize
	 * @return truncated vector
	 */
	public static LinkedHashMap<Integer,Float> truncateSortedVector(LinkedHashMap<Integer,Float> vector, int size)
	{
		LinkedHashMap<Integer,Float> sortedMap = new LinkedHashMap<Integer,Float>();
		
		int i = 1;
		for(int key : vector.keySet())
		{
			sortedMap.put(key, vector.get(key));
			if(i++ > size) break;
		}
		
		return normalizeVector(sortedMap);
	}
	
	/**
	 * Obtains the list of all offsets for all senses of all parts of speech of an input term 
	 * @param word
	 * @return
	 */
	public static List<String> getWordOffsets(String word)
	{
		List<String> allOffsets = new ArrayList<String>();
		
		for(POS tag : Arrays.asList(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB))
			allOffsets.addAll(getWordOffsets(word, tag));
		
		return allOffsets;
	}
	
	/**
	 * Obtains the list of offsets for all senses of an input term 
	 * @param word
	 * @param tag
	 * @return
	 */
	public static List<String> getWordOffsets(String word, POS tag)
	{
		List<IWord> senses = WordNet.getInstance().getSenses(word,tag);
		List<String> wordOffsets = new ArrayList<String>();
		
		for(IWord sense : senses)
		{
			int offset = sense.getSynset().getOffset();
			String strOffset = fixOffset(offset,tag);
			
			wordOffsets.add(strOffset);
		}
		
		return wordOffsets;
	}
	
	/**
	 * gets the mapping from integer IDs to WordNet 3.0 synset offsets
	 * @return
	 */
	public static HashMap<String,String> getIDtoOffsetMap()
	{
		HashMap<String,String> map = new HashMap<String,String>();
		
		try
		{
			BufferedReader br = Files.getBufferedReader(ADWConfiguration.getInstance().getOffsetMapPath());
			
			while(br.ready())
			{
				String line = br.readLine();
				String comps[] = line.split("\t");
				
				map.put(comps[1],comps[0]);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * extracts the synset offset from a signature's path
	 * @param path
	 * @return
	 */
	public static String getOffsetFromPath(String path)
	{
		String comps[] = path.split("/");
		return comps[comps.length-1].replace(".ppv", "");
	}
	
	/**
	 * gets the mapping from WordNet 3.0 synset offsets to integer IDs
	 * @return
	 */
	public static HashMap<String,String> getOffsettoIDMap()
	{
		HashMap<String,String> map = new HashMap<String,String>();
		
		try
		{
			BufferedReader br = Files.getBufferedReader(ADWConfiguration.getInstance().getOffsetMapPath());
			
			while(br.ready())
			{
				String line = br.readLine();
				String comps[] = line.split("\t");
				
				map.put(comps[0],comps[1]);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return map;
	}

}

