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
import java.util.List;

import eu.sisob.uma.api.crawler4j.crawler.WebCrawler;
import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.crawler.CrawlerTrace;
import eu.sisob.uma.crawler.ProjectLogger;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.XMLTags;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public class CrawlerResearchesPagesV3 extends WebCrawler
{         
    CrawlerResearchesPagesV3Controller refController = null;    
    
    @Override
    public void setMyController(CrawlController myController) 
    {            
        super.setMyController(myController);     
        
        if(myController != null)
        {
            refController = (CrawlerResearchesPagesV3Controller)myController;            
        }
        else
            refController = null;        
    } 

    static String URL1 = "http://www.amc.edu/research/cbc/faculty.cfm";
    static String URL2 = "http://www.amc.edu/research/img/faculty.cfm";
    static String URL3 = "http://www.bcm.edu/molvir/appointments";
    static String PURL1 = "http://www.amc.edu/research/cbc/faculty.cfm";
    static String PURL2 = "http://www.amc.edu/research/imd/faculty.cfm";
    static String PURL3 = "http://www.amc.edu/Research/CBC/faculty.cfm";
    
    public boolean shouldVisit(WebURL url)
    {           
        String associate_text = CandidateTypeURL.getCanonicalName(url.getAssociateText());        
        
        String associate_text_no_double_spaces = url.getAssociateText().trim().replace("\r\n", " ").replace("\r", " ").replace("\n", " ").replace("\t", " ");
        while(associate_text_no_double_spaces.contains("  ")) associate_text_no_double_spaces = associate_text_no_double_spaces.replace("  ", " ");
        
        String href = url.getURL();//;.toLowerCase();
        String parentURL = url.getParentUrl();//.toLowerCase();       

        if(
           href.equals(URL1) ||
           href.equals(URL2) ||
           href.equals(URL3) ||
           parentURL.equals(PURL1) ||
           parentURL.equals(PURL2) ||
           parentURL.equals(PURL3)
           )
        {          
            if(refController.crawler_trace != null)
            {
                if(CrawlerTrace.isTraceSearchActive()) 
                {
                    ProjectLogger.LOGGER.info("\tTEXT: '" + associate_text + "' HREF: '" + href + "' HREF_PARENT: '" + parentURL + "'");    
                }
            }
            //FIXME
            boolean b = true;
            b = false;                
        }        
        
        if(associate_text_no_double_spaces.split(" ").length > 10)
        {
            if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerResearchesPagesV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "TOO MANY KEY" + "\t" + href + "\t" + parentURL);
            return false;
        }

        /*
         * Control sentences
         */
        if(url.getAssociateText().equals(CrawlController.SEED_TAG)) 
            return true;        

        for(String s : refController.getLinkWrongKeywords())
        {
            //if(associate_text.toLowerCase().contains(s.toLowerCase()))
            if(CandidateTypeURL.containsTheWorkInLink(associate_text, s))
            {
                if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerResearchesPagesV3Controller.WRONG_KEYWORDS_RESULT_TAG, associate_text);
                if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerResearchesPagesV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "WRONG KEYWORD" + "\t" + href + "\t" + parentURL);
                return false;
            }
        }
        
        if (refController.getFilters().matcher(href).matches()){
            if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerResearchesPagesV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "FILTER" + "\t" + href + "\t" + parentURL);
            return false;
        }        

        boolean found = false;
        /*
         * Link must be visited if associate text any contains staff list keywords
         * Also, we will  try to add that url as STAFFLIST_RESULT_TAG
         */
        found = false;
        for(String s : refController.getLinkStaffKeywords()) {
            //if(associate_text.toLowerCase().contains(s.toLowerCase())) 
            if(CandidateTypeURL.containsTheWorkInLink(associate_text, s))
            {
                found = true;
                break;
            }
        }
        if(found)
        {
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV3Controller.STAFFLIST_RESULT_TAG, associate_text, href, parentURL);
            refController.addPossibleResults(c);                
            return true;
        }

        /*
         * Link must be visited if associate text any contains  staff list keywords
         * Also, we will  try to add that url as SUBSTAFFLIST_RESULT_TAG
         */
        found = false;
        for(String s : refController.getLinkSubstaffKeywords()) {
            if(CandidateTypeURL.containsTheWorkInLink(associate_text, s))
            {
                found = true;
                break;
            }
            /*
            if(s.length() < 3) {
                if(associate_text.toLowerCase().equals(s.toLowerCase())) {
                    found = true;
                    break;
                }
            } else {
                if(associate_text.toLowerCase().contains(s.toLowerCase())) {
                    found = true;
                    break;
                }
            } 
            */
        }
        if(found) {
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV3Controller.SUBSTAFFLIST_RESULT_TAG, associate_text, href, parentURL);
            refController.addPossibleResults(c);                
            return true;
        }        

        /*
         * Matching if the associate text has any researcher name in its content
         * Also, we will try to add that url as SUBSTAFFLIST_RESULT_TAG
         */
        ResearcherNameInfo researcher_name_info_detected = null;
        int number_of_occur = 0;
        
        
        String possible_researcher_name = "";
        possible_researcher_name  = url.getAssociateText();/*.replace("Phd ", " ").
                                                           replace(" Phd", " ").
                                                           replace("Ph.d ", " ").
                                                           replace(" Ph.d", " ").
                                                           replace("M.D ", " ").
                                                           replace(" M.D", " ").
                                                           replace("MD ", " ").
                                                           replace(" MD", " ").
                                                           replace("M.S ", " ").
                                                           replace(" M.S", " ").
                                                           replace("MS ", " ").
                                                           replace(" MS", " ");*/
                
        possible_researcher_name = CandidateTypeURL.getCanonicalName(possible_researcher_name).
                                                         replace("Professor ", " ").
                                                         replace("Prof ", " ").
                                                         replace("Dr ", " ").
                                                         replace(" Dr", " ").
                                                         replace(" Professor", " ").
                                                         replace(" Prof", " ");
        
        for(ResearcherNameInfo researcher_name_info : refController.getResearcherNamesInfoToSearch())
        {
            if(isThisName(possible_researcher_name, researcher_name_info))
            {
                number_of_occur++;
                researcher_name_info_detected = researcher_name_info.clone();
            }
        }
        if(number_of_occur == 1)
        {
            String sExt = getExtensionFromUrl(href);
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG, CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG_SUBTYPE_CV, sExt, associate_text, href, parentURL, researcher_name_info_detected);
            c.data = researcher_name_info_detected;
            refController.addPossibleResults(c);               
        }
        else if (number_of_occur > 1)
        {
            String sExt = getExtensionFromUrl(href);
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG, CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG_SUBTYPE_CV, sExt, associate_text, href, parentURL, null);
            Logger.getLogger("MyLog").info("REFUSE " + c.toString());
        }

        /*
         * Link must be visited if associate text any contains link keywords
         */
        found = false;
        for(String s : refController.getLinkKeywords())
        {
            if(CandidateTypeURL.containsTheWorkInLink(associate_text, s))
            {
                found = true;
                break;
            }
        }
        if(found) 
        {
            if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerResearchesPagesV3Controller.KEYWORDS_RESULT_TAG, associate_text);
            return true;
        }
        
        if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerResearchesPagesV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "NOTHING INTERESTING" + "\t" + href + "\t" + parentURL);
        return false;
    }

    /*
     * Simple matching function to check if in the associate_text there is a researcher name
     * FIXME
     * @param name
     * @param initials
     * 
     */
    private boolean isThisName(String associate_text,  ResearcherNameInfo researcher_name_info )
    {
        if(associate_text.toLowerCase().equals(researcher_name_info.last_name.toLowerCase()))
        {
            return true;
        }        
        else if(associate_text.contains(researcher_name_info.last_name.toLowerCase()))
        {
            //Contains the firstname
            if(researcher_name_info.first_name != null && !researcher_name_info.first_name.equals(""))
            {
                String[] first_names = researcher_name_info.first_name.split(" ");
                String first_name_used = first_names[0];
                if(first_name_used.length() > 1 && associate_text.contains(first_name_used))
                    return true;       
                else if(first_names.length > 1)
                {
                    first_name_used = first_names[1];
                    if(first_name_used.length() > 2 && associate_text.contains(first_name_used))
                        return true;
                }                    
            }
            //If there is not firstname, go with initials
            else
            {
                //Check if the name has all the initials
                char[] initials = researcher_name_info.initial.replaceAll("[^a-z^A-Z]","").toCharArray();                                                
                
                String sAuxText = associate_text.replace(researcher_name_info.last_name, "").trim();
                char[] initials_in_associate_text = sAuxText.replaceAll("[^a-z^A-Z]","").toCharArray();
                
                boolean found = false;
                for(char initial : initials)
                {                    
                    found = false;
                    for(char initial_in : initials_in_associate_text)
                    {
                        if(Character.toLowerCase(initial) == Character.toLowerCase(initial_in))
                        {
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                        break;                                            
                }
                
                return found;
            }
        }
        else
        {
            if(researcher_name_info.first_name != null && !researcher_name_info.first_name.equals(""))
            {
                if(associate_text.startsWith(researcher_name_info.first_name))
                    return true;
                else
                    return false;
            }            
            else
            {                
                return false;
            }
        }
        
        return false;
                //if((sAuxText.split(" ").length <= researcher_name_info.last_name.split(" ").length + 3))
//                {
//                    sAuxText = sAuxText.replace(researcher_name_info.last_name, "").trim();
//                    if(ac[0] == sAuxText.charAt(0))
//                    {
//                        return true;
//                    }
//                    else
//                    {
//                        int index = sAuxText.indexOf(Character.toString(ac[0]));
//                        if(index > 0){
//                            if(Character.getType(sAuxText.charAt(index-1)) == 12)
//                                return true;
//                        }
//                    }
//                }
            
        
    }

    /*
     * This funtions try to locate sub web pages of researcher web page given that could contain more information about the researcher.
     * In this case, the funtion try to locate links to cv documents (html, txt, pdf, etc)
     *               and to papers document
     * 
     * Using:
     *  CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG
     *  CrawlerResearchesPagesV3Controller.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_CV
     *  CrawlerResearchesPagesV3Controller.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_PUB
     * 
     * @param candidate CandidateTypeURL    - Out url object
     */
    public static List<CandidateTypeURL> locateSubPages(CandidateTypeURL candidate) throws UnsupportedEncodingException, FileNotFoundException, IOException, InterruptedException
    {   
        List<CandidateTypeURL> filesDownload = new ArrayList<CandidateTypeURL>();
        Thread.sleep(100);
        org.jsoup.nodes.Document doc = eu.sisob.uma.footils.Web.Downloader.tryToConnect(candidate.sURL, 20);        

        //Try to extract link from researcher page (link to CV, publications, etc)
        if(doc != null)
        {
            org.jsoup.select.Elements els = doc.body().select("a:containsOwn(curriculum), a:containsOwn(CV),a:containsOwn(biography), a:containsOwn(vitae)");

            if(!els.isEmpty())
            {
                org.jsoup.nodes.Element e = els.first();
                String cv_url = e.absUrl("href");  
                String sExt = getExtensionFromUrl(cv_url);
                CandidateTypeURL extra_webpage_result = new CandidateTypeURL(   CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG, 
                                                                                CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG_SUBTYPE_CV,
                                                                                sExt,
                                                                                e.text(), 
                                                                                cv_url, 
                                                                                candidate.sURL,
                                                                                candidate.data
                                                                             );                
                filesDownload.add(extra_webpage_result);                
                ProjectLogger.LOGGER.info("CV URL DETECTED => " + cv_url);      
            }
            
            els = doc.body().select("a[text()==Webpage]");            
            if(els.isEmpty()) 
                els = doc.body().select("a[contains(text(),'Webpage') and contains(text(),'My ')]");                        
            
            if(!els.isEmpty())
            {
                org.jsoup.nodes.Element e = els.first();
                String cv_url = e.absUrl("href");  
                String sExt = getExtensionFromUrl(cv_url);
                filesDownload.add(new CandidateTypeURL(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG, 
                                                       CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG_SUBTYPE_CV,
                                                       sExt,
                                                       e.text(), 
                                                       cv_url, 
                                                       candidate.sURL,
                                                       candidate.data)
                                 );
                ProjectLogger.LOGGER.info("CV URL DETECTED => " + cv_url);      
            }
            
            els = doc.body().select("a:containsOwn(publications)");

            if(!els.isEmpty())
            {                
                org.jsoup.nodes.Element e = els.first();
                String pubs_url = e.absUrl("href");         
                String sExt = getExtensionFromUrl(pubs_url);
                filesDownload.add(new CandidateTypeURL(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG, 
                                                       CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG_SUBTYPE_CV, 
                                                       sExt,                                                       
                                                       e.text(), pubs_url, candidate.sURL, candidate.data));                
                ProjectLogger.LOGGER.info("PUBS URL DETECTED => " + pubs_url);      
            }
        }        
        
        return filesDownload;
    }
     
    private static final String[] asCompatibleExt = {"xls",
                                                     "txt",
                                                     "xlsx",
                                                     "pdf",
                                                     "odp",
                                                     "ods",
                                                     "trf",
                                                     "odt",
                                                     "pptx",
                                                     "ppt",
                                                     "text",
                                                     "sgm",
                                                     "doc",
                                                     "docx"};
     
    public static String getExtensionFromUrl(String sNewURL)
    {
        String sExt = sNewURL.substring(sNewURL.lastIndexOf(".")+1);

        boolean bNotHTML = false;
        for(String sAuxExt : asCompatibleExt)
        {
            if(sAuxExt.equals(sExt))
            {
                bNotHTML = true;
                break;
            }
        }
        
        if(!bNotHTML) 
            sExt = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT_VALUE_DEFAULT_HTML;
        
        return sExt;
    }   
    
}