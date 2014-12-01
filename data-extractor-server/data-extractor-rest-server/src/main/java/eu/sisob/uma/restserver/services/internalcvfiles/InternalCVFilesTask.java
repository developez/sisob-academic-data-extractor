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

package eu.sisob.uma.restserver.services.internalcvfiles;

import eu.sisob.uma.restserver.services.email.*;
import eu.sisob.uma.NPL.Researchers.GateDataExtractorService;
import eu.sisob.uma.NPL.Researchers.GateDataExtractorSingle;
import eu.sisob.uma.NPL.Researchers.ProjectLogger;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTask;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecution;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecutionWithResource;
import eu.sisob.uma.api.h2dbpool.H2DBCredentials;
import eu.sisob.uma.api.prototypetextmining.RepositoryPreprocessDataMiddleData;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import eu.sisob.uma.extractors.adhoc.email.EmailExtractorService;
import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.FileSystemManager;
import eu.sisob.uma.restserver.TheResourceBundle;
import eu.sisob.uma.footils.File.ZipUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class InternalCVFilesTask 
{
    public final static String input_data_source_filename_prefix_csv = "data-researchers-documents-urls";         
    public final static String input_data_source_filename_ext_csv = ".csv";   
    
    public final static String input_data_documents_in_zip = "documents.zip";
    
    public final static String output_data_source_filename_prefix_csv = "data-researchers-documents-urls-suburls-";     
    public final static String output_data_source_filename_ext_csv = ".csv";   
    
    //public final static String output_data_source_nofound_filename_prefix_csv = "notfound.data-researchers-documents-urls";     
    //public final static String output_data_source_nofound_filename_ext_csv = ".csv";   
    
    public final static String output_data_with_files_source_filename_prefix_csv = "data-researchers-documents-urls-subfiles-";     
    public final static String output_data_with_files_data_source_filename_ext_csv = ".csv";   
    
    public final static String zip_output_filename = "downloads.zip";
    public final static String results_dirname = "downloads";
    
    public static boolean launch(String user, String pass, String task_code, String code_task_folder, String email, StringWriter message)
    {   
        if(message == null)
        {
            return false;
        }
        
        message.getBuffer().setLength(0);
        File code_task_folder_dir = new File(code_task_folder);
        
        File documents_dir = code_task_folder_dir;        
        
        File input_file = FileSystemManager.getFileIfExists(code_task_folder_dir, input_data_source_filename_prefix_csv, input_data_source_filename_ext_csv);        
        boolean validate = input_file != null;      
        
        if(!validate) {            
            message.write("You have not uploaded any file like this '" + input_data_source_filename_prefix_csv + "*" + input_data_source_filename_ext_csv +"' file");
            return false;
        }
        
        try {                    
             validate = FileFormatConversor.checkResearchersCSV(input_file, true);
        } catch(Exception ex) {              
            eu.sisob.uma.extractors.adhoc.email.ProjectLogger.LOGGER.error(message.toString(), ex);
            validate = false;
        }
            
        if(!validate) {            
            message.write("The format of '" + input_file.getName() + "' does not seems be correct"); //FIXME
            return false;
        }
            
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

        File zip_file = new File(code_task_folder_dir, input_data_documents_in_zip);                        

        if(zip_file.exists())
        {
            documents_dir = new File(code_task_folder_dir, AuthorizationManager.middle_data_dirname);
            if(!ZipUtil.unZipItToSameFolder(zip_file, documents_dir))                
            {
                message.write(input_data_documents_in_zip + " cannot bet unziped"); //FIXME
                return false;
            }
        }

        
        String output_filename = input_file.getName().replace(input_data_source_filename_prefix_csv, output_data_source_filename_prefix_csv);                
        File output_file = new File(results_data_dir, output_filename);                    
        
        String out_filename_2 = input_file.getName().replace(input_data_source_filename_prefix_csv, output_data_with_files_source_filename_prefix_csv);                
        File output_file_2 = new File(results_data_dir, out_filename_2);           
        

        InternalCVFilesExtractorTaskInRest task = new InternalCVFilesExtractorTaskInRest(user, task_code, code_task_folder, email, 
                                                                                         input_file, 
                                                                                         documents_dir, 
                                                                                         output_file,                                                                                          
                                                                                         new File(results_data_dir,results_dirname), 
                                                                                         new File(results_data_dir,zip_output_filename),
                                                                                         output_file_2);
        
        try 
        {
            EmailExtractorService.getInstance().addExecution((new CallbackableTaskExecution(task)));
            
            message.write(TheResourceBundle.getString("Jsp Task Executed Msg"));
        } 
        catch (Exception ex) 
        {
            message.write(TheResourceBundle.getString("Jsp Task Executed Error Msg"));
            ProjectLogger.LOGGER.error(message.toString(), ex);
            return false;
        }   
        
        return true;
    }
}
