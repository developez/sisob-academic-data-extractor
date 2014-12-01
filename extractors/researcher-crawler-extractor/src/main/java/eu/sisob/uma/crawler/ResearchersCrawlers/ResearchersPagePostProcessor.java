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

import eu.sisob.uma.crawler.ProjectLogger;
import eu.sisob.uma.crawler.ResearchersCrawlers.Utils.MurmurHash;
import eu.sisob.uma.crawler.ResearchersCrawlers.Workers.CleanerResearchersWebpages;
import eu.sisob.uma.footils.Web.Downloader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CompactHtmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


/**
 * This implements a mechanism to downloadAndLocateSubpages a researcher webpage and to try to locate other webpages in relations.
 * 
 * Example:
 * 
 *  mywebpage.html
 *      => Has link to mypublications.html
 *      => Has link to cv.pdf
 * 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ResearchersPagePostProcessor 
{
    /*
     * Simple single to obtain one instance of static_props
     */
    private static CleanerProperties static_props = null;
    public static CleanerProperties getCleanerProperties()
    {
        if(static_props == null)
        {
            static_props = new CleanerProperties();       

            static_props.setOmitComments(true);
            static_props.setOmitXmlDeclaration(true);
            static_props.setAdvancedXmlEscape(true);
            static_props.setNamespacesAware(false);
            static_props.setOmitDoctypeDeclaration(true);
        }
        
        return static_props;
    }
    
    /**     
     * 
     * @param props
     * @param path
     * @param nameFile
     * @param newNameFile
     */
    public static void cleanFile(CleanerProperties props, String path, String nameFile, String newNameFile)
    {        
        File fileURL = new File(path + File.separator + nameFile);
        // do parsing
        try
        {
            TagNode tagNode = new HtmlCleaner(props).clean(fileURL, "utf-8");                                                
            // serialize to xml file
            new CompactHtmlSerializer(props).writeToFile(
                tagNode, path + File.separator + newNameFile, "UTF-8"
            );
            ProjectLogger.LOGGER.info(path + File.separator + nameFile + " cleaned!");
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.warn(ex.getMessage() + " " + path + File.separator + nameFile + " NOT FOUND!");
        }
    }
    
    /**
     * Downloads and cleans a url passed to local media
     * 
     * Example:
     * 
     *  mywebpage.html
     *      => Has link to mypublications.html
     *      => Has link to cv.pdf
     *
     * @param destAbsPath   String Destination path of the file -   
     * @param type          type of the document (see XMLTags class)
     * @param url           url of the document
     * @param ext           extension of the document
     * @param clean         boolean - Indicate if the file must be cleaned
     * @param redownload    boolean - Indicate if the file must be redownloaded
     * 
     * @return file downloaded
     * 
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException 
     * @throws InterruptedException  
     */
    public static String downloadAndClean(String destAbsPath, String type, String url, String ext, boolean clean, boolean redownload) throws UnsupportedEncodingException, FileNotFoundException, IOException, InterruptedException
    {   
        String fileDownloaded = "";                
        
        String filename = getHashFileName(type, url, ext);
        File f = new File(destAbsPath + File.separator + filename);
        
        if(redownload)
        {   
            if(f.exists())
                f.delete();
        }
        
        if(!f.exists()) 
        {
            Thread.sleep(100);
            org.jsoup.nodes.Document doc = eu.sisob.uma.footils.Web.Downloader.tryToConnect(url, 20);
            
            if(doc != null)
            {   
                File fField = new File(destAbsPath + File.separator + filename);
                FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
                OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
                BufferedWriter bw = new java.io.BufferedWriter(writer);
                String sOut = doc.html();
                bw.write(sOut);
                bw.close();
                ProjectLogger.LOGGER.info(url + " ==> " + fField.getPath());
                fileDownloaded = fField.getName();
                if(clean)
                {
                   cleanFile(getCleanerProperties(),destAbsPath, fField.getName(), fField.getName());                    
                   fileDownloaded = fField.getName();
                }            
            }
            else
            {
                ProjectLogger.LOGGER.error(url + " cannot be downloaded");
            }
        } 
        else
        {
            ProjectLogger.LOGGER.warn(filename + " exist in the folder " + destAbsPath);
        }
     
        return fileDownloaded;
    }
    
    /*
     * Get a filename based in hash calculated from url
     * @param type  type of document
     * @param url   url of document
     * @param ext   extension of document
     */
    public static String getHashFileName(String type, String url, String ext)
    {
        return type + "." + Integer.toHexString(MurmurHash.hash(url.getBytes(), 5)) + "." + ext;
    }
    
    /*
     * 
     * [DEPRECATED] This implements a mechanism to downloadAndLocateSubpages a researcher webpage and to try to locate other webpages in relations.
     * 
     * Example:
     * 
     *  mywebpage.html
     *      => Has link to mypublications.html
     *      => Has link to cv.pdf
     *
     * @param props
     * @param destAbsPath
     * @param sURL
     * @param clean
     * @param redownload
     * @return
     * @throws UnsupportedEncodingException 
     * @throws FileNotFoundException 
     * @throws IOException
     * @throws InterruptedException  
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
    
     * 
    public static List<String> downloadCleanAndLocateSubpages(CleanerProperties props, String destAbsPath, String sURL, boolean clean, boolean redownload) throws UnsupportedEncodingException, FileNotFoundException, IOException, InterruptedException
    {   
        
        String filePathIndex = "";
        String filePathCV = "";
        String filePathPub = "";
        
        if(!redownload)
        {
            File fField = new File(destAbsPath + "\\" + "index.html");
            if(fField.exists())
            {
                ProjectLogger.LOGGER.info("EXIST " + sURL + " ==> " + fField.getPath());
                return null;
            }
        }                            

        Thread.sleep(100);
        org.jsoup.nodes.Document doc = eu.sisob.uma.footils.Web.Downloader.tryToConnect(sURL, 20);
        
        List<String> filesDownload = new ArrayList<String>();
    
        if(doc != null)
        {            
            filePathIndex = destAbsPath + File.separator + "index.html";
            File fField = new File(filePathIndex);
            FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
            OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
            BufferedWriter bw = new java.io.BufferedWriter(writer);
            String sOut = doc.html();
            bw.write(sOut);
            bw.close();
            ProjectLogger.LOGGER.info(sURL + " ==> " + fField.getPath());

            if(clean)
            {
                CleanerResearchersWebpages.cleanFile(props, destAbsPath, "index.html", "clean_index.html");
                filePathIndex = destAbsPath + File.separator + "clean_index.html";                
            }            
        }
        else
        {
            ProjectLogger.LOGGER.error("ERROR with " + sURL);
        }

        //Try to extract link from researcher page (link to CV, publications, etc)
        if(doc != null)
        {
            org.jsoup.select.Elements els = doc.body().select("a:containsOwn(curriculum), a:containsOwn(CV),a:containsOwn(biography), a:containsOwn(vitae)");

            if(!els.isEmpty())
            {
                org.jsoup.nodes.Element e = els.first();
                String sNewURL = e.absUrl("href");
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

                if(bNotHTML)
                {
                    try
                    {
                        filePathCV = destAbsPath + File.separator + "clean_cv." + sExt;
                        Downloader.downloadFile(sNewURL, filePathCV);
                        ProjectLogger.LOGGER.info(sNewURL + " ==> " + destAbsPath + File.separator + "clean_cv." + sExt);
                        filePathCV = (new File(filePathCV)).getAbsolutePath();
                    }
                    catch(Exception ex)
                    {
                        ProjectLogger.LOGGER.error("Error downloading: " + sNewURL + " ==> " + destAbsPath + File.separator + "clean_cv." + sExt);
                    }                                        
                }
                else
                {
                    org.jsoup.nodes.Document docCV = Downloader.tryToConnect(sNewURL, 20);

                    if(docCV != null)
                    {                            
                        filePathCV = destAbsPath + File.separator + "cv.html";
                        File fField = new File(destAbsPath + File.separator + "cv.html");
                        FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
                        OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
                        BufferedWriter bw = new java.io.BufferedWriter(writer);
                        String sOut = docCV.html();
                        bw.write(sOut);
                        bw.close();

                        ProjectLogger.LOGGER.info(sNewURL + " ==> " + fField.getPath());

                        if(clean)
                        {
                            CleanerResearchersWebpages.cleanFile(props, destAbsPath, "cv.html", "clean_cv.html");
                            filePathCV = destAbsPath + File.separator + "clean_cv.html";                            
                        }
                    }
                    else
                    {                                            
                        ProjectLogger.LOGGER.error("ERROR with " + sNewURL);
                    }
                }

            }
            
            els = doc.body().select("a:containsOwn(publications)");

            if(!els.isEmpty())
            {
                org.jsoup.nodes.Element e = els.first();
                String sNewURL = e.absUrl("href");

                String sExt = sNewURL.substring(sNewURL.lastIndexOf(".")+1);

                boolean bAux = false;
                for(String sAuxExt : asCompatibleExt)
                {
                    if(sAuxExt.equals(sExt))
                    {
                        bAux = true;
                    }
                }
                
                if(bAux)
                {
                    try
                    {
                        filePathPub = destAbsPath + File.separator + "clean_pub." + sExt;
                        Downloader.downloadFile(sNewURL, filePathPub);                                        
                        ProjectLogger.LOGGER.info(sNewURL + " ==> " + filePathPub);
                        filePathPub = (new File(filePathPub)).getAbsolutePath();
                    }
                    catch(Exception ex)
                    {
                        ProjectLogger.LOGGER.error("Error downloading: " + sNewURL + " ==> " + destAbsPath + File.separator + "clean_pub." + sExt);
                    }
                }
                else
                {
                    org.jsoup.nodes.Document docPub = eu.sisob.uma.footils.Web.Downloader.tryToConnect(sNewURL, 20);

                    if(docPub != null)
                    {
                        String s = docPub.baseUri();
                        filePathPub = destAbsPath + File.separator + "pub.html";
                        File fField = new File(filePathPub);
                        FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
                        OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
                        BufferedWriter bw = new java.io.BufferedWriter(writer);
                        String sOut = docPub.html();
                        bw.write(sOut);
                        bw.close();                                            
                        ProjectLogger.LOGGER.info(sNewURL + " ==> " + fField.getPath());

                        if(clean)
                        {
                            CleanerResearchersWebpages.cleanFile(props, destAbsPath, "pub.html", "clean_pub.html");
                            filePathPub = destAbsPath + File.separator + "clean_pub.html";                            
                        }                        
                    }
                    else
                    {                                            
                        ProjectLogger.LOGGER.info("ERROR with " + sNewURL);
                    }
                }
            }
        }
        
        if(filePathIndex != "")
            filesDownload.add(filePathIndex);
        
        if(filePathCV != "")
            filesDownload.add(filePathCV);
        
        if(filePathPub != "")
            filesDownload.add(filePathPub);
        
        return filesDownload;
    }
    */
}
