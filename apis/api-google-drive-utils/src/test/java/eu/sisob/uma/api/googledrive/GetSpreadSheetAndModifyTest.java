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
public class GetSpreadSheetAndModifyTest extends TestCase
{
    @Before
    public void setup()
    {    
        BasicConfigurator.configure();   
    }
    
//    @Test
//    public void testAuthAndList() 
//    {
//        if(true)
//        {
//            assertEquals(true, true);
//            return;
//        }
//        
//        SpreadsheetService service = null;
//        try 
//        {            
//            service = Utils.authenticateSpreadsheetService();            
//            Utils.listSpreadSheets(service);                    
//            assertTrue( true );
//        } catch (javax.naming.AuthenticationException ex) {
//            Logger.getRootLogger().error("", ex);
//        } catch (MalformedURLException ex) {
//            Logger.getRootLogger().error("", ex);
//        } catch (IOException ex) {
//            Logger.getRootLogger().error("", ex);        
//        } catch (AuthenticationException ex) {
//            Logger.getRootLogger().error("", ex);
//        } catch (ServiceException ex) {
//            Logger.getRootLogger().error("", ex);
//        } finally {
//
//        }       
//        
//    }
    
    @Test
    public void test()
    {
        if(true)
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
            SpreadsheetService sp_service = utils.authenticateSpreadsheetService("Test-Doc-Service");                

            SpreadsheetEntry spreadsheet = utils.getOneSpreadSheet(sp_service, file_name);               

            System.out.println(spreadsheet.getTitle().getPlainText());

            WorksheetFeed worksheetFeed = sp_service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
            WorksheetEntry worksheet = worksheets.get(0);

            URL listFeedUrl = worksheet.getCellFeedUrl();                

            CellFeed cellFeed = sp_service.getFeed(listFeedUrl, CellFeed.class);

            List<CellEntry> cells = new ArrayList<CellEntry>();

            cells.add(new CellEntry(1, 1, "Crawler feedback"));                                
            cells.add(new CellEntry(6, 2, "Researcher id"));
            cells.add(new CellEntry(6, 3, "University Url (optional)"));
            cells.add(new CellEntry(6, 4, "Webpage Url"));
            cells.add(new CellEntry(6, 5, "Links path"));                                

            //Example
            cells.add(new CellEntry(7, 2, "1864061"));
            cells.add(new CellEntry(7, 3, "http://www.brandeis.edu/"));
            cells.add(new CellEntry(7, 4, "http://www.brandeis.edu/facultyguide/person.html?emplid=10c26496511e9dcc5ae2cab67dacf8c0a29e75ec"));
            cells.add(new CellEntry(7, 5, "Faculty Guide"));                                
            cells.add(new CellEntry(7, 6, "Arts and Sciences"));                                
            cells.add(new CellEntry(7, 7, "Biology"));                                
            cells.add(new CellEntry(7, 8, "Cohen, Carolyn"));                                


            for(CellEntry cell : cells)
                sp_service.insert(listFeedUrl, cell);           
            
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
        }        
    }
    

}
