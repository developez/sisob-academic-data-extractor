/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
    Copyright (c) 2014 "(IA)2 Research Group. Universidad de Málaga"
                        http://iaia.lcc.uma.es | http://www.uma.es
    This file is part of SISOB Data Extractor.
    SISOB Data Extractor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    SISOB Data Extractor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with SISOB Data Extractor. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.sisob.uma.NPL.Researchers;

import eu.sisob.uma.api.prototypetextmining.RepositoryPreprocessDataMiddleData;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecutionWithResource;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskPoolExecutorWithResources;
import eu.sisob.uma.api.h2dbpool.H2DBCredentials;
import eu.sisob.uma.api.prototypetextmining.RepositoryProcessedDataXML;
import gate.Gate;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * TODO: 
 *  - Remove all possibel static methods. 
 *  - Do another best method.
 * 
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class GateDataExtractorService 
{    
    static final String LOG_NAME = "AcademicDataExtractor";
    
    private static GateDataExtractorService INSTANCE = null;
    
    private CallbackableTaskPoolExecutorWithResources ctpe; 
    TextMiningParserGateResearcher parsers[];
    static String GATE_PATH;                         
    /**
     *
     */
    public static String KEYWORDS_PATH;                         
    static int NUMBER_OF_GATEPARSERS = 1;    
    static int BLOCK_SPEED = 5;    
    static H2DBCredentials RESOLVERDB_H2DBCREDENTIALS_Resolver;    
    static H2DBCredentials RESOLVERDB_H2DBCREDENTIALS_Trad_Tables_Academic;    
    
    HashMap<String, String[]> blocks_and_keywords;
    
    H2DBCredentials resolver_db;
    
    /**
     *
     * @param gate_path
     * @param keywords_path
     * @param number_of_gateparsers
     * @param block_speed
     * @param resolverdb_h2dbcredentials_trad_tables_academic
     * @param resolverdb_h2dbcredentials_resolver
     */
    public static void setServiceSettings(String gate_path, String keywords_path, int number_of_gateparsers, int block_speed, 
                                          H2DBCredentials resolverdb_h2dbcredentials_trad_tables_academic, H2DBCredentials resolverdb_h2dbcredentials_resolver)
    { 
        //ClassLoader.getSystemClassLoader().getResource("eu/sisob/components/gatedataextractor/GATE-6.0").getPath()
        NUMBER_OF_GATEPARSERS = number_of_gateparsers;
        GATE_PATH = gate_path;
        KEYWORDS_PATH = keywords_path;
        BLOCK_SPEED = block_speed;        
        RESOLVERDB_H2DBCREDENTIALS_Trad_Tables_Academic = resolverdb_h2dbcredentials_trad_tables_academic;
        RESOLVERDB_H2DBCREDENTIALS_Resolver = resolverdb_h2dbcredentials_resolver;
        
        eu.sisob.uma.api.prototypetextmining.ProjectLogger.LOGGER = Logger.getLogger(LOG_NAME);
	eu.sisob.uma.api.prototypetextmining.gatedataextractor.ProjectLogger.LOGGER = Logger.getLogger(LOG_NAME);
	eu.sisob.uma.NPL.Researchers.ProjectLogger.LOGGER = Logger.getLogger(LOG_NAME);
	eu.sisob.uma.NPL.Researchers.Freebase.ProjectLogger.LOGGER = Logger.getLogger(LOG_NAME);
        
    }
    
    /**
     *
     * @return
     */
    public static H2DBCredentials getH2DBCredentials_Resolver()
    {
        return RESOLVERDB_H2DBCREDENTIALS_Resolver;
    }
    
    /**
     *
     * @return
     */
    public static H2DBCredentials getH2DBCredentials_Trad_Tables_Academic()
    {
        return RESOLVERDB_H2DBCREDENTIALS_Trad_Tables_Academic;
    }
    
    // Private constructor suppresses 
    private GateDataExtractorService() 
    {
        try
        {   
            ctpe = null;
            ProjectLogger.LOGGER.info("Initialising GATE ...");      
            
            if(!new File(GATE_PATH + File.separator + "gate.xml").exists())
                throw new FileNotFoundException("Could not locate the site configuration file! (" + GATE_PATH + ")");            
            
            String gate_path = GATE_PATH;
            File home_path = new File(gate_path);
            Gate.setGateHome(home_path);
            File plugins_path = new File(home_path + "//plugins");
            Gate.setPluginsHome(plugins_path);
            Gate.init();            
            File gateHome = new File(Gate.getGateHome().getAbsolutePath()/* + "\\resources\\GATE-6.0"*/);
            File pluginsHome = new File(gateHome, "plugins");
            Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURL());
            Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Tools").toURL());
            ProjectLogger.LOGGER.info("Done!"); 

            parsers = new TextMiningParserGateResearcher[NUMBER_OF_GATEPARSERS];

            for(int k = 0; k < parsers.length; k++)
            {                
                parsers[k] = new TextMiningParserGateResearcher(DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER, 
                                                                new RepositoryProcessedDataXML(),
                                                                BLOCK_SPEED,
                                                                new RepositoryPreprocessDataMiddleData(),
                                                                false,
                                                                gate_path);                
            }            
            
            for(TextMiningParserGateResearcher parser : parsers)
            {
                ProjectLogger.LOGGER.info("Begin load GATE parser");   
                parser.iniActions();
                ProjectLogger.LOGGER.info("Done!");   
            }     
            
            ProjectLogger.LOGGER.info("Begin load keywords");   
            
            File keywords_dir = new File(KEYWORDS_PATH);            
            blocks_and_keywords = CVBlocks.getCVBlocksAndKeywords(keywords_dir);
            
            ProjectLogger.LOGGER.info("Done!");   

            ctpe = new CallbackableTaskPoolExecutorWithResources(parsers, parsers.length+100, 60);
            
            ProjectLogger.LOGGER.info("GATE parser already to fight!");                    
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex.getMessage());      
            ctpe = null;
        }        
    } 
    
    /**
     *
     * @return
     */
    public HashMap<String, String[]> getBlocksAndKeywords(){
        return blocks_and_keywords;
    }
    
    private void releaseResources()
    {
        ctpe.shutDown();
        for(int k = 0; k < parsers.length; k++)
        {                
            parsers[k] = null;                
        } 
        parsers = null;        
    }
    
    /**
     *
     * @return
     */
    public boolean isGateServiceOnline()
    {
        return ctpe != null;
    } 
    
    /**
     *
     */
    public synchronized static void createInstance() 
    {
        if (INSTANCE == null) 
        { 
            INSTANCE = new GateDataExtractorService();
        }
    }
 
    /**
     *
     * @return
     */
    public static GateDataExtractorService getInstance() 
    {
        if (INSTANCE == null) 
        {
            createInstance();
        }
        return INSTANCE;
    }
    
    /*
     * 
     */
    /**
     *
     * @param cte
     * @throws Exception
     */
    public static void addExecution(CallbackableTaskExecutionWithResource cte) throws Exception
    {           
        CallbackableTaskPoolExecutorWithResources refctpe = GateDataExtractorService.getInstance().ctpe;
        if (refctpe != null) 
        {
            refctpe.runTask(cte);
        }
        else
        {            
            throw new Exception("Gate Data Extractor Service is not running");
        }
    }   
    
    /**
     *
     */
    public static void releaseInstance() 
    {
        if (INSTANCE != null) 
        {
            INSTANCE.releaseResources();
            INSTANCE = null;
        }        
    }
    
    
}
