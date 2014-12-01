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

import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.IteratorReseachersFile;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.LocalFormatType;
import eu.sisob.uma.crawler.ResearchersCrawlers.Utils.MurmurHash;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jsoup.Jsoup;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ExportDocumentsOnXMLFileForTextMiningCreatorWithFilter extends IteratorReseachersFile
{        
    String xpathExp;
    File dest_file_xml;
    org.dom4j.Document docOut;
    org.dom4j.Element  rootOut;
    String sContentForView;
    long lTimerAux;
    int hitsTable[][];
    
    /**
     * 
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @param xpathExp
     * @param destXmlFile
     */
    public ExportDocumentsOnXMLFileForTextMiningCreatorWithFilter(File source_file_xml, File downloadPagesDir, String xpathExp, File dest_file_xml, LocalFormatType local_format_type)
    {
         IteratorReseachersFile(source_file_xml, downloadPagesDir, local_format_type);
         this.dest_file_xml = dest_file_xml;
         this.xpathExp = xpathExp;
    }    

    /**
     * 
     */
    @Override
    protected void beginActions() 
    {
        docOut = new DocumentFactory().createDocument();
        rootOut = docOut.addElement("root");
        
        hitsTable = new int[2][5];

        lTimerAux = java.lang.System.currentTimeMillis();
        
        sContentForView = ""; //For collect results
    }

    /**
     * 
     * @param elementResearcher
     * @param path
     * @param sInstitutionName
     * @param sWebAddress
     * @param sUnitOfAssessment_Description
     * @param sResearchGroupDescription
     * @param sResearchName
     * @param sResearchInitials
     * @param sStaffIndentifier
     */
    @Override
    protected boolean actionsInResearcherNode(Element elementResearcher, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description, String sResearchGroupDescription, ResearcherNameInfo researcherNameInfo, String sStaffIndentifier)
    {
        File fAux = new File(path);

        File[] adirRW = fAux.listFiles();

        if(adirRW != null)
        {
            for ( File file : adirRW)
            {
                for ( Iterator i5 = elementResearcher.elementIterator("ResearcherWebAddress"); i5.hasNext(); )
                {
                    org.dom4j.Element e5 = (org.dom4j.Element) i5.next();

                    String sURL = e5.getText();

                    byte[] bytes = sURL.getBytes();
                    String sAuxxx =
                            path + "\\" + Integer.toHexString(MurmurHash.hash(bytes, 5));

                    if(file.getPath().equals(sAuxxx.replace("\\\\", "\\")))
                    {
                        File fileURL = new File(file.getPath() + "\\clean_index.html");

                        boolean b = true;
                        org.jsoup.nodes.Document doc = null;
                        try
                        {
//                                                org.jsoup.nodes.Document doc2 = Jsoup.connect(sURL).get();
                            doc = Jsoup.parse(fileURL, "UTF-8", sURL);

                            org.jsoup.select.Elements els = doc.body().select(xpathExp);
                            //(":containsOwn(" + sLiteralExp + ")");
                            //logger.info("URL: " + fileURL.getPath() + " uri: " + doc.baseUri());
                            //logger.log(Level.INFO, fileURL.getAbsolutePath());
                            if(els.size() > 0)
                            {
                                //logger.info(fileURL.getAbsolutePath());
                                Logger.getLogger("MyLog").log(Level.INFO, "(" + xpathExp + ") => " + fileURL.getAbsolutePath());
                                hitsTable[0][0]++;
                                rootOut.addElement("infoblock")
                                   .addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT, sStaffIndentifier)
                                   .addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_TEXTMININGPARSER_ATT, DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER)
                                   .addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ANNOTATIONRECOLLECTING, DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC)
                                   .addText(fileURL.getAbsolutePath());

                                sContentForView += sURL + " - " + els.first().text() + " - " + (els.first().absUrl("href")) + "\r\n\r\n";
                            }
                            else
                                hitsTable[1][0]++;
                        }
                        catch(Exception ex)
                        {
                            Logger.getLogger("MyLog").log(Level.SEVERE, "ERROR URL: " + fileURL.getPath() + " Msg: " + ex.getMessage() + ".");
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 
     * @throws Exception
     */
    @Override
    protected void endActions() throws Exception
    {
        lTimerAux = java.lang.System.currentTimeMillis() - lTimerAux;
        Logger.getLogger("MyLog").info("Extracting Links in: " + lTimerAux + " ms");

        Logger.getLogger("MyLog").info("RESULTS: ");
        Logger.getLogger("MyLog").info("HITS: ");
        Logger.getLogger("MyLog").info("\t" + hitsTable[0][0] + "/" + (hitsTable[0][0] + hitsTable[1][0]));
        
        FileOutputStream fileOS = new java.io.FileOutputStream(dest_file_xml, false);
        OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
        BufferedWriter bw = new java.io.BufferedWriter(writer);
        String sOut = docOut.asXML();
        bw.write(sOut);
        bw.close();
        Logger.getLogger("MyLog").info("ResearcherPages.xml export!.");

        Logger.getLogger("MyLog").info(sContentForView);
    }

}
