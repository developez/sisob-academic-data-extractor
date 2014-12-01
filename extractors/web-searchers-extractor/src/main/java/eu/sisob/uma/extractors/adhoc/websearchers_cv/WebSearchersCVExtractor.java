
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

package eu.sisob.uma.extractors.adhoc.websearchers_cv;

import au.com.bytecode.opencsv.CSVReader;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import eu.sisob.uma.extractors.adhoc.websearchers.WebSearchersExtractorCommons;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class WebSearchersCVExtractor extends WebSearchersExtractorCommons {
    
    final static String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    static String[] cv_keywords_in_name_list = new String[]{ "cv", "curriculum", "vitae", "cvitae"};
    static String cv_keywords_in_query = " AND (cv OR curriculum OR vitae)";
    static String files = " AND pdf ";
    final static char CSV_SEPARATOR = ';';    
 
    /**
     *
     * @param input_file
     * @param results_dir
     * @param zip_output_file
     * @param output_file_2
     * @param error_sw
     */
    public static void download_files(File input_file, File results_dir, File zip_output_file, File output_file_2, StringWriter error_sw){
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(input_file), CSV_SEPARATOR);
        } catch (FileNotFoundException ex) {
            Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
            return;
        }
        
        int idStaffIdentifier= -1;int idName= -1;int idFirstName= -1;int idLastName= -1;int idInitials= -1;int idUnitOfAssessment_Description= -1;
        int idInstitutionName= -1;int idWebAddress= -1;int idResearchGroupDescription= -1;int idResearcherWebAddress = -1;int idResearcherWebAddressType = -1;int idResearcherWebAddressExt = -1;
        int idScoreUrl = -1;
        
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
                            idUnitOfAssessment_Description = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_INSTITUTION_NAME))
                            idInstitutionName = i;
                    else if(column_name.equals(FileFormatConversor.CSV_COL_INSTITUTION_URL))
                            idWebAddress = i;                
                    else if(column_name.equals(FileFormatConversor.CSV_COL_RESEARCHER_PAGE_URL))
                            idResearcherWebAddress = i;          
                    else if(column_name.equals(FileFormatConversor.CSV_COL_RESEARCHER_PAGE_TYPE))
                            idResearcherWebAddressType = i;          
                    else if(column_name.equals(FileFormatConversor.CSV_COL_RESEARCHER_PAGE_EXT))
                            idResearcherWebAddressExt = i;                           
                    else if(column_name.equals(FileFormatConversor.CSV_COL_SCORE_URL))
                            idScoreUrl = i;                           
                }
                               
                
                
            }        
        } catch (Exception ex) {            
            String error_msg = "Error reading headers of " + input_file.getName();
            Logger.getRootLogger().error(error_msg + " - " + ex.toString());
            if(error_sw != null) error_sw.append(error_msg + "\r\n");
            
            return;
        }                        
                
        if(idResearcherWebAddress != -1 && idResearcherWebAddressType != -1 && idResearcherWebAddressExt != -1)
        {                
            Logger.getRootLogger().info("Going to downloads results files");
            
            try {                
                for(int i = 0; i < nextLine.length; i++)
                    nextLine[i] = "\"" + nextLine[i] + "\"";
                
                FileUtils.write(output_file_2, StringUtil.join(Arrays.asList(nextLine), ";") + "\r\n", "UTF-8", false);
            } catch (IOException ex) {
                Logger.getLogger("root").error(ex.toString());
            }
            
            if(!results_dir.exists())
                results_dir.mkdirs();
            try {
                while ((nextLine = reader.readNext()) != null) 
                {
                    String url = nextLine[idResearcherWebAddress];
                    String ext = nextLine[idResearcherWebAddressExt];
                    String id = nextLine[idStaffIdentifier];

                    try{
                        Logger.getRootLogger().info("Downloading " + url);
                        String filename = id + "." + ext;
                        FileUtils.copyURLToFile(new URL(url), new File(results_dir, filename));
                        
                        nextLine[idResearcherWebAddress] = filename;                        
                        try {
                            for(int i = 0; i < nextLine.length; i++){
                                nextLine[i] = "\"" + nextLine[i] + "\"";
                            }
                            FileUtils.write(output_file_2, StringUtil.join(Arrays.asList(nextLine), ";") + "\r\n", "UTF-8", true);
                        } catch (IOException ex) {
                            Logger.getLogger("root").error(ex.toString());
                        }
                        
                    } catch (IOException ex) {
                        Logger.getRootLogger().error("Error downloading " + url);
                    }
                }
            } catch (IOException ex) {
                Logger.getRootLogger().error("Error reading " + input_file.getName() + " " + ex.getMessage());
            }

            ZipFile zf;
            try {
                zip_output_file.delete();
                zf = new ZipFile(zip_output_file);
                zf.createZipFileFromFolder(results_dir, new ZipParameters(), false, 0); 
            } catch (ZipException ex) {
                Logger.getRootLogger().error("Error zipping results from " + input_file.getName());
            }
            
        }else{
            Logger.getRootLogger().error("Headers incorrect " + input_file.getName());
        }
    }
            
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
    protected String get_result(String[] nextLine, int idStaffIdentifier, int idName, int idFirstName, int idLastName, int idInitials, int idSubject, int idInstitutionName, int idWebAddress, String expression, Object[] params) {
        
        String domain = clean_site(nextLine[idWebAddress]);
        String subject = nextLine[idSubject];
        String expression_subject = expression + " " + subject + " " + files + " " + cv_keywords_in_query;
        expression_subject = expression_subject.replaceAll("\t", " ");
        expression_subject = expression_subject.replaceAll("  ", " ");                    

        String url = "https://duckduckgo.com/html/?q=" + expression_subject;
        Logger.getRootLogger().info("Go with " + url);
        boolean again = false;
        Document doc = null;
        do{
            doc = getDocumentFromPage(url, 10, 2000, 5000);                                            

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


        //if(doc.select("div[class*=links_main] > a[href*=" + domain + "]").size() > 0){
        String final_result = "";
        if(doc != null && doc.select("div[class*=links_main] > a").size() > 0){

            /* Write resercher founded */
            Elements elements = doc.select("div[class*=links_main] > a");


            /* We will take the first html page and the first pdf */ 

            List<String[]> results = new ArrayList<String[]>();
            final int EXT_I = 0;
            final int SCORE_INT_I = 1;
            final int SCORE_LETTER_I = 2;
            final int RESULT_I = 3;
            final int WORST_SCORE = 67;

            //int max_results = elements.size();
            //int i_result = 0; 
            for(Element e : elements)
            {                    
                if((
                        e.text().startsWith("[") && !e.text().startsWith("[PDF]")
                   ) || 
                   e.absUrl("href").contains("duckduckgo.com/y.js") ||
                   e.absUrl("href").contains("wikipedia.") ||
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
                    continue;
                }             

                boolean add = false;
                int score_int = WORST_SCORE;
                String score = "";
                String ext = "";

                if(e.text().startsWith("[PDF]") || e.text().startsWith("[DOCX]") || e.text().startsWith("[DOC]") || e.text().startsWith("[RTF]")){    

                    String clean_name_1 = e.text().replaceAll("[^\\w\\s]", "").toLowerCase();
                    int i = e.absUrl("href").lastIndexOf("/");
                    int f = e.absUrl("href").lastIndexOf(".");
                    String clean_name_2 = "";
                    if(i != -1 && f != -1)
                        clean_name_2 = e.absUrl("href").substring(i, f).toLowerCase();
                    boolean b = false;
                    for(String k : cv_keywords_in_name_list){
                        if(clean_name_1.contains(k) || clean_name_2.contains(k)){
                            b = true;
                            break;
                        }
                    }
                    if(b){
                        score_int--;
                    }

                    if(clean_name_1.contains(nextLine[idLastName]) || 
                       clean_name_2.contains(nextLine[idLastName])){
                        score_int--;
                    }

                    score = Character.toChars(score_int)[0] + "";
                    add = true;
                    ext = "PDF";
                }     

                //if(!results.containsKey("HTML") && !e.text().startsWith("[")){
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
                    results.add(new String[]{ext, score_int + "", score, result});

                    Logger.getRootLogger().info("Select " + e.absUrl("href") + " - " + score + " - " + e.text());
                }                            
            }

            final_result = "";
            int best_score = WORST_SCORE;
            for(String[] result : results){

                if(result[EXT_I].equals("PDF")){
                    int act_score = Integer.parseInt(result[SCORE_INT_I]);

                    if(act_score < best_score){
                        best_score = act_score;
                        final_result = result[RESULT_I];    
                    }                               

                }
            }                       
        } 
        
        return final_result;                   
    }
}
