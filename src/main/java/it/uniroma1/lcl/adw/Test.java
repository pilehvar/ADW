package it.uniroma1.lcl.adw;

import java.util.HashMap;
import java.util.Set;

import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.POS;
import it.uniroma1.lcl.adw.semsig.LKB;
import it.uniroma1.lcl.adw.semsig.SemSig;
import it.uniroma1.lcl.adw.semsig.SemSigProcess;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.jlt.util.IntegerCounter;
import it.uniroma1.lcl.jlt.wordnet.WordNet;

public class Test 
{
	public static void main(String args[])
	{
		HashMap<String, String> map = GeneralUtils.getIDtoOffsetMap();
		Set<ISynset> allSynsets = WordNet.getInstance().getAllSynsets();
		int total = 0;
		
		IntegerCounter<String> counter = new IntegerCounter<String>(); 
		
		for(ISynset synset : allSynsets)
		{
			if(synset.getPOS() != POS.ADJECTIVE) continue;
			
			String offset = GeneralUtils.fixOffset(synset.getOffset(), synset.getPOS());
			
			SemSig sig = SemSigProcess.getInstance().getSemSigFromOffset(offset, LKB.WordNetGloss, 100);
			
			for(int off : sig.getVector().keySet())
			{
				String offs = map.get(Integer.toString(off));
				String words = WordNet.getInstance().getSynsetFromOffset(offs).getWords().toString();
				counter.count(words);	
			}
			
			total++;
			
			if(total % 5000 == 0)
				counter.saveToFile("output/high-ranking.stats.a.tsv");
		}
		
		counter.saveToFile("output/high-ranking.stats.a.tsv");
		System.out.println("Total: "+ total);
	}

}
