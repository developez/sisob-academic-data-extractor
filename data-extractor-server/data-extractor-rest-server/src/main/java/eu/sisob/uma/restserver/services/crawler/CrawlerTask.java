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

package eu.sisob.uma.restserver.services.crawler;

import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecution;
import eu.sisob.uma.crawler.ProjectLogger;
import eu.sisob.uma.crawler.ResearchersCrawlerService;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.FileSystemManager;
import eu.sisob.uma.restserver.TheResourceBundle;
import eu.sisob.uma.restserver.services.crawler.ResearchersCrawlerTaskInRest;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import org.apache.log4j.Logger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CrawlerTask 
{    
    public final static String input_data_source_filename_prefix_csv = "data-researchers-urls";         
    public final static String input_data_source_filename_ext_csv = ".csv";        
    
    public final static String middle_data_source_filename_xml = "data-researchers-urls.xml";    
    
    public final static String output_data_source_filename_csv = "data-researchers-documents-urls";             
    public final static String output_data_source_filename_ext_csv = ".csv";        
    
    public final static String output_data_source_nofound_filename_prefix_csv = "data-researchers-unfounded";         
    public final static String output_data_source_nofound_filename_ext_csv = ".csv";                 
    
    public static boolean relaunch(String user, String pass, String task_code, String code_task_folder, String email, StringWriter message)
    {
        boolean success = false;
        
        String results_data_folder = code_task_folder + File.separator + AuthorizationManager.results_dirname;
        File results_data_dir = new File(results_data_folder);      
        
        FilenameFilter filter = new FilenameFilter() 
        {
            public boolean accept(File dir, String name) {

                    if (name.endsWith(AuthorizationManager.results_dirname) && dir.isDirectory()) {
                            return true;
                    } else {
                            return false;
                    }
            }
        };                 
                
        try
        {                  
            int results_dir = results_data_dir.getParentFile().list(filter).length;            
            results_data_dir.renameTo(new File(results_dir + "." + results_data_dir.getName()));
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex.getMessage(), ex);
        }
        
        if(success) {
            success = CrawlerTask.launch(user, pass, task_code, code_task_folder, email, message);        
        }
        
        return success;
    }
                    
    public static boolean launch(String user, String pass, String task_code, String code_task_folder, String email, StringWriter message)
    {   
        if(message == null)
        {
            return false;
        }
        boolean success = false;
        message.getBuffer().setLength(0);
        File code_task_folder_dir = new File(code_task_folder);
        
        File csv_data_source_file = FileSystemManager.getFileIfExists(code_task_folder_dir, input_data_source_filename_prefix_csv, input_data_source_filename_ext_csv);
        
        boolean validate = csv_data_source_file != null;                    

        if(!validate)
        {
            success = false;
            message.write("You have not uploaded 'data-researchers-urls.csv' file"); //FIXME
        }
        
        org.dom4j.Document document = null;        

        String middle_data_folder = code_task_folder + File.separator + AuthorizationManager.middle_data_dirname;
        File middle_data_dir = null;
        try {
            middle_data_dir = FileSystemManager.createFileAndIfExistsDelete(middle_data_folder);
        } catch(Exception ex) {
            ProjectLogger.LOGGER.error(ex.toString(), ex);
            message.append("The file couldn't be created " + middle_data_dir.getName() + "\r\n");
            return false;
        }  

        String results_data_folder = code_task_folder + File.separator + AuthorizationManager.results_dirname;
        File results_data_dir = null;
        try {
            results_data_dir = FileSystemManager.createFileAndIfExistsDelete(results_data_folder);
        } catch(Exception ex) {
            ProjectLogger.LOGGER.error(ex.toString(), ex);
            message.append("The file couldn't be created " + results_data_dir.getName() + "\r\n");
            return false;
        }  
        
        try
        {   
            File middle_data_source_file = new File(middle_data_dir, middle_data_source_filename_xml);
            validate = FileFormatConversor.createResearchersXMLFileFromCSV(csv_data_source_file, middle_data_source_file);            
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex.getMessage(), ex);
            validate = false;               
        }
        
        if(!validate)
        {
            message.append("The format of '" + csv_data_source_file.getName() + "' does not seems be correct. Please check the headers of the csv file (read in the instructions which are optionals) and be sure that the field separators are ';'. Please read the intructions of the task." + "\r\n");             
            return false;
        }
        
        try {
            File xmlFile = new File(middle_data_folder,  middle_data_source_filename_xml);                    
            org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
            document = reader.read(xmlFile);
        }
        catch(Exception ex)
        {
            message.write("The format of '" + csv_data_source_file.getName() + "' does not seems be correct"); //FIXME
            ProjectLogger.LOGGER.error(message.toString(), ex);
            return false;
        } 
                          
        String out_filename = csv_data_source_file.getName().replace(input_data_source_filename_prefix_csv, output_data_source_filename_csv);                
        File csv_data_output_file = new File(results_data_dir, out_filename); 
        ResearchersCrawlerTaskInRest task = new ResearchersCrawlerTaskInRest(document, new File(ResearchersCrawlerService.CRAWLER_DATA_PATH),
                                                                                       middle_data_dir,    
                                                                                       results_data_dir,                                                                                        
                                                                                       csv_data_output_file,
                                                                                       user, pass, task_code, code_task_folder, email);
        try {

            ResearchersCrawlerService.getInstance().addExecution(new CallbackableTaskExecution(task));                    
            success = true;
            message.write(TheResourceBundle.getString("Jsp Task Executed Msg"));

        } catch (InterruptedException ex) {
            
            message.write(TheResourceBundle.getString("Jsp Task Executed Error Msg"));
            ProjectLogger.LOGGER.error(message.toString(), ex);
            return false;
        }               
        
        return true;
    }
}
