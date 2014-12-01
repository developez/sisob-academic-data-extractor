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

package eu.sisob.uma.crawler;

import eu.sisob.uma.crawler.ResearchersCrawlers.CandidateTypeURL;
import eu.sisob.uma.crawler.ResearchersCrawlers.CrawlerDepartamentsV3;
import eu.sisob.uma.crawler.ResearchersCrawlers.CrawlerDepartamentsV3Controller;
import eu.sisob.uma.crawler.ResearchersCrawlers.CrawlerResearchesPagesV3;
import eu.sisob.uma.crawler.ResearchersCrawlers.CrawlerResearchesPagesV3Controller;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.IteratorReseachersFile;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.LocalFormatType;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.XMLTags;
import eu.sisob.uma.footils.File.FileFootils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class AirResearchersWebPagesExtractor extends IteratorReseachersFile 
{
    final static String CRAWLER_DATA_FOLDERNAME = "tmp-crawler-data";
    
    int[] counterSuccess;
    int[] counterTotal;    
    boolean refuseExecution;
    
    public int n_universities = 0;
    public int n_depts = 0;
    
    File keywords_data_dir;   
    
    
    /**
     * 
     * @param sourceXmlDocument 
     * @param keywords_dir 
     * @param workdir               - Directory to working with data (workdir has temporal crawler folder, 
     * @param split
     */
    public AirResearchersWebPagesExtractor(org.dom4j.Document sourceXmlDocument, File keywords_data_dir, File work_dir, boolean split)
    {
        IteratorReseachersFile(sourceXmlDocument, work_dir, LocalFormatType.PLAIN_DIRECTORY);      
        
        counterSuccess = new int[3];
        counterTotal = new int[3];
        for(int i = 0; i < counterSuccess.length; i++) counterSuccess[i] = 0;
        for(int i = 0; i < counterTotal.length; i++) counterTotal[i] = 0;       
        
        refuseExecution = false;
        
        this.keywords_data_dir = keywords_data_dir;        
    }
    
    /**
     * 
     * @param root 
     * @param keywords_dir
     * @param workdir 
     * @param split  
     */
    public AirResearchersWebPagesExtractor(org.dom4j.Element root, File keywords_data_dir, File work_dir, boolean split)
    {
        IteratorReseachersFile(root, work_dir, LocalFormatType.PLAIN_DIRECTORY);      
        
        counterSuccess = new int[3];
        counterTotal = new int[3];
        for(int i = 0; i < counterSuccess.length; i++) counterSuccess[i] = 0;
        for(int i = 0; i < counterTotal.length; i++) counterTotal[i] = 0;
        
        refuseExecution = false;
        
        this.keywords_data_dir = keywords_data_dir;                
    }
    
    /**
     * 
     * @throws Exception
     */
    @Override
    protected void beginActions() throws Exception
    {
        File dir = new File(this.work_dir, CRAWLER_DATA_FOLDERNAME);
        if(!dir.exists())
        {
            dir.mkdir();
        }        
    }
    
    /**
     * 
     * @throws Exception
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void endActions() throws Exception
    {        
        //Print result
        ProjectLogger.LOGGER.info("Researches results: Total :" + " - " + counterSuccess[2] + " / " + counterTotal[2] + " - Nº Depts: " + counterTotal[1] + " - Nº Depts with a least one res: " + counterSuccess[1]);
        
        
    }
    
    /**
     * In this block the crawler will try to extract the departments web adresses. 
     * The block works with a org.dom4j.Element
     * Notes:
     *  The function iterate the institution elemento taking all the UNIT_OF_ASSESSMENT to search all of them in same crawler call.     
     *  The UNIT_OF_ASSESSMENT will be stores in subjects array, next, it will be given to the crawler.
     * 
     * @param elementInstitution      
     * @param path
     * @param sInstitutionName
     * @param sWebAddress     
     * @return  
     */
    @Override
    protected boolean actionsInInstitutionNode(org.dom4j.Element elementInstitution, String path, String sInstitutionName, String sWebAddress)
    {                 
        if(refuseExecution) return false;
        
        String crawler_data_folder = this.work_dir.getAbsolutePath() + File.separator + CRAWLER_DATA_FOLDERNAME;
        
        List<String> subjects = new ArrayList<String>();    
        
        String sSeed = sWebAddress;
        String sContainPattern = sSeed.replace("http://www.", "");     
        int index = sContainPattern.indexOf("/");
        if(index == -1)
            index = sContainPattern.length() -1;
        sContainPattern = sContainPattern.substring(0, index);

        ProjectLogger.LOGGER.info("Department phase - " + sInstitutionName);
        
        /*
         * Taking subjects to search its web adresses         
         */        
        String sUnitOfAssessment_Description = "";
        for ( Iterator<org.dom4j.Element> i2 = elementInstitution.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext(); )
        {            
            sUnitOfAssessment_Description = i2.next().element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();            
            subjects.add(sUnitOfAssessment_Description);
            ProjectLogger.LOGGER.info("\tAdding subject '" + sUnitOfAssessment_Description + "' to search its section webpages");                            
        }        

        /*
         * Crawling to search the departments
         */
        CrawlerDepartamentsV3Controller controllerDepts = null;

        try 
        {
            String university_crawler_data_folder = crawler_data_folder + File.separator + sInstitutionName.replaceAll("\\W+", "").toLowerCase() + "-crawler-data";
            File university_crawler_data_dir = new File(university_crawler_data_folder);                
            if(university_crawler_data_dir.exists())
                FileFootils.deleteDir(university_crawler_data_dir);                         
            
            controllerDepts = new CrawlerDepartamentsV3Controller(university_crawler_data_folder, 
                                                                  this.keywords_data_dir, 
                                                                  subjects);
            controllerDepts.addSeed(sSeed);
            controllerDepts.setPolitenessDelay(200);
            controllerDepts.setMaximumCrawlDepth(3);
            controllerDepts.setMaximumPagesToFetch(-1);
            controllerDepts.setContainPattern(sContainPattern);
            controllerDepts.clearPossibleResults();
            
            ProjectLogger.LOGGER.info("Begin crawling: " + sInstitutionName + " (" + sWebAddress + ") - [" + sSeed + "]");
            long lTimerAux = java.lang.System.currentTimeMillis();

            controllerDepts.start(CrawlerDepartamentsV3.class, 1);
            
            lTimerAux = java.lang.System.currentTimeMillis() - lTimerAux;            
            ProjectLogger.LOGGER.info("End crawling: " + sInstitutionName + " - Time: " + lTimerAux + " ms - [" + sSeed + "]");                

        } catch (Exception ex) {
            ProjectLogger.LOGGER.error(ex.getMessage(), ex);
        }   
        finally
        {            
            if(CrawlerTrace.isTraceUrlsActive() && controllerDepts != null)
                controllerDepts.closeCrawlerTrace();
            
            controllerDepts.releaseResources();                        
        }

        /*
         * Update results
         */
        if(controllerDepts != null)
        {
            if(CrawlerTrace.isTraceSearchActive())
            {
                CandidateTypeURL.printResults("Results of: " + sInstitutionName + " (" + sWebAddress + ") by TYPE",
                                               controllerDepts.getPossibleResultsTYPE());
            }
            
           /*
            * Adding departments web addresses to xml document
            */
           for ( Iterator<org.dom4j.Element> i2 = elementInstitution.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext(); )
           {
               org.dom4j.Element e2 = i2.next();
               sUnitOfAssessment_Description = e2.element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();                        

               TreeMap<String, List<CandidateTypeURL>> t = controllerDepts.getPossibleResultsTYPE();
               Iterator<String> it = t.keySet().iterator();

               //
               String department_of = CrawlerDepartamentsV3Controller.DEPARTMENT_OF_RESULT_TAG + sUnitOfAssessment_Description;
               
               //FIXME, TEST THIS
               //while(it.hasNext())
               //{
               //    String department_of = it.next();
               //    if(department_of.toLowerCase().equals(CrawlerDepartamentsV3Controller.DEPARTMENT_OF_RESULT_TAG + sUnitOfAssessment_Description.toLowerCase()))
               //    {
                       List<CandidateTypeURL> lst = t.get(department_of);
                       if(lst != null)
                       {
                        for(CandidateTypeURL ss: lst)
                        {
                            ProjectLogger.LOGGER.info("Add department '" + department_of + "' the url '" + ss.sURL + "'");
                            e2.addElement(XMLTags.DEPARTMENT_WEB_ADDRESS).addText(ss.sURL);                            
                        }
                       }
               //        break;
               //    }
               //}
           }
        }       
                
        return true;
    }
    
    /**
     * 
     * @param elementUnitOfAssessment
     * @param path
     * @param sInstitutionName
     * @param sWebAddress
     * @param sUnitOfAssessment_Description     
     * @return  
     */
    @Override
    protected boolean actionsInUnitOfAssessmentNode(org.dom4j.Element elementUnitOfAssessment, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description)
    {   
        if(refuseExecution) return false;
        
        String crawler_data_folder = this.work_dir + File.separator + CRAWLER_DATA_FOLDERNAME;
        
        List<String> department_web_addresses = new ArrayList<String>();
        List<ResearcherNameInfo> researchers = new ArrayList<ResearcherNameInfo>();  
        
        String seed = sWebAddress;
        String contain_pattern = seed.replace("http://www.", "");        
        int index = contain_pattern.indexOf("/");
        if(index == -1)
            index = contain_pattern.length() -1;
        contain_pattern = contain_pattern.substring(0, index);        
         
        /*
         * Taking departments webpages to search in the researchers webpages
         */                    
        for ( Iterator<org.dom4j.Element> department_web_address_it = elementUnitOfAssessment.elementIterator(XMLTags.DEPARTMENT_WEB_ADDRESS); department_web_address_it.hasNext(); )
        {
            org.dom4j.Element department_web_address_element = (org.dom4j.Element) department_web_address_it.next();
            if(!department_web_address_element.getText().equals("")) department_web_addresses.add(department_web_address_element.getText());
        }
        
        /*
         * If there is not department webpage, then, add the university web to find staff page and something similar
         */
        if(department_web_addresses.isEmpty())
        {
            ProjectLogger.LOGGER.info("There is not dept webpages for [" + sUnitOfAssessment_Description + " - " + sInstitutionName + "]. Adding " + sWebAddress);
            //department_web_addresses.add(sWebAddress);
        }

        /*
         * Taking researchers info to search the researchers webs
         */ 
        for ( Iterator<org.dom4j.Element> research_group_it = elementUnitOfAssessment.elementIterator(XMLTags.RESEARCHGROUP); research_group_it.hasNext(); )
        {
            org.dom4j.Element research_group_element = research_group_it.next();            

            for ( Iterator<org.dom4j.Element> reseacher_it = research_group_element.elementIterator(XMLTags.RESEARCHER); reseacher_it.hasNext(); )
            {
                org.dom4j.Element reseacher_element = reseacher_it.next();               
                
                String initials = reseacher_element.element(XMLTags.RESEARCHER_INITIALS).getText();                               
                String last_name = reseacher_element.element(XMLTags.RESEARCHER_LASTNAME).getText();                        
                String first_name = reseacher_element.element(XMLTags.RESEARCHER_FIRSTNAME) == null ? "" : reseacher_element.element(XMLTags.RESEARCHER_FIRSTNAME).getText();
                String whole_name = reseacher_element.element(XMLTags.RESEARCHER_NAME) == null ? "" : reseacher_element.element(XMLTags.RESEARCHER_NAME).getText();                                    

                ResearcherNameInfo rsi = new ResearcherNameInfo(last_name, initials, first_name, whole_name);   
                researchers.add(rsi);                
            }
        }

        if(researchers.size() > 0 && !department_web_addresses.isEmpty())
        {
            /*
             * Crawling to search the researchers
             */
            CrawlerResearchesPagesV3Controller controllerReseachers = null;
            try
            {                
                String university_subject_crawler_data_folder = crawler_data_folder + File.separator +  sInstitutionName.replaceAll("\\W+", "").toLowerCase() + "-"+ sUnitOfAssessment_Description.replaceAll("\\W+", "").toLowerCase() + "-crawler-data";
                File university_subject_crawler_data_dir = new File(university_subject_crawler_data_folder);                
                if(university_subject_crawler_data_dir.exists())
                    FileFootils.deleteDir(university_subject_crawler_data_dir);                         
            
                controllerReseachers = new CrawlerResearchesPagesV3Controller(university_subject_crawler_data_folder,
                                                                              this.keywords_data_dir,
                                                                              researchers);
                String sSeeds = "";
                for(String s : department_web_addresses)
                {
                    controllerReseachers.addSeed(s);
                    sSeeds += s + ",";
                }

                controllerReseachers.setPolitenessDelay(200);
                controllerReseachers.setMaximumCrawlDepth(3);
                controllerReseachers.setMaximumPagesToFetch(-1);
                controllerReseachers.setContainPattern(contain_pattern);
                controllerReseachers.clearInterestingUrlsDetected();                            
                
                ProjectLogger.LOGGER.info("Begin crawling: " + sUnitOfAssessment_Description + " - " + sInstitutionName + " - [" + StringUtils.join(department_web_addresses, ",") + "]");
                long lTimerAux = java.lang.System.currentTimeMillis();

                controllerReseachers.start(CrawlerResearchesPagesV3.class, 1);

                controllerReseachers.postProcessResults();

                lTimerAux = java.lang.System.currentTimeMillis() - lTimerAux;
                ProjectLogger.LOGGER.info("End crawling: " + sUnitOfAssessment_Description + " - " + sInstitutionName + " - Time: " + lTimerAux + " ms - [" + StringUtils.join(department_web_addresses, ",") + "]");                
            }
            catch(Exception ex)
            {
                ProjectLogger.LOGGER.error(ex.getMessage(), ex);
            }
            finally
            {            
                if(CrawlerTrace.isTraceUrlsActive() && controllerReseachers != null)
                    controllerReseachers.closeCrawlerTrace();
            }

            /*
             * Update results
             */
            if(controllerReseachers != null)
            {
                /*
                 * Print the researchers
                 */
                if(CrawlerTrace.isTraceSearchActive())
                {
                    CandidateTypeURL.printResults("Results of: " + sUnitOfAssessment_Description + " - " + sInstitutionName + " (" + sWebAddress + ") by TYPE",
                                                  controllerReseachers.getInterestingUrlsDetected());
                }

                counterTotal[0] = 0;
                counterSuccess[0] = 0;

                try
                {
                    /*
                     * Add researcher webs to xml document             
                     */
                    for ( Iterator<org.dom4j.Element> research_group_it = elementUnitOfAssessment.elementIterator(XMLTags.RESEARCHGROUP); research_group_it.hasNext(); )
                    {
                        org.dom4j.Element research_group_element = research_group_it.next();

                        for ( Iterator<org.dom4j.Element> researcher_it = research_group_element.elementIterator(XMLTags.RESEARCHER); researcher_it.hasNext(); )
                        {
                            counterTotal[0]++;
                            org.dom4j.Element researcher_element = researcher_it.next();

                            String initials =  researcher_element.element(XMLTags.RESEARCHER_INITIALS).getText();                                    
                            String last_name = researcher_element.element(XMLTags.RESEARCHER_LASTNAME).getText();                                    
                            String first_name = researcher_element.element(XMLTags.RESEARCHER_FIRSTNAME) == null ? "" : researcher_element.element(XMLTags.RESEARCHER_FIRSTNAME).getText();
                            String whole_name = researcher_element.element(XMLTags.RESEARCHER_NAME) == null ? "" : researcher_element.element(XMLTags.RESEARCHER_NAME).getText();                                     

                            ResearcherNameInfo researcher_name_info = new ResearcherNameInfo(last_name, initials, first_name, whole_name);
                            researcher_name_info.first_name = CandidateTypeURL.getCanonicalName(researcher_name_info.first_name);
                            researcher_name_info.last_name = CandidateTypeURL.getCanonicalName(researcher_name_info.last_name);
                            researcher_name_info.initial = CandidateTypeURL.getCanonicalName(researcher_name_info.initial);
                            researcher_name_info.whole_name = CandidateTypeURL.getCanonicalName(researcher_name_info.whole_name);

                            TreeMap<String, List<CandidateTypeURL>> t = controllerReseachers.getInterestingUrlsDetected();

                            List<CandidateTypeURL> lst = t.get(CrawlerResearchesPagesV3Controller.RESEARCHER_RESULT_TAG);

                            boolean bExist = false;
                            if(lst != null)
                            {                            
                                //FIXME, contains and remove better
                                boolean lock1 = true;
                                for(CandidateTypeURL ss: lst)
                                {
                                    if(researcher_name_info.equals(ss.data))
                                    {                        
                                        ProjectLogger.LOGGER.info("Add researcher '" + researcher_name_info + "' the url '" + ss.sURL + "'");
                                        researcher_element.addElement(XMLTags.RESEARCHER_WEB_ADDRESS).addAttribute(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE, ss.sSubType)                                                                                             
                                                                                                     .addAttribute(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT, ss.sExt)       
                                                                                                     .addText(ss.sURL);
                                        lock1 = false;                                    
                                        bExist = true;
                                    }
                                }                                    
                            }
                            if(bExist)
                            {
                                counterSuccess[0]++;
                            }
                            else
                            {
                                ProjectLogger.LOGGER.warn("No webpage for " + researcher_name_info);
                            }
                        }
                    }
                }
                catch(Exception ex)
                {
                    ProjectLogger.LOGGER.error("Error", ex);
                }
                
                /*
                 * Show a little counting result
                 */
                ProjectLogger.LOGGER.info("Researches results: " + sInstitutionName + " - " +  sUnitOfAssessment_Description + " - " + counterSuccess[0] + " / " + counterTotal[0]);
                counterTotal[1] += 1;
                counterSuccess[1] += counterSuccess[0] > 0 ? 1 : 0;
                
                counterSuccess[2] += counterSuccess[0];                
                counterTotal[2] += counterTotal[0];
            }
        }
        
        return true;
    }    
}
