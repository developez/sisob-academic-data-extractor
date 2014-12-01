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

import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclWithKey;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.util.ServiceException;
import eu.sisob.uma.api.googledrive.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.naming.AuthenticationException;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class FeedbackManager {
    
    /*
     * Create spreadsheet google docs documents, if exist, return the exist
     * @return url of document
     */
    /**
     *
     * @param user
     * @param task_code
     * @param filename
     * @return
     */
    public static String createNewFeedBackDoc(String user, String task_code, String filename)
    {
        String url = "";        
        DocumentListEntry folder_entry = null;
        DocsService service = null;
        
        String system_gmail_user = TheConfig.getInstance().getString(TheConfig.SYSTEMEMAIL_ADDRESS);
        String system_gmail_password  = TheConfig.getInstance().getString(TheConfig.SYSTEMEMAIL_PASSWORD);
        
        try 
        {                
            Utils utils = new Utils(system_gmail_user, system_gmail_password);
            service = utils.authenticateDocsService("Test-Doc-Service");

            folder_entry = utils.getFolder(service, user, AuthorizationManager.TASKS_FOLDER_NAME);   
            if(folder_entry == null)
            {
                DocumentListEntry folder_1_entry = utils.getFolder(service,  AuthorizationManager.TASKS_FOLDER_NAME, null);
                if(folder_1_entry == null)               
                {
                    folder_1_entry = utils.createFolder(service,  AuthorizationManager.TASKS_FOLDER_NAME, null);                                                                           
                }

                folder_entry = utils.createFolder(service, user, folder_1_entry.getDocId());                                                                                           
            }         

            SpreadsheetEntry spreadsheet = utils.getOneSpreadSheet(service, filename, folder_entry.getDocId());
            if(spreadsheet == null)
            {
                spreadsheet = utils.createSpreadsheet(service, filename, folder_entry.getDocId());
            }                         

            DocumentListEntry spreadsheet_document_list = utils.getOneDocumentEntry(service, filename, folder_entry.getDocId());
                        
            url = spreadsheet_document_list.getDocumentLink().getHref();
            
//            service.insert(new URL(spreadsheet_document_list.getAclFeedLink().getHref()), 
//                           new AclScope(AclScope.Type.USER, user), 
//                           new AclRole("writer"));              
            
            try
            {
                AclEntry acl = new AclEntry();                
                acl.setScope(new AclScope(AclScope.Type.DEFAULT, null));
                AclWithKey withKey = new AclWithKey("", new AclRole("writer"));
                acl.setWithKey(withKey);
                service.insert(new URL(spreadsheet_document_list.getAclFeedLink().getHref()), acl);
            }
            catch(Exception ex)
            {
                ProjectLogger.LOGGER.error(ex.toString());                
            }   
            
        } catch (AuthenticationException ex) {
            ProjectLogger.LOGGER.error("", ex);            
        } catch (MalformedURLException ex) {
            ProjectLogger.LOGGER.error("", ex);            
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error("", ex);           
        } catch (ServiceException ex) {
            ProjectLogger.LOGGER.error("", ex);            
        }   
        
        return url;
    }   
    
}

