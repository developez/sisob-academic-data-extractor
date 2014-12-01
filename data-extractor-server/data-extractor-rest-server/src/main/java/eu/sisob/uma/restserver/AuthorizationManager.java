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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.io.FileUtils;

/**
 * TASK DESCRIPCION
 * 
 * Location of task physically:
 * 
 *   docroot + TASKS_FOLDER_NAME + TASKS_USERS_PATH + user + task_number
 * 
 *   Task folder:
 *   ..
 *   kind_flag_file         => File that contains the kind of the task
 *   begin_flag_file        => File that indicate if exits that the task has began
 *   end_flag_file          => File that indicate if exits that the task has ended
 * 
 *   results_dirname        => Dir. that contains the files resulted of the task
 *      ..
 *      feedback_flag_file  => File that contains the feedback of the task given from the user
 * 
 *   middle_data_dirname    => Dir. that contains the files generated in the task process
 *   
 * 
 *   
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class AuthorizationManager 
{
    /**
     * 
     */
    public static String TASKS_USERS_PATH;
    /**
     * 
     */
    public static final String TASKS_FOLDER_NAME = "sisob-tasks";
    
    static
    {        
        //TODO - Do in standar way, not in dirty explicit way        
        File dir = new File(System.getProperty("com.sun.aas.instanceRoot") + File.separator + "docroot" + File.separator + TASKS_FOLDER_NAME);
        if(!dir.exists()) dir.mkdir();        
        
        TASKS_USERS_PATH = dir.getAbsolutePath();
        
        dir = new File(dir.getAbsolutePath() + File.separator + "test-code");
        if(!dir.exists()) dir.mkdir();        
    }
    
    /**
     * 
     */
    public static final String begin_flag_file = "b.flag";
    
    /**
     * 
     */
    public static final String end_flag_file = "e.flag";
    
    /*
     * 
     */    
    /**
     *
     */
    public static final String feedback_flag_file = "feedback.flag";
    
    /*
     * 
     */    
    /**
     *
     */
    public static final String params_flag_file = "params.flag";
    
    /*
     * 
     */
    /**
     *
     */
    public static final String kind_flag_file = "kind.flag";
    
    /**
     * 
     */
    public static final String results_dirname = "results";
    
    /**
     * 
     */
    public static final String middle_data_dirname = "middle_data";
    
    /**
     * 
     */
    public static final String verbose_dirname = "verbose";
    
    /**
     * 
     */
    public static final String error_flag_file = "error.flag";
    
    private static final HashMap<String, Object> FILE_LOCKERS = new HashMap<String, Object>();
        
    /**
     *
     */
    public static final int MAX_TASKS_PER_USER = 5; 
    
    /**
     * 
     * @param code
     * @return
     */
    public static synchronized Object getLocker(String code)
    {
        if(!FILE_LOCKERS.containsKey(code))
            FILE_LOCKERS.put(code, new Object());
        
        return FILE_LOCKERS.get(code);            
    }
    
    private static boolean DBAuthorizeUserIn(String user, String pass)
    {
        boolean success = false;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try
        {   
            String query = "SELECT 1 FROM USERS WHERE user_email = ? and user_pass = ?";
            conn = SystemManager.getInstance().getSystemDbPool().getConnection();
            statement = conn.prepareStatement(query);
            statement.setString(1, user);
            statement.setString(2, pass);            
            
            rs = statement.executeQuery();
            if(rs.next()) 
                success = true;
            else
                success = false;
        }  
        catch (SQLException ex)                             
        {
            ProjectLogger.LOGGER.error("", ex);
            success = false;
        } 
        catch (Exception ex)                             
        {
            ProjectLogger.LOGGER.error("", ex);
            success = false;
        } 
        finally
        {
            if(rs != null) 
            try 
            {
                rs.close();
            } 
            catch (SQLException ex) 
            {
                ProjectLogger.LOGGER.error("", ex);
            }
            
            if(statement != null) 
            try 
            {                        
                statement.close();
            } 
            catch (SQLException ex) {
                ProjectLogger.LOGGER.error("", ex);
            }
            
            if(conn != null) 
            try 
            {
                conn.close();
            } 
            catch (SQLException ex) 
            {
                ProjectLogger.LOGGER.error("", ex);
            }
                 

            statement = null;
            rs = null;
        } 
        
        return success;
    }
    
    private static boolean DBAuthorizeUserIn(String user, String pass, UserAttributes out_attributes)
    {
        boolean success = false;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try
        {   
            String query = "SELECT `user_tasks_allow` as n_tasks_allow, `user_type` as account_type FROM USERS WHERE user_email = ? and user_pass = ?";
            conn = SystemManager.getInstance().getSystemDbPool().getConnection();
            statement = conn.prepareStatement(query);
            statement.setString(1, user);
            statement.setString(2, pass);            
            
            rs = statement.executeQuery();
            if(rs.next()) 
            {
                
                out_attributes.setAccountType(rs.getString("account_type"));
                out_attributes.setNTasksAllow((Integer) rs.getInt("n_tasks_allow"));                
                
                success = true;
            }
            else
                success = false;
        }  
        catch (SQLException ex)                             
        {
            ProjectLogger.LOGGER.error("", ex);
            success = false;
        } 
        catch (Exception ex)                             
        {
            ProjectLogger.LOGGER.error("", ex);
            success = false;
        } 
        finally
        {
            if(rs != null) 
            try 
            {
                rs.close();
            } 
            catch (SQLException ex) 
            {
                ProjectLogger.LOGGER.error("", ex);
            }
            
            if(statement != null) 
            try 
            {                        
                statement.close();
            } 
            catch (SQLException ex) {
                ProjectLogger.LOGGER.error("", ex);
            }
            
            if(conn != null) 
            try 
            {
                conn.close();
            } 
            catch (SQLException ex) 
            {
                ProjectLogger.LOGGER.error("", ex);
            }
                 

            statement = null;
            rs = null;
        } 
        
        return success;
    }
    
    /**
     *
     * @param user
     * @param pass
     * @param out_attributes
     * @param message
     * @return
     */
    public static boolean validateAccess(String user, String pass, UserAttributes out_attributes, StringWriter message)
    {
        boolean valid = true;
        
        //getCrawkerTaskStatus(code)
        if(message != null) message.getBuffer().setLength(0);
        
        if(!SystemManager.getInstance().IsRunning())
        {
            message.write("The back system is off. Please contact with the administrator."); //FIXME
            valid = false;
            return valid;
        }
        
        if(user != null && pass != null)
        {
            if(DBAuthorizeUserIn(user, pass, out_attributes))
            {
                String code_task_folder = TASKS_USERS_PATH + File.separator + user;            

                File f = new File(code_task_folder);
                if(!f.exists())
                    f.mkdir();
                
                valid = true;
                if(message != null) message.write(TheResourceBundle.getString("Jsp Auth Msg")); //FIXME               
            }
            else
            {
                valid = false;
                if(message != null) message.write(TheResourceBundle.getString("Jsp Unauth Msg")); //FIXME                
            }
        }
        else
        {
            valid = false;
            if(message != null) message.write(TheResourceBundle.getString("Jsp Params Invalid Msg")); //FIXME
        }
        
        return valid;                
    }
    
    /**
     * 
     * @param user
     * @param pass
     * @param message 
     * @return
     */
    public static boolean validateAccess(String user, String pass, StringWriter message)
    {
        boolean valid = true;
        
        //getCrawkerTaskStatus(code)
        if(message != null) message.getBuffer().setLength(0);
        
        if(!SystemManager.getInstance().IsRunning())
        {
            message.write("The back system is off. Please contact with the administrator."); //FIXME
            valid = false;
            return valid;
        }
        
        if(user != null && pass != null)
        {
            if(DBAuthorizeUserIn(user, pass))
            {
                String code_task_folder = TASKS_USERS_PATH + File.separator + user;            

                File f = new File(code_task_folder);
                if(!f.exists())
                    f.mkdir();
                
                valid = true;
                if(message != null) message.write(TheResourceBundle.getString("Jsp Auth Msg")); //FIXME               
            }
            else
            {
                valid = false;
                if(message != null) message.write(TheResourceBundle.getString("Jsp Unauth Msg")); //FIXME                
            }
        }
        else
        {
            valid = false;
            if(message != null) message.write(TheResourceBundle.getString("Jsp Params Invalid Msg")); //FIXME
        }
        
        return valid;
    }
    
    /**
     * Return the file names of the result of a task
     * @param user 
     * @param task_code
     * @return
     */
    public static List<String> getResultFiles(String user, String task_code)
    {        
        
        List<String> results = new ArrayList<String>();
        
        String result_code_task_folder = TASKS_USERS_PATH + File.separator + user + 
                                                            File.separator + task_code + 
                                                            File.separator + AuthorizationManager.results_dirname;
            
        File result_file = new File(result_code_task_folder);
        if(result_file.exists())
        {   
            List<File> tasks_folders = Arrays.asList(result_file.listFiles());
            for(File file : tasks_folders)
            {
                if(!file.isDirectory() && !file.getName().endsWith(".flag"))
                    results.add(file.getName());
            }                    
        }
        else
        {
            
        }

        return results;    
    }
    
    /**
     * Return the file names of the result of a task
     * @param user 
     * @param task_code
     * @return
     */
    public static List<String> getSourceFiles(String user, String task_code)
    {        
        
        List<String> sources = new ArrayList<String>();
        
        String code_task_folder_path = TASKS_USERS_PATH + File.separator + user + 
                                                          File.separator + task_code;
            
        File code_task_folder = new File(code_task_folder_path);
        if(code_task_folder.exists())
        {   
            List<File> source_files = Arrays.asList(code_task_folder.listFiles());
            for(File file : source_files)
            {
                if(!file.isDirectory() && !file.getName().endsWith(".flag"))
                    sources.add(file.getName());
            }                    
        }
        else
        {
            
        }

        return sources;    
    }    
    
    /**
     * Return the file names generated by verbose mode
     * @param user 
     * @param task_code
     * @return
     */
    public static List<String> getVerboseFiles(String user, String task_code)
    {        
        
        List<String> results = new ArrayList<String>();
        
        String result_code_task_folder = TASKS_USERS_PATH + File.separator + user + 
                                                            File.separator + task_code + 
                                                            File.separator + AuthorizationManager.verbose_dirname;
            
        File result_file = new File(result_code_task_folder);
        if(result_file.exists())
        {   
            List<File> tasks_folders = Arrays.asList(result_file.listFiles());
            for(File file : tasks_folders)
            {
                if(!file.isDirectory() && !file.getName().endsWith(".flag"))
                    results.add(file.getName());
            }                    
        }
        else
        {
            
        }

        return results;    
    }
    
    /**
     * Return the file names of the result of a task
     * @param user 
     * @param task_code
     * @param error_msg 
     * @return
     */
    public static boolean notifyResultError(String user, String task_code, String error_msg)
    {  
        boolean success = false;
        
        String result_code_task_folder = TASKS_USERS_PATH + File.separator + user + 
                                                          File.separator + task_code + 
                                                          File.separator + AuthorizationManager.results_dirname;
        try
        {
            

            if(new File(result_code_task_folder).exists())
            {   
                File error_file = new File(result_code_task_folder + File.separator + AuthorizationManager.error_flag_file);
                FileUtils.write(error_file, error_msg + "\r\n", "UTF-8", true);                
                success = true;
            }
            else
            {
                ProjectLogger.LOGGER.info("Results folder to notify error does not exist. (" + result_code_task_folder + ")");
                success = false;
            }
        }
        catch (Exception ex)
        {
            ProjectLogger.LOGGER.error("Error writing result errors (" + result_code_task_folder + ")", ex);
            success = false;
        }
        
        return success;
    }
    
    /**
     * Return the file according to the parameters (file_name of type of task_code of user)
     * @param user 
     * @param task_code
     * @param type 
     * @param file_name 
     * @return
     */
    public static File getFile(String user, String task_code, String file_name, String type)
    {
        String file_task_folder = AuthorizationManager.TASKS_USERS_PATH + 
                                                  File.separator + user + 
                                                  File.separator + task_code + 
                                                  (!type.equals("") ? File.separator + type : "") + 
                                                  File.separator + file_name;   
        File file = new File(file_task_folder);
        if(file.exists())
            return file;
        else
            return null;
    }    
    
    /**
     * Return the url to show the file in browser
     * @param user 
     * @param pass 
     * @param task_code
     * @param type 
     * @param file_name 
     * @return
     */
    public static String getGetFileUrlToShow(String user, String pass, String task_code, String file_name, String type)
    {
        String base_uri = TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "/resources/file/show"; //+ "/download"; //+ "&result";
        String file_url = base_uri + "?user=" + user + "&pass=" + pass + "&task_code=" + task_code + "&file=" + file_name + "&type=" + type;
        return file_url;
    } 
    
    /**
     * Return the url to donwload the file in browser
     * @param user 
     * @param pass 
     * @param task_code
     * @param type 
     * @param file_name 
     * @return
     */
    public static String getGetFileUrl(String user, String pass, String task_code, String file_name, String type)
    {
        String base_uri = TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "/resources/file/download"; //+ "/download"; //+ "&result";
        String file_url = base_uri + "?user=" + user + "&pass=" + pass + "&task_code=" + task_code + "&file=" + file_name + "&type=" + type;
        return file_url;
    }  
    
    /**
     * Return the url to delete the file in the task folder
     * @param user 
     * @param pass 
     * @param task_code
     * @param type 
     * @param file_name 
     * @return
     */
    public static String getDeleteFileUrl(String user, String pass, String task_code, String file_name, String type)
    {
        String base_uri = TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "/resources/file/delete"; //+ "/download";
        String file_url = base_uri + "?user=" + user + "&pass=" + pass + "&task_code=" + task_code + "&file=" + file_name + "&type=" + type;
        return file_url;
    }    
    
    /*
     * Write content in the feedback file of the task (feedback file is a plain text that may contain some text or some url to Google Docs, etc
     * @param user 
     * @param task_code 
     * @param feedback_content      
     * @return
     */
    /**
     *
     * @param user
     * @param task_code
     * @param feedback_content
     * @return
     */
    public static boolean updateFeedbackFile(String user, String task_code, String feedback_content)
    {
        boolean success = false;
        String file_task_folder = AuthorizationManager.TASKS_USERS_PATH + 
                                                  File.separator + user + 
                                                  File.separator + task_code + 
                                                  File.separator + AuthorizationManager.results_dirname + 
                                                  File.separator + feedback_flag_file;   
        try {
            FileUtils.write(new File(file_task_folder), feedback_content);
            success = true;
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error("Error writing (" + file_task_folder + ")", ex);
        }
        
        return success;
    }        
}
