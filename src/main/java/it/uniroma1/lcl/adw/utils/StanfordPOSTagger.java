package it.uniroma1.lcl.adw.utils;

import it.uniroma1.lcl.adw.ADWConfiguration;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class StanfordPOSTagger
{
	private final MaxentTagger tagger;
	
	private static StanfordPOSTagger instance;

	private StanfordPOSTagger()
	{
		try
		{
			this.tagger =
				new MaxentTagger(ADWConfiguration.getInstance().getStanfordPOSModel());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Cannot init: " + e);
		}
	}

	public static synchronized StanfordPOSTagger getInstance()
	{
		if (instance == null) instance = new StanfordPOSTagger();
		return instance;
	}
	
	public List<TaggedWord> tag(String sentence)
	{
		List<HasWord> tokens = SentenceUtils.toWordList(sentence.split("\\s+"));
		return tag(tokens);
	}
	
	public List<TaggedWord> tag(List<? extends HasWord> sentence)
	{
		if(sentence == null || sentence.size() == 0)
			return new ArrayList<TaggedWord>();
		
		return tagger.tagSentence(sentence);
	}
		
}

