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
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public final class CrawlerResearchesPagesV2Controller_deprecated extends CrawlController
{
    private final static boolean traceSearch = true;
    
    public final static String RESEARCHER_RESULT_TAG = "Researcher";    
    public final static String STAFFLIST_RESULT_TAG = "Stafflist";
    public final static String SUBSTAFFLIST_RESULT_TAG = "Substafflist";
    
    public final static String RESEARCHER_RESULT_TAG_SUBTYPE_CV = "CV";       
    public final static String RESEARCHER_RESULT_TAG_SUBTYPE_PUB = "PUB";
    
    private Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
                                            + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf"
                                            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");    
    
    //public CrawlStat crawlStat = null;
    
    private String sContainPattern = "";
    
    private List<String> link_wrong_keywords;
    private List<String> link_keywords;
    private List<String> link_staff_keywords;
    private List<String> link_substaff_keywords;
    private List<ResearcherNameInfo> researcher_names_info_to_search;         
    
    private TreeMap<String,List<CandidateTypeURL>> interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();    
    private TreeMap<String,List<CandidateTypeURL>> may_be_interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();   
    
    public CrawlerResearchesPagesV2Controller_deprecated(String storageFolder, List<ResearcherNameInfo> researchers) throws Exception            
    {        
        super(storageFolder, false);    
        
        interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();    
        may_be_interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();               
        
        link_wrong_keywords = new ArrayList<String>();
        link_keywords = new ArrayList<String>();
        link_staff_keywords = new ArrayList<String>();
        link_substaff_keywords = new ArrayList<String>();
        researcher_names_info_to_search = new ArrayList<ResearcherNameInfo>();          
        
        addListLinkKeyWords("About us");
        addListLinkKeyWords("About ");

        addLinkKeyWordsOfStaffList("People");
        addLinkKeyWordsOfStaffList("Staff");

        addLinkKeyWordsOfStaffList("Contact ");
        addLinkKeyWordsOfStaffList("Lectures");
        addLinkKeyWordsOfStaffList("Professors");
        addLinkKeyWordsOfStaffList("Faculty");
        addLinkKeyWordsOfStaffList("Faculty Directory");
        addLinkKeyWordsOfStaffList("Faculty Research");
        addLinkKeyWordsOfSubStaffList("Researchers");
        addLinkKeyWordsOfSubStaffList("Research Associates");
        addLinkKeyWordsOfSubStaffList("Research Associates");
        addLinkKeyWordsOfSubStaffList("A to Z");
        addLinkKeyWordsOfSubStaffList("A - Z");          
        
        for(ResearcherNameInfo researcher_name_info : researchers)
        {
            addLinkResearchesNames(researcher_name_info);
        }    

        clearInterestingUrlsDetected();        
    } 
    
    public void setContainPattern(String s)
    {
        sContainPattern = s;            
    }
    
    public void clearListLinkWrongKeyWords()
    {
        getLinkWrongKeywords().clear();
    }

    public void addListLinkWrongKeyWords(String sKeyWord)
    {
        getLinkWrongKeywords().add(sKeyWord);
    }

    public void clearListLinkKeyWords()
    {
        getLinkKeywords().clear();
    }

    public void addListLinkKeyWords(String sKeyWord)
    {
        getLinkKeywords().add(sKeyWord);
    }

    public void clearLinkKeyWordsOfStaffList()
    {
        getLinkStaffKeywords().clear();
    }

    public void addLinkKeyWordsOfStaffList(String sKeyWord)
    {
        getLinkStaffKeywords().add(sKeyWord);
    }

    public void clearLinkKeyWordsOfSubStaffList()
    {
        getLinkSubstaffKeywords().clear();
    }

    public void addLinkKeyWordsOfSubStaffList(String sKeyWord)
    {
        getLinkSubstaffKeywords().add(sKeyWord);
    }

    public void addLinkResearchesNames(ResearcherNameInfo researcher_name_info)
    {
        getResearcherNamesInfoToSearch().add(researcher_name_info);
    }

    public void clearLinkResearchesNames()
    {
        getResearcherNamesInfoToSearch().clear();
    }          

    
    public synchronized void clearInterestingUrlsDetected()
    {            
        interesting_urls_detected.clear();
        may_be_interesting_urls_detected.clear();
        
        interesting_urls_detected.put(RESEARCHER_RESULT_TAG, new ArrayList<CandidateTypeURL>());
        interesting_urls_detected.put(STAFFLIST_RESULT_TAG, new ArrayList<CandidateTypeURL>());
        interesting_urls_detected.put(SUBSTAFFLIST_RESULT_TAG, new ArrayList<CandidateTypeURL>());
        may_be_interesting_urls_detected.put(RESEARCHER_RESULT_TAG, new ArrayList<CandidateTypeURL>());
        may_be_interesting_urls_detected.put(STAFFLIST_RESULT_TAG, new ArrayList<CandidateTypeURL>());
        may_be_interesting_urls_detected.put(SUBSTAFFLIST_RESULT_TAG, new ArrayList<CandidateTypeURL>());        
    }

    public synchronized TreeMap<String,List<CandidateTypeURL>> getInterestingUrlsDetected()
    {
        return interesting_urls_detected;
    }
    
    public synchronized TreeMap<String,List<CandidateTypeURL>> getMayBeInterestingUrlsDetected()
    {
        return may_be_interesting_urls_detected;
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

//    public void iniCrawlStat()
//    {
//        crawlStat = new CrawlStat();
//    }

    /**
     * @return the filters
     */
    public Pattern getFilters() {
        return filters;
    }

    /**
     * @return the link_wrong_keywords
     */
    public List<String> getLinkWrongKeywords() {
        return link_wrong_keywords;
    }

    /**
     * @return the link_keywords
     */
    public List<String> getLinkKeywords() {
        return link_keywords;
    }

    /**
     * @return the link_staff_keywords
     */
    public List<String> getLinkStaffKeywords() {
        return link_staff_keywords;
    }

    /**
     * @return the link_substaff_keywords
     */
    public List<String> getLinkSubstaffKeywords() {
        return link_substaff_keywords;
    }

    /**
     * @return the researcher_names_info_to_search
     */
    public List<ResearcherNameInfo> getResearcherNamesInfoToSearch() {
        return researcher_names_info_to_search;
    }

    /**
     * @param lstLinkResearchesNames 
     */
    public void setResearcherNamesInfoToSearch(List<ResearcherNameInfo> lstLinkResearchesNames) {
        this.researcher_names_info_to_search = lstLinkResearchesNames;
    }

    /**
     * @return the sContainPattern
     */
    public String getContainPattern() {
        return sContainPattern;
    }    
    
    public synchronized void addPossibleResults(CandidateTypeURL c)
    {          
        try
        {
            if(c.sType.equals(SUBSTAFFLIST_RESULT_TAG))
            {
                if(!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(STAFFLIST_RESULT_TAG), c.sFromURL))
                {
                    ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM STAFFLIST <" + c.sType + "> " + c.toString());
                }
                else if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                }
            }
            else if (c.sType.contains(RESEARCHER_RESULT_TAG))
            {     
                if((!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(SUBSTAFFLIST_RESULT_TAG), c.sFromURL)) &&
                   (!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(STAFFLIST_RESULT_TAG), c.sFromURL)))
                {
                    ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM STAFFLIST OR SUB <" + c.sType + "> " + c.toString());
                    may_be_interesting_urls_detected.get(c.sType).add(c);
                    ProjectLogger.LOGGER.info("\tADD MAY BE <" + c.sType + "> " + c.toString());

                }
                else if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                }
            }
            else if (c.sType.equals(STAFFLIST_RESULT_TAG))
            {
                if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());
                }
            }        
        }
        catch(NullPointerException ex)
        {
            ProjectLogger.LOGGER.error("Null pointer exception here => ", ex);
        }        
    }
    
    
    
    /**
     * Apply some operation against candidate researchers web pages (from results data arrays)
     *  - Copy may be results if results are empty
     *  - Search more webpages from webpages (cv pdf files, publication links, etc)     
     */
    public void postProcessResults()
    {
        if(getInterestingUrlsDetected().get(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG).size() == 0)
        {
            getInterestingUrlsDetected().put(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG, 
                                         getMayBeInterestingUrlsDetected().get(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG));
        }
        
        List<CandidateTypeURL> extra_researcher_web_page_candidates = new ArrayList<CandidateTypeURL>();
        
        List<CandidateTypeURL> researcher_web_page_candidates = getInterestingUrlsDetected().get(RESEARCHER_RESULT_TAG);
        
        for(CandidateTypeURL researcher_web_page_candidate : researcher_web_page_candidates)
        {
            try 
            {                   
                
                List<CandidateTypeURL> sub_extra_researcher_web_pages_candidates = 
                        CrawlerResearchesPagesV2_deprecated.locateSubPages(researcher_web_page_candidate);                        
                extra_researcher_web_page_candidates.addAll(sub_extra_researcher_web_pages_candidates);
                
            } catch (UnsupportedEncodingException ex) {
                ProjectLogger.LOGGER.error(ex);
            } catch (FileNotFoundException ex) {
                ProjectLogger.LOGGER.error(ex);
            } catch (IOException ex) {
                ProjectLogger.LOGGER.error(ex);
            } catch (InterruptedException ex) {
                ProjectLogger.LOGGER.error(ex);
            }
        }
        
        researcher_web_page_candidates.addAll(extra_researcher_web_page_candidates);
    }   

}
