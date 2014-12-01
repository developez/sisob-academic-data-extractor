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

package eu.sisob.uma.restserver.services.websearchers;

import eu.sisob.uma.extractors.adhoc.email.ProjectLogger;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecution;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import eu.sisob.uma.extractors.adhoc.websearchers.WebSearchersExtractorService;
import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.FileSystemManager;
import eu.sisob.uma.restserver.TheResourceBundle;
import java.io.File;
import java.io.StringWriter;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class WebSearcherCVTask 
{
    public final static String input_data_source_filename_prefix_csv = "data-researchers-urls";         
    public final static String input_data_source_filename_ext_csv = ".csv";        
    
    public final static String output_data_source_filename_prefix_csv = "data-researchers-documents-urls";         
    public final static String output_data_source_filename_ext_csv = ".csv";    
    
    public final static String output_data_source_nofound_filename_prefix_csv = "notfound.data-researchers-urls";         
    public final static String output_data_source_nofound_filename_ext_csv = ".csv";    
    
    public final static String output_data_source_files_filename_prefix_csv = "data-researchers-documents-files";         
    public final static String output_data_source_files_filename_ext_csv = ".csv";    
    
    public final static String zip_output_filename = "downloads.zip";
    public final static String folder_results_pathname = "downloads";
    
    
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
            message.write("You have not uploaded any file like this '" + input_data_source_filename_prefix_csv + "*" + input_data_source_filename_ext_csv +"' file");
        }
        else
        {   
            try
            {                    
                 validate = FileFormatConversor.checkResearchersCSV(csv_data_source_file, false);
            }
            catch(Exception ex)
            {
                success = false;
                message.append("The format of '" + csv_data_source_file.getName() + "' does not seems be correct. Please check the headers of the csv file (read in the instructions which are optionals) and be sure that the field separators are ';'. Please read the intructions of the task." + "\r\n");             
                ProjectLogger.LOGGER.error(message.toString(), ex);
                validate = false;
            }            

            if(validate)
            {

                String results_data_folder = code_task_folder + File.separator + AuthorizationManager.results_dirname;
                File results_data_dir = null;
                try {
                    results_data_dir = FileSystemManager.createFileAndIfExistsDelete(results_data_folder);
                } catch(Exception ex) {
                    ProjectLogger.LOGGER.error(ex.toString(), ex);
                    message.append("The file couldn't be created " + results_data_dir.getName() + "\r\n");
                    return false;
                }              

                String out_filename = csv_data_source_file.getName().replace(input_data_source_filename_prefix_csv, output_data_source_filename_prefix_csv);                
                File csv_data_output_file = new File(results_data_dir, out_filename);            
                
                String out_filename_unfounded = csv_data_source_file.getName().replace(input_data_source_filename_prefix_csv, output_data_source_nofound_filename_prefix_csv);
                File csv_data_output_file_unfounded = new File(results_data_dir, out_filename_unfounded);            
                
                String output_filename_2 = csv_data_source_file.getName().replace(input_data_source_filename_prefix_csv, output_data_source_files_filename_prefix_csv);
                File output_file_2 = new File(results_data_dir, output_filename_2);            
                
                WebSearcherExtractorCVTaskInRest task = new WebSearcherExtractorCVTaskInRest(user, task_code, code_task_folder, email, csv_data_source_file, csv_data_output_file, csv_data_output_file_unfounded,
                                                                                             new File(results_data_dir,folder_results_pathname), 
                                                                                             new File(results_data_dir,zip_output_filename), 
                                                                                             output_file_2);                                                         

                try 
                {
                    WebSearchersExtractorService.getInstance().addExecution(new CallbackableTaskExecution(task));        
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
