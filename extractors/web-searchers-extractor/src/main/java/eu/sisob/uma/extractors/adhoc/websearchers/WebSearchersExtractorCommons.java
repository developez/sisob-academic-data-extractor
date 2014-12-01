
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

package eu.sisob.uma.extractors.adhoc.websearchers;

import au.com.bytecode.opencsv.CSVReader;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public abstract class WebSearchersExtractorCommons {
    
    final static String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    /**
     *
     */
    protected final char CSV_SEPARATOR = ';';
       
    /**
     *
     * @param input_file
     * @param output_file
     * @param notfound_output_file
     * @param error_sw
     */
    public void scrap_duckduckgo(File input_file, File output_file, File notfound_output_file, StringWriter error_sw)
    {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(input_file), CSV_SEPARATOR);
        } catch (FileNotFoundException ex) {
            Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
            return;
        }
        
        int idStaffIdentifier= -1;int idName= -1;int idFirstName= -1;int idLastName= -1;int idInitials= -1;
        int idSubject= -1;int idInstitutionName= -1;int idWebAddress= -1;
        
        Logger.getRootLogger().info("Going to search researchers using duckduckgo");
        
        String [] nextLine;   
        try {
            if ((nextLine = reader.readNext()) != null)
            {
                //Locate indexes            
                //Locate indexes                        
                for(int i = 0; i < nextLine.length; i++)            
                {
                    String column_name = nextLine[i];
                    if(column_name.equals(FileFormatConversor.CSV_COL_ID))
                            idStaffIdentifier = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_NAME))
                            idName = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_FIRSTNAME))
                            idFirstName = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_LASTNAME))
                            idLastName = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_INITIALS))
                            idInitials = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_SUBJECT))
                            idSubject = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_INSTITUTION_NAME))
                            idInstitutionName = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_INSTITUTION_URL))
                            idWebAddress = i;                                
                }                
            }        
        } catch (IOException ex) {            
            String error_msg = "Error reading headers of " + input_file.getName();
            Logger.getRootLogger().error(error_msg + " - " + ex.toString());
            if(error_sw != null) error_sw.append(error_msg + "\r\n");
            
            return;
        }         
        
        Logger.getRootLogger().info("Headers info of result file writed");
           
        if(idLastName != -1 && idInitials != -1 && idStaffIdentifier != -1 && idWebAddress != -1 && idSubject != -1)
        {            
            try {
                String header = "";
                header += "\"" + FileFormatConversor.CSV_COL_ID + "\";";
                header += "\"" + FileFormatConversor.CSV_COL_LASTNAME + "\";";
                header += "\"" + FileFormatConversor.CSV_COL_INITIALS + "\";";  
                if(idFirstName != -1) header += "\"" + FileFormatConversor.CSV_COL_FIRSTNAME + "\";";  
                if(idName != -1) header += "\"" + FileFormatConversor.CSV_COL_NAME + "\";";  
                header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_URL + "\";";
                header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_EXT + "\";";
                header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_TYPE + "\";";
                header += "\"" + FileFormatConversor.CSV_COL_SCORE_URL + "\";";                
                header += "\r\n";
                FileUtils.write(output_file, header, "UTF-8", false);
                
                header = "";
                header += "\"" + FileFormatConversor.CSV_COL_ID + "\";";
                header += "\"" + FileFormatConversor.CSV_COL_LASTNAME + "\";";
                header += "\"" + FileFormatConversor.CSV_COL_INITIALS + "\";";                  
                if(idFirstName != -1) header += "\"" + FileFormatConversor.CSV_COL_FIRSTNAME + "\";";  
                if(idName != -1) header += "\"" + FileFormatConversor.CSV_COL_NAME + "\";";  
                if(idInstitutionName != -1) header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_NAME + "\";";  
                header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_URL + "\";";                
                header += "\"" + FileFormatConversor.CSV_COL_SCORE_URL + "\";";                
                header += "\r\n";
                FileUtils.write(notfound_output_file, header, "UTF-8", false);

            } catch (IOException ex) {
                Logger.getLogger("root").error(ex.toString());
                error_sw.append("Error creating output files\r\n");
            }            
            
            try
            {
                while ((nextLine = reader.readNext()) != null) 
                { 
                    nextLine[idLastName] = nextLine[idLastName].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                    nextLine[idInitials] = nextLine[idInitials].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                    if(idFirstName != -1) nextLine[idFirstName] = nextLine[idFirstName].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                    if(idName != -1) nextLine[idName] = nextLine[idName].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                                        
                    String expression = "";
                    
                    String aux = nextLine[idLastName];                            
                    expression += aux + " AND ";
                    
                    if(idFirstName != -1) {
                        String ss[] = nextLine[idFirstName].split(" ");
                        for(String s : ss){                            
                            if(s.length() > 1)
                                expression += s + " AND ";                            
                        }                        
                        expression = expression.substring(0, expression.length()-5);
                    }else{                    
                        String ss[] = nextLine[idInitials].split(" ");
                        for(String s : ss){                                                        
                            expression += s + " AND ";
                        }                        
                        //expression += aux + " ";
                        expression = expression.substring(0, expression.length()-5);
                    }
                    
                    String final_result = get_result(nextLine, idStaffIdentifier, idName,  idFirstName,  idLastName,  idInitials, 
                                            idSubject,  idInstitutionName,  idWebAddress, expression, null);
                   
                    if(!final_result.equals("")){
                        try {
                            FileUtils.write(output_file, final_result, "UTF-8", true);
                            Logger.getRootLogger().info("Writed results");
                        } catch (IOException ex) {
                            Logger.getLogger("root").error(ex.toString());
                        }
                    }else{
                        final_result = "";                        
                        final_result += "\"" + nextLine[idStaffIdentifier] + "\";";
                        final_result += "\"" + nextLine[idLastName] + "\";";
                        final_result += "\"" + nextLine[idInitials] + "\";";        
                        if(idFirstName != -1) final_result += "\"" + nextLine[idFirstName] + "\";";        
                        if(idName != -1) final_result += "\"" + nextLine[idName] + "\";";        
                        if(idInstitutionName != -1) final_result += "\"" + nextLine[idInstitutionName] + "\";";  
                        final_result += "\"" + nextLine[idWebAddress] + "\"";                        
                        final_result += "\r\n";

                        try {
                            Logger.getRootLogger().info("No results");
                            FileUtils.write(notfound_output_file, final_result, "UTF-8", true);
                        } catch (IOException ex) {
                            Logger.getLogger("root").error(ex.toString());
                        }
                    }                    
                } 
                
                reader.close();
                
                Logger.getRootLogger().info("Researchers data info of results file writed");
           
                
            } catch (Exception ex) {
                String error_msg = "Error extracting web researchers from DuckDuckGo " + input_file.getName();
                Logger.getRootLogger().error(error_msg + " - " + ex.toString());                
                if(error_sw != null) error_sw.append(error_msg + "\r\n");
                return;    
            }
        }         
    }
    
    /**
     *
     * @param nextLine
     * @param idStaffIdentifier
     * @param idName
     * @param idFirstName
     * @param idLastName
     * @param idInitials
     * @param idSubject
     * @param idInstitutionName
     * @param idWebAddress
     * @param expression
     * @param params
     * @return
     */
    protected abstract String get_result(String[] nextLine, int idStaffIdentifier,int idName, int idFirstName, int idLastName, int idInitials, 
                                         int idSubject, int idInstitutionName, int idWebAddress, String expression, Object[] params);
    
    /**
     *
     * @param url
     * @param times
     * @param wait_before
     * @param wait_error
     * @return
     */
    public Document getDocumentFromPage(String url, int times, long wait_before, long wait_error){
        boolean yeah = false;
        boolean out = false;
        Document doc = null;
        int count = 0;
        while(!yeah){
            try {
                Thread.sleep(wait_before);
                doc = Jsoup.connect(url).timeout(60000).userAgent(AGENT).get();
                yeah = true;
            } catch(Exception ex) {                            
                doc = null;
                Logger.getLogger("root").error("Error loading " + url + " " + ex.toString());
                try {                   
                    Thread.sleep(wait_error);
                } catch (InterruptedException ex1) {
                    Logger.getLogger("root").error("Error sleeping");
                }
                yeah = false;
                count++;
                if(count == times){
                    yeah = true;
                    out = true;                                    
                }                                
            }
        } 
        return doc;
    }
 
      
    /**
     *
     * @param site
     * @return
     */
    protected String clean_site(String site){
        
        String aux = site.replace("http://", "").replace("https://", "");
        int index = aux.indexOf("/");
        if(index != -1)
            aux = aux.substring(0, index);                        

        //aux = aux.endsWith("/") ? aux.substring(0, aux.length() - 1) : aux;

        if(aux.startsWith("www."))
            aux = aux.replace("www.", "");
        else {                        
            index = aux.indexOf(".");
            if(index != -1 && aux.lastIndexOf(".") != index)
                aux = aux.substring(index+1);                        
        }
        
        return aux;                    
    }
            
}
