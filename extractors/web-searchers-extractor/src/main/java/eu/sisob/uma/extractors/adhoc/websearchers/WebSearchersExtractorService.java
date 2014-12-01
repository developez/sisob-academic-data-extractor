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

package eu.sisob.uma.extractors.adhoc.websearchers;

import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecution;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskPoolExecutor;
import eu.sisob.uma.extractors.adhoc.websearchers.ProjectLogger;
import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 * 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */

public class WebSearchersExtractorService
{    
    static final String LOG_NAME = "WebSearcherExtractorCV";
    
    private static WebSearchersExtractorService INSTANCE = null;
    
    private CallbackableTaskPoolExecutor ctpe;      
    
    /**
     *      
     */
    private WebSearchersExtractorService()
    {
        ctpe = new CallbackableTaskPoolExecutor(1, 100);      
        
        ProjectLogger.LOGGER = Logger.getLogger(LOG_NAME);
    }	        
    
    /**
     *
     */
    public synchronized static void createInstance() 
    {
        if (INSTANCE == null) 
        {             
            INSTANCE = new WebSearchersExtractorService();
        }
    }
 
    /**
     *
     * @return
     */
    public static WebSearchersExtractorService getInstance() 
    {
        if (INSTANCE == null) 
        {
            createInstance();
        }
        return INSTANCE;
    }
    
    /**
     * 
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
            INSTANCE = null;
        }
    }
}
