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

package eu.sisob.uma.restserver.restservices;

import eu.sisob.uma.NPL.Researchers.GateDataExtractorSingle;
import eu.sisob.uma.NPL.Researchers.GateDataExtractorTask;
import eu.sisob.uma.api.prototypetextmining.RepositoryPreprocessDataMiddleData;
import eu.sisob.uma.extractors.adhoc.websearchers.WebSearchersExtractor;
import eu.sisob.uma.restserver.services.communications.OutputTaskOperationResult;
import eu.sisob.uma.restserver.services.communications.OutputTaskStatus;

import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.ProjectLogger;
import eu.sisob.uma.restserver.SystemManager;
import eu.sisob.uma.restserver.TaskManager;
import eu.sisob.uma.restserver.TheConfig;
import eu.sisob.uma.restserver.TheResourceBundle;
import eu.sisob.uma.restserver.services.communications.InputAddTask;
import eu.sisob.uma.restserver.services.communications.InputLaunchTask;
import eu.sisob.uma.restserver.services.communications.InputParameter;
import eu.sisob.uma.restserver.services.communications.InputUpdateFeedback;
import eu.sisob.uma.restserver.services.communications.TasksParams;
import eu.sisob.uma.restserver.services.crawler.CrawlerTask;
import eu.sisob.uma.restserver.services.email.EmailTask;
import eu.sisob.uma.restserver.services.gate.GateTask;
import eu.sisob.uma.restserver.services.internalcvfiles.InternalCVFilesTask;
import eu.sisob.uma.restserver.services.websearchers.WebSearcherCVTask;
import eu.sisob.uma.restserver.services.websearchers.WebSearcherTask;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * REST Web Service
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
@Path("/task")
public class RESTSERVICETask {

    @Context
    private UriInfo context;

    /** Creates a new instance of HelloWorld */
    public RESTSERVICETask() 
    {
        
    }

    /**
     * Retrieves the state of the task
     * @param user 
     * @param pass 
     * @param task_code 
     * @return an instance of CrawlerTaskStatus with code provided
     */
    @GET
    @Produces("application/json")
    public OutputTaskStatus getTaskStatus(@QueryParam("user") String user, @QueryParam("pass") String pass, @QueryParam("task_code") String task_code) 
    {
        OutputTaskStatus task_status = null;                             
        
        synchronized (AuthorizationManager.getLocker(user)) {                

            task_status = TaskManager.getTaskStatus(user, pass, task_code, true, true, true);  
        }
                       
        return task_status;        
    }
    
    @POST
    @Produces("application/json")    
    @Path("/add")
    public OutputTaskStatus addNewTask(InputAddTask input) 
    {        
        StringWriter status = new StringWriter();        
        StringWriter message = new StringWriter();    
        
        String new_task_code = "";
        synchronized(AuthorizationManager.getLocker(input.user))
        {
            new_task_code = TaskManager.prepareNewTask(input.user, input.pass, status, message);
        }
        
        OutputTaskStatus r = new OutputTaskStatus();            
        
        r.status = status.toString();
        r.message = message.toString();
        r.task_code = new_task_code;
        
        return r;
    }   
    
    @POST
    @Produces("application/json")    
    @Path("/delete")
    public OutputTaskOperationResult deleteTask(InputLaunchTask input) 
    {        
        OutputTaskOperationResult result = new OutputTaskOperationResult();            
        
        synchronized (AuthorizationManager.getLocker(input.user)) 
        {                
            OutputTaskStatus task_status = TaskManager.getTaskStatus(input.user, input.pass, input.task_code, false, false, false);            
            
            result.message = task_status.message.toString();
            if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_NO_AUTH))            
            {
                result.success = false;                 
                result.message = "The task couldn't be deleted. " + task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_NO_ACCESS))            
            {
                result.success = false;                    
                result.message = "The task couldn't be deleted. " + task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTING))
            {
                result.success = false;              
                result.message = "The task couldn't be deleted. " + task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
            {
                result.success = false;              
                result.message = "The task couldn't be deleted. " + task_status.message;                
            }            
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
            { 
                String code_task_folder = AuthorizationManager.TASKS_USERS_PATH + File.separator + input.user + File.separator + input.task_code;
                File dir_to_delete = new File(code_task_folder);
                try{
                    FileUtils.deleteDirectory(dir_to_delete);   
                    if(dir_to_delete.exists()){
                        result.success = false;              
                        result.message = "The task couldn't be deleted"; // FIXME;
                    }else{
                        result.success = true;              
                        result.message = "The task " + input.task_code + " has been deleted";
                    }
                }catch(Exception ex){
                    result.success = false;              
                    result.message = ""; // FIXME;
                    ProjectLogger.LOGGER.error("Error deleting task " + input.task_code, ex); //FIXME
                }
            }
        }
        
        return result;
    }    
    
    /**
     * POST method for updating or creating an instance of HelloWorld
     * @param input 
     * @return an instance of RESTResult
     */
    @POST    
    @Produces("application/json")
    @Path("/relaunch")
    public OutputTaskOperationResult relaunchTask(InputLaunchTask input) 
    {        
        OutputTaskOperationResult result = new OutputTaskOperationResult();            
        
        synchronized (AuthorizationManager.getLocker(input.user)) 
        {                
            OutputTaskStatus task_status = TaskManager.getTaskStatus(input.user, input.pass, input.task_code, false, false, false);            
            
            result.message = task_status.message.toString();
            if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_NO_AUTH))            
            {
                result.success = false;                 
                result.message = task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_NO_ACCESS))            
            {
                result.success = false;                    
                result.message = task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTING))
            {
                result.success = false;              
                result.message = task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
            {
                result.success = false;              
                result.message = task_status.message;
                
            }            
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
            { 
                String code_task_folder = AuthorizationManager.TASKS_USERS_PATH + File.separator + input.user + File.separator + input.task_code;           
                
                try {
                       File params_file = (new File(code_task_folder + File.separator + AuthorizationManager.params_flag_file));
                       
                       List<String> params = FileUtils.readLines(params_file);

                       input.parameters = new InputParameter[params.size()];
                       
                       int i = 0;
                       for(String l : params)
                       {
                           String[] values = l.split("\\$");
                           if(values.length == 2){
                            InputParameter ip = new InputParameter();
                            ip.key = values[0];
                            ip.value = values[1];
                            input.parameters[i] = ip;
                            i++;                            
                           }                           
                       }                            
    
                } catch (Exception ex) {
                       ProjectLogger.LOGGER.error("Error reading params filename " + AuthorizationManager.params_flag_file + "(" + code_task_folder + ")", ex); //FIXME
                }   
                
                if(input.task_kind.equals("crawler"))
                {                       
                    StringWriter message = new StringWriter();

                    result.success = CrawlerTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message);                    
                    result.message = message.toString();   
                
                } else if(input.task_kind.equals("websearcher")) {                       
                    
                    StringWriter message = new StringWriter();
                    
                    WebSearchersExtractor.SearchPatterns pattern = WebSearchersExtractor.SearchPatterns.P2;
                    String value_mode = null;
                    
                    try{
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P1, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P1;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P2, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P2;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P3, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P3;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P4, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P4;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P5, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P5;
                        
                    }catch(Exception ex){
                        pattern = WebSearchersExtractor.SearchPatterns.P2;
                    }
                    
                    
                    result.success = WebSearcherTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, pattern, message);                    
                    
                    result.message = message.toString();                       
                
                } else if(input.task_kind.equals("websearcher_cv")) {                       
                    
                    StringWriter message = new StringWriter();

                    result.success = WebSearcherCVTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message);                    
                    result.message = message.toString();      
                    
                } else if(input.task_kind.equals("internalcvfiles")) {                       

                    StringWriter message = new StringWriter();
                    
                    result.success = InternalCVFilesTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message);                    
                    result.message = message.toString();                      
                
                } else if(input.task_kind.equals("email")) {                       

                    StringWriter message = new StringWriter();
                    
                    String value_filters = InputParameter.get(TasksParams.PARAM_EMAIL_FILTERS, input.parameters);
                    List<String> filters = new ArrayList<String>();
                    if(value_filters != null && !value_filters.equals(""))
                    {
                        String[] filters_string = value_filters.split(",");
                        for(String filter : filters_string){
                            filters.add(filter.trim());
                        }                     
                    }
                    
                    result.success = EmailTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, filters, message);                    
                    result.message = message.toString();                      
                
                } else if(input.task_kind.equals("gate")) {   
                    
                    StringWriter message = new StringWriter();
                    
                    boolean verbose = false;
                    String value_verbose = null;
                    try{
                        value_verbose = InputParameter.get(TasksParams.PARAM_GATE_VERBOSE, input.parameters);
                        if(value_verbose != null && value_verbose.equals(TasksParams.PARAM_TRUE))
                            verbose = true;
                    }catch(Exception ex){
                        verbose = true;
                    }
                    
                    boolean split = false;
                    String value_split = null;
                    try{
                        value_split = InputParameter.get(TasksParams.PARAM_GATE_SPLIT, input.parameters);
                        if(value_split != null && value_split.equals(TasksParams.PARAM_TRUE))
                            split = true;
                    }catch(Exception ex){
                        split = false;
                    }
                    
                    result.success = GateTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message, verbose, split);
                    result.message = message.toString();      
                }                
                else
                {
                    result.success = false;
                    result.message = TheResourceBundle.getString("Jsp Task Unknowed Msg");
                }
                
                /*
                 * Delete delete flags
                 */
                if(result.success)
                {
                    try {
                        (new File(code_task_folder + File.separator + AuthorizationManager.begin_flag_file)).delete();
                    } catch (Exception ex) {
                        ProjectLogger.LOGGER.error("Error deleting " + AuthorizationManager.begin_flag_file + "(" + code_task_folder + ")", ex); //FIXME
                    }                    
                    
                    try {                        
                        (new File(code_task_folder + File.separator + AuthorizationManager.begin_flag_file)).createNewFile();
                    } catch (IOException ex) {
                        ProjectLogger.LOGGER.error("Error creating " + AuthorizationManager.begin_flag_file + "(" + code_task_folder + ")", ex); //FIXME
                    }
                    
                    try {
                        (new File(code_task_folder + File.separator + AuthorizationManager.end_flag_file)).delete();
                    } catch (Exception ex) {
                        ProjectLogger.LOGGER.error("Error deleting " + AuthorizationManager.end_flag_file + "(" + code_task_folder + ")", ex); //FIXME
                    }                  
                                        
                }   
            }
        }
        
        return result;
    }

    /**
     * POST method for updating or creating an instance of HelloWorld
     * @param input 
     * @return an instance of RESTResult
     */
    @POST    
    @Produces("application/json")
    @Path("/launch")
    public OutputTaskOperationResult launchTask(InputLaunchTask input) 
    {        
        OutputTaskOperationResult result = new OutputTaskOperationResult();            
        
        synchronized (AuthorizationManager.getLocker(input.user)) 
        {                
            OutputTaskStatus task_status = TaskManager.getTaskStatus(input.user, input.pass, input.task_code, false, false, false);
            
            result.message = task_status.message.toString();
            if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_NO_AUTH))            
            {
                result.success = false;                 
                result.message = task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_NO_ACCESS))            
            {
                result.success = false;                    
                result.message = task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTING))
            {
                result.success = false;              
                result.message = task_status.message;
            }
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
            {
                result.success = false;              
                result.message = task_status.message;
                
            }            
            else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
            {     
                String code_task_folder = AuthorizationManager.TASKS_USERS_PATH + File.separator + input.user + File.separator + input.task_code;                 

                if(input.task_kind.equals("crawler"))
                {                       
                    StringWriter message = new StringWriter();

                    result.success = CrawlerTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message);                    
                    result.message = message.toString();                        
                
                } else if(input.task_kind.equals("websearcher")) {                       
                    
                    StringWriter message = new StringWriter();
                    
                    WebSearchersExtractor.SearchPatterns pattern = WebSearchersExtractor.SearchPatterns.P2;
                    String value_mode = null;
                    
                    try{
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P1, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P1;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P2, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P2;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P3, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P3;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P4, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P4;
                        
                        value_mode = InputParameter.get(TasksParams.PARAM_CRAWLER_P5, input.parameters);
                        if(value_mode != null && value_mode.equals(TasksParams.PARAM_TRUE))
                            pattern = WebSearchersExtractor.SearchPatterns.P5;
                        
                    }catch(Exception ex){
                        pattern = WebSearchersExtractor.SearchPatterns.P2;
                    }
                    
                    
                    result.success = WebSearcherTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, pattern, message);                    
                    result.message = message.toString();                         
                
                } else if(input.task_kind.equals("websearcher_cv")) {                                   
                    
                    StringWriter message = new StringWriter();

                    result.success = WebSearcherCVTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message);                    
                    result.message = message.toString();      
                    
                } else if(input.task_kind.equals("internalcvfiles")) {                       

                    StringWriter message = new StringWriter();
                    
                    result.success = InternalCVFilesTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message);                    
                    result.message = message.toString();                      
                
                } else if(input.task_kind.equals("email")) {                    
                    
                    StringWriter message = new StringWriter();
                    
                    String value_filters = InputParameter.get(TasksParams.PARAM_EMAIL_FILTERS, input.parameters);
                    List<String> filters = new ArrayList<String>();
                    if(value_filters != null && !value_filters.equals(""))
                    {
                        String[] filters_string = value_filters.split(",");
                        for(String filter : filters_string){
                            filters.add(filter.trim());
                        }
                        //filters = Arrays.asList(filters_string);                        
                    }

                    result.success = EmailTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, filters, message);                    
                    result.message = message.toString();      
                    
                } else if(input.task_kind.equals("gate")) {                       
                    StringWriter message = new StringWriter();
                    
                    boolean verbose = false;
                    String value_verbose = null;
                    try{
                        value_verbose = InputParameter.get(TasksParams.PARAM_GATE_VERBOSE, input.parameters);
                        if(value_verbose != null && value_verbose.equals(TasksParams.PARAM_TRUE))
                            verbose = true;
                    }catch(Exception ex){
                        verbose = true;
                    }
                    
                    boolean split = false;
                    String value_split = null;
                    try{
                        value_split = InputParameter.get(TasksParams.PARAM_GATE_SPLIT, input.parameters);
                        if(value_split != null && value_split.equals(TasksParams.PARAM_TRUE))
                            split = true;
                    }catch(Exception ex){
                        split = false;
                    }
                    
                    result.success = GateTask.launch(input.user, input.pass, input.task_code, code_task_folder, input.user, message, verbose, split);
                    result.message = message.toString();      
                }                
                else
                {
                    result.success = false;
                    result.message = TheResourceBundle.getString("Jsp Task Unknowed Msg");
                }
                
                /*
                 * Notify in the folder that the task has been launched
                 */
                if(result.success)
                {
                    try {
                        (new File(code_task_folder + File.separator + AuthorizationManager.begin_flag_file)).createNewFile();
                    } catch (IOException ex) {
                        ProjectLogger.LOGGER.error("Error creating " + AuthorizationManager.begin_flag_file + "(" + code_task_folder + ")", ex); //FIXME
                    }

                    try {
                        FileUtils.writeStringToFile(new File(code_task_folder + File.separator + AuthorizationManager.kind_flag_file), input.task_kind);                                                        
                    } catch (IOException ex) {
                        ProjectLogger.LOGGER.error("Error creating " + AuthorizationManager.kind_flag_file + "(" + code_task_folder + ")", ex); //FIXME
                    }
                    
                    try {
                        File params_file = (new File(code_task_folder + File.separator + AuthorizationManager.params_flag_file));
                        params_file.createNewFile();
                        
                        FileUtils.write(params_file, "", "UTF-8", false);
                        if(input.parameters != null){
                            for(InputParameter ip : input.parameters){
                                FileUtils.write(params_file, ip.key + "$" + ip.value + "\r\n", "UTF-8", true);
                            }                            
                        }                          
                    } catch (Exception ex) {
                        ProjectLogger.LOGGER.error("Error creating params filename " + AuthorizationManager.params_flag_file + "(" + code_task_folder + ")", ex); //FIXME
                    }                    
                    
                }   
                
            }            
        }

        return result;
        
    }
}
