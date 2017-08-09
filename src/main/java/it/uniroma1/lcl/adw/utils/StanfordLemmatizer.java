package it.uniroma1.lcl.adw.utils;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.process.Morphology;

public class StanfordLemmatizer
{
	private static StanfordLemmatizer singleton;

	private final Morphology analyzer;
	
	private StanfordLemmatizer()
	{
		this.analyzer = new Morphology();
	}

	public static synchronized StanfordLemmatizer getInstance()
	{
		if (singleton == null) singleton = new StanfordLemmatizer();
		return singleton;
	}
	
	public WordLemmaTag lemmatize(String word, String tag)
	{ 
	    final String lemma = Morphology.stemStatic(word, tag).word();
	    return new WordLemmaTag(word, lemma, tag);
	}

	public WordLemmaTag lemmatize(TaggedWord tw)
	{
		return lemmatize(tw.word(), tw.tag());
	}

	public List<WordTag> lemmatizeText(String tokenizedText)
	{
		final List<WordTag> lemmatized = new ArrayList<WordTag>();
		final String[] tokens = tokenizedText.split("\\s+");
		
		for (String token : tokens)
		{
			final String lemma = analyzer.stem(token);
			lemmatized.add(new WordTag(token, lemma));
		}
		
		return lemmatized;
	}

	public String[] lemmatizeText(String[] words)
	{ 
		final String[] stems = new String[words.length];
		for (int word = 0; word < words.length; word++)
		{
			stems[word] = analyzer.stem(words[word]); 
		}
	    return stems;
	}
	
	public String lemmatizeWord(String word)
	{ 
		return analyzer.stem(word); 
	}
	
}

