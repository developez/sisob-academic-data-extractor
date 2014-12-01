
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

package eu.sisob.uma.extractors.adhoc.email;

import au.com.bytecode.opencsv.CSVReader;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import eu.sisob.uma.footils.Web.Downloader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.helper.StringUtil;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class EmailExtractor {
    
    final static String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    final static int MAX_MAIL_PER_PAGE = 6;
    final static char CSV_SEPARATOR = ';';
    final static int max_in_mem = 100;
    final static boolean test_only_output = false;
    
    /**
     *
     * @param input_file
     * @param data_dir
     * @param output_file
     * @param norepeat_output_file
     * @param notfound_output_file
     * @param notfound_norepeat_output_file
     * @param filters
     * @param error_sw
     */
    public static void extract_emails(File input_file, File data_dir, File output_file, File norepeat_output_file, File notfound_output_file, File notfound_norepeat_output_file, List<String> filters, StringWriter error_sw)
    {        
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(input_file), CSV_SEPARATOR);
        } catch (FileNotFoundException ex) {
            Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
        }
        
        int idStaffIdentifier= -1;int idName= -1;int idFirstName= -1;int idLastName= -1;int idInitials= -1;int idUnitOfAssessment_Description= -1;
        int idInstitutionName= -1;int idWebAddress= -1;int idResearchGroupDescription= -1;int idResearcherWebAddress = -1;int idResearcherWebAddressType = -1;int idResearcherWebAddressExt = -1;
        int idScoreUrl = -1;
        
        String filter_literal = "(";
        for(String filter : filters){
            filter_literal += filter + ",";
        }
        filter_literal += ")";
        
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
                
        if(idResearcherWebAddress != -1 && idStaffIdentifier != -1 && idLastName != -1 && idInitials != -1)
        {                
            //if(!test_only_output)
            {
                try {
                    String header = "";
                    header += "\"" + FileFormatConversor.CSV_COL_ID + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_LASTNAME + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_INITIALS + "\"" + CSV_SEPARATOR;  
                    if(idFirstName != -1) header += "\"" + FileFormatConversor.CSV_COL_INITIALS + "\"" + CSV_SEPARATOR;  
                    if(idName != -1) header += "\"" + FileFormatConversor.CSV_COL_NAME + "\"" + CSV_SEPARATOR;  
                    header += "\"" + FileFormatConversor.CSV_COL_EMAIL + "\"" + CSV_SEPARATOR;  
                    if(idInstitutionName != -1) header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_NAME + "\"" + CSV_SEPARATOR;  
                    if(idWebAddress != -1) header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_URL + "\"" + CSV_SEPARATOR;  
                    header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_URL + "\"" + CSV_SEPARATOR;
                    if(idResearcherWebAddressExt != -1) header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_EXT + "\"" + CSV_SEPARATOR;
                    if(idResearcherWebAddressType != -1) header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_TYPE + "\"" + CSV_SEPARATOR;
                    if(idScoreUrl != -1) header += "\"" + FileFormatConversor.CSV_COL_SCORE_URL + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_SCORE_EMAIL + "\"";
                    header += "\r\n";
                    FileUtils.write(output_file, header, "UTF-8", false);

                    header = "";
                    header += "\"" + FileFormatConversor.CSV_COL_ID + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_LASTNAME + "\"" + CSV_SEPARATOR;
                    header += "\"" + FileFormatConversor.CSV_COL_INITIALS + "\"" + CSV_SEPARATOR;  
                    if(idFirstName != -1) header += "\"" + FileFormatConversor.CSV_COL_INITIALS + "\"" + CSV_SEPARATOR;  
                    if(idName != -1) header += "\"" + FileFormatConversor.CSV_COL_NAME + "\"" + CSV_SEPARATOR;                  
                    if(idInstitutionName != -1) header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_NAME + "\"" + CSV_SEPARATOR;  
                    if(idWebAddress != -1) header += "\"" + FileFormatConversor.CSV_COL_INSTITUTION_URL + "\"" + CSV_SEPARATOR;  
                    header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_URL + "\"" + CSV_SEPARATOR;
                    if(idResearcherWebAddressExt != -1) header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_EXT + "\"" + CSV_SEPARATOR;
                    if(idResearcherWebAddressType != -1) header += "\"" + FileFormatConversor.CSV_COL_RESEARCHER_PAGE_TYPE + "\"" + CSV_SEPARATOR;
                    if(idScoreUrl != -1) header += "\"" + FileFormatConversor.CSV_COL_SCORE_URL + "\"";                
                    header += "\r\n";

                    FileUtils.write(notfound_output_file, header, "UTF-8", false);                

                } catch (IOException ex) {
                    Logger.getLogger("root").error(ex.toString());
                    error_sw.append("Error creating output files\r\n");
                }      
            }
            
            try
            { 
                //if(!test_only_output)
                {
                    Pattern p1 = Pattern.compile("([a-zA-Z0-9#._-]+)+");

                    while ((nextLine = reader.readNext()) != null) 
                    {                 
                        nextLine[idLastName] = nextLine[idLastName].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                        nextLine[idInitials] = nextLine[idInitials].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                        if(idFirstName != -1) nextLine[idFirstName] = nextLine[idFirstName].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                        if(idName != -1) nextLine[idName] = nextLine[idName].replaceAll("[^a-zA-Z]", " ").toLowerCase();

                        String content = "";
                        String researcher_page_url = nextLine[idResearcherWebAddress];
                        Logger.getLogger("root").info("Go with " + researcher_page_url);
                        if(p1.matcher(researcher_page_url).matches()){                    

                            File f = new File(data_dir, researcher_page_url);  

                            if(researcher_page_url.endsWith(".doc") ||
                               researcher_page_url.endsWith(".docx")){                                                        

                                Logger.getLogger("root").error("The document " + researcher_page_url + " could not loaded");                    
                                error_sw.append("The document " + researcher_page_url + " could not loaded");                           

                            }else if(researcher_page_url.endsWith(".pdf")){

                                PDFParser parser = null;                            
                                PDFTextStripper pdfStripper = null;
                                PDDocument pdDoc = null;
                                COSDocument cosDoc = null;                           

                                try {
                                    parser = new PDFParser(new FileInputStream(f));
                                } catch (IOException e) {                                
                                    Logger.getLogger("root").error(e.toString());
                                    error_sw.append("Unable to open PDF called " + researcher_page_url);                                   
                                }

                                if(parser != null){
                                    try {
                                        parser.parse();
                                        cosDoc = parser.getDocument();
                                        pdfStripper = new PDFTextStripper();
                                        pdDoc = new PDDocument(cosDoc);
                                        pdfStripper.setStartPage(1);
                                        pdfStripper.setEndPage(2);
                                        content = pdfStripper.getText(pdDoc);
                                    } catch (Exception e) {                                    
                                            Logger.getLogger("root").error(e.toString());                                                                           
                                            error_sw.append("An exception occured in parsing the PDF Document.");                                        
                                    } finally {                                
                                        try {
                                            if (cosDoc != null)
                                                    cosDoc.close();
                                            if (pdDoc != null)
                                                    pdDoc.close();
                                        } catch (Exception e) {
                                             Logger.getLogger("root").error(e.toString());
                                        }
                                    }         
                                }
                            }

                        }else{
                            
                            try{
                                Logger.getRootLogger().info("Reading " + researcher_page_url);

                                File temp; 
                                
                                temp = File.createTempFile("temp-file-name", ".tmp");
                                URL fetched_url = Downloader.fetchURL(researcher_page_url);                        
                                FileUtils.copyURLToFile(fetched_url, temp);                                
                                long sizeInBytes = temp.length();
                                long sizeInMb = sizeInBytes / (1024 * 1024);
                                if(sizeInMb > 100){
                                    content = "";
                                }else{
                                    content = FileUtils.readFileToString(temp);                                    
                                    temp.delete();
                                }
                                
                            }catch(Exception ex){
                                Logger.getLogger("root").error("" + researcher_page_url + " could not loaded", ex);
                                error_sw.append("" + researcher_page_url + " could not loaded");
                                content = "";
                            }catch(java.lang.OutOfMemoryError ex2){
                                Logger.getLogger("root").error( researcher_page_url + " could not loaded (Jsoup OutOfMemoryError)", ex2);
                                error_sw.append("" + researcher_page_url + " could not loaded");
                                content = "";
                            }                         
                            
                        }


                        if(!content.equals("")){


                            //final String RE_MAIL = "([\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Za-z]{2,4})";
                            final String RE_MAIL = "([\\w\\-]([\\.\\w]){1,16}[\\w]{1,16}@([\\w\\-]{1,16}\\.){1,16}[A-Za-z]{2,4})";
                            Pattern p = Pattern.compile(RE_MAIL);
                            Matcher m = p.matcher(content);
                            List<String> emails = new ArrayList<String>();
                            while(m.find()) {
                                String email = m.group(1);                                
                                
                                if(!emails.contains(email))
                                {
                                    // Apply filter
                                    boolean pass = true;
                                    if(filters.size() > 0){
                                        pass = false;
                                        for(String filter : filters){

                                            String filter2 = filter.replace("*", ".*?");
                                            Pattern pattern  = Pattern.compile(filter2);
                                            if(pattern.matcher(email).matches()){
                                                pass = true;
                                                break;
                                            }else{

                                            }
                                        }
                                    }                                

                                    if(pass){
                                        Logger.getRootLogger().info(researcher_page_url + " => " + email + " PASS FILTER! " + filter_literal);    
                                        emails.add(email);                            
                                    }else{
                                        Logger.getRootLogger().info(researcher_page_url + " => " + email + " REFUSE BY FILTER! " + filter_literal);      
                                    }
                                }
                            }

                            if(emails.size() < MAX_MAIL_PER_PAGE)
                            {                         
                                for(String email : emails){

                                    String score_email = "";
                                    String lastname = nextLine[idLastName];
                                    if(lastname.length() > 5)
                                        lastname = lastname.substring(0, 6);                                    
                                    
                                    if(email.toLowerCase().contains(lastname)){
                                        score_email = "A";                                        
                                    } else {
                                        int temp_id = idFirstName;
                                        if(temp_id == -1)
                                            temp_id =  idInitials;
                                            
                                        if(!nextLine[idInitials].trim().equals("")){
                                            
                                            String firstname = nextLine[temp_id].split(" ")[0];
                                            if(firstname.length() > 5)
                                                firstname = firstname.substring(0, 5);
                                            if(firstname.length() > 1){
                                                if(email.toLowerCase().contains(firstname)){
                                                    score_email = "A";                                        
                                                }
                                            }                                            
                                        }
                                        
                                        if(score_email.equals("")){
                                            String initials = "";
                                            
                                            String[] arr = nextLine[temp_id].split(" ");
                                            for(int i = 0; i < arr.length; i++){
                                                if(arr[i].length() > 0) 
                                                    initials += arr[i].charAt(0);
                                            }
                                            initials += nextLine[idLastName].charAt(0);
                                            
                                            if(email.toLowerCase().contains(initials)){
                                                score_email = "B";                                        
                                            }
                                            else{
                                                score_email = "Z";                                        
                                            }
                                        }
                                        
                                    }                                    

                                    String result = "";                        
                                    result += "\"" + nextLine[idStaffIdentifier] + "\"" + CSV_SEPARATOR;
                                    result += "\"" + nextLine[idLastName] + "\"" + CSV_SEPARATOR;
                                    result += "\"" + nextLine[idInitials] + "\"" + CSV_SEPARATOR;                                    
                                    if(idFirstName != -1) result += "\"" + nextLine[idFirstName] + "\"" + CSV_SEPARATOR;  
                                    if(idName != -1) result += "\"" + nextLine[idName] + "\"" + CSV_SEPARATOR;  
                                    result += "\"" + email + "\"" + CSV_SEPARATOR;
                                    if(idInstitutionName != -1) result += "\"" + nextLine[idInstitutionName] + "\"" + CSV_SEPARATOR;  
                                    if(idWebAddress != -1) result += "\"" + nextLine[idWebAddress] + "\"" + CSV_SEPARATOR;                 
                                    result += "\"" + nextLine[idResearcherWebAddress] + "\"" + CSV_SEPARATOR;
                                    if(idResearcherWebAddressExt != -1) result += "\"" + nextLine[idResearcherWebAddressExt] + "\"" + CSV_SEPARATOR;
                                    if(idResearcherWebAddressType != -1) result += "\"" + nextLine[idResearcherWebAddressType] + "\"" + CSV_SEPARATOR;
                                    if(idScoreUrl != -1) result += "\"" + nextLine[idScoreUrl] + "\"" + CSV_SEPARATOR;
                                    result += "\"" + score_email + "\"";

                                    result += "\r\n";

                                    try {
                                        FileUtils.write(output_file, result, "UTF-8", true);
                                    } catch (IOException ex) {
                                        Logger.getLogger("root").error(ex.toString());
                                    }
                                }
                            }
                            else
                            {
                                content = "";
                            }

                            if(emails.size() == 0) 
                                content = "";
                        }

                        if(content == ""){

                            String result = "";                        
                            result += "\"" + nextLine[idStaffIdentifier] + "\"" + CSV_SEPARATOR;
                            result += "\"" + nextLine[idLastName] + "\"" + CSV_SEPARATOR;
                            result += "\"" + nextLine[idInitials] + "\"" + CSV_SEPARATOR;                                    
                            if(idFirstName != -1) result += "\"" + nextLine[idFirstName] + "\"" + CSV_SEPARATOR;  
                            if(idName != -1) result += "\"" + nextLine[idName] + "\"" + CSV_SEPARATOR;                              
                            if(idInstitutionName != -1) result += "\"" + nextLine[idInstitutionName] + "\"" + CSV_SEPARATOR;  
                            if(idWebAddress != -1) result += "\"" + nextLine[idWebAddress] + "\"" + CSV_SEPARATOR;                 
                            result += "\"" + nextLine[idResearcherWebAddress] + "\"" + CSV_SEPARATOR;
                            if(idResearcherWebAddressExt != -1) result += "\"" + nextLine[idResearcherWebAddressExt] + "\"" + CSV_SEPARATOR;
                            if(idResearcherWebAddressType != -1) result += "\"" + nextLine[idResearcherWebAddressType] + "\"" + CSV_SEPARATOR;
                            if(idScoreUrl != -1) result += "\"" + nextLine[idScoreUrl] + "\"";

                            result += "\r\n";

                            try {
                                FileUtils.write(notfound_output_file, result, "UTF-8", true);
                            } catch (IOException ex) {
                                Logger.getLogger("root").error(ex.toString());
                            }
                        }
                    }

                    reader.close();
                }
                
                Logger.getLogger("root").info("Applying deduplication algoritm - Counting duplications");
                
                boolean finish = false;                
                String alternate_filename_1 = "file1";                
                String alternate_filename_2 = "file2";
                
                File alternate_file_s = new File(output_file.getParentFile(), alternate_filename_1);
                File alternate_file_d = new File(output_file.getParentFile(), alternate_filename_2);
                
                FileUtils.copyFile(output_file, alternate_file_s);
                
                //FileUtils.write(output_file_wor_notfound, "", "UTF-8", false);
                FileUtils.write(norepeat_output_file, "", "UTF-8", false);
                
                while(!finish)
                {                    
                    reader = null;
                    try {
                        reader = new CSVReader(new FileReader(alternate_file_s), CSV_SEPARATOR);
                    } catch (FileNotFoundException ex) {
                        Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
                    }                

                    HashMap<String, Integer> count_dictionary = new HashMap<String, Integer>();
                    int idEmail = 3;
                    if(idFirstName != -1)
                        idEmail++;
                    if(idName != -1)
                        idEmail++;
                    
                    try {                        
                        FileUtils.write(alternate_file_d, "", "UTF-8", false);
                    } catch (IOException ex) {
                        Logger.getLogger("root").error(ex.toString());
                    }                    
                    finish = true;
                    while ((nextLine = reader.readNext()) != null) 
                    {   
                        Integer count = 1;
                        if(count_dictionary.containsKey(nextLine[idEmail].toString()))
                            count = count_dictionary.get(nextLine[idEmail].toString());
                        else
                        {
                            if(count_dictionary.size() < max_in_mem){
                                count_dictionary.put(nextLine[idEmail].toString(), count + 1);                            
                            }else{
                                try {
                                    for(int i = 0; i < nextLine.length; i++)
                                        nextLine[i] = "\"" + nextLine[i] + "\"";
                                    FileUtils.write(alternate_file_d, StringUtil.join(Arrays.asList(nextLine), String.valueOf(CSV_SEPARATOR)) + "\r\n", "UTF-8", true);
                                    finish = false;
                                } catch (IOException ex) {
                                    Logger.getLogger("root").error(ex.toString());
                                }
                            }
                        }                        
                    }

                    reader.close();

                    Logger.getLogger("root").info("Applying deduplication algoritm - Removing duplications");

                    reader = null;
                    try {
                        reader = new CSVReader(new FileReader(alternate_file_s), CSV_SEPARATOR);
                    } catch (FileNotFoundException ex) {
                        Logger.getRootLogger().error("Error reading " + input_file.getName() + " - " + ex.toString());
                    }    

                    String previous_id = "%previous%";
                    String previous_email = "%previous_email%";
                    List<String[]> cache = new ArrayList<String[]>();
                    
                    while ((nextLine = reader.readNext()) != null)
                    {   
                        String id = nextLine[idStaffIdentifier].toString();

                        if(previous_id.equals(id))
                        {
                            cache.add(nextLine);
                            previous_id = id;          
                        }
                        else
                        {
                            //Process
                            String[] winner_line = null;
                            String max_score = "Z";
                            for(String[] act_line : cache)
                            {
                                String act_score = "Z";
                                try
                                {
                                    act_score = act_line[act_line.length-1];
                                }
                                catch(Exception ex){}

                                String email = act_line[idEmail].toString();

                                if(count_dictionary.containsKey(email) && count_dictionary.get(email) > 0)
                                {
                                    if(max_score.compareTo(act_score) > 0 && !act_score.equals("")){
                                        winner_line = act_line;
                                        max_score = act_score;
                                    }

                                    count_dictionary.put(email, 0);
                                } 
                            }

                            if(winner_line != null)
                            {
                                try {
                                    for(int i = 0; i < winner_line.length; i++)
                                        winner_line[i] = "\"" + winner_line[i] + "\"";
                                    FileUtils.write(norepeat_output_file, StringUtil.join(Arrays.asList(winner_line), String.valueOf(CSV_SEPARATOR)) + "\r\n", "UTF-8", true);
                                } catch (IOException ex) {
                                    Logger.getLogger("root").error(ex.toString());
                                }

                            }
                            else
                            {
    //                            try {
    //                                FileUtils.write(output_file_wor_notfound, StringUtil.join(Arrays.asList(winner_line), String.valueOf(CSV_SEPARATOR)) + "\r\n", "UTF-8", true);
    //                            } catch (IOException ex) {
    //                                Logger.getLogger("root").error(ex.toString());
    //                            }
                            }

                            cache.clear();

                            cache.add(nextLine);

                            previous_id = id;                        
                        }

                    }

                    //Process
                    if(cache.size() > 0)
                    {                    
                        String[] winner_line = null;
                        String max_score = "Z";
                        for(String[] act_line : cache)
                        {
                            String act_score = "Z";
                            try
                            {
                                act_score = (act_line[act_line.length-1]);
                            }
                            catch(Exception ex){}
                            String email = act_line[idEmail];
                            if(count_dictionary.containsKey(email) && count_dictionary.get(email) > 0)
                            {
                                if(max_score.compareTo(act_score) > 0 && !act_score.equals("")){
                                    winner_line = act_line;
                                    max_score = act_score;
                                }

                                count_dictionary.put(email, 0);
                            }   
                        }

                        if(winner_line != null)
                        {
                            
                            
                            try {
                                for(int i = 0; i < winner_line.length; i++)
                                    winner_line[i] = "\"" + winner_line[i] + "\"";
                                FileUtils.write(norepeat_output_file, StringUtil.join(Arrays.asList(winner_line), String.valueOf(CSV_SEPARATOR)) + "\r\n", "UTF-8", true);
                            } catch (IOException ex) {
                                Logger.getLogger("root").error(ex.toString());
                            }                        
                        }
                        else
                        {
    //                        try {
    //                            FileUtils.write(output_file_wor_notfound, StringUtil.join(Arrays.asList(winner_line), String.valueOf(CSV_SEPARATOR)) + "\r\n", "UTF-8", true);
    //                        } catch (IOException ex) {
    //                            Logger.getLogger("root").error(ex.toString());
    //                        }
                        }
                    }

                    reader.close();
                    
                    //
                    if(!finish)
                    {                        
                        FileUtils.copyFile(alternate_file_d, alternate_file_s);                              
                        alternate_file_s = new File(output_file.getParentFile(), alternate_filename_1);
                        alternate_file_d = new File(output_file.getParentFile(), alternate_filename_2);
                    }
                }
                
                FileUtils.forceDelete(alternate_file_s);
                FileUtils.forceDelete(alternate_file_d);
                
                Logger.getLogger("root").info("Applying deduplication algoritm - Finish");
                
            } catch (Exception ex) {
                String error_msg = "Error extracting emails from extractor " + input_file.getName();
                Logger.getRootLogger().error(error_msg + " - " + ex.toString());                
                if(error_sw != null) error_sw.append(error_msg + "\r\n");
                return;    
            }
        }         
    }
}
