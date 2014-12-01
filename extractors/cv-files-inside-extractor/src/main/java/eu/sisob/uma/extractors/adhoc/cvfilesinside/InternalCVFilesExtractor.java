
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

package eu.sisob.uma.extractors.adhoc.cvfilesinside;

import au.com.bytecode.opencsv.CSVReader;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import eu.sisob.uma.footils.Web.Downloader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
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
public class InternalCVFilesExtractor {
    
    final static String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    final static int MAX_MAIL_PER_PAGE = 6;
    final static char CSV_SEPARATOR = ';';
    final static int max_in_mem = 100;
    final static boolean test_only_output = false;
    static String[] cv_keywords_in_name_list = new String[]{ "cv", "curriculum", "vitae", "cvitae"};
    static String[] pub_keywords_in_name_list = new String[]{ "publications", "pubs"};

    
    /**
     *
     * @param input_file
     * @param data_dir
     * @param output_file
     * @param error_sw
     */
    public static void extract_cv_files(File input_file, File data_dir, File output_file/*, File output_file_2, File results_dir,*/, StringWriter error_sw)
    {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(input_file), CSV_SEPARATOR);
        } catch (FileNotFoundException ex) {
            Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
        }
        
        int idStaffIdentifier= -1;int idName= -1;int idFirstName= -1;int idLastName= -1;int idInitials= -1;int idUnitOfAssessment_Description= -1;
        int idInstitutionName= -1;int idWebAddress= -1;int idResearchGroupDescription= -1;int idResearcherWebAddress = -1;int idResearcherWebAddressType = -1;int idResearcherWebAddressExt = -1;
        int idScoreUrl = -1; int idEmail = -1; int idScoreEmail = -1;
        
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
                    else if(column_name.equals(FileFormatConversor.CSV_COL_EMAIL))
                            idEmail = i;      
                    else if(column_name.equals(FileFormatConversor.CSV_COL_SCORE_EMAIL))
                            idScoreEmail = i;      
                }                
            }        
        } catch (Exception ex) {            
            String error_msg = "Error reading headers of " + input_file.getName();
            Logger.getRootLogger().error(error_msg + " - " + ex.toString());
            if(error_sw != null) error_sw.append(error_msg + "\r\n");
            
            return;
        }                        
                
        if(idResearcherWebAddress != -1 && idResearcherWebAddressType != -1 && idResearcherWebAddressExt != -1 &&
           idStaffIdentifier != -1 && idLastName != -1 && idInitials != -1)
        {                
            if(true)
            {
                try {
                    String header = "";
                    header += "\"" + FileFormatConversor.CSV_COL_ID + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_LASTNAME + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_INITIALS + "\"" + CSV_SEPARATOR;  
                    if(idFirstName != -1) header += "\"" + FileFormatConversor.CSV_COL_FIRSTNAME + "\"" + CSV_SEPARATOR;  
                    if(idName != -1) header += "\"" + FileFormatConversor.CSV_COL_NAME + "\"" + CSV_SEPARATOR;  
                    if(idEmail != -1) header += "\"" + FileFormatConversor.CSV_COL_EMAIL + "\"" + CSV_SEPARATOR;  
                    if(idInstitutionName != -1) header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_NAME + "\"" + CSV_SEPARATOR;  
                    if(idWebAddress != -1) header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_URL + "\"" + CSV_SEPARATOR;  
                    header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_URL + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_EXT + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_TYPE + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_SCORE_URL + "\"" + CSV_SEPARATOR;
                    if(idScoreEmail != -1) header += "\"" + FileFormatConversor.CSV_COL_SCORE_EMAIL + "\"" + CSV_SEPARATOR;                    
                    header += "\r\n";
                    FileUtils.write(output_file, header, "UTF-8", false); 
                    // DOWNLOAD HERE THE HOME PAGE 
                    //FileUtils.write(output_file_2, header, "UTF-8", false);

                } catch (IOException ex) {
                    Logger.getLogger("root").error(ex.toString());
                    error_sw.append("Error creating output files\r\n");
                }      
            }
            
            try
            {
//                DOWNLOAD HERE THE HOME PAGE 
//                if(!results_dir.exists())
//                    results_dir.mkdirs();                
//                File homepage_results_dirs = new File(results_dir, "HOMEPAGE");
//                if(!homepage_results_dirs.exists())
//                    homepage_results_dirs.mkdirs();
                //if(!test_only_output)
                {
                    Pattern p1 = Pattern.compile("([a-zA-Z0-9#._-]+)+");

                    while ((nextLine = reader.readNext()) != null) 
                    {                 
                        nextLine[idLastName] = nextLine[idLastName].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                        nextLine[idInitials] = nextLine[idInitials].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                        if(idFirstName != -1) nextLine[idFirstName] = nextLine[idFirstName].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                        if(idName != -1) nextLine[idName] = nextLine[idName].replaceAll("[^a-zA-Z]", " ").toLowerCase();

                        Document content = null;
                        String researcher_page_url = nextLine[idResearcherWebAddress];
                        File temp_file = null;                                  
                        if(p1.matcher(researcher_page_url).matches()){ 
                            
                        }else{
                            
                            try{
                                
                                Logger.getRootLogger().info("Reading " + researcher_page_url);

                                
                                temp_file = File.createTempFile("internal-cv-files-", ".tmp");
                                URL fetched_url = Downloader.fetchURL(researcher_page_url);
                                FileUtils.copyURLToFile(fetched_url, temp_file);                                
                                long sizeInBytes = temp_file.length();
                                long sizeInMb = sizeInBytes / (1024 * 1024);
                                if(sizeInMb > 100){
                                    content = null;
                                } else {
                                    String text_content = FileUtils.readFileToString(temp_file);  
                                    String check_string = "";
                                    if(text_content.length() <= 100){
                                        check_string = text_content.substring(0, text_content.length());
                                    }else{
                                        check_string = text_content.substring(0, 100);
                                    }
                                    if(check_string.toLowerCase().contains("html")){
                                        content = Jsoup.parse(text_content);
                                        content.setBaseUri(researcher_page_url);        
//                                          DOWNLOAD HERE THE HOME PAGE                                        
//                                        String filename = nextLine[idStaffIdentifier] + "_HOMEPAGE_" + MD5(researcher_page_url) + ".html";
//                                        FileUtils.copyFile(temp_file, new File(homepage_results_dirs, filename));                                        
//                                        
//                                        String result = "";                        
//                                        result += "\"" + nextLine[idStaffIdentifier] + "\"" + CSV_SEPARATOR;
//                                        result += "\"" + nextLine[idLastName] + "\"" + CSV_SEPARATOR;
//                                        result += "\"" + nextLine[idInitials] + "\"" + CSV_SEPARATOR;                                    
//                                        if(idFirstName != -1) result += "\"" + nextLine[idFirstName] + "\"" + CSV_SEPARATOR;  
//                                        if(idName != -1) result += "\"" + nextLine[idName] + "\"" + CSV_SEPARATOR;  
//                                        if(idEmail != -1) result += "\"" + nextLine[idEmail] + "\"" + CSV_SEPARATOR; 
//                                        if(idInstitutionName != -1) result += "\"" + nextLine[idInstitutionName] + "\"" + CSV_SEPARATOR;  
//                                        if(idWebAddress != -1) result += "\"" + nextLine[idWebAddress] + "\"" + CSV_SEPARATOR;                 
//                                        result += "\"" + filename + "\"" + CSV_SEPARATOR;
//                                        result += "\"" + nextLine[idResearcherWebAddressType] + "\"" + CSV_SEPARATOR;
//                                        result += "\"" + nextLine[idResearcherWebAddressExt] + "\"" + CSV_SEPARATOR;
//                                        result += "\"" + (idScoreUrl != -1 ? nextLine[idScoreUrl] : "") + "\"" + CSV_SEPARATOR;
//                                        if(idScoreEmail != -1) result += "\"" + nextLine[idScoreEmail] + "\"" + CSV_SEPARATOR; 
//                                        result += "\r\n";
//
//                                        try {
//                                            FileUtils.write(output_file_2, result, "UTF-8", true);
//                                        } catch (IOException ex) {
//                                            Logger.getLogger("root").error(ex.toString());
//                                        }
                                    } else {                                        
                                        throw new Exception(researcher_page_url + " is not html document");
                                    }                                    
                                }
                                
                            }catch(Exception ex){
                                Logger.getLogger("root").error("" + researcher_page_url + " could not loaded", ex);
                                error_sw.append("" + researcher_page_url + " could not loaded");
                                content = null;
                            }catch(java.lang.OutOfMemoryError ex2){
                                Logger.getLogger("root").error("" + researcher_page_url + " could not loaded (out of memory)", ex2);
                                error_sw.append("" + researcher_page_url + " could not loaded (out of memory)");
                                content = null;
                            }finally{
                                if(temp_file!=null)
                                    temp_file.delete();
                            }                            
                            
                        }
                        //Add sources to output
                        {
                            String result = "";                        
                            result += "\"" + nextLine[idStaffIdentifier] + "\"" + CSV_SEPARATOR;
                            result += "\"" + nextLine[idLastName] + "\"" + CSV_SEPARATOR;
                            result += "\"" + nextLine[idInitials] + "\"" + CSV_SEPARATOR;                                    
                            if(idFirstName != -1) result += "\"" + nextLine[idFirstName] + "\"" + CSV_SEPARATOR;  
                            if(idName != -1) result += "\"" + nextLine[idName] + "\"" + CSV_SEPARATOR;  
                            if(idEmail != -1) result += "\"" + nextLine[idEmail] + "\"" + CSV_SEPARATOR; 
                            if(idInstitutionName != -1) result += "\"" + nextLine[idInstitutionName] + "\"" + CSV_SEPARATOR;  
                            if(idWebAddress != -1) result += "\"" + nextLine[idWebAddress] + "\"" + CSV_SEPARATOR;                 
                            result += "\"" + nextLine[idResearcherWebAddress] + "\"" + CSV_SEPARATOR;                            
                            result += "\"" + nextLine[idResearcherWebAddressExt] + "\"" + CSV_SEPARATOR;
                            result += "\"HOMEPAGE\"" + CSV_SEPARATOR;
                            result += "\"" + (idScoreUrl != -1 ? nextLine[idScoreUrl] : "") + "\"" + CSV_SEPARATOR;
                            if(idScoreEmail != -1) result += "\"" + nextLine[idScoreEmail] + "\"" + CSV_SEPARATOR; 
                            result += "\r\n";

                            try {
                                FileUtils.write(output_file, result, "UTF-8", true);
                            } catch (IOException ex) {
                                Logger.getLogger("root").error(ex.toString());
                            }
                        }
                        
                        if(content != null){

                            Elements links = content.select("a[href]");
                            Elements links_worepeat = new Elements();
                            
                            for(Element link : links ){                                                            
                            
                                boolean b = false;
                                for(Element link_worepeat : links_worepeat ){
                                    if(link.absUrl("href").equals(link_worepeat.absUrl("href"))){
                                        b = true;
                                        break;
                                    }
                                }
                                
                                if(!b)
                                    links_worepeat.add(link);
                                
                            }
                             
                            for(Element link : links_worepeat ){
                            
                                boolean b = false;
                                link.setBaseUri(researcher_page_url);
                                String clean_name_1 = link.text().replaceAll("[^\\w\\s]", "").toLowerCase();
                                for(String k : cv_keywords_in_name_list){
                                    if(clean_name_1.contains(k)){
                                        b = true;
                                        break;
                                    }
                                }
                                if(b){
                                    Logger.getRootLogger().info("CV found " + link.absUrl("href") + " (" + link.text() + ")");
                                    String href = link.absUrl("href");
                                    
                                    String ext = "";
                                    String score = "";
                                    String type = "CV";                                   
                                    
                                    if(link.absUrl("href").endsWith(".pdf"))
                                        ext = "PDF";
                                    else if(link.absUrl("href").endsWith(".doc"))
                                        ext = "DOC";
                                    else if(link.absUrl("href").endsWith(".docx"))
                                        ext = "DOCX";
                                    else if(link.absUrl("href").endsWith(".rtf"))
                                        ext = "RTF";
                                    else if(link.absUrl("href").endsWith(".txt"))
                                        ext = "TXT";
                                    else
                                        ext = "HTML";
                                       
                                    if(ext.equals("HTML")){
                                        score = "B";                                        
                                    }else{
                                        score = "A";
                                    }
                                    
                                    String result = "";                        
                                    result += "\"" + nextLine[idStaffIdentifier] + "\"" + CSV_SEPARATOR;
                                    result += "\"" + nextLine[idLastName] + "\"" + CSV_SEPARATOR;
                                    result += "\"" + nextLine[idInitials] + "\"" + CSV_SEPARATOR;                                    
                                    if(idFirstName != -1) result += "\"" + nextLine[idFirstName] + "\"" + CSV_SEPARATOR;  
                                    if(idName != -1) result += "\"" + nextLine[idName] + "\"" + CSV_SEPARATOR;  
                                    if(idEmail != -1) result += "\"" + nextLine[idEmail] + "\"" + CSV_SEPARATOR; 
                                    if(idInstitutionName != -1) result += "\"" + nextLine[idInstitutionName] + "\"" + CSV_SEPARATOR;  
                                    if(idWebAddress != -1) result += "\"" + href + "\"" + CSV_SEPARATOR;                 
                                    result += "\"" + href + "\"" + CSV_SEPARATOR;
                                    result += "\"" + ext + "\"" + CSV_SEPARATOR;
                                    result += "\"" + type + "\"" + CSV_SEPARATOR;
                                    result += "\"" + score + "\"" + CSV_SEPARATOR;
                                    if(idScoreEmail != -1) result += "\"" + nextLine[idScoreEmail] + "\"" + CSV_SEPARATOR; 
                                    result += "\r\n";

                                    try {
                                        FileUtils.write(output_file, result, "UTF-8", true);
                                    } catch (IOException ex) {
                                        Logger.getLogger("root").error(ex.toString());
                                    }
                                    
                                }
                                
                                b = false;
                                link.setBaseUri(researcher_page_url);
                                clean_name_1 = link.text().replaceAll("[^\\w\\s]", "").toLowerCase();
                                for(String k : pub_keywords_in_name_list){
                                    if(clean_name_1.contains(k)){
                                        b = true;
                                        break;
                                    }
                                }
                                if(b){
                                    Logger.getRootLogger().info("PUB found " + link.absUrl("href") + " (" + link.text() + ")");
                                    String href = link.absUrl("href");
                                    
                                    String ext = "";
                                    String score = "";
                                    String type = "PUB";                                   
                                    
                                    if(link.absUrl("href").endsWith(".pdf"))
                                        ext = "PDF";
                                    else if(link.absUrl("href").endsWith(".doc"))
                                        ext = "DOC";
                                    else if(link.absUrl("href").endsWith(".docx"))
                                        ext = "DOCX";
                                    else if(link.absUrl("href").endsWith(".rtf"))
                                        ext = "RTF";
                                    else if(link.absUrl("href").endsWith(".txt"))
                                        ext = "TXT";
                                    else
                                        ext = "HTML";
                                       
                                    if(ext.equals("HTML")){
                                        score = "-";                                        
                                    }else{
                                        score = "-";
                                    }
                                    
                                    String result = "";                        
                                    result += "\"" + nextLine[idStaffIdentifier] + "\"" + CSV_SEPARATOR;
                                    result += "\"" + nextLine[idLastName] + "\"" + CSV_SEPARATOR;
                                    result += "\"" + nextLine[idInitials] + "\"" + CSV_SEPARATOR;                                    
                                    if(idFirstName != -1) result += "\"" + nextLine[idFirstName] + "\"" + CSV_SEPARATOR;  
                                    if(idName != -1) result += "\"" + nextLine[idName] + "\"" + CSV_SEPARATOR;  
                                    if(idEmail != -1) result += "\"" + nextLine[idEmail] + "\"" + CSV_SEPARATOR; 
                                    if(idInstitutionName != -1) result += "\"" + nextLine[idInstitutionName] + "\"" + CSV_SEPARATOR;  
                                    if(idWebAddress != -1) result += "\"" + href + "\"" + CSV_SEPARATOR;                 
                                    result += "\"" + href + "\"" + CSV_SEPARATOR;
                                    result += "\"" + ext + "\"" + CSV_SEPARATOR;
                                    result += "\"" + type + "\"" + CSV_SEPARATOR;
                                    result += "\"" + score + "\"" + CSV_SEPARATOR;
                                    if(idScoreEmail != -1) result += "\"" + nextLine[idScoreEmail] + "\"" + CSV_SEPARATOR; 
                                    result += "\r\n";

                                    try {
                                        FileUtils.write(output_file, result, "UTF-8", true);
                                    } catch (IOException ex) {
                                        Logger.getLogger("root").error(ex.toString());
                                    }
                                    
                                }
                            }
                            
                        }                        
                    }

                    reader.close();
                
                }
                
//                    reader = null;
//                    try {
//                        reader = new CSVReader(new FileReader(output_file), CSV_SEPARATOR);
//                    } catch (FileNotFoundException ex) {
//                        Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
//                    }
//
//                    reader.readNext();
//
//                    int newIdResearcherWebpage = 3;
//                    if(idFirstName != -1) newIdResearcherWebpage++; 
//                    if(idName != -1) newIdResearcherWebpage++; 
//                    if(idEmail != -1) newIdResearcherWebpage++; 
//                    if(idInstitutionName != -1) newIdResearcherWebpage++; 
//                    if(idWebAddress != -1) newIdResearcherWebpage++; 
//
//                    List<Object[]> urls_times = new ArrayList<Object[]>();
//                    while ((nextLine = reader.readNext()) != null) 
//                    {
//                        String url = nextLine[newIdResearcherWebpage];
//
//                        Object[] url_time = new Object[2];
//                        url_time[0] = url;
//                        boolean b = false;
//                        for(Object[] u : urls_times){
//                            if(u[0].equals(url_time[0])){
//                                u[1] = (Integer)u[1] + 1;         
//                                b = true;
//                                break;
//                            }
//                        }
//
//                        if(!b){
//                            url_time[1] = new Integer(1);
//                            urls_times.add(url_time);
//                        }
//                    }            
//
//                    reader.close();                    
                
                
//                try {
//                    reader = new CSVReader(new FileReader(output_file), CSV_SEPARATOR);
//                } catch (FileNotFoundException ex) {
//                    Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
//                }
//
//                nextLine = reader.readNext();
//                try {
//                    for(int i = 0; i < nextLine.length; i++)
//                        nextLine[i] = "\"" + nextLine[i] + "\"";
//                    FileUtils.write(output_file, StringUtil.join(Arrays.asList(nextLine), ";") + "\r\n", "UTF-8", false);
//                } catch (IOException ex) {
//                    Logger.getLogger("root").error(ex.toString());
//                }
//                
//                while ((nextLine = reader.readNext()) != null) 
//                {
//                    String url = nextLine[newIdResearcherWebpage];
//                    boolean b = false;
//                    for(Object[] u : urls_times){
//                        if(u[0].equals(url) && ((Integer)u[1] == 1)){                                
//                            b = true;
//                            break;
//                        }
//                    }
//                    
//                    if(b){
//                        try {
//                            for(int i = 0; i < nextLine.length; i++)
//                                nextLine[i] = "\"" + nextLine[i] + "\"";
//                            FileUtils.write(output_file, StringUtil.join(Arrays.asList(nextLine), ";") + "\r\n", "UTF-8", true);
//                        } catch (IOException ex) {
//                            Logger.getLogger("root").error(ex.toString());
//                        }
//                    }
//                }
//                
//                 reader.close();  
                
                
                
            } catch (Exception ex) {
                String error_msg = "Error extracting cv files from extractor " + input_file.getName();
                Logger.getRootLogger().error(error_msg + " - " + ex.toString());                
                if(error_sw != null) error_sw.append(error_msg + "\r\n");
                return;    
            }
        }         
    }
    
    /**
     *
     * @param input_file
     * @param results_dir
     * @param zip_output_file
     * @param output_file_2
     * @param error_sw
     */
    public static void download_files(File input_file, File results_dir, File zip_output_file, File output_file_2, StringWriter error_sw) {
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
        
        try {
            for(int i = 0; i < nextLine.length; i++)
                nextLine[i] = "\"" + nextLine[i] + "\"";
            FileUtils.write(output_file_2, StringUtil.join(Arrays.asList(nextLine), ";") + "\r\n", "UTF-8", false);
        } catch (IOException ex) {
            Logger.getLogger("root").error(ex.toString());
        }        
                
        if(idResearcherWebAddress != -1 && idResearcherWebAddressType != -1 && idResearcherWebAddressExt != -1)
        {                
            Logger.getRootLogger().info("Going to downloads results files");
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException ex) {                
            }
            
            if(!results_dir.exists())
                results_dir.mkdirs();
            
//            File cv_results_dirs = new File(results_dir, "CV");
//            if(!cv_results_dirs.exists())
//                cv_results_dirs.mkdirs();
//                
//            File pub_results_dirs = new File(results_dir, "PUB");
//            if(!pub_results_dirs.exists())
//                pub_results_dirs.mkdirs();
//            
//            File homepage_results_dirs = new File(results_dir, "HOMEPAGE");
//            if(!homepage_results_dirs.exists())
//                homepage_results_dirs.mkdirs();
            
            try {
                while ((nextLine = reader.readNext()) != null) 
                {
                    String url = nextLine[idResearcherWebAddress];
                    String ext = nextLine[idResearcherWebAddressExt];
                    String type = nextLine[idResearcherWebAddressType];
                    String id = nextLine[idStaffIdentifier];

                    try{
                        Logger.getRootLogger().info("Downloading " + url);                        
                                                  
                        String filename = type + "_" + id + "_" + MD5(url) + "." + ext;
                        File dest = null;
//                        if(type.equals("CV"))
//                            dest = new File(cv_results_dirs, filename);
//                        else if(type.equals("PUB"))
//                            dest = new File(pub_results_dirs, filename);
//                        else if(type.equals("HOMEPAGE"))
//                            dest = new File(homepage_results_dirs, filename);
//                        else
                            dest = new File(results_dir, filename);
                        
                        int max = 10;
                        int num = 0;
                        boolean download_finish = false;
                        while(!download_finish){
                            try{
                                Thread.sleep(200);
                                URL fetched_url = Downloader.fetchURL(url);
                                FileUtils.copyURLToFile(fetched_url, dest);
                                download_finish = true;
                            } catch (Exception ex) {
                                Logger.getRootLogger().error("Error downloading " + url, ex);
                                num++;
                            }
                            if(max <= num)
                                throw new Exception("Error download time overflowed");
                        }
                        
                        nextLine[idResearcherWebAddress] = filename;
                        try {
                            for(int i = 0; i < nextLine.length; i++)
                                nextLine[i] = "\"" + nextLine[i] + "\"";
                            FileUtils.write(output_file_2, StringUtil.join(Arrays.asList(nextLine), ";") + "\r\n", "UTF-8", true);
                        } catch (Exception ex) {
                            Logger.getLogger("root").error(ex.toString());
                        }
                        
                    } catch (Exception ex) {
                        Logger.getRootLogger().error("Error manage downloading " + url, ex);
                    }
                }
            } catch (Exception ex) {
                Logger.getRootLogger().error("Error reading " + input_file.getName() + " " + ex.getMessage());
            }

            ZipFile zf;
            try {
                zf = new ZipFile(zip_output_file);                
                zf.createZipFileFromFolder(results_dir, new ZipParameters(), false, 0);
            } catch (Exception ex) {
                Logger.getRootLogger().error("Error zipping results from " + input_file.getName());
            }
            
        }else{
            Logger.getRootLogger().error("Headers incorrect " + input_file.getName());
        }
    }
    
    
    public static void merge_output_files(File input_file, File output_file, File output_file_2){
        
    }
    /**
     *
     * @param md5
     * @return
     */
    public static String MD5(String md5) {
        try {
             java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             byte[] array = md.digest(md5.getBytes());
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < array.length; ++i) {
               sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
             return sb.toString();
         } catch (java.security.NoSuchAlgorithmException e) {
         }
         return "";
     }
     
    
   
    
     
}