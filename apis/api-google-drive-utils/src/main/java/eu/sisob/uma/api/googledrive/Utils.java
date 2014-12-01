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

package eu.sisob.uma.api.googledrive;

import com.google.gdata.client.DocumentQuery;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.Category;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.naming.AuthenticationException;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class Utils 
{
            
    String system_gmail_user;
    String system_gmail_password;
            
    public Utils(String system_gmail_user, String system_gmail_password)
    {
        this.system_gmail_user = system_gmail_user;
        this.system_gmail_password = system_gmail_password;
    }
    
    
    /**
     * Authenticate SpreadsheetService with user and password
     * @return
     * @throws AuthenticationException
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public SpreadsheetService authenticateSpreadsheetService(String application_name)
      throws AuthenticationException, MalformedURLException, IOException, ServiceException 
    {
        SpreadsheetService service = new SpreadsheetService("MySpreadsheetIntegration-v1");
        service.setProtocolVersion(SpreadsheetService.Versions.V3);
        service.setUserCredentials(system_gmail_user, system_gmail_password);        
        return service;            
    }
    
    /**
     * 
     * @param application_name
     * @param user
     * @param pass
     * @return
     * @throws AuthenticationException
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public DocsService authenticateDocsService(String application_name)
      throws AuthenticationException, MalformedURLException, IOException, ServiceException 
    {

        DocsService docsService = new DocsService(application_name);
        docsService.setProtocolVersion(DocsService.Versions.V3);
        docsService.setUserCredentials(system_gmail_user, system_gmail_password);                
        
        return docsService;
    }        
    
    /**
     * Get SpreadsheetEntry in one folder with exact title
     * @param docsService
     * @param title
     * @param folder_id
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public SpreadsheetEntry createSpreadsheet(DocsService docsService, String title, String folder_id) throws MalformedURLException, IOException, ServiceException
    {
        SpreadsheetEntry new_spreadsheet = new SpreadsheetEntry();

        Category category = new Category("http://schemas.google.com/docs/2007#spreadsheet");
        new_spreadsheet.getCategories().add(category);
        
        String spreadSheetTitle = title;
        new_spreadsheet.setTitle(new PlainTextConstruct(spreadSheetTitle));           
         
        return docsService.insert(new URL("https://docs.google.com/feeds/default/private/full/folder%3A" + folder_id + "/contents"), new_spreadsheet);
    }
    
    /**
     * Get a List of SpreadsheetEntry from one folder with exact title. (folder and title can be null).
     * @param service
     * @param title
     * @param folder_id
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public List<DocumentListEntry> getDocumentEntries(DocsService service, String title, String folder_id) throws MalformedURLException, IOException, ServiceException
    {   
        URL url = new URL("https://docs.google.com/feeds/default/private/full/" + (folder_id == null ? "" : "folder%3A" + folder_id + "/contents"));
        DocumentQuery query = new DocumentQuery(url);
        
        if(title != null)
        {
            query.setTitleQuery(title);        
            query.setTitleExact(true);
        }
        
        DocumentListFeed feed = service.getFeed(query, DocumentListFeed.class);
        
        return feed.getEntries();
    }
    
    /**
     * Get the first SpreadsheetEntry from a search in one folder with exact title. (folder and title can be null).
     * @param service
     * @param title
     * @param folder_id
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public DocumentListEntry getOneDocumentEntry(DocsService service, String title, String folder_id) throws MalformedURLException, IOException, ServiceException
    {
        DocumentListEntry result = null;
        
        List<DocumentListEntry> l = getDocumentEntries(service, title, folder_id);
                
        if(l != null && l.size() > 0)
            result = l.get(0);            
         
        return result;
    }
    
    /**
     * Get a List of SpreadsheetEntry from one folder with exact title. (folder and title can be null).
     * @param service
     * @param title
     * @param folder_id
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public List<SpreadsheetEntry> getSpreadSheets(DocsService service, String title, String folder_id) throws MalformedURLException, IOException, ServiceException
    {   
        URL url = new URL("https://docs.google.com/feeds/default/private/full/" + (folder_id == null ? "" : "folder%3A" + folder_id + "/contents"));
        SpreadsheetQuery query = new SpreadsheetQuery(url);
        
        if(title != null)
        {
            query.setTitleQuery(title);        
            query.setTitleExact(true);
        }
        
        SpreadsheetFeed feed = service.getFeed(query, SpreadsheetFeed.class);
        
        return feed.getEntries();
    }
    
    /**
     * Get the first SpreadsheetEntry from a search in one folder with exact title. (folder and title can be null).
     * @param service
     * @param title
     * @param folder_id
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public SpreadsheetEntry getOneSpreadSheet(DocsService service, String title, String folder_id) throws MalformedURLException, IOException, ServiceException
    {
        SpreadsheetEntry result = null;
        
        List<SpreadsheetEntry> l = getSpreadSheets(service, title, folder_id);
                
        if(l != null && l.size() > 0)
            result = l.get(0);            
         
        return result;
    }
    
    /**
     * Get a List of SpreadsheetEntry from one folder with exact title. (folder and title can be null).
     * @param service
     * @param title
     * @param folder_id
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public List<SpreadsheetEntry> getSpreadSheets(SpreadsheetService service, String title) throws MalformedURLException, IOException, ServiceException
    {   
        String folder_id = null;
        //"https://spreadsheets.google.com/feeds/spreadsheets/private/full/"
        URL url = new URL(FeedURLFactory.getDefault().getSpreadsheetsFeedUrl() + (folder_id == null ? "" : "/folder%3A" + folder_id + "/contents"));
        SpreadsheetQuery query = new SpreadsheetQuery(url);
        
        if(title != null)
        {
            query.setTitleQuery(title);        
            query.setTitleExact(true);
        }
        
        SpreadsheetFeed feed = service.getFeed(query, SpreadsheetFeed.class);
        
        return feed.getEntries();
    }
    
    /**
     * Get the first SpreadsheetEntry from a search in one folder with exact title. (folder and title can be null).
     * @param service
     * @param title
     * @param folder_id
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public SpreadsheetEntry getOneSpreadSheet(SpreadsheetService service, String title) throws MalformedURLException, IOException, ServiceException
    {
        SpreadsheetEntry result = null;
        
        List<SpreadsheetEntry> l = getSpreadSheets(service, title);
                
        if(l != null && l.size() > 0)
            result = l.get(0);            
         
        return result;
    }
    
    /**
     * 
     * @param docsService
     * @param title
     * @param folder_id
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public FolderEntry createFolder(DocsService docsService, String title, String folder_id) throws MalformedURLException, IOException, ServiceException
    {
        FolderEntry newEntry  = new FolderEntry();

        Category category = new Category("http://schemas.google.com/docs/2007#folder");
        newEntry.getCategories().add(category);
        
        String spreadSheetTitle = title;
        newEntry.setTitle(new PlainTextConstruct(spreadSheetTitle));           
         
        URL url = new URL("https://docs.google.com/feeds/default/private/full/" + (folder_id == null ? "" : "folder%3A" + folder_id + "/contents"));
        return docsService.insert(url, newEntry);
    }
    
    /**
     * 
     * @param docsService
     * @param name
     * @param parent
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    public DocumentListEntry getSpreadsheet2(DocsService docsService, String name, String parent) throws IOException, ServiceException
    {
        DocumentListFeed feed = docsService.getFeed(new URL("https://docs.google.com/feeds/default/private/full/-/spreadsheet"),
                                                    DocumentListFeed.class);
                        
        boolean found = false;
        DocumentListEntry result = null;
        for(DocumentListEntry entry : feed.getEntries())      
        {   
            String folder_name = entry.getTitle().getPlainText();
            if(folder_name.equals(name))
            {
                result = entry;
                break;                        
            }            
        }
        
        return result;
    }
    
    /**
     * 
     * @param docsService
     * @param name
     * @param parent
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    public DocumentListEntry getFolder(DocsService docsService, String name, String parent) throws IOException, ServiceException
    {
        DocumentListFeed feed = docsService.getFeed(new URL("https://docs.google.com/feeds/default/private/full/-/folder"),
                                                    DocumentListFeed.class);
                        
        boolean found = false;
        DocumentListEntry result = null;
        for(DocumentListEntry entry : feed.getEntries())      
        {   
            String folder_name = entry.getTitle().getPlainText();
            if(folder_name.equals(name))
            {
                if(entry.getParentLinks().size() == 0)                    
                {
                    if(parent != null && !parent.equals(""))
                    {
                        found = false;
                    }
                    else
                    {
                        result = entry;
                        found = true;
                    }                    
                }
                else
                {
                    if(parent != null && !parent.equals(""))
                    {                                            
                        for(Link link : entry.getParentLinks())
                        {
                            String link_title = link.getTitle();
                            if(link_title.equals(parent))
                            {
                                found = true;
                                result = entry;
                                break;
                            }
                        }
                    }
                    else
                    {
                        result = entry;
                        found = true;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * 
     * @param docsService
     * @throws MalformedURLException
     * @throws IOException
     * @throws ServiceException
     */
    public void listFolders(DocsService docsService) throws MalformedURLException, IOException, ServiceException
    {
        // Instantiate a DocumentsListQuery object to retrieve documents.
//        DocumentQuery query = new DocumentQuery(new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full"));
//        
//        Category category = new Category("http://schemas.google.com/docs/2007#folder");        
//        CategoryFilter category_filter = new CategoryFilter();
//        category_filter.addCategory(category);
//        query.getCategoryFilters().add(category_filter);
        
        DocumentListFeed feed = docsService.getFeed(new URL("https://docs.google.com/feeds/default/private/full/-/folder"),
                                                    DocumentListFeed.class);

        //See in com.google.gdata.data.docs (it must wear something like that @com.google.gdata.data.Kind.Term(value = "http://schemas.google.com/docs/2007#folder"))
        //FolderEntry feed = docsService.getFeed(query, FolderEntry.class);
              
        for(DocumentListEntry entry : feed.getEntries())      
        {   
            List<Link> lst = entry.getParentLinks();
            System.out.println(entry.getTitle().getPlainText());            
        }
        

//        // Set the trashed category
//        query.Trashed = true;
//
//        // Make a request to the API and get all documents.
//        DocumentsFeed feed = service.Query(query);
//
//        // Iterate through all of the documents returned
//        foreach (DocumentEntry entry in feed.Entries)
//        {
//        // Print the title of this document to the screen
//        Console.WriteLine(entry.Title.Text);
//        }
    }    

}
