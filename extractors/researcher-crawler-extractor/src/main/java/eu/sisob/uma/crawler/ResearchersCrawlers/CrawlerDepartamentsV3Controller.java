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

package eu.sisob.uma.crawler.ResearchersCrawlers;

import au.com.bytecode.opencsv.CSVWriter;
import eu.sisob.uma.api.crawler4j.crawler.CrawlController;
import eu.sisob.uma.crawler.CrawlerTrace;
import eu.sisob.uma.crawler.ProjectLogger;
import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.atteo.evo.inflector.English;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public final class CrawlerDepartamentsV3Controller extends CrawlController
{
    final public  static String WRONG_URLS_RESULT_TAG = "wrong_urls";   
    final public  static String KEYWORDS_RESULT_TAG = "dep_keywords";   
    final public  static String WRONG_KEYWORDS_RESULT_TAG = "dep_wrong_keywords";   
    final public  static String SUBDEPARTMENTS_RESULT_TAG = "subdepartments";    
    final public  static String DEPARTMENTS_RESULT_TAG = "departments";    
    //This tag has got the subject (Department of Chemistry)
    final public  static String DEPARTMENT_OF_RESULT_TAG = "department of ";    
    
    private List<String> wronk_link_keywords;
    private List<String> link_keywords;
    private List<String> link_departments_keywords;
    private List<String> link_subdepartment_keywords;
    private List<Map.Entry<String, String>> link_departments_type_keywords;
    
    private String sContainPattern = "";        
    
    private Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
                                            + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf"
                                            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    
    private TreeMap<String,List<CandidateTypeURL>> interesting_urls_detected;
    
    private String keyword_separator;
    
    public CrawlerTrace crawler_trace;
    
    public CrawlerDepartamentsV3Controller(String storageFolder, File data_dir, List<String> subjects) throws Exception
    {
        super(storageFolder, false);      
        
        wronk_link_keywords = new ArrayList<String>();
        link_keywords = new ArrayList<String>();
        link_departments_keywords = new ArrayList<String>();
        link_subdepartment_keywords = new ArrayList<String>();
        link_departments_type_keywords = new ArrayList<Map.Entry<String, String>>();
        
        interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();
        
        setKeyWordSeparator("/");          
        
        /**
         * ADDING KEYWORDS, ALL KEYWORD ARE PASSED THROUGH getCanonicalName function
         * Departments type key may have got many values separated by keyword_separator
         */
        List<String> lst = null;
        File f = null;
        try
        {
            f = new File(data_dir, DEPARTMENTS_RESULT_TAG + ".keywords");
            lst = FileUtils.readLines(f, "UTF-8");
            for(String keyword : lst){
                keyword = CandidateTypeURL.getCanonicalName(keyword);
                addListLinkKeyWordsOfDepartmentsList(keyword);
            }
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex);
        }
        
        try
        {
            f = new File(data_dir, SUBDEPARTMENTS_RESULT_TAG + ".keywords");
            lst = FileUtils.readLines(f, "UTF-8");
            for(String keyword : lst){
                keyword = CandidateTypeURL.getCanonicalName(keyword);
                addListLinkKeyWordsOfSubDepartmentsList(keyword);
            }
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex);
        } 
        
        try
        {
            f = new File(data_dir, KEYWORDS_RESULT_TAG + ".keywords");
            lst = FileUtils.readLines(f, "UTF-8");
            for(String keyword : lst){
                keyword = CandidateTypeURL.getCanonicalName(keyword);
                addListLinkKeyWords(keyword);
            }
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex);
        }        
        
        
        try
        {
            f = new File(data_dir, WRONG_KEYWORDS_RESULT_TAG + ".keywords");
            lst = FileUtils.readLines(f, "UTF-8");
            for(String keyword : lst){
                keyword = CandidateTypeURL.getCanonicalName(keyword);
                this.addListLinkWrongKeyWords(keyword);
            }
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex);
        } 
        
        //Add subjects
        for(String subject : subjects)
        {   
            String canonice_subject = "";
            for(String sub_subject : subject.split(keyword_separator))
            {
                if(Character.isLetter(sub_subject.charAt(0)))
                    addListLinkKeyWordsOfSubDepartmentsList(String.valueOf(sub_subject.charAt(0)));
                
                canonice_subject += keyword_separator + CandidateTypeURL.getCanonicalName(sub_subject);
                
            }    
                
            addListLinkKeyWordsOfDepartmentsType(subject, canonice_subject.substring(1));            
        }
        
        addPluraliceWords(this.getLstLinkKeyWordsOfDepartmentsType());
        
        if(CrawlerTrace.isTraceUrlsActive())
        {
            try
            {
                String[] filenames = {WRONG_URLS_RESULT_TAG, KEYWORDS_RESULT_TAG, WRONG_KEYWORDS_RESULT_TAG, SUBDEPARTMENTS_RESULT_TAG, DEPARTMENTS_RESULT_TAG, DEPARTMENT_OF_RESULT_TAG};
                crawler_trace = new CrawlerTrace(storageFolder, filenames);
            }
            catch(Exception ex)
            {
                Logger.getLogger("error").error(ex.getMessage(), ex);
                crawler_trace = null;        
            }
        }
        else
        {
            crawler_trace = null;        
        }
            
    }       

    public void setKeyWordSeparator(String s)
    {
        keyword_separator = s;            
    }
    
    public String getKeyWordSeparator()
    {
        return keyword_separator;
    }
    
    public void setContainPattern(String s)
    {
        sContainPattern = s;            
    }
    
    public String getContainPattern()
    {
        return getsContainPattern();
    }
    
    public void clearListLinkWrongKeyWords()
    {
        getLstLinkWrongKeyWords().clear();
    }

    public void addListLinkWrongKeyWords(String sKeyWord)
    {
        getLstLinkWrongKeyWords().add(sKeyWord);
    }

    public void clearListLinkKeyWords()
    {
        getLstLinkKeyWords().clear();
    }

    public void addListLinkKeyWords(String sKeyWord)
    {
        getLstLinkKeyWords().add(sKeyWord);
    }

    public void clearListLinkKeyWordsOfSubDepartmentsList()
    {
        getLstLinkKeyWordsOfSubDepartmentsList().clear();
    }

    public void addListLinkKeyWordsOfSubDepartmentsList(String keyword)
    {
        getLstLinkKeyWordsOfSubDepartmentsList().add(keyword);
    }

    public void clearListLinkKeyWordsOfDepartmentsList()
    {
        getLstLinkKeyWordsOfDepartmentsList().clear();
    }

    public void addListLinkKeyWordsOfDepartmentsList(String keyword)
    {
        getLstLinkKeyWordsOfDepartmentsList().add(keyword);
    }

    public void clearListLinkKeyWordsOfDepartmentsType()
    {
        getLstLinkKeyWordsOfDepartmentsType().clear();
    }

    public void addListLinkKeyWordsOfDepartmentsType(String keyword, String keyword_vars)
    {
        getLstLinkKeyWordsOfDepartmentsType().add(new AbstractMap.SimpleEntry<String, String>(keyword, keyword_vars));
    }    

    public synchronized void clearPossibleResults()
    {
        interesting_urls_detected.clear();
    }

    public synchronized TreeMap<String,List<CandidateTypeURL>> getPossibleResultsTYPE()
    {
        return interesting_urls_detected;
    }
    
    
    public synchronized int getNumberOfLinksOfResults()
    {
        int n = 0;
        for(String key : interesting_urls_detected.keySet())
        {
            n += interesting_urls_detected.get(key).size();
        }
        return n;
    }
        
    public synchronized void addPossibleResults(CandidateTypeURL c)
    {
        if(!interesting_urls_detected.containsKey(c.sType))
        {
           List<CandidateTypeURL> lst = new ArrayList<CandidateTypeURL>();
           //lst.add(c);
           if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tCREATE NEW TYPE <" + c.sType + "> ");
           interesting_urls_detected.put(c.sType, lst);
        }        
        
        try        
        {            
            if (c.sType.equals(DEPARTMENTS_RESULT_TAG))
            {
                if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {                    
                    interesting_urls_detected.get(c.sType).add(c);
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                    if(this.crawler_trace != null) this.crawler_trace.anotate(c.sType, c);
                }
            }
            else if(c.sType.equals(SUBDEPARTMENTS_RESULT_TAG))
            {
                if(!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(DEPARTMENTS_RESULT_TAG), c.sFromURL) || !CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(SUBDEPARTMENTS_RESULT_TAG), c.sFromURL))
                {
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM DEPARTMENT <" + c.sType + "> " + c.toString());
                }
                else if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                    if(this.crawler_trace != null) this.crawler_trace.anotate(c.sType, c);
                }                
            }
            else if (c.sType.contains(CrawlerDepartamentsV3Controller.DEPARTMENT_OF_RESULT_TAG))
            {
                if((!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(DEPARTMENTS_RESULT_TAG), c.sFromURL)) &&
                   (!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(SUBDEPARTMENTS_RESULT_TAG), c.sFromURL)) &&
                   (!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sFromURL)) &&
                   (!this.seeds.contains(c.sFromURL))                        
                  )
                {
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM DEPARTMENT <" + c.sType + "> " + c.toString());
                }
                else if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
//                    else if(checkURLContainInList(interesting_urls_detected.get(c.sType), c.sURL))
//                    {
//                        Logger.getLogger(Literals.NAME_LOG_Crawler).info("\tREFUSE BY CONTAIN <" + c.sType + "> " + c.toString());
//                    }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                    if(this.crawler_trace != null) this.crawler_trace.anotate(CrawlerDepartamentsV3Controller.DEPARTMENT_OF_RESULT_TAG, c);
                }
            }        
        }
        catch(NullPointerException ex)
        {
            ProjectLogger.LOGGER.error("Null pointer exception here => ", ex);
        } 
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error("Exception here => ", ex);
        } 
    }
    


    /**
     * @return the wronk_link_keywords
     */
    public List<String> getLstLinkWrongKeyWords() {
        return wronk_link_keywords;
    }

    /**
     * @param wronk_link_keywords the wronk_link_keywords to set
     */
    public void setLstLinkWrongKeyWords(List<String> lstLinkWrongKeyWords) {
        this.wronk_link_keywords = lstLinkWrongKeyWords;
    }

    /**
     * @return the link_keywords
     */
    public List<String> getLstLinkKeyWords() {
        return link_keywords;
    }

    /**
     * @return the link_departments_keywords
     */
    public List<String> getLstLinkKeyWordsOfDepartmentsList() {
        return link_departments_keywords;
    }

    /**
     * @return the link_subdepartment_keywords
     */
    public List<String> getLstLinkKeyWordsOfSubDepartmentsList() {
        return link_subdepartment_keywords;
    }

    /**
     * @return the link_departments_type_keywords
     */
    public List<Map.Entry<String, String>> getLstLinkKeyWordsOfDepartmentsType() {
        return link_departments_type_keywords;
    }

    /**
     * @return the filters
     */
    public Pattern getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(Pattern filters) {
        this.filters = filters;
    }

    /**
     * @return the sContainPattern
     */
    public String getsContainPattern() {
        return sContainPattern;
    }
    
    public void closeCrawlerTrace()
    {
        if(this.crawler_trace != null)
        {
            this.crawler_trace.close();
        }
    }
    
    
    /**
     * Obtain similar words from subjects word and add to the list
     * @param lst 
     */
    public void addPluraliceWords(List<Map.Entry<String, String>> lst)
    {
        List<Map.Entry<String, String>> new_lst = new ArrayList<Map.Entry<String, String>>();        
        
        for(Map.Entry<String, String> line : lst)
        {            
            String new_line = "";
            for(String words : line.getValue().split(keyword_separator))
            {
                String[] sub_words = words.split(" ");
                
                String new_words = "";
                if(!sub_words[sub_words.length - 1].endsWith("s"))
                {
                    new_words = org.sonatype.nexus.util.Inflector.getInstance().pluralize(sub_words[sub_words.length - 1]);
                }
                else
                {
                    new_words = org.sonatype.nexus.util.Inflector.getInstance().singularize(sub_words[sub_words.length - 1]);
                }
                
                for(int i = 0; i < sub_words.length - 2; i++)
                    new_words = sub_words[i] + " " + new_words; 

                new_line += keyword_separator + words + keyword_separator + new_words;
                
            }     
            Map.Entry<String,String> entry = new AbstractMap.SimpleEntry<String, String>(line.getKey(), new_line.substring(1));
            new_lst.add(entry);
        }        
        
        lst.clear();
        lst.addAll(new_lst);        
    }
     
}
