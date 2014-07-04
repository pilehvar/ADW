package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.jlt.util.Pair;
import it.uniroma1.lcl.jlt.wordnet.WordNet;
import it.uniroma1.lcl.jlt.wordnet.WordNetVersion;
import it.uniroma1.lcl.jlt.wordnet.data.WordNetExtendedMappings;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;

public class WordNetUtils
{
	static final Pattern readableFormat = Pattern.compile("([^ ]*)\\.([anvr])\\.(\\d+)");
	
	public static String mapIWordToReadableForm(WordNetVersion wnv, IWord word)
	{
		return mapOffsetToReadableForm(wnv, fixOffset(word.getSynset().getOffset(),word.getPOS()), word.getLemma(), word.getPOS());
	}
	
	public static String mapOffsetsToReadableForm(WordNetVersion wnv, List<String> offsets, String word, POS tag)
	{
		StringBuffer sb = new StringBuffer();
		
		for(String offset : offsets)
		{
			String rForm = mapOffsetToReadableForm(wnv, offset, word, tag);
			sb.append(rForm);
			sb.append("+");
		}
		
		String resOffset = sb.toString();
		resOffset = resOffset.substring(0,resOffset.length()-1);
		
		return resOffset;
	}
	
	public static String mapOffsetToReadableForm(WordNetVersion wnv, String offset, String word, POS tag)
	{
		List<IWord> senses = WordNet.getInstance(wnv).getSenses(word, tag);
		ISynset syn = WordNet.getInstance(wnv).getSynsetFromOffset(offset);
			
		int senseRank = 1;
		for(IWord sense : senses)
		{
			if(sense.getSynset() == syn)
				break;
			
			senseRank++;
		}
		
		if(senseRank > senses.size())
		{
			System.out.println("[ERROR: could not generate the readable form for "+word+" "+offset);
			return "null";
		}

		return word+"."+senseRank;
	}
	
	public static IWord mapWordSenseToIWord(WordNetVersion wnv, String wordSense)
	{
		Matcher m = readableFormat.matcher(wordSense);
		
		if(m.find())
		{
			String word = m.group(1);
			POS tag = GeneralUtils.getTagfromTag(m.group(2));
			int sense = Integer.parseInt(m.group(3));
			
			List<IWord> senses = WordNet.getInstance(wnv).getSenses(word, tag);
			
			if(senses.size() < sense)
				return null;
			
			return senses.get(sense-1);			
			
		}
		else
		{
			System.out.println("[ERROR: non matching regular expression at "+ wordSense+"]");
			System.exit(0);
			return null;
		}			
	}
	
	/**
	 * Could not find any good reason why I have to use this method
	 * @param word
	 * @param wnv1
	 * @param wnv2
	 * @return
	 */
	public static IWord mapIWordAcrossVersions(IWord word, WordNetVersion wnv1, WordNetVersion wnv2)
	{
		IWord mapped = null;
		
		WordNetExtendedMappings wnm = WordNetExtendedMappings.getInstance();
		
		POS pos = word.getPOS();
		String offset = fixOffset(word.getSynset().getOffset(), pos);
		String lemma = word.getLemma();
		
		String mappedOffset = wnm.getTargetOffset(offset, wnv1, wnv2);
		mappedOffset = mappedOffset.substring(0,8)+"-"+mappedOffset.substring(8,9);
		
		for(IWord sense : WordNet.getInstance(wnv2).getSenses(lemma, pos))
		{
			String tOffset = fixOffset(sense.getSynset().getOffset(), pos);
			if(tOffset.equals(mappedOffset))
			{
				mapped = sense;
				break;
			}
		}
		
		return mapped;
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
	
	public static void main(String args[])
	{
		//System.out.println(mapOffsetsToReadableForm(WordNetVersion.WN_30, Arrays.asList("05563770-n","02737660-n"), "arm", POS.NOUN));
		//System.out.println(mapOffsetToReadableForm(WordNetVersion.WN_30, "09044862-n", "United states of America", POS.NOUN));
		
		/*
		for (int i = 0; i < 2; i++) {
			IWord iword = WordNet.getInstance(WordNetVersion.WN_171).getSenses("mouse", POS.NOUN).get(i); 
			System.out.println(mapIWordToReadableForm(WordNetVersion.WN_171, iword));
				
		}*/
		
		//System.out.println(mapReadableForm(WordNetVersion.WN_171, WordNetVersion.WN_30,"bar.12",POS.NOUN));
		
		//IWord word = mapReadableToIWord(WordNetVersion.WN_171,"u.s..2", POS.NOUN);
		//System.out.println(mapIWordToReadableForm(WordNetVersion.WN_171, word));
		
		//System.out.println(mapReadableForm(WordNetVersion.WN_171,WordNetVersion.WN_30,"arm.4",POS.NOUN));
		
//		System.out.println(WordNetUtils.mapReadableToIWord(WordNetVersion.WN_30,"mouse.1",POS.NOUN));
//		System.out.println(WordNet.getInstance(WordNetVersion.WN_171).getSenses("levee", POS.NOUN));
		
		System.out.println(WordNetUtils.mapWordSenseToIWord(WordNetVersion.WN_30, "mouse.n.4"));
		
//		System.out.println(mapReadableForm(WordNetVersion.WN_171,WordNetVersion.WN_30,"levee.1",POS.NOUN));
		
	}
}
