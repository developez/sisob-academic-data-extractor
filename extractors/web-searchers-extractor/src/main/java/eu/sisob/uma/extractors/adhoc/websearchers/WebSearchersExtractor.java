
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

import eu.sisob.uma.footils.Web.Downloader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class WebSearchersExtractor extends WebSearchersExtractorCommons {   
   
    /**
     *
     */
    public enum SearchPatterns{
        /**
         *
         */
        P1,
        /**
         *
         */
        P2,
        /**
         *
         */
        P3,
        /**
         *
         */
        P4,
        /**
         *
         */
        P5
    }
    
    SearchPatterns search_patterns;
    /**
     *
     * @param search_patterns
     */
    public WebSearchersExtractor(SearchPatterns search_patterns){
        
        this.search_patterns = search_patterns;
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
    @Override
    protected String get_result(String[] nextLine, int idStaffIdentifier,int idName, int idFirstName, int idLastName, int idInitials, 
                                int idSubject, int idInstitutionName, int idWebAddress, String expression, Object[] params) {
        
        String keywords = " (PROFILE OR PHD OR RESEARCHER OR FACULTY OR PROFESSOR OR RESEARCH) AND ";
        keywords = "";  
        
        String domain = clean_site(nextLine[idWebAddress]);
        String subject = nextLine[idSubject];
        String and_institution_name = (idInstitutionName != -1 ? " AND " + nextLine[idInstitutionName] : "" );
        String expression_subject = expression + " AND " + subject;
        String expression_site = expression + " site: " + domain;
        String expression_inst_name = expression + and_institution_name;
        String expression_inst_name_and_subject = expression + and_institution_name + " AND " + subject;
                
        String url = "";
        
        switch(search_patterns){
            case P1:
                url = "https://duckduckgo.com/html/?q=" + keywords + expression;
                break;
            case P2:
                url = "https://duckduckgo.com/html/?q=" + keywords + expression_subject;
                break;
            case P3:
                url = "https://duckduckgo.com/html/?q=" + keywords + expression_site;
                break;
            case P4:
                url = "https://duckduckgo.com/html/?q=" + keywords + expression_inst_name;
                break;
            case P5:
                url = "https://duckduckgo.com/html/?q=" + keywords + expression_inst_name_and_subject;
                break;
            default:
                url = "https://duckduckgo.com/html/?q=" + keywords + expression_subject;
                break;
        }
        Logger.getRootLogger().info("Go with " + url);
        boolean again = false;
        Document doc = null;
        do{
            doc = getDocumentFromPage(url, 10, 1000, 5000);                                            

            if(doc != null && doc.text().contains("If this error persists, please let us know")){
               try {
                    Thread.sleep(30000);
               } catch (InterruptedException ex) {
               }
               again = true;
            }else{
                again = false;
            }                            
        }while(again);        

        String final_result = "";
        if(doc != null && doc.select("div[class*=links_main] > a").size() > 0){

            /* Write resercher founded */
            Elements elements = doc.select("div[class*=links_main] > a");


            /* We will take the first html page and the first pdf */ 

            HashMap<String, String> results = new HashMap<String, String>();

            int max_results = 2;
            int i_result = 0; 
            for(Element e : elements)
            {                    
                if((
                        e.text().startsWith("[") 
                        //&& !e.text().startsWith("[PDF]")
                   ) || 
                   e.absUrl("href").contains("duckduckgo.com/y.js") ||
                   e.absUrl("href").contains("wikipedia.") ||
                   e.absUrl("href").contains("facebook.com") ||
                   e.absUrl("href").contains("microsoft.com") ||
                   e.absUrl("href").contains("google.com") ||
                   e.absUrl("href").contains("linkedin") ||
                   e.absUrl("href").contains("www.biography.com")  ||
                   e.absUrl("href").contains("biomedexperts.com")  ||
                   e.absUrl("href").contains("www.experts.scival.com")  ||
                   e.absUrl("href").contains("ratemyprofessors.com")  ||                        
                   e.absUrl("href").contains("flickr.com")  ||
                   e.absUrl("href").endsWith(".txt")  ||
                   e.absUrl("href").endsWith(".csv")  ||
                   e.absUrl("href").endsWith(".xml")  ||
                   e.absUrl("href").endsWith(".doc")  ||
                   e.absUrl("href").endsWith(".docx")  ||
                   e.absUrl("href").endsWith(".xls")  ||
                   e.absUrl("href").endsWith(".xlxs")  ||
                   e.absUrl("href").contains("www.amazon"))
                {
                    max_results++;
                    continue;
                }                            

                boolean add = false;
                String score = "";
                String ext = "";
                if(!results.containsKey("HTML") && !e.text().startsWith("[")){
                    //results.put("html", )
                    
                    File temp; 
                    try {
                        temp = File.createTempFile("temp-file-name", ".tmp");
                        URL fetched_url = Downloader.fetchURL(e.absUrl("href"));                        
                        FileUtils.copyURLToFile(fetched_url, temp);
                        long sizeInBytes = temp.length();
                        long sizeInMb = sizeInBytes / (1024 * 1024);
                        if(sizeInMb > 100){
                            score = "B";
                        }else{
                            String content = FileUtils.readFileToString(temp);
                            if(content.contains(nextLine[idLastName]))
                            {
                                score = "A";
                            }              
                            else
                            {
                                score = "B";
                            } 
                        }
                    } catch (IOException ex) {
                        score = "B";
                    } 

                    ext = "HTML";
                    add = true;
                }

                //if(!results.containsKey("PDF") && e.text().startsWith("[PDF]")){                                                        
                //    score = "A";
                //    ext = "PDF";
                //    add = true;
                //}                          

                if(add){
                    String result = "";
                    result += "\"" + nextLine[idStaffIdentifier] + "\";";
                    result += "\"" + nextLine[idLastName] + "\";";
                    result += "\"" + nextLine[idInitials] + "\";";        
                    if(idFirstName != -1) result += "\"" + nextLine[idFirstName] + "\";";        
                    if(idName != -1) result += "\"" + nextLine[idName] + "\";";        
                    result += "\"" + e.absUrl("href") + "\";";
                    result += "\"" + ext + "\";";
                    result += "\"" + "CV" + "\";";
                    result += "\"" + score + "\"";
                    result += "\r\n";           
                    results.put(ext, result);

                    Logger.getRootLogger().info("Select " + e.absUrl("href") + " - " + e.text());
                } 

//                if(results.containsKey("PDF") && results.containsKey("HTML")){
//                    break;
//                }

                i_result++;
                if(max_results <= i_result){
                    break;
                }
            }

//            if(results.containsKey("PDF"))
//                final_result = results.get("PDF");
//            else 
                if(results.containsKey("HTML"))
                final_result = results.get("HTML");
            else
                final_result = "";
        }         
        
        return final_result;
    }    
    
}
