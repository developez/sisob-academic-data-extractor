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

import eu.sisob.uma.extractors.adhoc.websearchers.ProjectLogger;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTask;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskWithResource;
import eu.sisob.uma.extractors.adhoc.websearchers.WebSearchersExtractor;
import eu.sisob.uma.extractors.adhoc.websearchers.WebSearchersExtractorTask;
import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.Mailer;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class WebSearcherExtractorTaskInRest extends WebSearchersExtractorTask
{       
    String user;    
    String pass;    
    String email;    
    String task_code;
    String task_code_folder;         
    
    /*
     * @param document - xml document with the information     
     */    
    public WebSearcherExtractorTaskInRest(String user, String task_code, String task_code_folder, String email, File csv_reseacher_input_file, File csv_reseacher_output_file, File csv_researchers_output_file_unfounded, WebSearchersExtractor.SearchPatterns search_pattern)
    {   
        super(csv_reseacher_input_file, csv_reseacher_output_file, csv_researchers_output_file_unfounded, search_pattern);
        
        this.user = user;
        this.pass = pass;
        this.email = email;
        this.task_code = task_code;
        this.task_code_folder = task_code_folder;  
        
    }          
    
    /*
     * Notify in email to the user and create the end_flag
     */
    @Override
    public void executeCallBackOfTask() 
    {    
        if(this.error_sw != null){
            if(!this.error_sw.toString().equals("")){
                AuthorizationManager.notifyResultError(this.user, this.task_code, error_sw.toString());
            }
        }
        Mailer.notifyResultsOfTask(user, pass, task_code, email, "websearcher", "This kind of task has not feedback document associated, please report any problem using this email.");  
        
        synchronized(AuthorizationManager.getLocker(user))
        {
            try 
            {
                (new File(task_code_folder + File.separator + AuthorizationManager.end_flag_file)).createNewFile();
            } 
            catch (IOException ex) 
            {
                ProjectLogger.LOGGER.error("Error creating " + AuthorizationManager.end_flag_file + "(" + task_code_folder + ")", ex);  //FIXME                
                AuthorizationManager.notifyResultError(this.user, this.task_code, "Error creating end notification flag.");
            }
        }
            
        super.executeCallBackOfTask();
    }      
}

