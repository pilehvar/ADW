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
	public static String getTagfromTag(POS tag)
	{
		switch(tag)
		{
			case NOUN:
				return "n";
				
			case VERB:
				return "v";
				
			case ADJECTIVE:
				return "a";
				
			case ADVERB:
				return "r";
		}
		
		return null;
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
			if(tag.toLowerCase().startsWith("j") || tag.toLowerCase().startsWith("adj") || tag.toLowerCase().startsWith("a"))
				return POS.ADJECTIVE;
			
		return null;
	}
	
	
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
	
	
	public static String getLongTagFromShort(String tag)
	{
		if(tag.toLowerCase().startsWith("n"))
			return "NN";
		
		else
			if(tag.toLowerCase().startsWith("v"))
				return "VB";
		
		else
			if(tag.toLowerCase().startsWith("r") || tag.toLowerCase().startsWith("adv"))
				return "RB";
			
		else
			if(tag.toLowerCase().startsWith("j") || tag.toLowerCase().startsWith("adj") || tag.toLowerCase().startsWith("a"))
				return "JJ";
			
		return null;
	}
	
	public static String fixOffset(int off, POS tag)
	{
		String offset = Integer.toString(off);
		
		while(offset.length() < 8)
			offset = "0" + offset;
		
		offset = offset + "-" + getTagfromTag(tag);
		
		return offset;
	}
	
	public static IntegerCounter<String> counterReader(String path)
	{
		IntegerCounter<String> counter = new IntegerCounter<String>();
		
		try
		{
			BufferedReader br = Files.getBufferedReader(path);
			
			while(br.ready())
			{
				String line = br.readLine();
				counter.count(line.split("\t")[0], Integer.parseInt(line.split("\t")[1]));
			}
			
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return counter;
	}
	
	
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
	
	public static List<String> getWordOffsets(String word)
	{
		List<String> allOffsets = new ArrayList<String>();
		
		for(POS tag : Arrays.asList(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB))
			allOffsets.addAll(getWordOffsets(word, tag));
		
		return allOffsets;
	}
	
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
	
	
	public static String getOffsetFromPath(String path)
	{
		String comps[] = path.split("/");
		return comps[comps.length-1].replace(".ppv", "");
	}
	
	public static LinkedHashMap<String,Double> normalize(LinkedHashMap<String,Double> vector)
	{
		LinkedHashMap<String,Double> normalizedVector = new LinkedHashMap<String,Double>();
		
		double sum = 0;
		for(String w : vector.keySet())
			sum += vector.get(w);
				
		for(String w : vector.keySet())
			normalizedVector.put(w, vector.get(w)/sum);
			
		return normalizedVector;
	}

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

