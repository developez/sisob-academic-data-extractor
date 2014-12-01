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

package eu.sisob.uma.restserver;

import static eu.sisob.uma.restserver.AuthorizationManager.TASKS_USERS_PATH;
import static eu.sisob.uma.restserver.AuthorizationManager.begin_flag_file;
import static eu.sisob.uma.restserver.AuthorizationManager.end_flag_file;
import static eu.sisob.uma.restserver.AuthorizationManager.error_flag_file;
import static eu.sisob.uma.restserver.AuthorizationManager.feedback_flag_file;
import static eu.sisob.uma.restserver.AuthorizationManager.kind_flag_file;
import static eu.sisob.uma.restserver.AuthorizationManager.params_flag_file;
import eu.sisob.uma.restserver.services.communications.InputParameter;

import eu.sisob.uma.restserver.services.communications.OutputTaskStatus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * This class cover the methos about tasks. This is dependent from AuthorizationManager.
 * 
 * Is important to know, that some method of this class working in a mutex code area of the AuthorizationManager mutex.
 * 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class TaskManager {
    
    /**
     * 
     *  Is possible that use thee locker of AuthorizationManager
     * @param user
     * @param pass
     * @param task_code
     * @param retrieveTiming 
     * @param retrieveDataUrls 
     * @param retrieveFeedback 
     * @return
     */
    public static OutputTaskStatus getTaskStatus(String user, String pass, String task_code, boolean retrieveTiming, boolean retrieveDataUrls, boolean retrieveFeedback)
    {
        boolean valid = true;
        OutputTaskStatus task_status = new OutputTaskStatus();
        task_status.name = task_code;
        task_status.task_code = task_code;
        
        if(user != null && pass != null && task_code != null)
        {
            StringWriter message = new StringWriter();
            if(AuthorizationManager.validateAccess(user, pass, message))
            {
                message.getBuffer().setLength(0);
                String code_task_folder = TASKS_USERS_PATH + File.separator + user + File.separator + task_code;            
                File file = new File(code_task_folder);
                if(file.exists())
                {       
                    /* Retrieve data created*/                                        
                    if(retrieveTiming)
                    {
                        task_status.date_created = (new SimpleDateFormat("yyyy.MM.dd G - HH:mm:ss")).format(new Date(file.lastModified()));
                        
                        File kind_file = new File(code_task_folder + File.separator + kind_flag_file);
                        if(kind_file.exists())
                        {  
                            try 
                            {                            
                                task_status.kind = FileUtils.readFileToString(kind_file);                                
                            } 
                            catch (FileNotFoundException ex) 
                            {
                                ProjectLogger.LOGGER.error("Error, cant read " + kind_file.getAbsolutePath());
                                task_status.kind = "none";
                            }
                            catch (IOException ex) 
                            {
                                ProjectLogger.LOGGER.error("Error, cant read " + kind_file.getAbsolutePath());
                                task_status.kind = "none";
                            }
                        }
                        else
                        {
                            task_status.kind = "none";
                        }
                    }
                    
                    File begin_file = new File(code_task_folder + File.separator + begin_flag_file);
                    if(begin_file.exists())
                    {  
                        if(retrieveTiming)
                        {
                            task_status.date_started = (new SimpleDateFormat("yyyy.MM.dd G - HH:mm:ss")).format(new Date(begin_file.lastModified()));
                        }
                        
                        File end_file = new File(code_task_folder + File.separator + end_flag_file);
                        
                        if(end_file.exists())
                        {
                            if(retrieveTiming)
                            {
                                task_status.date_finished = (new SimpleDateFormat("yyyy.MM.dd G - HH:mm:ss")).format(new Date(end_file.lastModified()));
                            }
                            
                            valid = true;
                            task_status.status = (OutputTaskStatus.TASK_STATUS_EXECUTED);            
                            task_status.message = (TheResourceBundle.getString("Jsp Task Executed Msg"));                            
                            
                            if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
                            {
                                if(retrieveDataUrls)
                                {               
                                    task_status.result = "";
                                    List<String> fresults = AuthorizationManager.getResultFiles(user, task_code);

                                    for(String fresult : fresults)
                                    {
                                        String file_url = AuthorizationManager.getGetFileUrl(user, pass, task_code, fresult, "results");
                                        task_status.result += fresult + ";" + file_url + ";";
                                    }

                                    task_status.source = "";
                                    List<String> fsources = AuthorizationManager.getSourceFiles(user, task_code);

                                    for(String fsource : fsources)
                                    {
                                        String file_url = AuthorizationManager.getGetFileUrl(user, pass, task_code, fsource, "");
                                        task_status.source += fsource + ";" + file_url + ";";
                                    }
                                    //Also do method to build path
                                    task_status.verbose = "";
                                    List<String> fverboses = AuthorizationManager.getVerboseFiles(user, task_code);

                                    for(String fverbose : fverboses)
                                    {
                                        String file_url = AuthorizationManager.getGetFileUrlToShow(user, pass, task_code, fverbose, "verbose");
                                        task_status.verbose += fverbose + ";" + file_url + ";";
                                    }
                                    
                                    File errors_file = new File(code_task_folder + File.separator + AuthorizationManager.results_dirname + File.separator + error_flag_file);
                                    if(errors_file.exists())
                                    {
                                        try {
                                            task_status.errors = FileUtils.readFileToString(errors_file);
                                        } catch (IOException ex) {
                                            ProjectLogger.LOGGER.error("Error, cant read " + errors_file.getAbsolutePath());
                                            task_status.errors = "Error, cant read " + errors_file.getName();
                                        }
                                    } 
                                    
                                    File params_file = new File(code_task_folder + File.separator + params_flag_file);
                        
                                    task_status.params = "";
                                    List<InputParameter> params_list = new ArrayList<InputParameter>();
                                    if(params_file.exists())
                                    {
                                        try {
                                            String params = FileUtils.readFileToString(params_file);
                                            if(!params.equals("")){
                                                String[] lines = params.split("\r\n");
                                                
                                                for(String line : lines){
                                                    String[] values = line.split("\\$");
                                                    if(values.length > 1){
                                                        if(!task_status.params.equals(""))
                                                            task_status.params += ";";
                                                        
                                                        task_status.params += values[0] + ";" + values[1];                                                       
                                                    }
                                                }
                                                task_status.params = task_status.params;
                                            }
                                            
                                        } catch (IOException ex) {
                                            ProjectLogger.LOGGER.error("Error, cant read " + params_file.getAbsolutePath());
                                            task_status.errors = "Error, cant read " + params_file.getName();
                                        }
                                    }
                                }          
                                
                                if(retrieveFeedback)
                                {
                                    File feedback_file = new File(code_task_folder + File.separator + AuthorizationManager.results_dirname + File.separator + feedback_flag_file);
                        
                                    String feedback = "";
                                    
                                    if(feedback_file.exists())
                                    {                                        
                                        try {
                                            feedback = FileUtils.readFileToString(feedback_file);
                                        } catch (IOException ex) {
                                            ProjectLogger.LOGGER.error("Error, cant read " + feedback_file.getAbsolutePath());
                                        }
                                    }
                                    
                                    task_status.feedback = feedback;
                                }
                            }  
                            
                            
                            
                        }                            
                        else
                        {
                            valid = true;
                            task_status.status = (OutputTaskStatus.TASK_STATUS_EXECUTING);            
                            task_status.message = (TheResourceBundle.getString("Jsp Task Executing Msg"));
                        }                        
                    }
                    else
                    {
                        valid = true;
                        task_status.status = (OutputTaskStatus.TASK_STATUS_TO_EXECUTE);            
                        task_status.message = (TheResourceBundle.getString("Jsp Task To Execute Msg"));
                    }                   
                    
                }
                else
                {
                    valid = false;
                    task_status.status = (OutputTaskStatus.TASK_STATUS_NO_ACCESS);   
                    task_status.message = (TheResourceBundle.getString("Jsp Task Unknowed Msg"));
                }
            }
            else
            {
                valid = false;
                task_status.status = (OutputTaskStatus.TASK_STATUS_NO_AUTH);
                task_status.message = message.toString();
            }            
        }
        else
        {
            valid = false;
            task_status.status = (OutputTaskStatus.TASK_STATUS_NO_AUTH);            
            task_status.message = (TheResourceBundle.getString("Jsp Params Invalid Msg"));
        }
        
        return task_status;
    }  
    
   /**
     * Create a new folder for a new task if is possible to launch new task
     * Note:
     *  use MAX_TASKS_PER_USER and validateAccess()
     *  Is possible that use thee locker of AuthorizationManager
     * @param user
     * @param pass
     * @param status
     * @param message 
     * @return
     */
    public static String prepareNewTask(String user, String pass, StringWriter status, StringWriter message)
    {
        String new_folder_name = "";        
        if(message == null) return "";        
        message.getBuffer().setLength(0);
        
        if(user != null && pass != null)
        {
            if(AuthorizationManager.validateAccess(user, pass, message))
            {
                message.getBuffer().setLength(0);
                List<String> task_code_list = TaskManager.listTasks(user, pass);                
                
                int num_tasks_alive = 0;
                int max = -1;
                if(task_code_list.size() > 0)
                {
                    for(String task_code : task_code_list)
                    {
                        OutputTaskStatus task_status = TaskManager.getTaskStatus(user, pass, task_code, false, false, false);
                        if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
                        {

                        }
                        else
                        {
                            //Think about it
                            num_tasks_alive++;
                        }
                        
                        try{
                            int act = Integer.parseInt(task_code);
                            if(max < act){
                                max = act;
                            }
                        }catch(Exception ex){
                            
                        }
                            
                    }
                }                    

                if(num_tasks_alive < AuthorizationManager.MAX_TASKS_PER_USER)
                {   
                    new_folder_name = String.valueOf(max + 1);                    
                    
                    String code_task_folder = TASKS_USERS_PATH + File.separator + user + File.separator + new_folder_name;
                    File task_dir = new File(code_task_folder);
                    if(!task_dir.exists())
                    {
                        task_dir.mkdir();
                        status.append(OutputTaskStatus.TASK_STATUS_TO_EXECUTE);
                        if(message != null) message.append("A new task has been created successfully."); //FIXME
                    }
                    else
                    {
                        new_folder_name = "";
                        status.append(OutputTaskStatus.TASK_STATUS_NO_ACCESS);
                        if(message != null) message.append("Error creating place for the new task."); //FIXME
                    }
                }
                else
                {                        
                     new_folder_name = "";
                     status.append(OutputTaskStatus.TASK_STATUS_EXECUTING);
                     if(message != null) message.append("There are still tasks running or there are a task created ready to be launched."); //FIXME
                }                    
                
            }
            else
            {
                new_folder_name = "";
                status.append(OutputTaskStatus.TASK_STATUS_NO_AUTH);                
            }
        }
        else
        {   
            new_folder_name = "";
            status.write(OutputTaskStatus.TASK_STATUS_NO_AUTH);
            if(message != null) message.write(TheResourceBundle.getString("Jsp Params Invalid Msg"));
        }

        return new_folder_name;
    }
   
            
    /**
     * Return the task codes of a user
     * 
     *  Is possible that use thee locker of AuthorizationManager
     * 
     * @param user
     * @param pass
     * @return
     */
    public static List<String> listTasks(String user, String pass)
    {
        List<String> results = new ArrayList<String>();
        
        String result_code_task_folder = TASKS_USERS_PATH + File.separator + user;
            
        File result_file = new File(result_code_task_folder);
        if(result_file.exists())
        {   
            List<String> tasks_folders = Arrays.asList(result_file.list());
            for(String folder : tasks_folders)
            {
                results.add(folder);
            }                    
        }
        else
        {
            
        }

        return results;   
    }
            
}
