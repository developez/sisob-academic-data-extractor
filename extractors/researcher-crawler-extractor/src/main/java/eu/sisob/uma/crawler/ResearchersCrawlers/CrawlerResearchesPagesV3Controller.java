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

import eu.sisob.uma.api.crawler4j.crawler.CrawlController;
import eu.sisob.uma.crawler.CrawlerTrace;
import eu.sisob.uma.crawler.ProjectLogger;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public final class CrawlerResearchesPagesV3Controller extends CrawlController
{
    final public static String WRONG_URLS_RESULT_TAG = "wrong_urls";
    final public static String KEYWORDS_RESULT_TAG = "res_keywords";   
    final public static String WRONG_KEYWORDS_RESULT_TAG = "res_wrong_keywords";   
    
    public final static String RESEARCHER_RESULT_TAG = "researcher";    
    public final static String STAFFLIST_RESULT_TAG = "stafflist";
    public final static String SUBSTAFFLIST_RESULT_TAG = "substafflist";
    
    public final static String RESEARCHER_RESULT_TAG_SUBTYPE_CV = "CV";       
    public final static String RESEARCHER_RESULT_TAG_SUBTYPE_PUB = "PUB";
    
    private Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
                                            + "|png|tiff?|mid|mp2|mp3|mp4" 
                                            + "|wav|avi|mov|mpeg|ram|m4v|pdf|flv"
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
    
    public CrawlerTrace crawler_trace;
    
    public CrawlerResearchesPagesV3Controller(String storageFolder, File data_dir, List<ResearcherNameInfo> researchers) throws Exception            
    {        
        super(storageFolder, false);    
        
        interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();    
        may_be_interesting_urls_detected = new TreeMap<String,List<CandidateTypeURL>>();               
        
        link_wrong_keywords = new ArrayList<String>();
        link_keywords = new ArrayList<String>();
        link_staff_keywords = new ArrayList<String>();
        link_substaff_keywords = new ArrayList<String>();
        researcher_names_info_to_search = new ArrayList<ResearcherNameInfo>();          
        
        List<String> lst = null;
        File f = null;
        try
        {
            f = new File(data_dir, STAFFLIST_RESULT_TAG + ".keywords");
            lst = FileUtils.readLines(f, "UTF-8");
            for(String keyword : lst){
                keyword = CandidateTypeURL.getCanonicalName(keyword);
                addLinkKeyWordsOfStaffList(keyword);
            }
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex);
        }
        
        try
        {
            f = new File(data_dir, SUBSTAFFLIST_RESULT_TAG + ".keywords");
            lst = FileUtils.readLines(f, "UTF-8");
            for(String keyword : lst){
                keyword = CandidateTypeURL.getCanonicalName(keyword);
                addLinkKeyWordsOfSubStaffList(keyword);
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
        
        for(ResearcherNameInfo researcher_name_info : researchers)
        {            
            researcher_name_info.first_name = CandidateTypeURL.getCanonicalName(researcher_name_info.first_name);
            researcher_name_info.last_name = CandidateTypeURL.getCanonicalName(researcher_name_info.last_name);
            researcher_name_info.initial = CandidateTypeURL.getCanonicalName(researcher_name_info.initial);
            researcher_name_info.whole_name = CandidateTypeURL.getCanonicalName(researcher_name_info.whole_name);
            addLinkResearchesNames(researcher_name_info);
            
            for(String sub_last_name : researcher_name_info.last_name.split(" "))
            {
                if(Character.isLetter(sub_last_name.charAt(0)))
                    addLinkKeyWordsOfSubStaffList(String.valueOf(sub_last_name.charAt(0)));
            }
        }    

        clearInterestingUrlsDetected();        
        
        if(CrawlerTrace.isTraceUrlsActive())
        {
            try
            {
                String[] filenames = {WRONG_URLS_RESULT_TAG, KEYWORDS_RESULT_TAG, WRONG_KEYWORDS_RESULT_TAG, STAFFLIST_RESULT_TAG, SUBSTAFFLIST_RESULT_TAG};
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
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM STAFFLIST <" + c.sType + "> " + c.toString());
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
            else if (c.sType.contains(RESEARCHER_RESULT_TAG))
            {     
                    if((!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(SUBSTAFFLIST_RESULT_TAG), c.sFromURL)) &&
                   (!CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(STAFFLIST_RESULT_TAG), c.sFromURL)))
                {
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY NOT FROM STAFFLIST OR SUB <" + c.sType + "> " + c.toString());
                    if(CandidateTypeURL.checkIfURLExistsInList(may_be_interesting_urls_detected.get(c.sType), c.sURL))
                    {
                        if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                    }
                    may_be_interesting_urls_detected.get(c.sType).add(c);
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tADD MAY BE <" + c.sType + "> " + c.toString());

                }
                else if(CandidateTypeURL.checkIfURLExistsInList(interesting_urls_detected.get(c.sType), c.sURL))
                {
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tREFUSE BY REPEAT <" + c.sType + "> " + c.toString());
                }
                else
                {
                    interesting_urls_detected.get(c.sType).add(c);
                    if(CrawlerTrace.isTraceSearchActive()) ProjectLogger.LOGGER.info("\tADD <" + c.sType + "> " + c.toString());                    
                }
            }
            else if (c.sType.equals(STAFFLIST_RESULT_TAG))
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
        if(getInterestingUrlsDetected().get(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG).size() == 0)
        {
            getInterestingUrlsDetected().put(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG, 
                                         getMayBeInterestingUrlsDetected().get(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG));
        }
        
        List<CandidateTypeURL> extra_researcher_web_page_candidates = new ArrayList<CandidateTypeURL>();
        
        List<CandidateTypeURL> researcher_web_page_candidates = getInterestingUrlsDetected().get(RESEARCHER_RESULT_TAG);
        
        for(CandidateTypeURL researcher_web_page_candidate : researcher_web_page_candidates)
        {
            try 
            {                   
                
                List<CandidateTypeURL> sub_extra_researcher_web_pages_candidates = CrawlerResearchesPagesV3.locateSubPages(researcher_web_page_candidate);                                                        
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
    
    public void closeCrawlerTrace()
    {
        if(this.crawler_trace != null)
        {
            this.crawler_trace.close();
        }
    }   
}
