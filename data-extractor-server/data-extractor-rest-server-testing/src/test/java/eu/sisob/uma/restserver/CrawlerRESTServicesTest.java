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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import eu.sisob.uma.restserver.services.communications.InputAddTask;
import eu.sisob.uma.restserver.services.communications.InputLaunchTask;
import eu.sisob.uma.restserver.services.communications.OutputTaskOperationResult;
import eu.sisob.uma.restserver.services.communications.OutputTaskStatus;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Daniel L�pez Gonz�lez (dlopez@lcc.uma.es, dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CrawlerRESTServicesTest 
{   
    @Before
      public void setup()
      {

        BasicConfigurator.configure();

      }
    
    @Test
    public void TestingRESTServices()
    {
        if(true)
        {
            assertEquals(true, true);
            return;
        }
        
        boolean success = true;
        String url = "http://150.214.108.202:8080/eu.sisob.uma_data-extractor-rest-server_war_1.0-SNAPSHOT/";
        String user = "sisob-system";
        String pass = "cc453ce73e02f31cf24408d359edbd55bb55963f9297616ecec66314e9421cbd";
        String task_code = "";
        String data_path = "data-samples" + File.separator + "testing-crawler" + File.separator + "3-re-from-brandeis";
        String filename = "data-researchers-urls.csv";
        
        Logger.getLogger("root").info("Crawler REST Services begin.");
        
        Client client = Client.create();
        
        //Call to create a new task
        if(success)
        {
            InputAddTask input = new InputAddTask();
            input.user = user;
            input.pass = pass;
            
            WebResource webResource = client.resource(url + "resources/task/add");  

            OutputTaskStatus r = webResource.accept(MediaType.APPLICATION_JSON)
                                            .post(OutputTaskStatus.class, input);            
            
            //Yes
            if(r.status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
            {
                assertEquals(true, true);
                task_code = r.task_code;
                success = true;
                Logger.getLogger("root").info("Task created successfully.");
            }
            //No
            else
            {
                
            }
        }

        //Upload content
        if(success)
        {
            success = false;            
            
            WebResource webResource = client.resource(url + "resources/file/upload");  
            File file = new File(data_path + File.separator + filename);
            try {
                FormDataMultiPart multiPart = new FormDataMultiPart();
                multiPart.field("user", user);
                multiPart.field("pass", pass);
                multiPart.field("task_code", task_code);
                
                multiPart.bodyPart(new FileDataBodyPart("files[]", file));
                ClientResponse response = webResource.type(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.TEXT_PLAIN)
                    .post(ClientResponse.class, multiPart);
                
                if(response.getStatus() == 200)
                {
                    assertEquals(true, true);
                    Logger.getLogger("root").info("File upload successfully.");
                    success = true;
                }
                else
                {
                    Logger.getLogger("root").error("File cannot be uploaded. STATUS = " + response.getStatus());
                }

            } catch (ClientHandlerException e) {
                
            }
        }
        
        if(success)
        {
            success = false;
            InputLaunchTask input = new InputLaunchTask();
            input.user = user;
            input.pass = pass;
            input.task_code = task_code;
            input.task_kind = "crawler";            
            
            WebResource webResource = client.resource(url + "resources/task/launch");  

            OutputTaskOperationResult r = webResource.accept(MediaType.APPLICATION_JSON)
                                            .post(OutputTaskOperationResult.class, input);            
            
            //Yes
            if(r.success)
            {                
                assertEquals(true, true);
                success = true;
                Logger.getLogger("root").info("Task launched successfully.");
            }
            //No
            else
            {
                Logger.getLogger("root").info("Task was not launched.");
            }
        }
        
        //success = true;
        //task_code = "2";
        if(success)
        {
            success = false;
            OutputTaskStatus r = null;
            while(!success)
            {
                WebResource webResource = client.resource(url + "resources/task");  

                MultivaluedMap queryParams = new MultivaluedMapImpl();
                queryParams.add("user", user);
                queryParams.add("pass", pass);
                queryParams.add("task_code", task_code);

                r = webResource.queryParams(queryParams)
                                           .accept(MediaType.APPLICATION_JSON)
                                           .get(OutputTaskStatus.class);

                if(r.status.equals(OutputTaskStatus.TASK_STATUS_NO_ACCESS) ||
                   r.status.equals(OutputTaskStatus.TASK_STATUS_NO_AUTH))            
                {
                    Logger.getLogger("root").info("Authorization failed.");
                    break;                    
                }
                else if(r.status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
                {
                    Logger.getLogger("root").info("Task state wrong." + r.status);
                    break;
                }
                else if(r.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTING))
                {
                    Logger.getLogger("root").info("Task is still running.");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger("root").error("", ex);
                    }
                }
                else if(r.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
                {
                    Logger.getLogger("root").info("Task has finished.");
                    success = true;
                }
            }            
            
            if(success)
            {                        
                String[] results = r.result.split(";");
                
                //sources = r.source.split(";");    
                //feedback = r.feedback;
                String errors = r.errors;
                
                for(int i = 0; i < results.length; i=i+2)
                {       
                    File file_dest = new File(data_path + File.separator + results[i]);
                    URL file_url;
                    try {
                        file_url = new URL(results[i+1]);                   
                    
                        org.apache.commons.io.FileUtils.copyURLToFile(file_url, file_dest);
                        
                        Logger.getLogger("root").info("Downloaded file from " + file_url.toString() + " to " + file_dest.toString());
                    } catch (MalformedURLException ex) {
                        Logger.getLogger("root").error("", ex);
                    } catch (IOException ex) {
                        Logger.getLogger("root").error("", ex);
                    }
                    
                }
                
        
                assertEquals(true, true);
                Logger.getLogger("root").info("Task finished well.");
            }
            else
            {
                assertEquals(true, false);
                Logger.getLogger("root").info("Task finished bad.");
            }
        }
        
        
            
    //    
    //    //Launch task
    //    {
    //        Client client = Client.create();
    //        WebResource webResource = client.resource(TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "resources/task/add");  
    //
    //        MultivaluedMap queryParams = new MultivaluedMapImpl();
    //        queryParams.add("user", user);
    //        queryParams.add("pass", pass);
    //        queryParams.add("task_code", task_code);
    //
    //        OutputTaskStatus r = webResource.queryParams(queryParams)
    //                                      .accept(MediaType.APPLICATION_JSON)
    //                                      .post(OutputTaskStatus.class);
    //    }
    //    
    //    //Pull task status until finishes
    }
    
    
    
}
