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

package eu.sisob.uma.crawler;

import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTask;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import eu.sisob.uma.footils.File.FileFootils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ResearchersCrawlerTask  implements CallbackableTask
{    
    final static String SPLITTED_XML_FILES_FOLDERNAME = "tmp-splitted-xml-files";
    final static String SPLITTED_PREFIX = "split.";
    final static String MATRIX_RESULTS_PREFIX = "results.";
    final static String NOTFOUND_RESEARCHERS_PREFIX = "notfound.";
    final static String MIDDLE_XML_DOCUMENT = "middle_document.xml";
    
    Boolean finished;
    protected org.dom4j.Document document;
    
    protected File resultsdata_dir;    
    protected File middledata_dir;
    protected File keywordsdata_dir;            
    
    protected File output_file_csv;    
    
    
    protected boolean split;
    
    /*
     * 
     * @param document - xml document with the information     
     */
    public ResearchersCrawlerTask(org.dom4j.Document document, File crawler_data_dir, File middledata_dir, File resultsdata_dir, File output_file_csv, boolean split)
    {
        this.document = document;        
        //if(!crawler_data_dir.endsWith(File.separator)) crawler_data_dir += File.separator;
        
        this.keywordsdata_dir = new File(crawler_data_dir, "keywords");
        this.middledata_dir = middledata_dir;
        this.resultsdata_dir = resultsdata_dir;
        this.finished = false;        
        this.output_file_csv = output_file_csv;       
        this.split = split;        
    }
            
    /*
     * Launch the crawler
     */
    public void executeTask() 
    {
        if(middledata_dir == null) {
            middledata_dir = new File(System.getProperty("java.io.tmpdir"));        
        } else {            
            if(!middledata_dir.exists())
                middledata_dir.mkdirs();            
        }        
        
        if(this.split)
        {
            File splitted_xml_dir = new File(middledata_dir, SPLITTED_XML_FILES_FOLDERNAME);
            
            ResearcherXMLFileSplitter splitter = new ResearcherXMLFileSplitter(this.document, splitted_xml_dir, SPLITTED_PREFIX);
            try
            {
                splitter.iterate();
            }
            catch(Exception ex)
            {
                ProjectLogger.LOGGER.error(ex.getMessage(), ex);
            }                  
            
            FilenameFilter filter = new FilenameFilter() 
            {
                public boolean accept(File dir, String name) {

                        if (name.startsWith(SPLITTED_PREFIX)) {
                                return true;
                        } else {
                                return false;
                        }
                }
            };                 
            
            File[] files = splitted_xml_dir.listFiles(filter);
            for(File file : files)
            {                
                org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
                org.dom4j.Document new_doc = null;
                
                try {
                    new_doc = reader.read(file);
                } catch (DocumentException ex) {
                    ProjectLogger.LOGGER.error(ex.getMessage(), ex);
                    new_doc = null;
                }
        
                if(new_doc != null)
                {
                    AirResearchersWebPagesExtractor o = new AirResearchersWebPagesExtractor(new_doc, keywordsdata_dir, middledata_dir, split);        
                    try
                    {                        
                        ProjectLogger.LOGGER.info("Begin Crawl - " + file.getName());
                        o.iterate();
                    }
                    catch(Exception ex)
                    {
                        ProjectLogger.LOGGER.error(ex.getMessage(), ex);
                    }
                    finally
                    {
                        ProjectLogger.LOGGER.info("End Crawl" + file.getName());
                    }
                    
                    try 
                    {                        
                        FileUtils.write(file, new_doc.asXML(), "UTF-8");                                        
                        ProjectLogger.LOGGER.info("Updated file '" + file.getPath() + "'");
                    } 
                    catch (IOException ex) 
                    {
                         ProjectLogger.LOGGER.error("Split file cannot be updated '" + file.getPath() + "'", ex);
                    }    
                }
                else
                {
                    ProjectLogger.LOGGER.error("Split file cannot be openend '" + file.getPath() + "'");
                }
            }
        }
        else
        {
            AirResearchersWebPagesExtractor o = new AirResearchersWebPagesExtractor(document, keywordsdata_dir, middledata_dir, split);        
            try
            {
                ProjectLogger.LOGGER.info("Begin Crawl - " + middledata_dir.getName());
                o.iterate();
            }
            catch(Exception ex)
            {
                ProjectLogger.LOGGER.error(ex.getMessage(), ex);
            }
            finally
            {
                ProjectLogger.LOGGER.info("End Crawl");            
            }        

            
            if(middledata_dir == null)
            {
                ProjectLogger.LOGGER.error("Something was wrong. Temporal directory is not set.");
            }
            else
            {            
                try 
                {
                    File fField = new File(middledata_dir, MIDDLE_XML_DOCUMENT);
                    FileUtils.write(fField, this.document.asXML(), "UTF-8");                
                    ProjectLogger.LOGGER.info(fField.getAbsoluteFile() + " created.");
                } 
                catch (IOException ex) 
                {
                    ProjectLogger.LOGGER.error(ex.getMessage(), ex);
                }            
            }
            
        }
    }

    /*
     * Callback function to manage the results
     */
    public void executeCallBackOfTask() 
    {  
        
        if(this.split)
        {
            FilenameFilter filter = new FilenameFilter() 
            {
                public boolean accept(File dir, String name) {

                        if (name.startsWith(ResearchersCrawlerTask.SPLITTED_PREFIX)) {
                                return true;
                        } else {
                                return false;
                        }
                }
            };
            
//            File dir = new File(middle_data_dir + File.separator + SPLITTED_XML_FILES_FOLDERNAME);
//            if(!dir.exists())
//                dir.mkdirs();
//            else
//            {                
//                dir.delete();
//                dir.mkdir();
//            }
            File splitted_xml_dir = new File(middledata_dir, SPLITTED_XML_FILES_FOLDERNAME);
            File[] files = splitted_xml_dir.listFiles(filter);
            
            try {
                File output_file_notfound_csv = new File(this.output_file_csv.getParentFile(), NOTFOUND_RESEARCHERS_PREFIX + this.output_file_csv.getName());
                //boolean success = FileFormatConversor.createResearchersCSVFileFromXML(files, this.results_dir + File.separator + output_csv_filename);
                boolean success = FileFormatConversor.createResearchersCSVFileFromXML(files, this.output_file_csv, output_file_notfound_csv);
                if(success)
                    Logger.getLogger("root").error("Create CSV results file '" + this.output_file_csv.getName() + "'");
                    
            } catch (FileNotFoundException ex) {
                ProjectLogger.LOGGER.error("Error creating csv from xml' " + this.output_file_csv.getName());
            } catch (IOException ex) {
                ProjectLogger.LOGGER.error("Error creating csv from xml' " + this.output_file_csv.getName());
            }
            
            //Build result matrix
            
            HashMap<String, HashMap<String, Map.Entry<Integer, Integer>>> resultsMatrix = new HashMap<String, HashMap<String, Map.Entry<Integer, Integer>>>();
            HashMap<String, Integer> universities_axis = new HashMap<String, Integer>();
            HashMap<String, Integer> dept_axis = new HashMap<String, Integer>();

            for(File file : files)
            {                
                org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
                org.dom4j.Document new_doc = null;
                
                try {
                    new_doc = reader.read(file);
                } catch (DocumentException ex) {
                    ProjectLogger.LOGGER.error(ex.getMessage(), ex);
                    new_doc = null;
                }
        
                if(new_doc != null)
                {
                    MatrixResultBuilder o = new MatrixResultBuilder(new_doc, middledata_dir, universities_axis, dept_axis, resultsMatrix);        
                    try
                    {                        
                        ProjectLogger.LOGGER.info("Begin Count - " + file.getName());
                        o.iterate();
                    }
                    catch(Exception ex)
                    {
                        ProjectLogger.LOGGER.error(ex.getMessage(), ex);
                    }
                    finally
                    {
                        ProjectLogger.LOGGER.info("End Count"  + file.getName());            
                    }                      
                }
                else
                {
                    ProjectLogger.LOGGER.error("Split file cannot be openend '" + file.getPath() + "'");
                }
            }            
            
            //MatrixResultBuilder.writeResultsMatrix(new File(this.results_dir + File.separator + "results." + output_csv_filename), universities_axis, dept_axis, resultsMatrix);
            MatrixResultBuilder.writeResultsList(new File(this.resultsdata_dir, MATRIX_RESULTS_PREFIX + this.output_file_csv.getName()), universities_axis, dept_axis, resultsMatrix);
        }
        else
        {
            try {
                File output_file_notfound_csv = new File(this.output_file_csv.getParentFile(), NOTFOUND_RESEARCHERS_PREFIX);
                boolean success = FileFormatConversor.createResearchersCSVFileFromXML(this.document,
                                                                                      this.output_file_csv, output_file_notfound_csv);
            } catch (FileNotFoundException ex) {
                ProjectLogger.LOGGER.error("Error creating csv from xml' " + this.output_file_csv.getName());
            } catch (IOException ex) {
                ProjectLogger.LOGGER.error("Error creating csv from xml' " + this.output_file_csv.getName());
            }
        }
        
        setFinished(true);
    }
    
    public boolean isFinished()
    {
        synchronized(finished) 
        {
            return finished;
        }
    }
    
    public void setFinished(boolean b)
    {
        synchronized(finished) 
        {
            finished = b;
        }
    }
    
}
