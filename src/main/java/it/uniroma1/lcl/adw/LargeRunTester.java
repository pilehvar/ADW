package it.uniroma1.lcl.adw;

import it.uniroma1.lcl.adw.comparison.Cosine;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import it.uniroma1.lcl.jlt.util.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

public class LargeRunTester 
{
	
	public static void main(String args[])
	{
		List<String> list = readFile();
		
		ADW pipeline = ADW.getInstance();
		
		try
		{
			BufferedWriter bw = Files.getBufferedWriter("output/test.adw.v6.scores");

			for(int i=0; i<list.size()-5; i+=2)
			{
				
				String text1 = list.get(i);
				String text2 = list.get(i+1);
				
				if(text1.trim().length() == 0 || text2.trim().length() == 0)
					continue;
				
				long init = System.currentTimeMillis();
		        double score = pipeline.getPairSimilarity(
		                text1, text2,
		                DisambiguationMethod.ALIGNMENT_BASED, 
		                new Cosine(),
		                LexicalItemType.SURFACE, LexicalItemType.SURFACE);
		        
		        System.out.println(score+"\t"+text1+"\t"+text2);
		        System.out.println((System.currentTimeMillis()-init)+"\t"+"Sentence comparison time!\n");
		        
		    
		        bw.write(score+"\t"+text1+"\t"+text2+"\n");
		        
			}
			
			bw.close();
			
		}
		catch(Exception e)
		{
		}
		
		
		
	}
	
	public static List<String> readFile()
	{
		List<String> file =  new ArrayList<String>();
		
		try
		{
			BufferedReader br = Files.getBufferedReader("/home/pilehvar/Works/ADW/europarl-v7.bg-en.en");
			
			while(br.ready())
			{
				String line = br.readLine();
		
				file.add(line);
			}
		}
		catch(Exception e)
		{
		}
		
		return file;
	}

}
