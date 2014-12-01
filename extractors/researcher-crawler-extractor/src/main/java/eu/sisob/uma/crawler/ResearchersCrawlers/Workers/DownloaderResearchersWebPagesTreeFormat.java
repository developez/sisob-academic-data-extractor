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
import eu.sisob.uma.footils.File.FileFootils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;

/**
 * Class that implements a downloadAndLocateSubpages mechanismo to downloadAndLocateSubpages the pages of researchers to a local media.
 * The format used is a TreeMap like this:
 * 
 *  Tree info format of treeInstitution     
 *      TreeMap<String,     => subject
 *              TreeMap<String, => researcher
 *              List<String>    => researchers_pages
 *          >>
 * 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class DownloaderResearchersWebPagesTreeFormat extends Thread
{
    private static class monitorThread
    {
        public monitorThread(int nmaxthread)
        {            
            nthreadslots = nmaxthread;
        }

        public synchronized void notifyTheadBegin()
        {
            nthreadslots--;
        }

        public synchronized void notifyTheadEnd()
        {
            nthreadslots++;
        }

        public synchronized boolean canCreateNewThread()
        {
            return (nthreadslots > 0);
        }
        
        public int nthreadslots;
    }

    TreeMap<String,
            TreeMap<String, List<String>>
    > treeInstitution;
    String destDir;
    monitorThread refmonitor;
    String sInstitutionName; 
    
    
    /**
     * Constructor
     * Note:     
     *  Tree info format of treeInstitution     
     *      TreeMap<String,     => subject
     *              TreeMap<String, => researcher
     *              List<String>    => researchers_pages
     *          >>
     * @param destDir     
     * @param sInstitutionName
     * @param treeInstitution
     * @param refmonitor     
     */    
    private DownloaderResearchersWebPagesTreeFormat(String destDir, 
                                      String sInstitutionName,
                                      TreeMap<String,
                                              TreeMap<String, List<String>>
                                      > treeInstitution, 
                                      monitorThread refmonitor)
    {
        this.sInstitutionName = sInstitutionName;
        this.treeInstitution = treeInstitution;        
        this.destDir = destDir;
        this.refmonitor = refmonitor;
        this.refmonitor.notifyTheadBegin();
    }

    @Override
    public void run()
    {
        ProjectLogger.LOGGER.info("Thread begin the work! ");
        downloadResearchesPages(destDir, sInstitutionName, treeInstitution);
        ProjectLogger.LOGGER.info("Thread end the work! " );
        this.refmonitor.notifyTheadEnd();
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

    /*
     * Download all researches pages from Institution element (treemap)
     *  Tree info format of treeInstitution     
     *      TreeMap<String,     => subject
     *              TreeMap<String, => researcher
     *              List<String>    => researchers_pages
     *          >>
     * 
     * @param destDir     
     * @param sInstitutionName
     * @param treeInstitution    
     */
    public static void downloadResearchesPages(String destDir,
                                               String sInstitutionName,
                                               TreeMap<String,
                                                TreeMap<String, List<String>>
                                               > treeInstitution) 
    {
        try
        {
            CleanerProperties props = new CleanerProperties();

            // set some properties to non-default values
            //props.setTranslateSpecialEntities(true);
            //props.setTransResCharsToNCR(true);
            props.setOmitComments(true);
            props.setOmitXmlDeclaration(true);
            props.setAdvancedXmlEscape(true);
            props.setNamespacesAware(false);
            props.setOmitDoctypeDeclaration(true);
            
            String sUnitOfAssessment_Description = "";
            String sResearchGroupDescription = "";
            String sResearchName = "";
            String sResearchInitials = "";            

            File dirI = new File(destDir + System.getProperty("file.separator") + sInstitutionName.replaceAll("[^a-z^A-Z]","") + System.getProperty("file.separator"));
            if(!dirI.mkdir()) throw new Exception("Cant create " + dirI.getPath());
            else
            for (String keyAssessment_Description : treeInstitution.keySet())
            {
                sUnitOfAssessment_Description = keyAssessment_Description;
                        
                if(sUnitOfAssessment_Description.length() > 20) sUnitOfAssessment_Description = sUnitOfAssessment_Description.substring(0, 20);

                File dirUAD = new File(dirI.getPath() + System.getProperty("file.separator") + sUnitOfAssessment_Description.replaceAll("[^a-z^A-Z]","") + System.getProperty("file.separator"));
                if(!dirUAD.mkdir()) throw new Exception("Cant create " + dirUAD.getPath());
                
                    TreeMap<String, List<String>> treeResearchers = treeInstitution.get(keyAssessment_Description);
                    
                    for (String keyResearcher : treeResearchers.keySet())
                    {
                        String sAux = keyResearcher;

                        File dirR = new File(dirUAD.getPath() + System.getProperty("file.separator") + sAux + System.getProperty("file.separator"));
                        if(!dirR.exists())
                        {
                            if(!dirR.mkdir()) throw new Exception("Cant create " + dirR.getPath());
                        }
                        else
                        {
                            ProjectLogger.LOGGER.info("Repeated: " + sAux);
                            break;
                        }
                        
                        List<String> lstResearcherWebAddress = treeResearchers.get(keyResearcher);

                        //int iCount = 0;
                        List<String> lstLocalResearcherWebAddress = new ArrayList<String>();
                        for (String url : lstResearcherWebAddress)
                        {
                            byte[] bytes = url.getBytes();         
                            
                            String ext = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT_VALUE_DEFAULT_HTML;
                            String type = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_DEFAULT_CV;
                            
                            String fileDownloaded = ResearchersPagePostProcessor.downloadAndClean(dirR.getAbsolutePath(), type, url, ext, true, true);    
                            if(fileDownloaded != "")                            
                                lstLocalResearcherWebAddress.add(fileDownloaded);                            
                        }            
                        
                        lstResearcherWebAddress.clear();
                        lstResearcherWebAddress.addAll(lstLocalResearcherWebAddress);
                }
            }
        }
        catch(Exception ex)
        {            
            ProjectLogger.LOGGER.error("ERROR: "+ ex.getMessage());
        }
    }
    
    /*
     * Download all researches pages from the treemap with many thread
     * 
     *  TreeMap<String, => Instituion
     *      TreeMap<String,     => subject
     *              TreeMap<String, => researcher
     *              List<String>    => researchers_pages
     *          >>>
     *      
     * @param tree
     * @param destDir     
     * @param numberThreads    
     */    
    public static void downloadAllResearchersPagesWithThreads(TreeMap<String,
                                                              TreeMap<String,
                                                                    TreeMap<String, List<String>>
                                                                   >
                                                              > tree,
                                                              String destDir, 
                                                              int numberThreads)
    {        
        try
        {                  
            
            FileFootils.deleteDir(destDir + System.getProperty("file.separator"));

            File dir = new File(destDir + System.getProperty("file.separator"));
            if(!dir.mkdir()) throw new Exception("Cant create " + dir.getPath());

            monitorThread monitor = new monitorThread(numberThreads);

            //downloaderResearchesPages[] ath = new downloaderResearchesPages[numberThreads];
            //for(int i = 0; i < ath.length; i++) ath[i] = null;

            boolean bExit = false;
            boolean bAnyWorks = true;
            for (String keyInstitution : tree.keySet())
            {
                if(monitor.canCreateNewThread())
                {
                    DownloaderResearchersWebPagesTreeFormat ath = new DownloaderResearchersWebPagesTreeFormat(destDir, keyInstitution, tree.get(keyInstitution), monitor);
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
     * Download all researches pages from the treemap in single thread
     * 
     *  TreeMap<String, => Instituion
     *      TreeMap<String,     => subject
     *              TreeMap<String, => researcher
     *              List<String>    => researchers_pages
     *          >>>
     *      
     * @param tree
     * @param destDir          
     */    
    public static void downloadAllResearchersPages (TreeMap<String,
                                                            TreeMap<String,
                                                                    TreeMap<String, List<String>>
                                                                   >
                                                           > tree,
                                                    String destDir)
    {
        try
        {            
            //if(true) return;           
            ProjectLogger.LOGGER.info("Begin to download and clean pages ("+destDir+")");
            FileFootils.deleteDir(destDir + System.getProperty("file.separator"));

            String sInstitutionName = "";
            String sUnitOfAssessment_Description = "";
            String sResearchGroupDescription = "";
            String sResearchName = "";
            String sResearchInitials = "";

            File dir = new File(destDir + System.getProperty("file.separator"));
            if(!dir.mkdir()) throw new Exception("Cant create " + dir.getPath());

            for (String keyInstitution : tree.keySet())
            {
                DownloaderResearchersWebPagesTreeFormat.downloadResearchesPages(destDir, keyInstitution, tree.get(keyInstitution));
            }
        }
        catch(Exception ex)
        {            
            ProjectLogger.LOGGER.error(ex.getMessage());
        }
        
        ProjectLogger.LOGGER.info("Download and clean pages end");
    }    
}
