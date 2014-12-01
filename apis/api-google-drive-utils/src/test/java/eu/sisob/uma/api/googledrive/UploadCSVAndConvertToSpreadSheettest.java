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

package eu.sisob.uma.api.googledrive;

import java.util.Collection;
import java.util.logging.Level;
import java.util.regex.Pattern;
import com.google.gdata.data.acl.AclWithKey;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.*;
import junit.framework.TestCase;
import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class UploadCSVAndConvertToSpreadSheettest extends TestCase
{
    @Before
    public void setup()
    {    
        BasicConfigurator.configure();   
    }
    
    @Test
    public void test()
    {
        if(false)
        {
            assertEquals(true, true);
            return;
        }        
        
        try 
        {            
            String system_gmail_user = "sisob.data.extractor.system@gmail.com";
            String system_gmail_password = "sisob1234";       
            
            String task_folder = "sisob-tasks-test";
            String user = "dlopezgonzalez@gmail.com-test";
            String task_code = "1";
            String file_name = "test-feedback-" + user + "-task-" + task_code;
            DocumentListEntry folder_entry = null;
            Utils utils = new Utils(system_gmail_user,system_gmail_password);
            
            //GET SPREADSHEET AND MODIFY
            DocsService service = utils.authenticateDocsService("Test-Doc-Service");                
            
            folder_entry = utils.getFolder(service, user, task_folder);   
            if(folder_entry == null)
            {
                DocumentListEntry folder_1_entry = utils.getFolder(service, task_folder, null);
                if(folder_1_entry == null)               
                {
                    folder_1_entry = utils.createFolder(service, task_folder, null);                                                                           
                }

                folder_entry = utils.createFolder(service, user, folder_1_entry.getDocId());                                                                                           
            }         
            
            File d = new File("test-data");
            
            DocumentUploader uploader = new DocumentUploader(service);
            List<File> files = new ArrayList<File>();
            for(File f : d.listFiles())
                files.add(f);            
            //new URL("https://docs.google.com/feeds/default/private/full/folder%3A" + folder_id + "/contents"
            //        "https://docs.google.com/feeds/upload/create-session/default/private/full";
            String folder_id = null;
            String url = DocumentUploader.DEFAULT_DOCLIST_FEED_URL + (folder_id == null ? "" : "/folder%3A" + folder_id + "/contents");
            uploader.uploadFiles(url, files, DocumentUploader.DEFAULT_CHUNK_SIZE);            
            
            assertTrue( true );
            
           } catch (javax.naming.AuthenticationException ex) {
            assertTrue( false );
            Logger.getRootLogger().error(ex.getMessage(), ex);
        } catch (MalformedURLException ex) {
            assertTrue( false );
            Logger.getRootLogger().error(ex.getMessage(), ex);
        } catch (IOException ex) {
            assertTrue( false );
            Logger.getRootLogger().error(ex.getMessage(), ex);
        } catch (AuthenticationException ex) {
            assertTrue( false );
            Logger.getRootLogger().error(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            assertTrue( false );
            Logger.getRootLogger().error(ex.getMessage(), ex);        
        } catch (InterruptedException ex) {
            assertTrue( false );
            Logger.getRootLogger().error(ex.getMessage(), ex);        
        }
    }
    

}
