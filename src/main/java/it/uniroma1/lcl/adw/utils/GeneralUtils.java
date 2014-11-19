package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.adw.ADWConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
	
	/**
	 * converts string to {@link POS}
	 * @param tag
	 * @return
	 */
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
		List<IWord> senses = WordNetUtils.getInstance().getSenses(word,tag);
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
			BufferedReader br = new BufferedReader(new FileReader(ADWConfiguration.getInstance().getOffsetMapPath()));
			
			while(br.ready())
			{
				String line = br.readLine();
				String comps[] = line.split("\t");
				
				map.put(comps[1],comps[0]);
			}
			
			br.close();
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
			BufferedReader br = new BufferedReader(new FileReader(ADWConfiguration.getInstance().getOffsetMapPath())); 
			
			while(br.ready())
			{
				String line = br.readLine();
				String comps[] = line.split("\t");
				
				map.put(comps[0],comps[1]);
			}
			
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return map;
	}

}

