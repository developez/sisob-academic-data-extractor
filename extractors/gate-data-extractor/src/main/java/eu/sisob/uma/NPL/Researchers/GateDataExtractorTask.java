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


import eu.sisob.uma.NPL.Researchers.Freebase.LocationDataResolver;
import eu.sisob.uma.NPL.Researchers.Freebase.LocationDataResolver_Method2;
import java.lang.Thread.UncaughtExceptionHandler;
import org.apache.log4j.Logger;
import eu.sisob.uma.api.prototypetextmining.DataRepository;
import eu.sisob.uma.api.prototypetextmining.MiddleData;
import eu.sisob.uma.api.prototypetextmining.RepositoryProcessedDataXML;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskWithResource;
import eu.sisob.uma.api.concurrent.threadpoolutils.ExecutorResource;
import eu.sisob.uma.api.h2dbpool.H2DBCredentials;
import eu.sisob.uma.api.h2dbpool.H2DBPool;
import eu.sisob.uma.api.prototypetextmining.RepositoryPreprocessDataMiddleData;
import eu.sisob.uma.api.prototypetextmining.globals.CVItemExtracted;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;


/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class GateDataExtractorTask implements CallbackableTaskWithResource                               
{        
    /* string with result in xml */                

    private ExecutorResource executorResource;    
    //private TextMiningParserGateResearcher parser;          

    /**
     *
     */
    protected org.dom4j.Document xml_doc_results;        
    boolean occursUncaugthExceptionInGateProcess;

    protected RepositoryPreprocessDataMiddleData taskRepPrePro;

    int iaux = 0;

    Boolean finished;
    
    boolean use_dataresolver;
    H2DBCredentials credentials_dataresolver;
    boolean use_academic_trad_tables;
    H2DBCredentials credentials_academic_trad_tables;    
    
    /**
     *
     * @param taskRepPrePro
     * @param use_academic_trad_tables
     * @param credentials_academic_trad_tables
     * @param use_resolver
     * @param credentials_resolver
     */
    public GateDataExtractorTask(RepositoryPreprocessDataMiddleData taskRepPrePro, boolean use_academic_trad_tables, H2DBCredentials credentials_academic_trad_tables, boolean use_resolver, H2DBCredentials credentials_resolver)
    {
        this.taskRepPrePro = taskRepPrePro;
        this.finished = false;        
        this.use_dataresolver = use_resolver;        
        this.credentials_dataresolver = credentials_resolver;    
        
        this.use_academic_trad_tables = use_academic_trad_tables;        
        this.credentials_academic_trad_tables = credentials_academic_trad_tables;    
        
    }	
    
    /**
     *
     * @return
     */
    public org.dom4j.Document getXMLResults()
    {
        if(this.finished)
            return xml_doc_results;
        else
            return null;
    }
        
    /**
     *
     */
    @Override
        public void executeTask() 
        {
            if(getExecutorResource() != null)
            {  
                
                TextMiningParserGateResearcher parser = null;
                DataRepository parserRepPrePro = null;
                RepositoryProcessedDataXML repXML = null;
                
                try 
                {
                    //Take parser and load input repository from CSV                    
                    parser = (TextMiningParserGateResearcher)this.executorResource.getResource();                         
                    parserRepPrePro = (DataRepository) parser.getRepOutput();                    
                    repXML = (RepositoryProcessedDataXML) parser.getRepInput();
                    
                    List<MiddleData> mds = null;
                    while((mds = taskRepPrePro.getData(DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER, 100)).size() > 0)
                    {
                        for(MiddleData md : mds)
                            parserRepPrePro.addData(md);
                    }                   
                    
                    Thread th = null;
                    UncaughtExceptionHandler he = new UncaughtExceptionHandler() 
                    {
                        public Throwable e = null;
                    @Override
                        public void uncaughtException(Thread t, Throwable e) 
                        {                        
                            occursUncaugthExceptionInGateProcess = true;
                            ProjectLogger.LOGGER.info("uncaughtException: " + e.getMessage());                        
                        }
                    };
                    
                    //Launch GATE parser     
                    this.occursUncaugthExceptionInGateProcess = false;
                    th = new Thread(parser);
                    th.setUncaughtExceptionHandler(he);                    
                    th.start();                    
                    th.join();  
                    
                    this.xml_doc_results = (Document) repXML.getDocXML().clone(); 
                    
                    // Augmented Information Block
                    if(this.use_dataresolver && this.credentials_academic_trad_tables != null)
                    {
                        //Using data resolver "method 2", see the class for the heuristic
                        
                        try{
                            DataResearcherAugmentedInformation.resolveLocationOfEntities(xml_doc_results, 
                                                                                     new LocationDataResolver_Method2(true, this.credentials_dataresolver));
                        }catch(Exception ex){
                            Logger.getRootLogger().error("Error resolving locator. " + ex.toString());
                        }
                            
                    }    
                    
                    // Augmented Information Block
                    if(this.use_academic_trad_tables && this.credentials_academic_trad_tables != null)
                    {
                        //Using data resolver "method 2", see the class for the heuristic                        
                        //DataResearcherAugmentedInformation.resolveLocationOfEntities(xml_doc_results, 
                        //new LocationDataResolver_Method2(true, this.credentials_academic_trad_tables));                       
                        
                        H2DBPool dbpool_academic_trad_tables = new H2DBPool(credentials_academic_trad_tables);
                        try{
                            DataResearcherAugmentedInformation.resolveAcademicPosistion(xml_doc_results, dbpool_academic_trad_tables);
                        }catch(Exception ex){
                            Logger.getRootLogger().error("Error academic positions. " + ex.toString());
                        }
                    } 
                }
                catch(Exception ex)
                {            
                    ProjectLogger.LOGGER.error(ex.getMessage());
                }
                finally
                {   
                    if(this.occursUncaugthExceptionInGateProcess)
                    {                        
                        if(parser != null)
                        {
                            ProjectLogger.LOGGER.info("Begin reload GATE parser because succeded a uncaughtException!");        
                            try
                            {                                          
                                parser.endActions();
                                parser.iniActions();
                            }
                            catch (Exception ex)
                            {
                                ProjectLogger.LOGGER.error(null, ex);
                            }
                            ProjectLogger.LOGGER.info("Done!"); 
                        }
                    }
                    
                    if(parserRepPrePro != null) parserRepPrePro.clearData();                    
                    
                    if(repXML != null) repXML.clearData();                    
                }                   
            }
            else
            {
                ProjectLogger.LOGGER.info(this.iaux + " can't get GATE Data Extractor resource");
                this.xml_doc_results = null;
            }
        }        
        
        /*
         * Processing the results here      
         */
    /**
     *
     */
    @Override
        public void executeCallBackOfTask() 
        {
            if(this.xml_doc_results != null)
            {
                ProjectLogger.LOGGER.info("Result success");                                                       
            }
            else
            {
                ProjectLogger.LOGGER.info("No results.");
            }
            setFinished(true);
        }   	
        
        /**
         * @return the executorResource
         */
    @Override
        public synchronized ExecutorResource getExecutorResource() {
            return this.executorResource;
        }

        /**
         * @param executorResource the executorResource to set
         */
    @Override
        public synchronized void setExecutorResource(ExecutorResource executorResource) 
        {
            this.executorResource = executorResource;
        }

    /**
     *
     * @return
     */
    @Override
        public boolean isFinished()
        {
            synchronized(finished) 
            {
                return finished;
            }
        }

        /**
     *
     * @param b
     */
    public void setFinished(boolean b)
        {
            synchronized(finished) 
            {
                finished = b;
            }
        }
        
       


}
