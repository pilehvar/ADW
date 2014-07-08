package it.uniroma1.lcl.adw.semsig;

import it.uniroma1.lcl.adw.ADWConfiguration;
import it.uniroma1.lcl.adw.LexicalItemType;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.adw.utils.WordNetUtils;
import it.uniroma1.lcl.jlt.util.Files;
import it.uniroma1.lcl.jlt.wordnet.WordNetVersion;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;

/**
 * a class to work with {@link SemSig}s
 * @author pilehvar
 *
 */
public class SemSigProcess 
{
	static final int MAX_VECTOR_SIZE = 120000;
	static private SemSigProcess instance;
	
	private static final Log log = LogFactory.getLog(SemSigProcess.class);
	public static HashMap<String,String> IDtoOffsetMap = null;
	
	/**
	 * Used to access {@link SemSigProcess}
	 * 
	 * @return an instance of {@link SemSigProcess}
	 */
	public static SemSigProcess getInstance()
	{
		try
		{
			if (instance == null) instance = new SemSigProcess();
			return instance;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not init SemSigProcess: " + e.getMessage());
		}
	}
	
	private SemSigProcess()
	{
		IDtoOffsetMap = GeneralUtils.getIDtoOffsetMap();
	}
	
	
	public SemSig getCustomSemSigFromCompressed(String path, int size)
	{
		return getCustomSemSigFromCompressed(path, size, true);
	}
	
	public SemSig getCustomSemSigFromCompressed(String path, int size, boolean warnings)
	{
		return getCustomSemSigFromCompressed(path, size, warnings, null);
	}
	
	/**
	 * Assumes that the SemSigs are already sorted and normalized
	 * @param path
	 * @param size
	 * @param warnings
	 * @param normalizationLKB
	 * @return
	 */
	public SemSig getCustomSemSigFromCompressed(String path, int size, boolean warnings, LKB normalizationLKB)
	{
		if(size == 0 || size > MAX_VECTOR_SIZE) 
			size = MAX_VECTOR_SIZE;
		
		SemSig vector = new SemSig();
		String offset = GeneralUtils.getOffsetFromPath(path);
		vector.setOffset(offset);
		
		LinkedHashMap<Integer,Float> map = new LinkedHashMap<Integer,Float>(); 
		
		if(!new File(path).exists())
		{
			if (warnings)
				log.info("[WARNING: "+path+ " does not exist]");
			
			//generate the vector
			//move it to the path
			//compress it
			//if still non existing, return
			//otherwise read it

			return vector;
		}
		
		try
		{
			BufferedReader br = Files.getBufferedReader(path);
			
			float prob;
			float lastProb = 0.0f;
			int lineCounter = 1;
			
			while(br.ready())
			{
				String line = br.readLine();
				if(line.startsWith("!!")) continue;
				
				String[] lineSplit = line.split("\t");
				
				//keeping the IDs
				//String off = IDtoOffsetMap.get(lineSplit[0]);
				int off = Integer.parseInt(lineSplit[0]);
				
				if(lineSplit.length == 1)
				{
					prob = lastProb;
				}
				else
				{
					prob = Float.parseFloat(lineSplit[1]);
					lastProb = prob;
				}
				
				map.put(off, prob);
				
				if(lineCounter++ >= size)
					break;
			}
			
			br.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		if(size != MAX_VECTOR_SIZE)
			map = SemSigUtils.truncateSortedVector(map,size);

		vector.setVector(map);
		
		return vector;
	}
	
	public List<SemSig> getWordSemSigs(String word, POS tag, LKB lkb, int size, boolean warnings)
	{
		return getWordSemSigs(word, tag, lkb, size, warnings, null);
	}
	
	public List<SemSig> getWordSemSigs(String word, POS tag, LKB lkb, int size, boolean warnings, LKB normalizationLKB)
	{
		List<SemSig> vectors = new ArrayList<SemSig>();
		
		List<String> wordOffsets = (tag == null)? GeneralUtils.getWordOffsets(word) : GeneralUtils.getWordOffsets(word, tag);
		
		for(String offset : wordOffsets)
			vectors.add(getSemSigFromOffset(offset, lkb, size, normalizationLKB));
			
		if(vectors.size() == 0 || vectors.size() !=  wordOffsets.size())
		{
			if(warnings)
				log.info("[Warning! no vector or incomplete vectors generated for "+word+":"+tag+"]");
			//System.exit(0);
		}
		
		return vectors;
	}
	
	public List<SemSig> getAllOffsetsFromWordPosList(
			List<String> wordPosList,
			LexicalItemType srcTextType) 
			{
		
		List<SemSig> allSemSigs = new ArrayList<SemSig>();
		
		switch(srcTextType)
		{
			case SENSE_OFFSETS:
				for(String offset : wordPosList)
					allSemSigs.add(getSemSigFromOffset(offset, LKB.WordNetGloss, 0, true));
				
				break;
				
			case WORD_SENSE:
				for(String offset : wordPosList)
					allSemSigs.add(getSemSigFromOffset(offset, LKB.WordNetGloss, 0, true));
				
				break;
				
			case SENSE_KEYS:
				for(String offset : wordPosList)
					allSemSigs.add(getSemSigFromOffset(offset, LKB.WordNetGloss, 0, true));
				
				break;
				
			case SURFACE_TAGGED:
				for(String wordPos : wordPosList)
					allSemSigs.addAll(getWordPosSemSigs(wordPos, LKB.WordNetGloss, 0, true));
				
				break;
				
			case SURFACE:
				for(String wordPos : wordPosList)
					allSemSigs.addAll(getWordPosSemSigs(wordPos, LKB.WordNetGloss, 0, true));
				
				break;
				
		}
		
		
		return allSemSigs;
	}
	
	/**
	 * 
	 * @param wordsPOS in forms of lemma#[n,v,a,r]
	 * @param lkb
	 * @param size
	 * @param warnings
	 * @param normalizationLKB
	 * @return
	 */
	public SemSig getAveragedWordsSemSig(
			List<String> wordsPOS, 
			LKB lkb, 
			int size, 
			boolean warnings, 
			LKB normalizationLKB)
	{
		List<SemSig> vectors = new ArrayList<SemSig>();
		
		for(String wordPOS : wordsPOS)
		{
			String comps[] = wordPOS.split("#");
			String lemma = comps[0];
			POS pos = GeneralUtils.getTagfromTag(comps[1]);
		
			List<SemSig> thisWordVectors = getWordSemSigs(lemma, pos, lkb, size, warnings, normalizationLKB);
			vectors.addAll(thisWordVectors);
		}
		
		return SemSigUtils.averageSemSigs(vectors);
	}
	
	public SemSig getAveragedWordSemSig(String word, POS tag, LKB lkb, int size, boolean warnings, LKB normalizationLKB)
	{
		List<SemSig> vectors = getWordSemSigs(word, tag, lkb, size, warnings, normalizationLKB);

		return SemSigUtils.averageSemSigs(vectors);
	}
	
	public List<SemSig> getWordPosSemSigs(String taggedWord, LKB lkb, int size, boolean warnings)
	{
		String comps[] = taggedWord.split("#");
		String word = comps[0];
		String tag = comps[1];
		
		POS tg = GeneralUtils.getTagfromTag(tag);
		
		return getWordSemSigs(word, tg, lkb, size, warnings);
	}
	
	
	/**
	 * returns non-normalized vector of size top
	 * @param offset	
	 * @param top		vector size (if 0, take all)
	 * @param lkb		determines the vectors path
	 * @return
	 */
	public SemSig getSemSigFromOffset(String offset, LKB lkb, int size, boolean warnings)
	{
		String basePath = ADWConfiguration.getInstance().getPPVPath(lkb);
		//return getCustomVectorFrom(basePath+getSubdirectory(offset)+offset+".ppv",true);
		
		return getCustomSemSigFromCompressed(basePath+getSubdirectory(offset)+offset+".ppv", size, warnings);
	}
	
	public SemSig getSemSigFromOffset(String offset, LKB lkb, int size)
	{
		if(offset == null || offset.equals("null"))
			return null;
		
		return getSemSigFromOffset(offset, lkb, size, null);
	}
	
	public SemSig getSemSigFromOffset(String offset, LKB lkb, int size, LKB normalizationLKB)
	{
		String basePath = ADWConfiguration.getInstance().getPPVPath(lkb);
		String path = basePath+getSubdirectory(offset)+offset+".ppv";
		
		return getCustomSemSigFromCompressed(path, size, true, normalizationLKB);
	}
	
	public static String getSubdirectory(String offset)
	{
//		return offset.substring(0,2)+"/"+offset.substring(2,4)+"/"+offset.substring(4,6)+"/"+offset.substring(6,8)+"/";
//		return offset.substring(0,2)+"/"+offset.substring(2,4)+"/"+offset.substring(4,6)+"/";
		return offset.substring(0,2)+"/"+offset.substring(2,4)+"/";
	}
	
	public SemSig getSemSigFromIWord(IWord sense, LKB lkb, int size)
	{
		String offset = GeneralUtils.fixOffset(sense.getSynset().getOffset(),sense.getPOS());
		return getSemSigFromOffset(offset, lkb, size);
	}
	
	public SemSig getSemSigFromWordSense(String wordSense, LKB lkb, int size)
	{
		IWord sense = WordNetUtils.mapWordSenseToIWord(WordNetVersion.WN_30, wordSense);
		String offset = GeneralUtils.fixOffset(sense.getSynset().getOffset(),sense.getPOS());
		return getSemSigFromOffset(offset, lkb, size);
	}
	
	public static void main(String args[])
	{
		//List<Vector> vs = getWordVectors("mouse", POS.NOUN, LKB.WordNet);
		
		//System.out.println(getNighboursHashMap(LKB.WordNet, "02958343-n", 15).size());
		
		/*
		SemSig w1 = Vectoring.getAveragedWordVector("browser", POS.NOUN, LKB.WordNetGloss, 0, true, null);
		SemSig w2 = Vectoring.getVectorFromOffset("06571301-n", LKB.WordNetGloss, 0);
		
		System.out.println(w1.getVector().get("03580615-n"));
		System.out.println(w2.getVector().get("03580615-n"));
		
		System.out.println(VectorComparator.compare(w1, w2, SimilarityMeasure.COSINE, 0));
		
		System.exit(0);
		*/
		
		//HashMap<String,Double> map = getGraphFuzzyHashMap(LKB.WordNet, "02958343-n");
		
		//Vector v1 = getGraphFuzzyVector(LKB.WordNet, "07777945-n", 4);
		//Vector v2 = getGraphFuzzyVector(LKB.WordNet, "07777512-n", 4);
		
		//SemSig v1 = getVectorFromOffset("07777945-n", LKB.WordNet, 1000);
		//SemSig v2 = getVectorFromOffset("07777512-n", LKB.WordNet, 1000);
		
//		SemSig v1 = Vectoring.getInstance().getCustomVectorFromCompressed("/media/backup/fromSDA/ppvs.30g.full.classified/08/08/97/97/0808997-n.ppv", 10);
//		SemSig v2 = Vectoring.getInstance().getCustomVectorFromCompressed("/media/backup/fromSDA/ppvs.30g.full.classified/08/08/97/97/08089797-n.ppv", 50, false);
		
//		List<SemSig> v1s = SemSigProcess.getInstance().getWordPosSemSigs("tornado#n", LKB.WordNetGloss, 0, true);
//		List<SemSig> v2s = SemSigProcess.getInstance().getWordPosSemSigs("twister#n", LKB.WordNetGloss, 0, true);
		
//		SemSig v1 = SemSigUtils.averageSemSigs(v1s);
//		SemSig v2 = SemSigUtils.averageSemSigs(v2s);
		
		SemSig v1 = SemSigProcess.getInstance().getSemSigFromWordSense("gem.n.3", LKB.WordNetGloss, 0);
		SemSig v2 = SemSigProcess.getInstance().getSemSigFromWordSense("jewel.n.2", LKB.WordNetGloss, 0);

		
		double sim = SemSigComparator.compare(v1, v2, new WeightedOverlap(), 0);
		System.out.println(sim);
		
		/*
		double sum1 = 0;
		double sum2 = 0;

		System.out.println(v1.getVector().size()+"\t"+v2.getVector().size());
		
		for(String s : v1.getVector().keySet())
		{
			//System.out.println(s+"\t"+v1.getVector().get(s)+"\t"+v2.getVector().get(s));
			
			sum1 += v1.getVector().get(s);
			//sum2 += v2.getVector().get(s);
		}
		
		System.out.println(sum1+"\t"+sum2);
		
		System.out.println(sim);
		*/
		
		//log.info(vs.size());
		//log.info(vs.get(0).getSortedVector());
	}
}
