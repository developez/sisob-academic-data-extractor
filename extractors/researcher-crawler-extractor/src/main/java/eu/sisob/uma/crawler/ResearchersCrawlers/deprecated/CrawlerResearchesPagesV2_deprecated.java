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
import java.util.List;

import eu.sisob.uma.api.crawler4j.crawler.WebCrawler;
import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.crawler.ProjectLogger;
import eu.sisob.uma.crawler.ResearchersCrawlers.CandidateTypeURL;
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

public class CrawlerResearchesPagesV2_deprecated extends WebCrawler
{         
    CrawlerResearchesPagesV2Controller_deprecated refController = null;    
    
    @Override
    public void setMyController(CrawlController myController) 
    {            
        super.setMyController(myController);     
        
        if(myController != null)
        {
            refController = (CrawlerResearchesPagesV2Controller_deprecated)myController;            
        }
        else
            refController = null;        
    } 

    public boolean shouldVisit(WebURL url)
    {        
        String href = url.getURL();   
        String associate_text = url.getAssociateText();
        String parentURL = url.getParentUrl();           

        if(
           href.contains("http://www.bcm.edu/genetics/index.cfm") ||
           parentURL.contains("http://www.bcm.edu/genetics/index.cfm")
           )
        {                
            //FIXME
            boolean b = true;
            b = false;                
        }

        /*
         * Control sentences
         */
        if(associate_text.equals(CrawlController.SEED_TAG)) 
            return true;        

        for(String s : refController.getLinkWrongKeywords())
        {
            if(associate_text.toLowerCase().contains(s.toLowerCase()))
            {
                return false;
            }
        }
        
        if (refController.getFilters().matcher(href).matches()) return false;        
        
        
        /*
         * Clean associate_text
         */  
        associate_text = associate_text.trim().replace("\r\n", "").replace("\r", "");
        while(associate_text.contains("  ")) associate_text = associate_text.replace("  ", " ");

        /*
         * Link must be visited if associate text any contains staff list keywords
         * Also, we will  try to add that url as STAFFLIST_RESULT_TAG
         */
        String aux_string = "";
        for(String s : refController.getLinkStaffKeywords())
        {
            if(associate_text.toLowerCase().contains(s.toLowerCase()))
            {
                aux_string += s + " ";
            }
        }
        if(aux_string != "")
        {
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV2Controller_deprecated.STAFFLIST_RESULT_TAG, associate_text, href, parentURL);
            refController.addPossibleResults(c);                
            return true;
        }

        /*
         * Link must be visited if associate text any contains  staff list keywords
         * Also, we will  try to add that url as SUBSTAFFLIST_RESULT_TAG
         */
        aux_string = "";        
        for(String s : refController.getLinkSubstaffKeywords())
        {
            if(associate_text.toLowerCase().contains(s.toLowerCase()))
            {
                aux_string += s + " ";
            }
        }
        if(aux_string != "")
        {
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV2Controller_deprecated.SUBSTAFFLIST_RESULT_TAG, associate_text, href, parentURL);
            refController.addPossibleResults(c);                
            return true;
        }        

        /*
         * Matching if the associate text has any researcher name in its content
         * Also, we will try to add that url as SUBSTAFFLIST_RESULT_TAG
         */
        ResearcherNameInfo researcher_name_info_detected = null;
        int number_of_occur = 0;
        for(ResearcherNameInfo researcher_name_info : refController.getResearcherNamesInfoToSearch())
        {
            if(isThisName(associate_text, researcher_name_info))
            {
                number_of_occur++;
                researcher_name_info_detected = researcher_name_info.clone();
            }
        }
        if(number_of_occur == 1)
        {
            String sExt = getExtensionFromUrl(href);
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG, CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG_SUBTYPE_CV, sExt, associate_text, href, parentURL, researcher_name_info_detected);
            c.data = researcher_name_info_detected;
            refController.addPossibleResults(c);               
        }
        else if (number_of_occur > 1)
        {
            String sExt = getExtensionFromUrl(href);
            CandidateTypeURL c = new CandidateTypeURL(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG, CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG_SUBTYPE_CV, sExt, associate_text, href, parentURL, null);
            Logger.getLogger("MyLog").info("REFUSE " + c.toString());
        }

        /*
         * Link must be visited if associate text any contains link keywords
         */
        aux_string = "";
        for(String s : refController.getLinkKeywords())
        {
            if(associate_text.toLowerCase().contains(s.toLowerCase()))
            {
                aux_string = s;
                break;
            }
        }
        if(aux_string != "") 
            return true;

        return false;
    }

    /*
     * Simple matching function to check if in the associate_text there is a researcher name
     * FIXME
     * @param name
     * @param initials
     * 
     */
    private boolean isThisName(String sAssociateText,  ResearcherNameInfo researcher_name_info )
    {
        if(sAssociateText.toLowerCase().contains(researcher_name_info.last_name.toLowerCase()))
        {
            char[] ac = researcher_name_info.initial.replaceAll("[^a-z^A-Z]","").toCharArray();
            String sAuxText = sAssociateText.replace("Professor ", "").replace("Prof ", "").replace("Dr ", "").replace(", Professor", "").replace(", Prof", "").replace(", Dr", "");
            if(sAuxText.toLowerCase().equals(researcher_name_info.last_name.toLowerCase()))
            {
                return true;
            }
            else if((sAuxText.split(" ").length <= researcher_name_info.last_name.split(" ").length + 3))
            {
                sAuxText = sAuxText.replace(researcher_name_info.last_name, "").trim();
                if(ac[0] == sAuxText.charAt(0))
                {
                    return true;
                }
                else
                {
                    int index = sAuxText.indexOf(Character.toString(ac[0]));
                    if(index > 0){
                        if(Character.getType(sAuxText.charAt(index-1)) == 12)
                            return true;
                    }
                }
            }
        }

        return false;
    }

    /*
     * This funtions try to locate sub web pages of researcher web page given that could contain more information about the researcher.
     * In this case, the funtion try to locate links to cv documents (html, txt, pdf, etc)
     *               and to papers document
     * 
     * Using:
     *  CrawlerResearchesPagesV2Controller.RESEARCHER_RESULT_TAG
     *  CrawlerResearchesPagesV2Controller.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_CV
     *  CrawlerResearchesPagesV2Controller.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_PUB
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
                filesDownload.add(new CandidateTypeURL(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG, 
                                                       CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG_SUBTYPE_CV,
                                                       sExt,
                                                       e.text(), cv_url, candidate.sURL, candidate.data));
                ProjectLogger.LOGGER.info("CV URL DETECTED => " + cv_url);      
            }
            
            els = doc.body().select("a[text()==Webpage]");

            if(!els.isEmpty())
            {
                org.jsoup.nodes.Element e = els.first();
                String cv_url = e.absUrl("href");  
                String sExt = getExtensionFromUrl(cv_url);
                filesDownload.add(new CandidateTypeURL(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG, 
                                                       CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG_SUBTYPE_CV,
                                                       sExt,
                                                       e.text(), cv_url, candidate.sURL, candidate.data));
                ProjectLogger.LOGGER.info("CV URL DETECTED => " + cv_url);      
            }
            
            els = doc.body().select("a:containsOwn(publications)");

            if(!els.isEmpty())
            {                
                org.jsoup.nodes.Element e = els.first();
                String pubs_url = e.absUrl("href");         
                String sExt = getExtensionFromUrl(pubs_url);
                filesDownload.add(new CandidateTypeURL(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG, 
                                                       CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG_SUBTYPE_CV, 
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