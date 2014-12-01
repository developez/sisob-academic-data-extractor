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

package eu.sisob.uma.crawler;

import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecution;
import eu.sisob.uma.api.crawler4j.crawler.PageFetcher;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskPoolExecutor;
import eu.sisob.uma.footils.File.FileFootils;
import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 * Agent Manager for crawler
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */

public class ResearchersCrawlerService
{    
    static final String LOG_NAME = "ResearchersCrawlerFinder";
    
    private static ResearchersCrawlerService INSTANCE = null;
    
    private CallbackableTaskPoolExecutor ctpe;      
    
    public static String CRAWLER_DATA_PATH = null;
    
    private static URL RESOURCES_URL = null;
    
    public static void setServiceSettings(String crawler_data_path, URL resources_url, boolean traceUrls, boolean traceSearch)
    {
        CRAWLER_DATA_PATH = crawler_data_path;
        RESOURCES_URL = resources_url;
        
        //
        eu.sisob.uma.api.crawler4j.crawler.ProjectLogger.LOGGER = Logger.getLogger(LOG_NAME);
	eu.sisob.uma.crawler.ProjectLogger.LOGGER = Logger.getLogger(LOG_NAME);        
                
        CrawlerTrace.setActivate(traceUrls, traceSearch);
    }
  
    /**
     *      
     */
    private ResearchersCrawlerService()
    {
        ctpe = new CallbackableTaskPoolExecutor(2, 100);   
        
        //todo, REMOVE LITERALS
        if(CRAWLER_DATA_PATH != null)
        {
            File keywords_dir = new File(CRAWLER_DATA_PATH);
            if(!(keywords_dir).exists()) {
                keywords_dir.mkdir();
                FileFootils.copyResourcesRecursively(RESOURCES_URL, keywords_dir);        
            }
            ProjectLogger.LOGGER.info("Crawler resources copied");
        }
        else
        {            
            throw new NullPointerException("Crawler data path must be initialized");
        }
    }	        
    
    public synchronized static void createInstance() 
    {
        if (INSTANCE == null) 
        { 
            PageFetcher.startConnectionMonitorThread();  
            INSTANCE = new ResearchersCrawlerService();
        }
    }
 
    public static ResearchersCrawlerService getInstance() 
    {
        if (INSTANCE == null) 
        {
            createInstance();
        }
        return INSTANCE;
    }
    
    /**
     * Adds crawler execution
     * @param cte
     * @throws InterruptedException
     */
    public void addExecution(CallbackableTaskExecution cte) throws InterruptedException
    {   
        ctpe.runTask(cte);
    }
    
    /**
     * End crawler manager and crawler
     */
    public static void releaseInstance()
    {                   
        if(INSTANCE != null)
        {
            PageFetcher.stopConnectionMonitorThread();  
            INSTANCE = null;
        }
    }
}
