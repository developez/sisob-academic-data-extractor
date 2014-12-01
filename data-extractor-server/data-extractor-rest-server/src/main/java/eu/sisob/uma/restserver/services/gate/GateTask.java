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

package eu.sisob.uma.restserver.services.gate;

import eu.sisob.uma.NPL.Researchers.CVBlocks;
import eu.sisob.uma.NPL.Researchers.GateDataExtractorService;
import eu.sisob.uma.NPL.Researchers.GateDataExtractorSingle;
import eu.sisob.uma.NPL.Researchers.ProjectLogger;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecutionWithResource;
import eu.sisob.uma.api.h2dbpool.H2DBCredentials;
import eu.sisob.uma.api.prototypetextmining.MiddleData;
import eu.sisob.uma.api.prototypetextmining.RepositoryPreprocessDataMiddleData;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.FileSystemManager;
import eu.sisob.uma.restserver.TheResourceBundle;
import eu.sisob.uma.footils.File.ZipUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class GateTask 
{
    public final static String input_data_source_filename_prefix_csv = "data-researchers-documents-urls";         
    public final static String input_data_source_filename_ext_csv = ".csv";   
    
    public final static String input_data_documents_in_zip = "documents.zip";         
    
    public final static String output_data_extracted_filename_xml = "data-extracted-researchers.xml";         
    
    
    public static boolean launch(String user, String pass, String task_code, String code_task_folder, String email, StringWriter message, boolean verbose, boolean split_by_keyword)
    {   
        if(message == null)
        {
            return false;
        }
        boolean success = false;
        message.getBuffer().setLength(0);
        File code_task_folder_dir = new File(code_task_folder);
        
        File documents_dir = code_task_folder_dir;               
        
        File csv_data_source_file = FileSystemManager.getFileIfExists(code_task_folder_dir, input_data_source_filename_prefix_csv, input_data_source_filename_ext_csv);
        
        boolean validate = csv_data_source_file != null;      
        
        if(!validate)
        {
            success = false;
            message.write("You have not uploaded any file like this '" + input_data_source_filename_prefix_csv + "*" + input_data_source_filename_ext_csv +"' file");
        }
        else
        {
            String middle_data_folder = code_task_folder + File.separator + AuthorizationManager.middle_data_dirname;
            File middle_data_dir = null;
            try {
                middle_data_dir = FileSystemManager.createFileAndIfExistsDelete(middle_data_folder);
            } catch(Exception ex) {
                ProjectLogger.LOGGER.error(ex.toString(), ex);
                message.append("The file couldn't be created " + middle_data_dir.getName() + "\r\n");
            }  

            String results_data_folder = code_task_folder + File.separator + AuthorizationManager.results_dirname;
            File results_data_dir = null;
            try {
                results_data_dir = FileSystemManager.createFileAndIfExistsDelete(results_data_folder);
            } catch(Exception ex) {
                ProjectLogger.LOGGER.error(ex.toString(), ex);
                message.append("The file couldn't be created " + results_data_dir.getName() + "\r\n");
            }  
                
            File zip_file = new File(code_task_folder_dir, input_data_documents_in_zip);                        
            
            if(zip_file.exists())
            {
                documents_dir = new File(code_task_folder_dir, AuthorizationManager.middle_data_dirname);
                if(!ZipUtil.unZipItToSameFolder(zip_file, documents_dir))                
                {
                    success = false;
                    message.write(input_data_documents_in_zip + " cannot bet unziped"); //FIXME
                    return success;
                } else {
                    
                }   
            }
            
            RepositoryPreprocessDataMiddleData preprocessedRep = null;
            try 
            {                
                File verbose_dir = null;
                
                if(verbose)
                {
                    verbose_dir = new File(code_task_folder_dir, AuthorizationManager.verbose_dirname);
                    if(!verbose_dir.exists())
                        verbose_dir.mkdir();
                }
                
                HashMap<String, String[]> blocks_and_keywords = null;
                if(split_by_keyword){
                    blocks_and_keywords = GateDataExtractorService.getInstance().getBlocksAndKeywords();
                }
                        
                preprocessedRep = GateDataExtractorSingle.createPreprocessRepositoryFromCSVFile(csv_data_source_file, 
                                                                                                ';',
                                                                                                documents_dir, 
                                                                                                verbose,
                                                                                                verbose_dir, 
                                                                                                split_by_keyword, 
                                                                                                blocks_and_keywords,
                                                                                                middle_data_dir);
                
                
                
            } 
            catch (Exception ex) 
            {
                message.append("The format of '" + csv_data_source_file.getName() + "' does not seems be correct. Please check the headers of the csv file (read in the instructions which are optionals) and be sure that the field separators are ';'. Please read the intructions of the task. \r\nAlso check that you have uploaded all the documents referenced in the csv file (if you have upload all the documents compressed in " + input_data_documents_in_zip + " file, please, check that it has all the files referenced in the csv).<br>Message: " + ex.getMessage()); //FIXME
                ProjectLogger.LOGGER.error(message.toString(), ex);
            }
            
            if(preprocessedRep != null)
            {
                H2DBCredentials cred_resolver = GateDataExtractorService.getH2DBCredentials_Resolver();
                H2DBCredentials cred_trad = GateDataExtractorService.getH2DBCredentials_Trad_Tables_Academic();
                GateDataExtractorTaskInRest task = new GateDataExtractorTaskInRest(preprocessedRep, true, cred_trad, true, cred_resolver, user, pass, task_code, code_task_folder, email);                                                         

                try 
                {
                    GateDataExtractorService.getInstance().addExecution((new CallbackableTaskExecutionWithResource(task)));
                    success = true;
                    message.write(TheResourceBundle.getString("Jsp Task Executed Msg"));
                } 
                catch (Exception ex) 
                {
                    success = false;
                    message.write(TheResourceBundle.getString("Jsp Task Executed Error Msg"));
                    ProjectLogger.LOGGER.error(message.toString(), ex);
                    validate = false;
                }                
            }
        }
        
        return success;
    }
}
