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

package eu.sisob.uma.crawler.ResearchersCrawlers.deprecated;

import eu.sisob.uma.api.crawler4j.crawler.CrawlController;
import eu.sisob.uma.crawler.ProjectLogger;
import eu.sisob.uma.crawler.ResearchersCrawlers.CandidateTypeURL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public final class CrawlerDepartamentsV2Controller_deprecated extends CrawlController
{
   final boolean traceSearch = true;    
   
    final public  static String SUBDEPARTMENTS_RESULT_TAG = "Subdepartments";
    final public  static String DEPARTMENTS_RESULT_TAG = "Stafflist";    
    
    private List<String> wronk_link_keywords;
    private List<String> link_keywords;
    private List<String> link_departments_keywords;
    private List<String> link_subdepartment_keywords;
    private List<String> link_departments_type_keywords;
    
    private String sContainPattern = "";        
    
    private Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
                                            + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf"
                                            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    
    private TreeMap<String,List<CandidateTypeURL>> interesting_urls_detected;
    
    private String keyword_separator = "";
    
    public CrawlerDepartamentsV2Controller_deprecated(String storageFolder, List<String> subjects) throws Exception
    {
        super(storageFolder, false);      
        
        wronk_link_keywords = new ArrayList<String>();
        link_keywords = new ArrayList<String>();
        link_departments_keywords = new ArrayList<String>();
        link_subdepartment_keywords = new ArrayList<String>();
        link_departments_type_keywords = new ArrayList<String>();
        
        interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();
        
        setKeyWordSeparator("/");          

        addListLinkKeyWordsOfDepartmentsList("Directory");
        addListLinkKeyWordsOfDepartmentsList("Schools");
        addListLinkKeyWordsOfDepartmentsList("Departments");
        addListLinkKeyWordsOfDepartmentsList("Faculties");
        addListLinkKeyWordsOfDepartmentsList("Divisions");
        addListLinkKeyWordsOfDepartmentsList("Faculty");
        addListLinkKeyWordsOfDepartmentsList("Choose a department");
        addListLinkKeyWordsOfDepartmentsList("Choose departments");
        addListLinkKeyWordsOfDepartmentsList("A to Z");
        addListLinkKeyWordsOfDepartmentsList("A-Z");
        addListLinkKeyWordsOfDepartmentsList("A - Z");

        addListLinkKeyWordsOfSubDepartmentsList("Division of Science");
        addListLinkKeyWordsOfSubDepartmentsList("Sciences");
        addListLinkKeyWordsOfSubDepartmentsList("Engineering");
        addListLinkKeyWordsOfSubDepartmentsList("Technology");
        addListLinkKeyWordsOfSubDepartmentsList("Arts");
        addListLinkKeyWordsOfSubDepartmentsList("Medical");
        addListLinkKeyWordsOfSubDepartmentsList("A");
        addListLinkKeyWordsOfSubDepartmentsList("B");
        addListLinkKeyWordsOfSubDepartmentsList("C");
        addListLinkKeyWordsOfSubDepartmentsList("D");
        addListLinkKeyWordsOfSubDepartmentsList("E");
        addListLinkKeyWordsOfSubDepartmentsList("F");
        addListLinkKeyWordsOfSubDepartmentsList("G");
        addListLinkKeyWordsOfSubDepartmentsList("H");
        addListLinkKeyWordsOfSubDepartmentsList("I");
        addListLinkKeyWordsOfSubDepartmentsList("J");
        addListLinkKeyWordsOfSubDepartmentsList("K");
        addListLinkKeyWordsOfSubDepartmentsList("L");
        addListLinkKeyWordsOfSubDepartmentsList("M");
        addListLinkKeyWordsOfSubDepartmentsList("N");
        addListLinkKeyWordsOfSubDepartmentsList("O");
        addListLinkKeyWordsOfSubDepartmentsList("P");
        addListLinkKeyWordsOfSubDepartmentsList("Q");
        addListLinkKeyWordsOfSubDepartmentsList("R");
        addListLinkKeyWordsOfSubDepartmentsList("S");
        addListLinkKeyWordsOfSubDepartmentsList("T");
        addListLinkKeyWordsOfSubDepartmentsList("U");
        addListLinkKeyWordsOfSubDepartmentsList("V");
        addListLinkKeyWordsOfSubDepartmentsList("W");
        addListLinkKeyWordsOfSubDepartmentsList("X");
        addListLinkKeyWordsOfSubDepartmentsList("Y");
        addListLinkKeyWordsOfSubDepartmentsList("Z");

        addListLinkKeyWords("About us");
        addListLinkKeyWords("About ");

        //addListLinkWrongKeyWords(" biom");
        //addListLinkWrongKeyWords("medicine");
        //addListLinkWrongKeyWords("staff");        
        
        for(String subject : subjects)
        {
            addListLinkKeyWordsOfDepartmentsType(subject);
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

    public void addListLinkKeyWordsOfSubDepartmentsList(String sKeyWord)
    {
        getLstLinkKeyWordsOfSubDepartmentsList().add(sKeyWord);
    }

    public void clearListLinkKeyWordsOfDepartmentsList()
    {
        getLstLinkKeyWordsOfDepartmentsList().clear();
    }

    public void addListLinkKeyWordsOfDepartmentsList(String sKeyWord)
    {
        getLstLinkKeyWordsOfDepartmentsList().add(sKeyWord);
    }

    public void clearListLinkKeyWordsOfDepartmentsType()
    {
        getLstLinkKeyWordsOfDepartmentsType().clear();
    }

    public void addListLinkKeyWordsOfDepartmentsType(String sKeyWord)
    {
        getLstLinkKeyWordsOfDepartmentsType().add(sKeyWord);
    }    

    public synchronized void clearPossibleResults()
    {
        //resultsURL.clear();
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
           if(traceSearch) ProjectLogger.LOGGER.info("\tCREATE NEW TYPE <" + c.sType + "> ");
           interesting_urls_detected.put(c.sType, lst);
        }        
        
        try        
        {
            if(c.sType.equals("Subdepartments"))
            {
                if(!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get("Departments"), c.sFromURL) || !CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get("Subdepartments"), c.sFromURL))
                {
                    if(traceSearch) ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM DEPARTMENT <" + c.sType + "> " + c.toString());
                }
                else if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    if(traceSearch) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    if(traceSearch) ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                }
            }
            else if (c.sType.contains("Department of"))
            {
                if((!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get("Departments"), c.sFromURL)) &&
                   (!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get("Subdepartments"), c.sFromURL)) &&
                   (!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sFromURL))
                  )
                {
                    if(traceSearch) ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM DEPARTMENT <" + c.sType + "> " + c.toString());
                }
                else if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    if(traceSearch) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
//                    else if(checkURLContainInList(interesting_urls_detected.get(c.sType), c.sURL))
//                    {
//                        Logger.getLogger(Literals.NAME_LOG_Crawler).info("\tREFUSE BY CONTAIN <" + c.sType + "> " + c.toString());
//                    }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    if(traceSearch) ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                }
            }
            else if (c.sType.equals("Departments"))
            {
                if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    if(traceSearch) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    if(traceSearch) ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
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
    public List<String> getLstLinkKeyWordsOfDepartmentsType() {
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
    
     
}
