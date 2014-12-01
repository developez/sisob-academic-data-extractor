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
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTask;
import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskWithResource;
import eu.sisob.uma.extractors.adhoc.cvfilesinside.InternalCVFilesExtractorTask;
import eu.sisob.uma.extractors.adhoc.email.ProjectLogger;
import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.Mailer;
import java.io.File;
import java.io.IOException;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class InternalCVFilesExtractorTaskInRest extends InternalCVFilesExtractorTask
{       
    String user;    
    String pass;    
    String email;    
    String task_code;
    String task_code_folder;     
    
    /*
     * @param document - xml document with the information     
     */    
    public InternalCVFilesExtractorTaskInRest(String user, String task_code, String task_code_folder, String email, File input_file, File data_dir, File output_file, File results_dir, File zip_output_file, File output_file_2)
    {   
        super(input_file, data_dir, output_file, results_dir, zip_output_file, output_file_2);
        
        this.user = user;
        this.pass = pass;
        this.email = email;
        this.task_code = task_code;
        this.task_code_folder = task_code_folder;  
        
    }   
   
    @Override
    public void executeCallBackOfTask() 
    {            
        Mailer.notifyResultsOfTask(user, pass, task_code, email, "", "This kind of task has not feedback document associated, please report any problem using this email.");  
        
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

