package it.uniroma1.lcl.adw.utils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordTokenFactory;


public class StanfordTokenizer
{
	//these should all appear as tokens themselves
	private static final String[] OLDSTRINGS =
	{
		"\\(", "\\)", "\\[", "\\]", "\\{", "\\}", "\\*", "/","_", "-"
	};
	private static final String[] NEWSTRING =
	{
		"-LRB-", "-RRB-", "-LCB-", "-RCB-", "-LCB-", "-RCB-", "\\*", "\\/",
		"-", " "
	};

	// these are chars that might appear inside tokens
	private static final char[] DEFAULTOLDCHARS = {'*', '/'};
	
	// the "backoff" tokenizer
	private final PennTokenizer pennTokenizer;
	
	// the "default" tokenizer
	private PTBTokenizer<Word> tokenizer;
	
	private static StanfordTokenizer singleton;

	private StanfordTokenizer()
	{ 
		this.pennTokenizer = new PennTokenizer();
	}

	public static synchronized StanfordTokenizer getInstance()
	{
		if (singleton == null) singleton = new StanfordTokenizer();
		return singleton;
	}
	
	public List<String> tokenizeString(String string)
	{ 
		final List<String> tokens = new ArrayList<String>();
		for (Word w : tokenize(string))
		{
			tokens.add(w.word());
		}
		return tokens;
	}
	
	public List<Word> tokenize(String string)
	{ 
		this.tokenizer = 
			new PTBTokenizer<Word>(
					new StringReader(string), 
					new WordTokenFactory(), 
					"untokenizable=noneDelete,ptb3Escaping=true");
		try
		{
			return tokenizer.tokenize();
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			
			final List<Word> tokens = new ArrayList<Word>();
			for (String token : pennTokenizer.tokenize(string).split("\\s+"))
			{ 
				tokens.add(new Word(token));
			}
			return tokens;
		}
	}
	
	public String unTokenize(String tokenized) {
		return PTBTokenizer.ptb2Text(tokenized);
	}
	
	private static class PennTokenizer
	{
		
		private Map<String,String> stringSubs;
		
		private char[] oldChars;
		  
		private PennTokenizer()
		{ this(makeStringMap(), DEFAULTOLDCHARS); }

		private PennTokenizer(Map<String,String> stringSubs, char[] oldChars)
		{
			this.stringSubs = stringSubs;
			this.oldChars = oldChars;
		}

		private static Map<String,String> makeStringMap()
		{
			Map<String, String> map = new HashMap<String,String>();
			for (int i = 0; i < OLDSTRINGS.length; i++)
			{ map.put(OLDSTRINGS[i], NEWSTRING[i]); }
			return map;
		}
		
		/**
		 * Tokenizes according to the Penn Treebank conventions.
		 */
		public String tokenize(String str)
		{
			str = str.replaceAll("``", "`` ");
			str = str.replaceAll("''", "  ''");
			str = str.replaceAll("([?!\".,;:@#$%&])", " $1 ");
			str = str.replaceAll("\\.\\.\\.", " ... ");
			str = str.replaceAll("\\s+", " ");
			
			str = str.replaceAll(",([^0-9])", " , $1");

			str = str.replaceAll("([^.])([.])([\\])}>\"']*)\\s*$", "$1 $2$3 ");
			
			str = str.replaceAll("([\\[\\](){}<>])", " $1 ");
			str = str.replaceAll("--", " -- ");

			str = str.replaceAll("$", " ");
			str = str.replaceAll("^", " ");

	        //str = str.replaceAll("\"", " '' ");		
			str = str.replaceAll("([^'])' ", "$1 ' ");
			str = str.replaceAll("'([sSmMdD]) ", " '$1 ");
			str = str.replaceAll("'ll ", " 'll ");
			str = str.replaceAll("'re ", " 're ");
			str = str.replaceAll("'ve ", " 've ");
			str = str.replaceAll("n't ", " n't ");
			str = str.replaceAll("'LL ", " 'LL ");
			str = str.replaceAll("'RE ", " 'RE ");
			str = str.replaceAll("'VE ", " 'VE ");
			str = str.replaceAll("N'T ", " N'T ");

			str = str.replaceAll(" ([Cc])annot ", " $1an not ");
			str = str.replaceAll(" ([Dd])'ye ", " $1' ye ");
			str = str.replaceAll(" ([Gg])imme ", " $1im me ");
			str = str.replaceAll(" ([Gg])onna ", " $1on na ");
			str = str.replaceAll(" ([Gg])otta ", " $1ot ta ");
			str = str.replaceAll(" ([Ll])emme ", " $1em me ");
			str = str.replaceAll(" ([Mm])ore'n ", " $1ore 'n ");
			str = str.replaceAll(" '([Tt])is ", " $1 is ");
			str = str.replaceAll(" '([Tt])was ", " $1 was ");
			str = str.replaceAll(" ([Ww])anna ", " $1an na ");

			// "Nicole I. Kidman" gets tokenized as "Nicole I . Kidman"
			str = str.replaceAll(" ([A-Z])\\ +\\.", " $1. ");
			
			str = str.replaceAll("\\s+", " ");
			str = str.replaceAll("^\\s+", "");
			str = str.trim();
			return process(str);
		}
		
		private String process(String s)
		{
			for (String string : stringSubs.keySet())
			{ s = s.replaceAll(string, stringSubs.get(string)); }
			return escapeString(s);
		}
		
		private String escapeString(final String s)
		{
			final StringBuffer buff = new StringBuffer();
			for (int i = 0; i < s.length(); i++)
			{
				char curChar = s.charAt(i);
			    if (curChar == '\\')
			    {
				    // add this and the next one
				    buff.append(curChar);
				    i++;
				    if (i < s.length())
				    {
				    	curChar = s.charAt(i);
				    	buff.append(curChar);
				    }
			    } 
			    else
			    {
			    	// run through all the chars we need to escape
			    	for (int j = 0; j < oldChars.length; j++)
			    	{
			          if (curChar == oldChars[j])
			          { buff.append('\\'); break; }
			        }
			        // append the old char no matter what
			        buff.append(curChar);
			    }
			}
			return buff.toString();
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println(StanfordTokenizer.getInstance().tokenize("\"Weird Al\" F.C. Yankovic"));
	}
}

