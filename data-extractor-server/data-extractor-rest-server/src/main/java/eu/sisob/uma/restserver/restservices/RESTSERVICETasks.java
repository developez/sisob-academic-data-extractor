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

import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.SystemManager;
import eu.sisob.uma.restserver.TaskManager;
import eu.sisob.uma.restserver.TheResourceBundle;
import eu.sisob.uma.restserver.services.communications.OutputAuthorizationResult;
import eu.sisob.uma.restserver.services.communications.OutputTaskStatus;
import eu.sisob.uma.restserver.services.communications.OutputTaskStatusList;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * REST Web Service
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
@Path("/tasks")
public class RESTSERVICETasks
{
    @Context
    private UriInfo context;

    /** Creates a new instance of HelloWorld */
    public RESTSERVICETasks() {

    }       
    
    /**
     * FIXME (OutputTaskStatus cannot report about the authentication, think about this).
     * Retrieves the state of the task
     * @param user 
     * @param pass      
     * @return  If the authorization is correct and a new code task
     */
    @GET
    @Produces("application/json")
    public OutputTaskStatusList getTasksList(@QueryParam("user") String user, @QueryParam("pass") String pass) 
    {           
        OutputTaskStatusList result = new OutputTaskStatusList();
        
        if(user == null) user = "";
        if(pass == null) pass = "";
        
        StringWriter message = new StringWriter();
        synchronized(user)
        {
            if(AuthorizationManager.validateAccess(user, pass, message))
            {
                List<String> task_codes = TaskManager.listTasks(user, pass);
                List<OutputTaskStatus> task_status_list = new ArrayList<OutputTaskStatus>();
                
                for(String task_code : task_codes)
                {
                    OutputTaskStatus task_status = TaskManager.getTaskStatus(user, pass, task_code, true, false, false);
                    task_status_list.add(task_status);
                }
                
                Collections.sort(task_status_list, new Comparator<OutputTaskStatus>() {

                    @Override
                    public int compare(OutputTaskStatus o1, OutputTaskStatus o2) {                        
                        int r = -1;                    
                        try {                            
                            Integer i1 = Integer.parseInt(o1.task_code);                            
                            r = i1.compareTo(Integer.parseInt(o2.task_code));
                        } catch(Exception ex) {
                            
                        }
                        return r;
                    }
                    
                });
                
                result.success = true;                
                result.task_status_list = task_status_list.toArray(new OutputTaskStatus[task_status_list.size()]);
            }
            else
            {
                result.success = false;
                result.task_status_list = null;
            }
                   
        }
        
        /*
        OutputTaskStatus r = new OutputTaskStatus();                    
        r.message = status.toString();
        r.message = message.toString();
        r.data = new_task_code;         
        */
        
        return result;
    }
}
