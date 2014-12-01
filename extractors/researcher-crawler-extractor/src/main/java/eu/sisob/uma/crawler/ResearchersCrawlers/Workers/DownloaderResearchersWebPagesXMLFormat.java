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

package eu.sisob.uma.crawler.ResearchersCrawlers.Workers;

import eu.sisob.uma.crawler.ProjectLogger;
import eu.sisob.uma.crawler.ResearchersCrawlers.ResearchersPagePostProcessor;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.XMLTags;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.LocalFormatType;
import eu.sisob.uma.footils.File.FileFootils;
import eu.sisob.uma.footils.Threads.MonitorThread;
import java.io.File;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;

/**
 * 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class DownloaderResearchersWebPagesXMLFormat extends Thread
{
    org.dom4j.Element elInstitution = null;
    String destDir = null;
    MonitorThread refmonitor = null;
    boolean redownload;
    LocalFormatType downloading_format_type;

    private DownloaderResearchersWebPagesXMLFormat(String destDir, LocalFormatType downloading_format_type, org.dom4j.Element elInstitution, MonitorThread refmonitor, boolean redownload)
    {
        this.elInstitution = elInstitution;        
        this.destDir = destDir;
        this.refmonitor = refmonitor;
        this.refmonitor.notifyTheadBegin();
        this.redownload = redownload;
        this.downloading_format_type = downloading_format_type;
    }

    @Override
    public void run()
    {
        ProjectLogger.LOGGER.info("Thread begin the work! ");
        downloadResearchesPages(destDir, downloading_format_type, elInstitution, redownload);
        ProjectLogger.LOGGER.info("Thread end the work! " );
        this.refmonitor.notifyTheadEnd();
    }    
    
    private static File giveMeTheDirectory(String path,boolean redownload) throws Exception
    {
        File dir = new File(path);
        if(redownload)
        {
            FileFootils.deleteDir(dir);
            if(!dir.mkdir()) throw new Exception("Cant create " + dir.getPath());
        }
        else
        {
            if(!dir.exists())
            {
                if(!dir.mkdir()) throw new Exception("Cant create " + dir.getPath());
            }
        }   
        return dir;
    }

    /*
     * Download all researches pages from Institution element (XML)
     * Format XML element:
     * <Institution>
     * <InstitutionName>Cardiff University</InstitutionName>
     *      <WebAddress>http://www.cardiff.ac.uk/</WebAddress>
     *      <UnitOfAssessment>
     *          <UnitOfAssessment_Description>Chemistry</UnitOfAssessment_Description>
     *          <ResearchGroup>
     *              <ResearchGroupDescription>Chemical Biology</ResearchGroupDescription>
     *              <Researcher>
     *                  <StaffIdentifier>102364</StaffIdentifier>
     *                  <LastName>Redman</LastName>
     *                 <Initials>J E</Initials>
     *                  <ResearcherWebAddress>http://www.cardiff.ac.uk/chemy/contactsandpeople/academicstaff/redman-james-overview_new.html</ResearcherWebAddress>
     *              </Researcher>-
     *          ...
     * 
     * To tree directory:
     * 
     *  dir:University
     *      =>  dir: UnitOfAssessment_Description1
     *          => dir: LastName#Initials
     *              => file: TYPE.HAST.EXT 
     *              => file: TYPE.HAST.EXT 
     *              => file: TYPE.HAST.EXT 
     *      
     * 
     * To plain directory: (we need the xml to map)
     * 
     *              => file: TYPE.HAST.EXT 
     *              => file: TYPE.HAST.EXT 
     *  
     */
    public static void downloadResearchesPages(String destDir, LocalFormatType downloading_format_type, org.dom4j.Element elInstitution, boolean redownload)
    {
        try
        {            
            String sInstitutionName = "";
            String sUnitOfAssessment_Description = "";
            String sResearchGroupDescription = "";
            String sResearchName = "";
            String sResearchInitials = "";

            org.dom4j.Element e1 = elInstitution; //(org.dom4j.Element) i1.next();

            sInstitutionName = e1.element(XMLTags.INSTITUTION_NAME).getText();           
            
            File dirBase = null;
            dirBase = DownloaderResearchersWebPagesXMLFormat.giveMeTheDirectory(destDir, false);
            
            File dirI = null;
            if(downloading_format_type.equals(LocalFormatType.TREE_DIRECTORY))                
                dirI = DownloaderResearchersWebPagesXMLFormat.giveMeTheDirectory(destDir + "\\" + sInstitutionName.replaceAll("[^a-z^A-Z]","") + "\\", redownload);            
           
            for ( Iterator i2 = e1.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext(); )
            {
                org.dom4j.Element e2 = (org.dom4j.Element) i2.next();

                sUnitOfAssessment_Description = e2.element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();                
                
                File dirUAD = null;
                if(downloading_format_type.equals(LocalFormatType.TREE_DIRECTORY)) 
                    dirUAD = DownloaderResearchersWebPagesXMLFormat.giveMeTheDirectory(dirI.getPath() + "\\" + sUnitOfAssessment_Description.replaceAll("[^a-z^A-Z]","") + "\\", redownload);                

                for ( Iterator i3 = e2.elementIterator(XMLTags.RESEARCHGROUP); i3.hasNext(); )
                {
                    org.dom4j.Element e3 = (org.dom4j.Element) i3.next();
                    sResearchGroupDescription = e3.element(XMLTags.RESEARCHGROUP_DESCRIPTION).getText();

                    for ( Iterator i4 = e3.elementIterator(XMLTags.RESEARCHER); i4.hasNext(); )
                    {
                        org.dom4j.Element e4 = (org.dom4j.Element) i4.next();

                        sResearchName = e4.element(XMLTags.RESEARCHER_LASTNAME).getText();
                        sResearchInitials = e4.element(XMLTags.RESEARCHER_INITIALS).getText();

                        String sAux = sResearchName.replaceAll("[^a-z^A-Z]","") + "#" + sResearchInitials.replaceAll("[^a-z^A-Z]","");

                        File dirR = null;
                        if(downloading_format_type.equals(LocalFormatType.TREE_DIRECTORY)) 
                            dirR = DownloaderResearchersWebPagesXMLFormat.giveMeTheDirectory(dirUAD.getPath() + "\\" + sAux + "\\", false);
                        else
                            dirR = dirBase;                            
                        
                        for ( Iterator i5 = e4.elementIterator(XMLTags.RESEARCHER_WEB_ADDRESS); i5.hasNext(); )
                        {
                            org.dom4j.Element e5 = (org.dom4j.Element) i5.next();

                            String url = e5.getText();
                            if(!url.equals(""))
                            {                                
                                String ext = e5.attributeValue(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT);
                                if(ext == null || ext == "") ext = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT_VALUE_DEFAULT_HTML;
                                String type = e5.attributeValue(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE);
                                if(type == null || type == "") ext = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_DEFAULT_CV;                            
                           
                                String fileDownloaded = ResearchersPagePostProcessor.downloadAndClean(dirR.getAbsolutePath(), type, url, ext, true, redownload);        
                                fileDownloaded = fileDownloaded;
                            }
                        }
                    }
                }
            }
        }
        catch(Exception ex)
        {            
            ProjectLogger.LOGGER.error("ERROR: "+ ex.getMessage());
        }
    }    


    /*
     * Download all researches pages from xml file (see comment of downloadResearchesPages) (multithread)
     * Format XML:
     * <root>
     * <Institution>...</Institution>
     * <Institution>...</Institution>
     */
    public static void downloadAllResearchersPagesWithThreads(String xmlFile, String destDir, LocalFormatType downloading_format_type, int numberThreads, boolean redownload)
    {        
        try
        {
            org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = reader.read(xmlFile);
            org.dom4j.Element root = document.getRootElement();                        
            
            //if(redownload)
            //    FileFootils.deleteDir(destDir + "\\");

            String sInstitutionName = "";
            String sUnitOfAssessment_Description = "";
            String sResearchGroupDescription = "";
            String sResearchName = "";
            String sResearchInitials = "";

            File dir = new File(destDir + "\\");
            if(!dir.exists())
            {                
                if(!dir.mkdir()) throw new Exception("Cant create " + dir.getPath());
            }

            MonitorThread monitor = new MonitorThread(numberThreads);

            //downloaderResearchesPages[] ath = new DownloaderResearchersWebPagesXMLFormat[numberThreads];
            //for(int i = 0; i < ath.length; i++) ath[i] = null;

            boolean bExit = false;
            boolean bAnyWorks = true;
            for ( Iterator i1 = root.elementIterator("Institution"); i1.hasNext() && !bExit;)
            {
                if(monitor.canCreateNewThread())
                {
                    DownloaderResearchersWebPagesXMLFormat ath = new DownloaderResearchersWebPagesXMLFormat(destDir, downloading_format_type, (org.dom4j.Element)i1.next(), monitor, redownload);
                    ath.start();
                }
                else
                {
                    sleep(5000);
                }
            }
        }
        catch(Exception ex)
        {
            //
        }
    }

    /*
     * Download all researches pages from xml file (see comment of downloadResearchesPages) (monothread)
     * Format XML:
     * <root>
     * <Institution>...</Institution>
     * <Institution>...</Institution>
     */
    public static void downloadAllResearchersPages(String xmlFile, String destDir, LocalFormatType downloading_format_type, boolean redownload)
    {
        try
        {            
            //if(true) return;
            org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = reader.read(xmlFile);
            org.dom4j.Element root = document.getRootElement();
            
            FileFootils.deleteDir(destDir + "\\");

            String sInstitutionName = "";
            String sUnitOfAssessment_Description = "";
            String sResearchGroupDescription = "";
            String sResearchName = "";
            String sResearchInitials = "";

            File dir = new File(destDir + "\\");
            if(!dir.mkdir()) throw new Exception("Cant create " + dir.getPath());

            for ( Iterator i1 = root.elementIterator("Institution"); i1.hasNext();)
            {
                org.dom4j.Element e1 = (org.dom4j.Element) i1.next();

                DownloaderResearchersWebPagesXMLFormat.downloadResearchesPages(destDir, downloading_format_type, e1, redownload);
            }
        }
        catch(Exception ex)
        {            
            ProjectLogger.LOGGER.error(ex.getMessage());
        }

    }

}
