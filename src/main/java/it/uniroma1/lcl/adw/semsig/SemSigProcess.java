package it.uniroma1.lcl.adw.semsig;

import it.uniroma1.lcl.adw.ADWConfiguration;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.utils.GeneralUtils;
import it.uniroma1.lcl.adw.utils.SemSigUtils;
import it.uniroma1.lcl.adw.utils.WordNetUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;

import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;


/**
 * a class to work with {@link SemSig}s
 * @author pilehvar
 *
 */
public class SemSigProcess 
{
	static private final int MAX_VECTOR_SIZE = 120000;
	static private SemSigProcess instance;
	
	private static final Log log = LogFactory.getLog(SemSigProcess.class);
//	public static HashMap<String,String> IDtoOffsetMap = null;
	private HashMap<LKB,String> basePathMap = new HashMap<LKB,String>();
	private HashMap<LKB,String> wordBasePathMap = null;
	
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
//		IDtoOffsetMap = GeneralUtils.getIDtoOffsetMap();
		
		if(wordBasePathMap == null)
		{
			wordBasePathMap = new HashMap<LKB,String>();

			String path = ADWConfiguration.getInstance().getWordPPVPath(LKB.WordNetGloss);
			if(new File(path).exists())
				wordBasePathMap.put(LKB.WordNetGloss, path);
		}
	}
	
	public boolean wordVectorsExist(LKB lkb)
	{
		if(wordBasePathMap.containsKey(lkb))
			return true;
		else
			return false;
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
		
		TIntFloatMap map = new TIntFloatHashMap(size); 
		
		if(!new File(path).exists())
		{
			if (warnings)
				log.info("[WARNING: "+path+ " does not exist]");
			
			return vector;
		}
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(path));

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
                        map = SemSigUtils.truncateVector(map, true, size, true);

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
	
	public List<SemSig> getAllSemSigsFromWordPosList(
			List<String> wordPosList,
			ItemType srcTextType,
			int alignmentVecSize) 
			{
		
		List<SemSig> allSemSigs = new ArrayList<SemSig>();
		
		switch(srcTextType)
		{
			case SENSE_OFFSETS:
				for(String offset : wordPosList)
					allSemSigs.add(getSemSigFromOffset(offset, LKB.WordNetGloss, alignmentVecSize, true));
				
				break;
				
			case WORD_SENSE:
				for(String wordSense : wordPosList)
					allSemSigs.add(getSemSigFromOffset(wordSense, LKB.WordNetGloss, alignmentVecSize, true));
				
				break;
				
			case SENSE_KEYS:
				for(String senseKeys : wordPosList)
					allSemSigs.add(getSemSigFromOffset(senseKeys, LKB.WordNetGloss, alignmentVecSize, true));
				
				break;
				
			//To avoid bias towards more frequent senses, signatures are macro-averaged  
			case SURFACE_TAGGED:
				for(String surfaceTagged : wordPosList)
				{
					List<SemSig> vectors = getWordPosSemSigs(surfaceTagged, LKB.WordNetGloss, alignmentVecSize, true);
					allSemSigs.add(SemSigUtils.averageSemSigs(vectors));
				}
				
				break;
				
			case SURFACE:
				//even in this case the sentence has already been POS tagged
				for(String wordPos : wordPosList)
				{
					List<SemSig> vectors = getWordPosSemSigs(wordPos, LKB.WordNetGloss, alignmentVecSize, true); 
					allSemSigs.add(SemSigUtils.averageSemSigs(vectors));
				}
				
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
		
		//if there exists vectors for words
		if(SemSigProcess.getInstance().wordVectorsExist(lkb))
		{
			List<SemSig> vector = new ArrayList<SemSig>();
			vector.add(SemSigProcess.getInstance().getSemSigFromWord(word, tag, lkb, size));
			
			return vector;
		}
		
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
		
		return getCustomSemSigFromCompressed(basePath+getSubdirectory(offset)+offset+".ppv", size, warnings);
	}
	
	public SemSig getSemSigFromOffset(String offset, LKB lkb, int size)
	{
		SemSig sig = new SemSig();
		
		if(offset == null || offset.equals("null"))
			return sig;
		
		return getSemSigFromOffset(offset, lkb, size, null);
	}
	
	public SemSig getSemSigFromOffset(String offset, LKB lkb, int size, LKB normalizationLKB)
	{
		if(!basePathMap.containsKey(lkb))
			basePathMap.put(lkb, ADWConfiguration.getInstance().getPPVPath(lkb));
		
		String basePath = basePathMap.get(lkb);
		String path = basePath+getSubdirectory(offset)+offset+".ppv";
		
		return getCustomSemSigFromCompressed(path, size, true, normalizationLKB);
	}
	
	public SemSig getSemSigFromWord(String word, POS tag, LKB lkb, int size)
	{
		return getSemSigFromWord(word, tag, lkb, size, null);
	}
	
	public SemSig getSemSigFromWord(String word, POS tag, LKB lkb, int size, LKB normalizationLKB)
	{
		String tg = Character.toString(GeneralUtils.getTagfromTag(tag));
		
		return getSemSigFromWord(word, tg, lkb, size, normalizationLKB);
	}
	
	public SemSig getSemSigFromWord(String word, String tag, LKB lkb, int size)
	{
		return getSemSigFromWord(word, tag, lkb, size, null);
	}
	
	public SemSig getSemSigFromWord(String word, String tag, LKB lkb, int size, LKB normalizationLKB)
	{
		if(!wordBasePathMap.containsKey(lkb))
			wordBasePathMap.put(lkb, ADWConfiguration.getInstance().getWordPPVPath(lkb));
		
		String basePath = wordBasePathMap.get(lkb);
		String path = basePath+word.substring(0,1)+"/"+word+"."+tag+".ppv";
		
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
		IWord sense = WordNetUtils.getInstance().mapWordSenseToIWord(wordSense);
		String offset = GeneralUtils.fixOffset(sense.getSynset().getOffset(),sense.getPOS());
		return getSemSigFromOffset(offset, lkb, size);
	}
	
	public static void main(String args[])
	{
//		SemSig v1 = SemSigProcess.getInstance().getSemSigFromWordSense("gem.n.3", LKB.WordNetGloss, 0);
//		SemSig v2 = SemSigProcess.getInstance().getSemSigFromWordSense("jewel.n.2", LKB.WordNetGloss, 0);

		
//		double sim = SemSigComparator.compare(v1, v2, new WeightedOverlap(), 0, true, true);
//		System.out.println(sim);
		
		System.out.println(SemSigProcess.getInstance().getSemSigFromWord("monkey", POS.NOUN, LKB.WordNetGloss, 0, null));
	}
	
	/**
	 * gets a word#pos and returns all the offsets associated with that word and part of speech
	 * @param wordPos
	 * @return
	 */
	public Set<String> getWordNetOffsets(String wordPos)
	{
		Set<String> offsets = new HashSet<String>();

		wordPos = wordPos.trim();
		
		if(wordPos.matches("[0-9]*-[nrva]"))
		{
			offsets.add(wordPos);
		}
		else
		{
			String comps[] = wordPos.split("#");
			
			if(comps.length != 2)
			{
				log.error("mal-formatted word-pos: "+wordPos);
				return null;
			}
			
			String word = comps[0];
			POS pos = GeneralUtils.getTagfromTag(comps[1]);
			
			List<IWord> senses = WordNetUtils.getInstance().getSenses(word, pos);
			
			if(senses == null)
				return null;
			
			for(IWord sense : senses)
			{
				offsets.add(GeneralUtils.fixOffset(sense.getSynset().getOffset(), pos));
			}
		}
		
		return offsets;
	}
}
