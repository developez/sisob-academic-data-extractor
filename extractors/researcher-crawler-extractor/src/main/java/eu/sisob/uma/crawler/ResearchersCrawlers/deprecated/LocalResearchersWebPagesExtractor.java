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

import eu.sisob.uma.crawler.ResearchersCrawlers.deprecated.CrawlerDepartamentsV2_deprecated;
import eu.sisob.uma.crawler.ResearchersCrawlers.CandidateTypeURL;
import eu.sisob.uma.crawler.ResearchersCrawlers.deprecated.CrawlerResearchesPagesV2_deprecated;
import eu.sisob.uma.api.crawler4j.crawler.PageFetcher;
import eu.sisob.uma.api.crawler4j.crawler.WebCrawler;
import eu.sisob.uma.crawler.ProjectLogger;

import eu.sisob.uma.crawler.ResearchersCrawlers.deprecated.CrawlerDepartamentsV2Controller_deprecated;
import eu.sisob.uma.crawler.ResearchersCrawlers.deprecated.CrawlerResearchesPagesV2Controller_deprecated;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.LocalFormatType;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import eu.sisob.uma.crawler.ResearchersCrawlers.Workers.*;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.XMLTags;
import eu.sisob.uma.footils.File.FileFootils;
import org.apache.log4j.Logger;

/**
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 * Data extraction tasks (prototype). See: DataExtractionPrototype_1.pdf.
 * 
 * This class has steps to locate and download researcher webpages from university webpages from xml file
 * 
 * SKETCH of xml format:
 * <Institution>
 *	<InstitutionName>...</InstitutionName>
 *	<WebAddress>...</WebAddress>
 *	<UnitOfAssessment>
 *		<UnitOfAssessment_Description></UnitOfAssessment_Description>
 *		<DepartamentWebAddress></DepartamentWebAddress>
 *		<ResearchGroup>
 *			<ResearchGroupDescription>
 *			</ResearchGroupDescription>
 *			<Researcher> 
 *				<StaffIdentifier></StaffIdentifier>
 *				<FirstName></FirstName>
 *				<LastName></LastName>
 *				<Initials></Initials>
 *				<ResearcherWebAddress>
 *				</ResearcherWebAddress>
 *			</Researcher>
 *		</ResearchGroup>
 * 
 * See IteratorReseachersFile for see more datails of the format of xml file. 
 * 
 * INCLUDE: PROCESS STEPS 1, 2, 3
 */
public class LocalResearchersWebPagesExtractor 
{              

    /*
     * PROCESS STEP 1
     * Launches the crawler in order to find the researcher's personal web pages
     * within the corresponding university website.
     * 
     * First tries to search each department webpage, next, tries to search the researcher's pages
     * within departments.
     * 
     * @param xmlFile
     */   
    public static void P1_step_collectResearcherLinks(String xmlFilePath, int numberOfCrawlers)
    {
        P1_step_collectResearcherLinks(xmlFilePath, numberOfCrawlers, "");
    }
    
    public static void P1_step_collectResearcherLinks(String xmlFilePath, int numberOfCrawlers, String sControlInstitutionName)
    {
        try
        {
            /*
             * rootfolder is a folder where intermediate crawl data is
             * stored.
             */
            String rootFolder = "temp/";

            FileFootils.deleteDir(rootFolder);
            /*
             * numberOfCrawlers shows the number of concurrent threads
             * that should be initiated for crawling.
             */         
            File xmlFile = new File(xmlFilePath);
                    
            org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = reader.read(xmlFile);
            org.dom4j.Element root = document.getRootElement();

            String sInstitutionName = "";
            String sWebAddress = "";
            String sUnitOfAssessment_Description = "";
            String sResearchGroupDescription = "";
            String sResearchers = "";
            String sResearchersInitials = "";

            PageFetcher.startConnectionMonitorThread();
            
            WebCrawler.setTraceLinkName(true);
            WebCrawler.setTracePageName(true);

            TreeMap<String,TreeMap<String, List<CandidateTypeURL>>> finalResults = new TreeMap<String, TreeMap<String, List<CandidateTypeURL>>>();

            boolean bFlagInstitutionName = false;
            String sControlUnitOfAssessment_Description = "";
            boolean bFlagUnitOfAssessmentName = false;            
            boolean bSaveFile = true;           
            
            boolean bSetEmptyAllResearchers = true;

            if(bSetEmptyAllResearchers)
            {
                File fField = new File(xmlFilePath.replace(".xml", "backup.xml"));
                FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
                OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
                BufferedWriter bw = new java.io.BufferedWriter(writer);
                String sOut = document.asXML();
                bw.write(sOut);
                bw.close();
                ProjectLogger.LOGGER.info(xmlFilePath + " backuped.");
            }

            int[] counterSuccess = new int[3];
            int[] counterTotal = new int[3];
            for(int i = 0; i < counterSuccess.length; i++) counterSuccess[i] = 0;
            for(int i = 0; i < counterTotal.length; i++) counterTotal[i] = 0;

            for ( Iterator i1 = root.elementIterator(XMLTags.INSTITUTION); i1.hasNext();)
            {
                bSaveFile = false;

                org.dom4j.Element e1 = (org.dom4j.Element) i1.next();

                sInstitutionName = e1.element(XMLTags.INSTITUTION_NAME).getText();
                sWebAddress = e1.element(XMLTags.INSTITUTION_WEBADDRESS).getText();
                if(sWebAddress.charAt(sWebAddress.length()-1) != '/')
                    sWebAddress += "/";

                if(!sInstitutionName.toLowerCase().contains(sControlInstitutionName.toLowerCase()) && !bFlagInstitutionName) continue;
                bFlagInstitutionName = true;

                List<String> subjects = new ArrayList<String>();                

                ProjectLogger.LOGGER.info("Department phase - " + sInstitutionName);

                boolean bNeedToSearchDeparmentWebAddress = false;
                for ( Iterator i2 = e1.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext(); )
                {
                    org.dom4j.Element e2 = (org.dom4j.Element) i2.next();

                    sUnitOfAssessment_Description = e2.element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();
                    //FIXME if(sUnitOfAssessment_Description.length() > 20) sUnitOfAssessment_Description = sUnitOfAssessment_Description.substring(0, 20);

                    if(e2.element(XMLTags.DEPARTMENT_WEB_ADDRESS) != null && e2.element("DepartamentWebAddress").elements().size() != 0)
                    {
                        ProjectLogger.LOGGER.info("\tExist departments webaddress for " + sUnitOfAssessment_Description);
                    }
                    else
                    {
                        subjects.add(sUnitOfAssessment_Description);
                        ProjectLogger.LOGGER.info("\tNot exist departments webaddress for " + sUnitOfAssessment_Description);
                        bNeedToSearchDeparmentWebAddress = true;
                    }
                }

                String sSeed = sWebAddress;
                String sContainPattern = sSeed.replace("http://www.", "");
                int iAux = sContainPattern.indexOf("/");
                sContainPattern = sContainPattern.substring(0, iAux);
                
                if(bNeedToSearchDeparmentWebAddress)
                {
                    CrawlerDepartamentsV2Controller_deprecated controllerDepts = new CrawlerDepartamentsV2Controller_deprecated(rootFolder + sInstitutionName.replace(" ", ".") + ".Researchers", 
                                                                     subjects);
                    controllerDepts.addSeed(sSeed);
                    controllerDepts.setPolitenessDelay(200);
                    controllerDepts.setMaximumCrawlDepth(3);
                    controllerDepts.setMaximumPagesToFetch(-1);
                    controllerDepts.setContainPattern(sContainPattern);
                    controllerDepts.clearPossibleResults();
                    
                    ProjectLogger.LOGGER.info("======================================================================");
                    ProjectLogger.LOGGER.info("Begin crawling: " + sInstitutionName + " (" + sWebAddress + ")");
                    long lTimerAux = java.lang.System.currentTimeMillis();

                    controllerDepts.start(CrawlerDepartamentsV2_deprecated.class, 1);
                    
                    lTimerAux = java.lang.System.currentTimeMillis() - lTimerAux;
                    ProjectLogger.LOGGER.info("Extracting Links in: " + lTimerAux + " ms");
                    ProjectLogger.LOGGER.info("======================================================================");

                    CandidateTypeURL.printResults("Results of: " + sInstitutionName + " (" + sWebAddress + ") by TYPE",
                                 controllerDepts.getPossibleResultsTYPE());

                    for ( Iterator i2 = e1.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext(); )
                    {
                        org.dom4j.Element e2 = (org.dom4j.Element) i2.next();
                        sUnitOfAssessment_Description = e2.element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();                        

                        TreeMap<String, List<CandidateTypeURL>> t = controllerDepts.getPossibleResultsTYPE();
                        Iterator<String> it = t.keySet().iterator();

                        while(it.hasNext())
                        {
                            String s = it.next();
                            if(s.toLowerCase().equals("department of " + sUnitOfAssessment_Description.toLowerCase()))
                            {
                                if(e2.element(XMLTags.DEPARTMENT_WEB_ADDRESS) != null && e2.element(XMLTags.DEPARTMENT_WEB_ADDRESS).elements().size() != 0)
                                {
                                    throw new Exception(sUnitOfAssessment_Description + " must be empty.");
                                }

                                List<CandidateTypeURL> lst = t.get(s);
                                for(CandidateTypeURL ss: lst)
                                {
                                    e2.addElement(XMLTags.DEPARTMENT_WEB_ADDRESS).addText(ss.sURL);
                                    bSaveFile = true;
                                }
                                break;
                            }
                        }
                    }
                }
                
                ProjectLogger.LOGGER.info("Researcher phase - " + sInstitutionName);

                if(sContainPattern != "")
                    sContainPattern = sContainPattern;
                
                for ( Iterator i2 = e1.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext(); )
                {
                    org.dom4j.Element e2 = (org.dom4j.Element) i2.next();

                    sUnitOfAssessment_Description =  e2.element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();
                    //FIXME if(sUnitOfAssessment_Description.length() > 20) sUnitOfAssessment_Description = sUnitOfAssessment_Description.substring(0, 20);
                    
                    List<String> lstDepartmentWebAddress = new ArrayList<String>();
                    for ( Iterator i3 = e2.elementIterator(XMLTags.DEPARTMENT_WEB_ADDRESS); i3.hasNext(); )
                    {
                        org.dom4j.Element e3 = (org.dom4j.Element) i3.next();
                        if(!e3.getText().equals("")) lstDepartmentWebAddress.add(e3.getText());
                    }

                    if(lstDepartmentWebAddress.size() > 0)
                    {
                        ProjectLogger.LOGGER.info("\tExist departments webaddress for " + sUnitOfAssessment_Description);

                        boolean bExistResearcherWebAddress = false;
                        
                        List<ResearcherNameInfo> researchers = new ArrayList<ResearcherNameInfo>();                        

                        for ( Iterator i3 = e2.elementIterator(XMLTags.RESEARCHGROUP); i3.hasNext(); )
                        {
                            org.dom4j.Element e3 = (org.dom4j.Element) i3.next();
                            sResearchGroupDescription = e3.element(XMLTags.RESEARCHGROUP_DESCRIPTION).getText();

                            for ( Iterator i4 = e3.elementIterator(XMLTags.RESEARCHER); i4.hasNext(); )
                            {
                                org.dom4j.Element e4 = (org.dom4j.Element) i4.next();

                                if(bSetEmptyAllResearchers)
                                {
                                    boolean aux = true;
                                    while(aux)
                                    {
                                        org.dom4j.Element eaux = e4.element(XMLTags.RESEARCHER_WEB_ADDRESS);
                                        if(eaux != null)
                                            e4.remove(eaux);
                                        else
                                            aux = false;
                                    }
                                }

                                if(e4.element(XMLTags.RESEARCHER_WEB_ADDRESS) == null)
                                {   
                                    String initials = e4.element(XMLTags.RESEARCHER_INITIALS).getText();                               
                                    String last_name = e4.element(XMLTags.RESEARCHER_LASTNAME).getText();                        
                                    String first_name = e4.element(XMLTags.RESEARCHER_FIRSTNAME) == null ? "" : e4.element(XMLTags.RESEARCHER_FIRSTNAME).getText();
                                    String whole_name = e4.element(XMLTags.RESEARCHER_NAME) == null ? "" : e4.element(XMLTags.RESEARCHER_NAME).getText();                                    
                                    
                                    ResearcherNameInfo rsi = new ResearcherNameInfo(last_name, initials, first_name, whole_name);   
                                    researchers.add(rsi);
                                    bExistResearcherWebAddress = false;
                                }
                                else if(bSetEmptyAllResearchers)
                                {
                                    throw new Exception("XML element of " + e4.element(XMLTags.RESEARCHER_INITIALS).getText() + "," + e4.element(XMLTags.RESEARCHER_LASTNAME).getText() + " must not have researcher web address at this moment");
                                }
                            }
                        }

                        if(!bExistResearcherWebAddress)
                        {
                            ProjectLogger.LOGGER.info("\tMiss researchers webaddress for " + sUnitOfAssessment_Description + ". Try to search.");

                            CrawlerResearchesPagesV2Controller_deprecated controllerReseachers = 
                                                    new CrawlerResearchesPagesV2Controller_deprecated(rootFolder + sInstitutionName.replace(" ", ".") + "_"+ sUnitOfAssessment_Description.replace(" ", "."),
                                                                                           researchers);

                            String sSeeds = "";

                            for(String s : lstDepartmentWebAddress)
                            {
                                controllerReseachers.addSeed(s);
                                sSeeds += s + ",";
                            }

                            controllerReseachers.setPolitenessDelay(200);
                            controllerReseachers.setMaximumCrawlDepth(3);
                            controllerReseachers.setMaximumPagesToFetch(-1);
                            controllerReseachers.setContainPattern(sContainPattern);
                            controllerReseachers.clearInterestingUrlsDetected();                            

                            if(!sUnitOfAssessment_Description.contains(sControlUnitOfAssessment_Description) && !bFlagUnitOfAssessmentName) continue;
                            bFlagUnitOfAssessmentName = true;

                            ProjectLogger.LOGGER.info("======================================================================");
                            ProjectLogger.LOGGER.info("Begin crawling: " + sUnitOfAssessment_Description + " - " + sInstitutionName + " (" + sSeeds + ")");
                            long lTimerAux = java.lang.System.currentTimeMillis();

                            controllerReseachers.start(CrawlerResearchesPagesV2_deprecated.class, 1);
                            
                            controllerReseachers.postProcessResults();

                            lTimerAux = java.lang.System.currentTimeMillis() - lTimerAux;
                            ProjectLogger.LOGGER.info("Extracting Links in: " + lTimerAux + " ms");
                            ProjectLogger.LOGGER.info("======================================================================");

                            CandidateTypeURL.printResults("Results of: " + sUnitOfAssessment_Description + " - " + sInstitutionName + " (" + sWebAddress + ") by TYPE",
                                         controllerReseachers.getInterestingUrlsDetected());

                            counterTotal[0] = 0;
                            counterSuccess[0] = 0;
                            
                            for ( Iterator i3 = e2.elementIterator(XMLTags.RESEARCHGROUP); i3.hasNext(); )
                            {
                                org.dom4j.Element e3 = (org.dom4j.Element) i3.next();

                                for ( Iterator i4 = e3.elementIterator(XMLTags.RESEARCHER); i4.hasNext(); )
                                {
                                    counterTotal[0]++;
                                    org.dom4j.Element e4 = (org.dom4j.Element) i4.next();
                                    
                                    String initials =  e4.element(XMLTags.RESEARCHER_INITIALS) == null ? "" : e4.element(XMLTags.RESEARCHER_INITIALS).getText();                                    
                                    String last_name = e4.element(XMLTags.RESEARCHER_LASTNAME) == null ? "" : e4.element(XMLTags.RESEARCHER_LASTNAME).getText();                                    
                                    String first_name = e4.element(XMLTags.RESEARCHER_FIRSTNAME) == null ? "" : e4.element(XMLTags.RESEARCHER_FIRSTNAME).getText();
                                    String whole_name = e4.element(XMLTags.RESEARCHER_NAME) == null ? "" : e4.element(XMLTags.RESEARCHER_NAME).getText();                                     
                                    
                                    ResearcherNameInfo rsi = new ResearcherNameInfo(last_name, initials, first_name, whole_name);
                                                                      
                                    TreeMap<String, List<CandidateTypeURL>> t = controllerReseachers.getInterestingUrlsDetected();

                                    List<CandidateTypeURL> lst = t.get(CrawlerResearchesPagesV2Controller_deprecated.RESEARCHER_RESULT_TAG);

                                    boolean bExist = false;
                                    if(lst != null)
                                    {                                    
                                        boolean lock1 = true;
                                        for(CandidateTypeURL ss: lst)
                                        {
                                            if(rsi.equals(ss.data))
                                            {                                                                     
                                                e4.addElement(XMLTags.RESEARCHER_WEB_ADDRESS).addAttribute(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE, ss.sSubType)                                                                                             
                                                                                             .addAttribute(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT, ss.sExt)       
                                                                                             .addText(ss.sURL);
                                                lock1 = false;
                                                bSaveFile = true;
                                                bExist = true;
                                            }
                                        }                                    
                                    }
                                    if(bExist)
                                    {
                                        counterSuccess[0]++;
                                    }
                                }
                            }

                            ProjectLogger.LOGGER.info("Researches results: " + sInstitutionName + " - " +  sUnitOfAssessment_Description + " - " + counterSuccess[0] + " / " + counterTotal[0]);

                        }
                        else
                        {
                            ProjectLogger.LOGGER.info("\tExist researchers webaddress for " + sUnitOfAssessment_Description + ".");

                            counterTotal[0] = 0;
                            counterSuccess[0] = 0;
                            for ( Iterator i3 = e2.elementIterator(XMLTags.RESEARCHGROUP); i3.hasNext(); )
                            {
                                org.dom4j.Element e3 = (org.dom4j.Element) i3.next();

                                for ( Iterator i4 = e3.elementIterator(XMLTags.RESEARCHER); i4.hasNext(); )
                                {
                                    counterTotal[0]++;

                                    org.dom4j.Element e4 = (org.dom4j.Element) i4.next();

                                    if(e4.element(XMLTags.RESEARCHER_WEB_ADDRESS) != null && e4.element(XMLTags.RESEARCHER_WEB_ADDRESS).elements().size() > 0)
                                    {
                                        counterSuccess[0]++;
                                    }
                                }
                            }

                            ProjectLogger.LOGGER.info("Results exist: " + sInstitutionName + " - " +  sUnitOfAssessment_Description + " - " + counterSuccess[0] + " / " + counterTotal[0]);
                        }
                    }
                    else
                    {
                        ProjectLogger.LOGGER.info("\tNot exist departments webaddress for " + sUnitOfAssessment_Description);                        

                        counterTotal[0] = 0;
                        counterSuccess[0] = 0;
                        for ( Iterator i3 = e2.elementIterator(XMLTags.RESEARCHGROUP); i3.hasNext(); )
                        {
                            org.dom4j.Element e3 = (org.dom4j.Element) i3.next();

                            for ( Iterator i4 = e3.elementIterator(XMLTags.RESEARCHER); i4.hasNext(); )
                            {
                                counterTotal[0]++;

                                org.dom4j.Element e4 = (org.dom4j.Element) i4.next();

                                if(e4.element(XMLTags.RESEARCHER_WEB_ADDRESS) != null && e4.element(XMLTags.RESEARCHER_WEB_ADDRESS).elements().size() > 0)
                                {
                                    counterSuccess[0]++;
                                }
                            }
                        }
                        if(counterSuccess[0] > 0)
                            ProjectLogger.LOGGER.info("\tExist researchers webaddress for " + sUnitOfAssessment_Description + ".");
                        else
                            ProjectLogger.LOGGER.info("\tNot exist researchers webaddress for " + sUnitOfAssessment_Description + ".");

                        ProjectLogger.LOGGER.info("Results exist: " + sInstitutionName + " - " +  sUnitOfAssessment_Description + " - " + counterSuccess[0] + " / " + counterTotal[0]);
                    }
                }

                counterSuccess[1]+=counterSuccess[0];
                counterTotal[1]+=counterTotal[0];

                if(bSaveFile)
                {
                    File fField = new File(xmlFilePath);
                    FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
                    OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
                    BufferedWriter bw = new java.io.BufferedWriter(writer);
                    String sOut = document.asXML();
                    bw.write(sOut);
                    bw.close();
                    ProjectLogger.LOGGER.info(xmlFile + " updated.");
                }
            }

            ProjectLogger.LOGGER.info("Researches results:" + counterSuccess[1] + " / " + counterTotal[1]);
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex.getMessage(), ex);
        }
        finally
        {
            PageFetcher.stopConnectionMonitorThread();
        }
    }   

  
    /*
     * Check effectivity of the recollection and recount found web pages. (of PROCESS STEP 1)
     */
    public static void P1_checkEffectivityCollectResearcherLinks(String xmlFile)
    {
        P1_checkEffectivityCollectResearcherLinks(xmlFile, false, -1);
    }

    /**
     * Check effectivity of the recollection and recount found web pages. (of PROCESS STEP 1)
     * @param showOnlyBad
     * @param topPercent
     */
    public static void P1_checkEffectivityCollectResearcherLinks(String xmlFile, boolean showOnlyBad, float topPercent)
    {
        try
        {
            org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = reader.read(xmlFile);
            org.dom4j.Element root = document.getRootElement();

            String sInstitutionName = "";
            String sWebAddress = "";
            String sUnitOfAssessment_Description = "";
            String sResearchGroupDescription = "";
            String sResearchers = "";
            String sResearchersInitials = "";

            int[] counterSuccess = new int[2];
            int[] counterTotal = new int[2];
            for(int i = 0; i < counterSuccess.length; i++) counterSuccess[i] = 0;
            for(int i = 0; i < counterTotal.length; i++) counterTotal[i] = 0;

            if(showOnlyBad)
            {
                ProjectLogger.LOGGER.info("Show only departments with less than " + topPercent + "%.\r\n");
            }

            for ( Iterator i1 = root.elementIterator(XMLTags.INSTITUTION); i1.hasNext();)
            {
                org.dom4j.Element e1 = (org.dom4j.Element) i1.next();

                sInstitutionName = e1.element(XMLTags.INSTITUTION_NAME).getText();
                sWebAddress = e1.element(XMLTags.INSTITUTION_WEBADDRESS).getText();

                for ( Iterator i2 = e1.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext(); )
                {
                    org.dom4j.Element e2 = (org.dom4j.Element) i2.next();

                    sUnitOfAssessment_Description = e2.element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();
                    //FIXME if(sUnitOfAssessment_Description.length() > 20) sUnitOfAssessment_Description = sUnitOfAssessment_Description.substring(0, 20);
                    
                    boolean bExistDept = false;
                    String sURLs = "";

                    for ( Iterator i5 = e2.elementIterator(XMLTags.DEPARTMENT_WEB_ADDRESS); i5.hasNext(); )
                    {
                        org.dom4j.Element e5 = (org.dom4j.Element) i5.next();

                        sURLs += " " + e5.getText();
                        bExistDept = true;
                    }

//                    if(e2.element(XMLTags.DEPARTMENT_WEB_ADDRESS) != null)
//                    {
//                        bExistDept = true;
//                        //ProjectLogger.LOGGER.info("\tExist departments webaddress for " + sUnitOfAssessment_Description);
//                    }
//                    else
//                    {
//                        bExistDept = false;
//                    }

                    String sOut = "";
                    if(!bExistDept)
                    {
                        sOut = "FAIL: " + sInstitutionName + "(" + sWebAddress + ") departments webaddress: " + sUnitOfAssessment_Description;
                    }
                    else
                    {
                        sOut = "SUCCESS: " + sInstitutionName + "(" + sWebAddress + ") departments webaddress: " + sUnitOfAssessment_Description + " URLS= " + sURLs;
                    }

                    counterTotal[0] = 0;
                    counterSuccess[0] = 0;

                    String researchersText = "";
                    String researchersMissText = "";
                    for ( Iterator i3 = e2.elementIterator(XMLTags.RESEARCHGROUP); i3.hasNext(); )
                    {
                        org.dom4j.Element e3 = (org.dom4j.Element) i3.next();
                        sResearchGroupDescription = e3.element(XMLTags.RESEARCHGROUP_DESCRIPTION).getText();

                        for ( Iterator i4 = e3.elementIterator(XMLTags.RESEARCHER); i4.hasNext(); )
                        {
                            org.dom4j.Element e4 = (org.dom4j.Element) i4.next();
                            counterTotal[0]++;
                            if(e4.element(XMLTags.RESEARCHER_WEB_ADDRESS) != null && e4.element(XMLTags.RESEARCHER_WEB_ADDRESS).elements().size() > 0)
                            {
                                researchersText += ", " + e4.elementText(XMLTags.RESEARCHER_LASTNAME) + " " + e4.elementText(XMLTags.RESEARCHER_INITIALS);
                                counterSuccess[0]++;
                            }
                            else
                            {
                                researchersMissText += ", " + e4.elementText(XMLTags.RESEARCHER_LASTNAME) + " " + e4.elementText(XMLTags.RESEARCHER_INITIALS);
                            }
                        }
                    }

                    int percent = (counterSuccess[0]*100) / counterTotal[0];
                    if(showOnlyBad)
                    {
                        if(percent <= topPercent)
                        {
                            ProjectLogger.LOGGER.info("");
                            ProjectLogger.LOGGER.info("BAD RESULTS: " + sOut);
                            ProjectLogger.LOGGER.info("\tResearchers found: " + counterSuccess[0] + "/" + counterTotal[0] + "\t(" + percent + " %)");
                            ProjectLogger.LOGGER.info("\tFound: " + researchersText);
                            ProjectLogger.LOGGER.info("\tMiss: " + researchersMissText);
                        }
                    }
                    else
                    {
                        ProjectLogger.LOGGER.info("");
                        ProjectLogger.LOGGER.info(sOut);
                        ProjectLogger.LOGGER.info("\tResearchers found: " + counterSuccess[0] + "/" + counterTotal[0] + "\t(" + percent + " %)");
                        ProjectLogger.LOGGER.info("\tFound: " + researchersText);
                        ProjectLogger.LOGGER.info("\tMiss: " + researchersMissText);
                    }


                    counterTotal[1] += counterTotal[0];
                    counterSuccess[1] += counterSuccess[0];
                }
            }

            ProjectLogger.LOGGER.info("");
            ProjectLogger.LOGGER.info("TOTAL Researchers found: " + counterSuccess[1] + "/" + counterTotal[1]);
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.info(ex.getMessage());
        }
    }

    /*
     * PROCESS STEP 2 (1/2)
     * Downloads the websites identified as researcher's personal pages in this structure:
     *  .\[DIR_DOWNLOAD_PAGES]\CardiffUniversity\Chemistry\Redman#JE\55d8cb13\index.html, pub.html, cv.html
     * @param xmlFile
     * @param downloadPagesDir
     * @param nThreads
     */
    public static void P2_step_downloadResearchesPages(String xmlFile, String downloadPagesDir, int nThreads)
    {
        DownloaderResearchersWebPagesXMLFormat.downloadAllResearchersPagesWithThreads(xmlFile, downloadPagesDir, LocalFormatType.PLAIN_DIRECTORY, nThreads, false);
    }

    /*
     * FIX FOR PROCESS STEP 2 (1/2)
     * Downloads the websites identified as researcher's personal pages in this structure of a Insitution:
     *  .\[DIR_DOWNLOAD_PAGES]\CardiffUniversity\Chemistry\Redman#JE\55d8cb13\index.html, pub.html, cv.html
     * @param xmlFile
     * @param Institution
     * @param destDir
     */
    public static void P2_redownloadInstitution(String xmlFile, String Institution, String destDir) throws Exception
    {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        org.dom4j.Document document = reader.read(xmlFile);
        org.dom4j.Element root = document.getRootElement();
        
        for ( Iterator i1 = root.elementIterator(XMLTags.INSTITUTION); i1.hasNext();)
        {
            org.dom4j.Element e1 = (org.dom4j.Element) i1.next();
            
            if(e1.element(XMLTags.INSTITUTION_NAME).getText().equals(Institution))
            {
                DownloaderResearchersWebPagesXMLFormat.downloadResearchesPages(destDir, LocalFormatType.PLAIN_DIRECTORY, e1, true);
            }
        }
    }
    
    /*
     * PROCESS STEP 2 (2/2)
     * Cleans downloaded webpages removing useless information, such as HTML headers, JavaScript modules, etc
     *  .\[DIR_DOWNLOAD_PAGES]\*
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @throws Exception
     */
    public static void P2_step_cleanHtmlAllResearcherPages(String sourceXmlFile, String downloadPagesDir) throws Exception
    {        
        try
        {   
            CleanerResearchersWebpages o = new CleanerResearchersWebpages(new File(sourceXmlFile), new File(downloadPagesDir), LocalFormatType.PLAIN_DIRECTORY);
            o.iterate();
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.info(ex.getMessage());
        }
    }
    
     
    /*
     *  PROCESS STEP 3
     *  Writes XML file with the content of webpages for Data Collector.
     *  By the moment we used path files: only <infoblock id="xxxx" type=2>filepath</infoblock>
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @param destXmlFile
     * @throws Exception
     */     
    
    public static void P3_step_exportDocumentsOnXMLFileForTextMining(String sourceXmlFile, String downloadPagesDir, String destXmlFile) throws Exception
    {
        try
        {   
            ExportDocumentsOnXMLFileForTextMiningCreator o = new ExportDocumentsOnXMLFileForTextMiningCreator(new File(sourceXmlFile), new File(downloadPagesDir), new File(destXmlFile), LocalFormatType.PLAIN_DIRECTORY);
            o.iterate();
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.info(ex.getMessage());
        }
    }

    /*
     *  PROCESS STEP 3 (VERSION 2) - Not matter Hash, only read what there are in directories
     *  Writes XML file with the content of webpages for Data Collector.
     *  By the moment we used path files: only <infoblock id="xxxx" type=2>filepath</infoblock>
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @param destXmlFile
     * @throws Exception
     */
    public static void P3_step_exportWebPagesOnXMLFileForTextMining_v2_nommaterhash(String sourceXmlFile, String downloadPagesDir, String destXmlFile) throws Exception
    {
        try
        {         
            ExportDocumentsOnXMLFileForTextMiningCreatorV2 o = new ExportDocumentsOnXMLFileForTextMiningCreatorV2(new File(sourceXmlFile), new File(downloadPagesDir), new File(destXmlFile), LocalFormatType.PLAIN_DIRECTORY);
            o.iterate();
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.info(ex.getMessage());
        }

    }
    
    /*
     *  PROCESS STEP 3 (OPTIONAL, for study patterns)
     *  Filters webpages with xpath expression and writes XML file with the content
     *  of webpages for Data Collector.
     *  By the moment we used path files: only <infoblock id="xxxx" type=2>filepath</infoblock>
     * @param sourceXmlFile
     * @param downloadPagesDir     
     * @param xpathExp
     * @param destXmlFile 
     * @throws Exception
     */
    public static void P3_step_filterWebPagesWriteWebPagesOnXMLFileForTextMining(String sourceXmlFile, String downloadPagesDir, String xpathExp, String destXmlFile) throws Exception
    {
        try
        {         
            ExportDocumentsOnXMLFileForTextMiningCreatorWithFilter o = new ExportDocumentsOnXMLFileForTextMiningCreatorWithFilter(new File(sourceXmlFile), new File(downloadPagesDir), xpathExp, new File(destXmlFile), LocalFormatType.PLAIN_DIRECTORY);
            o.iterate();
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.info(ex.getMessage());
        }
    }          
   
}
