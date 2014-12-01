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

import eu.sisob.uma.api.crawler4j.crawler.Page;
import eu.sisob.uma.api.crawler4j.crawler.WebCrawler;
import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.crawler.CrawlerTrace;
import eu.sisob.uma.crawler.ProjectLogger;
import java.util.Map;

/**
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */

public class CrawlerDepartamentsV3 extends WebCrawler
{        
      
    CrawlerDepartamentsV3Controller refController = null;
    String keyword_separator = "/";
        
    @Override
    public void setMyController(CrawlController myController) 
    {            
        super.setMyController(myController);     
        
        if(myController != null)
        {
            refController = (CrawlerDepartamentsV3Controller)myController;
            keyword_separator = refController.getKeyWordSeparator();
        }
        else
            refController = null;        
    }    
    
    static String URL1 = "http://www.bme.cmu.edu";
    static String URL2 = "http://www.cmu.edu/academics/schools.shtml";
    static String URL3 = "http://www.bcm.edu/molvir/appointments";
    static String PURL1 = "http://www.bme.cmu.edu";
    static String PURL2 = "http://www.cmu.edu/academics/schools.shtml";
    static String PURL3 = "http://www.amc.edu/Research/CBC/faculty.cfm";

    @Override
	public boolean shouldVisit(WebURL url)
        {
            String associate_text = CandidateTypeURL.getCanonicalName(url.getAssociateText());
            String associate_text_no_double_spaces = url.getAssociateText().trim().replace("\r\n", " ").replace("\r", " ").replace("\n", " ").replace("\t", " ");
            while(associate_text_no_double_spaces.contains("  ")) associate_text_no_double_spaces = associate_text_no_double_spaces.replace("  ", " ");
            
            String href = url.getURL();//.toLowerCase();
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

            if(url.getAssociateText().equals(CrawlController.SEED_TAG)) {
                return true;
            }
            
            if(associate_text_no_double_spaces.split(" ").length > 8)
            {
                if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerDepartamentsV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "TOO MANY KEY" + "\t" + href + "\t" + parentURL);
                return false;
            }
            
            for(String s : refController.getLstLinkWrongKeyWords()) {
                if(CandidateTypeURL.containsTheWorkInLink(associate_text, s))
                {
                    if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerDepartamentsV3Controller.WRONG_KEYWORDS_RESULT_TAG, associate_text);
                    if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerDepartamentsV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "WRONG KEYWORDS" + "\t" + href + "\t" + parentURL);
                    return false;
                }
            }           

            if (refController.getFilters().matcher(href).matches()) {
                if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerDepartamentsV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "FILTER" + "\t" + href + "\t" + parentURL);
            	return false;
            }

            //PATTERN 1: Links with departments keywords
            //Exmaple: Departments, Schools and departments, Out Faculties, our departments, etc
            boolean bShouldVisit = false;
            
            boolean found = false;
            for(String department_keyword : refController.getLstLinkKeyWordsOfDepartmentsList()) {
                //if(sAssociateText.toLowerCase().contains(department_keyword.toLowerCase())) {
                if(CandidateTypeURL.containsTheWorkInLink(associate_text, department_keyword))
                {
                    found = true;
                    break;
                }
            }
            if(found) {
                CandidateTypeURL c = new CandidateTypeURL(CrawlerDepartamentsV3Controller.DEPARTMENTS_RESULT_TAG, associate_text, href, parentURL);
                ((CrawlerDepartamentsV3Controller)this.getMyController()).addPossibleResults(c);                
                bShouldVisit = true;
            }

            //PATTERN 1.2: Links with lstLinkKeyWordsOfSubDepartmentsType
            //Exmaple: Computer Sciences, Faculty of Computer Sciences
            found = false;
            for(String subdepartment_keyword : refController.getLstLinkKeyWordsOfSubDepartmentsList())
            {
                if(CandidateTypeURL.containsTheWorkInLink(associate_text, subdepartment_keyword))
                {
                    found = true;
                    break;
                }
//                subdepartment_keyword = subdepartment_keyword.toLowerCase();
//                if(subdepartment_keyword.length() < 3) {
//                    if(sAssociateText.toLowerCase().equals(subdepartment_keyword.toLowerCase())) {
//                        found = true;
//                        break;
//                    }
//                } else {
//                    if(sAssociateText.toLowerCase().contains(subdepartment_keyword.toLowerCase())) {
//                        found = true;
//                        break;
//                    }
//                }                
            }
            if(found)
            {                
                CandidateTypeURL c = new CandidateTypeURL(CrawlerDepartamentsV3Controller.SUBDEPARTMENTS_RESULT_TAG, associate_text, href, parentURL);
                ((CrawlerDepartamentsV3Controller)this.getMyController()).addPossibleResults(c);             
                bShouldVisit = true;//              
            }

            //PATTERN 2: Links with one word of lstLinkKeyWordsOfDepartmentsType AND that 
            //           his parent are a Department link (look previous link pattern)
            //Exmaple: Departmentss, Schools and Departmentss, Out Faculties, our departaments, etc
            found = false;
            String department_type_keywords_line_found = "";
            for(Map.Entry<String, String> entry : refController.getLstLinkKeyWordsOfDepartmentsType())
            {
                String department_type_keywords_key = entry.getKey();
                String department_type_keywords_line = entry.getValue();
                String[] department_type_keywords = department_type_keywords_line.split(keyword_separator);
                
                for(String department_type_keyword : department_type_keywords)
                {
                    if(associate_text.contains(department_type_keyword)) {
                        found = true;
                        department_type_keywords_line_found = department_type_keywords_key; //department_type_keywords_line;
                        break;
                    }
                }
                if(found) break;                
            }

            if(found)
            {
                CandidateTypeURL c2 = new CandidateTypeURL(CrawlerDepartamentsV3Controller.DEPARTMENT_OF_RESULT_TAG + department_type_keywords_line_found, associate_text, href, parentURL);
                ((CrawlerDepartamentsV3Controller)this.getMyController()).addPossibleResults(c2);                    
                bShouldVisit = true;
            }

            // REDUNDANT BECAUSE ALL OF THIS PATTERN CONTAINS THE department_type_keyword (pattern 2)
            //PATTERN 3: Links with lstLinkKeyWordsOfDepartmentsType and determine words.
            //Example: "Departments of Informatics", "Departments of Chemistry", "School of Chemistry", etc
//            found = false;
//            for(String department_type_keywords_line : refController.getLstLinkKeyWordsOfDepartmentsType())
//            {
//                String[] department_type_keywords = department_type_keywords_line.split(keyword_separator);
//                
//                for(String department_type_keyword : department_type_keywords)
//                {
//                    String text = sAssociateText.toLowerCase().trim();
//
//                    for(String department_keyword : refController.getLstLinkKeyWordsOfDepartmentsList())
//                    {
//                        if(
//                            (text.equals(department_keyword.toLowerCase() + " of " + department_type_keyword.toLowerCase())) ||
//                            (text.contains(department_keyword.toLowerCase()) && text.contains(department_type_keyword.toLowerCase()))
//                           )                        
//                        {
//                            found = true;
//                            break;
//                        }
//                    }
//                    
//                    if(found) break;                    
//                }
//                
//                if(found)
//                {
//                    CandidateTypeURL c2 = new CandidateTypeURL(CrawlerDepartamentsV3Controller.DEPARTMENT_OF_RESULT_TAG + aux_string, sAssociateText, href, parentURL);
//                    ((CrawlerDepartamentsV3Controller)this.getMyController()).addPossibleResults(c2);
//                    return true;
//                }
//            }

            if(bShouldVisit) return true;

            found = false;
            for(String s : refController.getLstLinkKeyWords())
            {
                if(CandidateTypeURL.containsTheWorkInLink(associate_text, s))
                {
                    found = true;
                    break;
                }
            }
            if(found){
                if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerDepartamentsV3Controller.KEYWORDS_RESULT_TAG, associate_text);
                return true;
            }
                
            if(refController.crawler_trace != null) refController.crawler_trace.anotate(CrawlerDepartamentsV3Controller.WRONG_URLS_RESULT_TAG, associate_text  + "\t" + "NOTHING INTERESTING" + "\t" + href + "\t" + parentURL);
            return false;
	}

	public void visit(Page page)
        {
//            int docid = page.getWebURL().getDocid();
//            String url = page.getWebURL().getURL();
//            String text = page.getText();
//            List<WebURL> links = page.getURLs();
//            int parentDocid = page.getWebURL().getParentDocid();
//            
//            if(refControllercrawlStat != null)
//            {
//              crawlStat.incProcessedPages();
//              crawlStat.incTotalLinks(links.size());
//            }
//                
//           logger.info("Processing: " + url);
            
	}

        private synchronized boolean isFinishRecollected()
        {            
            if(refController.getPossibleResultsTYPE().size() >= refController.getLstLinkKeyWordsOfDepartmentsType().size() + 1)
            {
                ProjectLogger.LOGGER.info("Recollected finish!");
                return true;
            }
            return false;
        }
}