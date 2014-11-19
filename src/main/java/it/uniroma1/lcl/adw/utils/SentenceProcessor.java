package it.uniroma1.lcl.adw.utils;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.WordLemmaTag;

public class SentenceProcessor
{
	private static SentenceProcessor instance;

	private SentenceProcessor() { }

	public static synchronized SentenceProcessor getInstance()
	{
		if (instance == null) instance = new SentenceProcessor();
		return instance;
	}

	public List<WordLemmaTag> processSentence(String sentence, boolean isTokenized)
	{
		final StanfordLemmatizer lemmatizer = StanfordLemmatizer.getInstance();
		final StanfordPOSTagger tagger = StanfordPOSTagger.getInstance();
    	final List<WordLemmaTag> tlSentence = new ArrayList<WordLemmaTag>();
		
    	// the tagged sentence
    	List<TaggedWord> tSentence = null;
    	if (isTokenized) tSentence = tagger.tag(sentence);
    	else
    	{
    		StanfordTokenizer tokenizer = StanfordTokenizer.getInstance();
    		List<Word> tokens = tokenizer.tokenize(sentence);
    		tSentence = tagger.tag(tokens);
    	}
    	
    	// add to the lemmatized sentence
    	for (TaggedWord tw : tSentence) 
    		tlSentence.add(lemmatizer.lemmatize(tw));

    	return tlSentence;
	}
	
}








