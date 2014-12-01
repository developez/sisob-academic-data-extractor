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

import eu.sisob.uma.api.crawler4j.crawler.Page;
import eu.sisob.uma.api.crawler4j.crawler.WebCrawler;
import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.crawler.ResearchersCrawlers.CandidateTypeURL;
import java.util.logging.Logger;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public class CrawlerDepartamentsV2_deprecated extends WebCrawler
{        
      
    CrawlerDepartamentsV2Controller_deprecated refController = null;
    String keyword_separator = "/";
        
    @Override
    public void setMyController(CrawlController myController) 
    {            
        super.setMyController(myController);     
        
        if(myController != null)
        {
            refController = (CrawlerDepartamentsV2Controller_deprecated)myController;
            keyword_separator = refController.getKeyWordSeparator();
        }
        else
            refController = null;        
    }    
    

    @Override
	public boolean shouldVisit(WebURL url)
        {
            //FIXME, put in crawler code
            

            String href = url.getURL();

            if(href.equals(""))
            {
                
            }
            
            String sAssociateText = url.getAssociateText();            
            String parentURL = url.getParentUrl();

            boolean bShouldVisit = false;

            if(sAssociateText.equals(CrawlController.SEED_TAG))
            {
                return true;
            }
            
            for(String s : refController.getLstLinkWrongKeyWords())
            {
                if(sAssociateText.toLowerCase().contains(s.toLowerCase()))
                {
                    return false;
                }
            }           

//            if (!href.contains(sContainPattern))
//            {
//            	return false;
//            }

            if (refController.getFilters().matcher(href).matches())
            {
            	return false;
            }

            //PATTERN 1: Links with lstLinkKeyWordsOfDepartments
            //Exmaple: Departments, Schools and departments, Out Faculties, our departments, etc
            String aux_string = "";
            
            for(String department_keyword : refController.getLstLinkKeyWordsOfDepartmentsList())
            {
                if(sAssociateText.toLowerCase().contains(department_keyword.toLowerCase()))
                {
                    aux_string += department_keyword + " ";
                }
            }
            if(aux_string != "")
            {
                CandidateTypeURL c = new CandidateTypeURL("Departments", sAssociateText, href, parentURL);
                ((CrawlerDepartamentsV2Controller_deprecated)this.getMyController()).addPossibleResults(c);
                //this.logger.info("\t<Departments> Occ: (" + aux_string + ") " + " Text: " + sAssociateText + " From: " + parentURL + " [Link:" + href + "]");
                //return true;
                bShouldVisit = true;
            }

            //PATTERN 1.2: Links with lstLinkKeyWordsOfSubDepartmentsType
            //Exmaple: Computer Sciences, Faculty of Computer Sciences
            aux_string = "";
            for(String subdepartment_keyword : refController.getLstLinkKeyWordsOfSubDepartmentsList())
            {
                if(sAssociateText.toLowerCase().contains(subdepartment_keyword.toLowerCase()))
                {
                    aux_string += subdepartment_keyword + " ";
                }
            }
            if(aux_string != "")
            {
                //if(resultsURL.containsKey(url.getParentUrl()))
                //{
                    CandidateTypeURL c = new CandidateTypeURL("Subdepartments", sAssociateText, href, parentURL);
                    ((CrawlerDepartamentsV2Controller_deprecated)this.getMyController()).addPossibleResults(c);
                    //this.logger.info("\t<Departments> Occ: (" + aux_string + ") " + " Text: " + sAssociateText + " From: " + parentURL + " [Link:" + href + "]");
                    //return true;
                    bShouldVisit = true;
//                }
//                else
//                {
//                    this.logger.info("\tREFUSED BY CONDITION <Departments> (SUB) Occ: (" + aux_string + ") " + " Text: " + sAssociateText + " From: " + parentURL + " [Link:" + href + "]");
//                }
            }

            //PATTERN 2: Links with lstLinkKeyWordsOfDepartmentsType AND that his parent are a Department link (look previous link pattern)
            //Exmaple: Departmentss, Schools and Departmentss, Out Faculties, our departaments, etc
            aux_string = "";
            for(String department_type_keywords_line : refController.getLstLinkKeyWordsOfDepartmentsType())
            {
                String[] department_type_keywords = department_type_keywords_line.split(keyword_separator);
                boolean b = false;
                for(String department_type_keyword : department_type_keywords)
                {
                    if(sAssociateText.toLowerCase().contains(department_type_keyword.toLowerCase()))
                    {
                        b = true;
                    }
                }
                if(b)
                {
                    if(aux_string != "")
                    {
                        aux_string = "";
                        break;
                    }
                    else
                    {
                        aux_string = department_type_keywords_line;
                    }
                }
            }

            if(aux_string != "")
            {
//                if(resultsURL.containsKey(url.getParentUrl()))
//                {
                    CandidateTypeURL c2 = new CandidateTypeURL("department of " + aux_string, sAssociateText, href, parentURL);
                    ((CrawlerDepartamentsV2Controller_deprecated)this.getMyController()).addPossibleResults(c2);
                    //this.logger.info("\t<Candidate of department of " + aux_string + "> Occ: (" + aux_string + ") " + " Text: " + sAssociateText + " From: " + parentURL + " [Link:" + href + "]");
                    //return true;
                    bShouldVisit = true;
//                }
//                else
//                {
//                    this.logger.info("\tREFUSED ======> <Department of " + aux_string + "> Occ: (" + aux_string + ") " + " Text: " + sAssociateText + " From: " + parentURL + " [Link:" + href + "]");
//                }
            }

            //PATTERN 3: Links with lstLinkKeyWordsOfDepartmentsType and determine words.
            //Example: "Departments of Informatics", "Departments of Chemistry", "School of Chemistry", etc
            for(String department_type_keywords_line : refController.getLstLinkKeyWordsOfDepartmentsType())
            {
                String[] department_type_keywords = department_type_keywords_line.split(keyword_separator);
                boolean b = false;
                for(String department_type_keyword : department_type_keywords)
                {
                    String text = sAssociateText.toLowerCase().trim();

                    for(String department_keyword : refController.getLstLinkKeyWordsOfDepartmentsList())
                    {
                        if(text.equals(department_keyword.toLowerCase() + " of " + department_type_keyword.toLowerCase()))
                        {
                            b = true;
                            aux_string = department_keyword;
                            break;
                        }
                    }
                    
                    if(b)
                        break;
                    
                    
                    //if(sText.equals("department of " + s.toLowerCase()) ||
                    //   sText.equals("school of " + s.toLowerCase()) ||
                    //  sText.equals("facultie of " + s.toLowerCase()) ||
                    //   sText.equals(s.toLowerCase() + " department"))
                    //{
                    //    b = true;
                    //    aux_string = sKeyWord;
                    //    break;
                    //}
                }
                if(b)
                {
                    CandidateTypeURL c2 = new CandidateTypeURL("department of " + aux_string, sAssociateText, href, parentURL);
                    ((CrawlerDepartamentsV2Controller_deprecated)this.getMyController()).addPossibleResults(c2);
                    return true;
                }
            }

            if(bShouldVisit) return true;

            aux_string = "";
            for(String s : refController.getLstLinkKeyWords())
            {
                if(sAssociateText.toLowerCase().contains(s.toLowerCase()))
                {
                    aux_string = s;
                    break;
                }
            }
            if(aux_string != "") return true;
                
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
                Logger.getLogger("MyLog").info("Recollected finish!");
                return true;
            }
            return false;
        }
}