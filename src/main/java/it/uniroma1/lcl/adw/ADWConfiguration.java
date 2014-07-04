package it.uniroma1.lcl.adw;

import it.uniroma1.lcl.adw.semsig.LKB;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ADWConfiguration
{
	private org.apache.commons.configuration.PropertiesConfiguration config = null;
	private static final Log log = LogFactory.getLog(ADWConfiguration.class);

	static private ADWConfiguration instance = null;
	static private String CONFIG_DIR = "config/";
	static public String CONFIG_FILE = "adw.properties";

	/**
	 * Private constructor. By default loads config/knowledge.properties
	 * 
	 * @throws ConfigurationException
	 */
	private ADWConfiguration()
	{
		File configFile = new File(CONFIG_DIR, CONFIG_FILE);
		
		boolean bDone = false;
		if (configFile.exists())
		{
			log.info("Loading " + CONFIG_FILE + " FROM " + configFile.getAbsolutePath());
			try
			{
				config = new PropertiesConfiguration(configFile);
				bDone = true;
			}
			catch (ConfigurationException ce)
			{
				ce.printStackTrace();
			}
		}
		
		if (!bDone)
		{
			log.info("ADW starts with empty configuration");
			config = new PropertiesConfiguration();
		}
	}

	public static synchronized ADWConfiguration getInstance()
	{
		if (instance == null)
		{
			instance = new ADWConfiguration();
		}
		return instance;
	}
	
	public void setConfigurationFile(File configurationFile)
	{
		log.info("Changing configuration properties to " + configurationFile);
		try
		{
			config = new PropertiesConfiguration(configurationFile);
			config.setBasePath(
				configurationFile.getParentFile().getAbsolutePath());
		}
		catch (ConfigurationException ce)
		{
			ce.printStackTrace();
			log.info("Setting BabelNet to an empty configuration");
			config = new PropertiesConfiguration();
		}
	}
	
	public int getTotalFolds()
	{
		return config.getInt("total.folds");
	}
	
	public int getFoldNumber()
	{
		return config.getInt("fold.number");
	}
	
	public String getPPVPath()
	{
		return getPPVPath(LKB.WordNet);
	}
	
	public String getPairwiseSimilarityPath()
	{
		return config.getString("pairwise.path");
	}
	
	public String getWordSimilarity353Path()
	{
		return config.getString("wordsim.path");
	}

	public String getStaticVectorPath()
	{
		return config.getString("static.path");
	}
	
	public String getPPVPath(LKB lkb)
	{
		switch (lkb)
		{
			case WordNet:
				return config.getString("wordnet.ppv.path");
				
			case WordNetGloss:
				return config.getString("wn30g.ppv.path");
				
		}
		
		return null;
	}
	
	
	public String getStaticPPVPath(LKB lkb)
	{
		switch (lkb)
		{
			case WordNet:
				return config.getString("wordnet.static.ppv.path");
				
			case WordNetGloss:
				return config.getString("wn30g.static.ppv.path");
				
		}
		
		return null;
	}
	
	
	public String getWordPPVPath(LKB lkb)
	{
		switch (lkb)
		{
			case WordNet:
				return config.getString("wordnet.single.ppv.path");
				
			case WordNetGloss:
				return config.getString("wn30g.single.ppv.path");
				
				
		}
		
		return null;
	}
	
	public String getLKBPath(LKB lkb)
	{
		switch (lkb)
		{
			case WordNet:
				return config.getString("wordnet.lkb.path");
				
			case WordNetGloss:
				return config.getString("wn30g.lkb.path");
				
		}
		
		return null;
	}
	
	
	public String getWordNetDictPath()
	{
		return config.getString("wordnet.dict.path");
	}
	
	public String getOffsetMapPath()
	{
		return config.getString("offset.map.file");
	}
	
	public String getBINPath(LKB lkb)
	{
		switch (lkb)
		{
			case WordNet:
				return config.getString("wordnet.bin.path");
				
			case WordNetGloss:
				return config.getString("wn30g.bin.path");
				
		}
		
		return null;
	}
	
	public String getIndexPath(LKB lkb)
	{
		switch (lkb)
		{
			case WordNet:
				return config.getString("wordnet.index");
				
			case WordNetGloss:
				return config.getString("wn30g.index");
				
		}
		
		return null;
	}

	public String getSTSTrainPath()
	{
		return config.getString("STS.train.path");
	}
	
	public String getFeaturesBasePath()
	{
		return config.getString("features.base.path");
	}
	
	public String getSTSTestPath()
	{
		return config.getString("STS.test.path");
	}
	
	public String getSTSMixPath()
	{
		return config.getString("STS.mix.path");
	}
	
	public String getMixedFeaturesPath()
	{
		return getFeaturesBasePath() + "mix/";
	}

	
	public String getWorkingDirectory() 
	{
		return config.getString("workingDirectory");
	}

	public int getGeneratedVectorSize() 
	{
		return config.getInt("generatedVectorSize");
	}

	public int getTestVectorSize() 
	{
		return config.getInt("testedVectorSize");
	}
	
	public int getAlignmentVectorSize() 
	{
		return config.getInt("alignmentVecSize");
	}

	public SimilarityMeasure getAlignmentSimilarityMeasure() 
	{
		return getEnumFromString(SimilarityMeasure.class,config.getString("alignmentSimMeasure"));
	}

	public SimilarityMeasure getComparisonSimilarityMeasure() 
	{
		return getEnumFromString(SimilarityMeasure.class,config.getString("comparisonMeasure"));
	}
	
	
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string)
	{
	    if( c != null && string != null )
	    {
	        try
	        {
	            return Enum.valueOf(c, string.trim().toUpperCase());
	        }
	        catch(IllegalArgumentException ex)
	        {
	        }
	    }
	    return null;
	}
}
