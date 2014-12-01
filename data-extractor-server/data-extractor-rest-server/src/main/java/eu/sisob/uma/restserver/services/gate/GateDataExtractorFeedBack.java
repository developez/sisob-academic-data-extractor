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

package eu.sisob.uma.restserver.services.gate;

import eu.sisob.uma.restserver.services.crawler.*;
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
import eu.sisob.uma.api.googledrive.Utils;
import eu.sisob.uma.restserver.Mailer;
import eu.sisob.uma.restserver.ProjectLogger;
import eu.sisob.uma.restserver.TheConfig;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class GateDataExtractorFeedBack {
    
    public static void initializeFeedback(String user, String task_code, String filename)
    {     
        SpreadsheetService sp_service = null;
        SpreadsheetEntry spreadsheet = null;
        
        try {
            String system_gmail_user = TheConfig.getInstance().getString(TheConfig.SYSTEMEMAIL_ADDRESS);
            String system_gmail_password  = TheConfig.getInstance().getString(TheConfig.SYSTEMEMAIL_PASSWORD);
            
            Utils utils = new Utils(system_gmail_user, system_gmail_password);
            
            sp_service = utils.authenticateSpreadsheetService("Test-Doc-Service");                
                    
            spreadsheet = utils.getOneSpreadSheet(sp_service, filename);               

            System.out.println(spreadsheet.getTitle().getPlainText());

            WorksheetFeed worksheetFeed = sp_service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
            WorksheetEntry worksheet = worksheets.get(0);

            URL listFeedUrl = worksheet.getCellFeedUrl();                

            List<CellEntry> cells = new ArrayList<CellEntry>();

            cells.add(new CellEntry(1, 1, "Gate Data Extractor feedback"));
            cells.add(new CellEntry(5, 2, "Filename"));
            cells.add(new CellEntry(6, 2, "<Here filename!>"));
            cells.add(new CellEntry(6, 3, "Expected expression"));
            cells.add(new CellEntry(6, 4, "Detected expression"));
            cells.add(new CellEntry(6, 5, "User comment"));
            
            cells.add(new CellEntry(7, 3, "Professor of Chemistry, 2010, University of Málaga"));
            cells.add(new CellEntry(7, 4, "Professor of Chemistry, 2010"));
            cells.add(new CellEntry(7, 5, "Miss the university!"));
            
            for(CellEntry cell : cells)
                sp_service.insert(listFeedUrl, cell);
            
        } catch (javax.naming.AuthenticationException ex) {           
            ProjectLogger.LOGGER.error("Error build feedback template: " + ex.toString());                
        } catch (MalformedURLException ex) {
            ProjectLogger.LOGGER.error("Error build feedback template: " + ex.toString());                
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error("Error build feedback template: " + ex.toString());                
        } catch (ServiceException ex) {
            ProjectLogger.LOGGER.error("Error build feedback template: " + ex.toString());                
        }
    }
}
