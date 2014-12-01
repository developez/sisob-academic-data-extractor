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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.ProjectLogger;
import eu.sisob.uma.restserver.TaskManager;
import eu.sisob.uma.restserver.TheResourceBundle;
import eu.sisob.uma.restserver.services.communications.OutputAuthorizationResult;
import eu.sisob.uma.restserver.services.communications.OutputTaskStatus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.ws.rs.Consumes;
import org.apache.log4j.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


/**
 * 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
@Path("/file")
public class RESTSERVICEFile {
    
    @GET
    @Path("/show")
    public Response showFile(@QueryParam("user") String user, @QueryParam("pass") String pass, @QueryParam("task_code") String task_code, @QueryParam("file") String file, @QueryParam("type") String type)  
    {        
        Response response = null;
                
        //Security
        if(file.contains("\\") || file.contains("/"))
        {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return response;
        }
        
        StringWriter message = new StringWriter();

        if(AuthorizationManager.validateAccess(user, pass, message))
        {   
            String fpath = AuthorizationManager.TASKS_USERS_PATH + File.separator + 
                           user + File.separator +
                           task_code + File.separator + 
                           (!type.equals("") ? type + File.separator : "") + 
                            file;        
            File f = new File(fpath);

            byte[] docStream = null;
            
            try {
                docStream = org.apache.commons.io.FileUtils.readFileToByteArray(f);
                response = Response
                        .ok(docStream, MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Type","text/html; charset=utf-8")
                        .build();

            } catch (FileNotFoundException ex) {
                ProjectLogger.LOGGER.error("FIXME", ex);   
                response = Response.status(Response.Status.NOT_FOUND).build();            
            } catch (Exception ex) {
                ProjectLogger.LOGGER.error("FIXME", ex);   
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }        
        }
        else
        {
            response = Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        return response;
    }
        
    @GET
    @Path("/download")
    public Response getFile(@QueryParam("user") String user, @QueryParam("pass") String pass, @QueryParam("task_code") String task_code, @QueryParam("file") String file, @QueryParam("type") String type)  
    {        
        Response response = null;
                
        //Security
        if(file.contains("\\") || file.contains("/"))
        {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return response;
        }
        
        StringWriter message = new StringWriter();

        if(AuthorizationManager.validateAccess(user, pass, message))
        {   
            String fpath = AuthorizationManager.TASKS_USERS_PATH + File.separator + 
                           user + File.separator +
                           task_code + File.separator + 
                           (!type.equals("") ? type + File.separator : "") + 
                            file;        
            File f = new File(fpath);

            byte[] docStream = null;
            
            try {
//                docStream = org.apache.commons.io.FileUtils.readFileToByteArray(f);
//                response = Response
//                        .ok(docStream, MediaType.APPLICATION_OCTET_STREAM)
//                        .header("content-disposition","attachment; filename = " + f.getName())
//                        .build();                
                
		response =  Response.ok((Object) f)
                            .header("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"")
                            .build();

            //} catch (FileNotFoundException ex) {
            //    ProjectLogger.LOGGER.error("FIXME", ex);   
            //    response = Response.status(Response.Status.NOT_FOUND).build();            
            } catch (Exception ex) {
                ProjectLogger.LOGGER.error("FIXME", ex);   
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }        
            
            // Alternative http://www.mkyong.com/webservices/jax-rs/download-pdf-file-from-jax-rs/
            // http://stackoverflow.com/questions/7106775/how-to-download-large-files-without-memory-issues-in-java
        }
        else
        {
            response = Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        return response;
    }
    
    @GET
    @Path("/delete")    
    public OutputAuthorizationResult deleteFile(@QueryParam("user") String user, @QueryParam("pass") String pass, @QueryParam("task_code") String task_code, @QueryParam("file") String file)  
    {        
        OutputAuthorizationResult r = new OutputAuthorizationResult();
        
        //Security
        if(file.contains("\\") || file.contains("/"))
        {
            r.success = false;
            r.message = "Do not try shit bro";
        }
        
        StringWriter message = new StringWriter();
        if(AuthorizationManager.validateAccess(user, pass, message))
        {
            String fpath = AuthorizationManager.TASKS_USERS_PATH + File.separator + user + File.separator + task_code + File.separator + File.separator + file;        
            File f = new File(fpath);
            if(f.exists())
            {
                try
                {
                    f.delete();
                    r.success = true;
                }
                catch(Exception ex)
                {
                    Logger.getLogger("root").error("Error deleting file (" + f.getAbsolutePath() + ")");
                    r.success = false;
                }
            }
            else
            {
                r.success = false;
            }
        }
        else
        {
            r.success = false;
            r.message = message.toString();
        }     
        
        return r;
    }            
    
    
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
                                @FormDataParam("user") String user,
                                @FormDataParam("pass") String pass,
                                @FormDataParam("task_code") String task_code,            
                                @FormDataParam("files[]") InputStream uploadedInputStream,
                                @FormDataParam("files[]") FormDataContentDisposition fileDetail
                              ) 
    {
        Response response = null;

        JSONArray json = new JSONArray();
        if(uploadedInputStream != null && fileDetail != null && user != null && pass != null && task_code != null)
        {
            OutputTaskStatus task_status = TaskManager.getTaskStatus(user, pass, task_code, false, false, false);

            if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
            {
                //Security
                if(fileDetail.getFileName().contains("\\") || fileDetail.getFileName().contains("/"))
                {
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    return response;
                }
                
                File file = new File(AuthorizationManager.TASKS_USERS_PATH + File.separator + user + File.separator + task_code, fileDetail.getFileName());                
                
                long size = 0;
                OutputStream out = null;
                int read = 0;
                byte[] bytes = new byte[1024];
                JSONObject jsono = new JSONObject();                    
                
                try 
                {                    
                    out = new FileOutputStream(file);
                    while ((read = uploadedInputStream.read(bytes)) != -1) {
                            size += read;
                            out.write(bytes, 0, read);
                    }            
                    
                    jsono.put("name", fileDetail.getFileName());
                    jsono.put("size", size);
                    jsono.put("url", AuthorizationManager.getGetFileUrl(user, pass, task_code, fileDetail.getFileName(), ""));
                    jsono.put("thumbnail_url", "");
                    jsono.put("delete_url", AuthorizationManager.getDeleteFileUrl(user, pass, task_code, fileDetail.getFileName(), ""));
                    jsono.put("delete_type", "GET");
                    json.put(jsono);
                    
                    response = Response.status(200).entity(json.toString()).build();
                }                 
                catch (IOException e) 
                {
                    Logger.getLogger("root").error("Error uploading file (" + file.getAbsolutePath() + ")");
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(TheResourceBundle.getString("Upload Fail")).build();
                }     
                catch (JSONException ex) 
                {
                    Logger.getLogger("root").error("Error generatin json (" + file.getAbsolutePath() + ")");
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(TheResourceBundle.getString("Upload Fail")).build();
                } 
                finally
                {
                    try
                    {
                        out.flush();
                        out.close();
                        out = null;
                        System.gc();
                    }
                    catch (IOException e)
                    {          
                        Logger.getLogger("root").error("Error closing file (" + file.getAbsolutePath() + ")");
                        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(TheResourceBundle.getString("Upload Fail")).build();
                    }
                }    
            }
            else
            {
                response = Response.status(Response.Status.PRECONDITION_FAILED).entity(task_status.message).build();
            }
        }
        else
        {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(TheResourceBundle.getString("Upload Fail")).build();            
        }
        // save it
        //writeToFile(uploadedInputStream, uploadedFileLocation);
        return response;
        
    }
}
