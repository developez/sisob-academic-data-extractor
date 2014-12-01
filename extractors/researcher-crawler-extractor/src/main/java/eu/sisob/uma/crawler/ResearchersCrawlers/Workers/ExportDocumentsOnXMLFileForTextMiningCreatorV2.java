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
import java.util.logging.Logger;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

/**
 * This class create an xml file that contains infoblock which parsing the prototypeTextMiningGate object.
 * The format is like this:
 *  <infoblock DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT=sStaffIndentifier 
 *             MIDDLE_ELEMENT_XML_ID_TEXTMININGPARSER_ATT=ID_TEXTMININGPARSER_GATERESEARCHER
 *             MIDDLE_ELEMENT_XML_ID_ANNOTATIONRECOLLECTING=ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC>  
 *  Local URI of webpage
 *  Note: In this version the hash of file used for to locate the folder doesnt matter (check this)
 *  </infoblock>
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ExportDocumentsOnXMLFileForTextMiningCreatorV2 extends IteratorReseachersFile
{        
    File dest_file_xml;
    org.dom4j.Document docOut;
    org.dom4j.Element  rootOut;
    long lTimerAux;
    int hitsTable[][];
    
    /**
     * 
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @param destXmlFile
     */
    public ExportDocumentsOnXMLFileForTextMiningCreatorV2(File source_file_xml, File downloadPagesDir, File dest_file_xml, LocalFormatType local_format_type)
    {
         IteratorReseachersFile(source_file_xml, downloadPagesDir, local_format_type);
         this.dest_file_xml = dest_file_xml;
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
    }

    /**
     * Reader folder of one researcher and takes the uri of clean file for to make infoblock. 
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
                if(!file.isDirectory()) continue;
                boolean b = false;
                for ( Iterator i5 = elementResearcher.elementIterator("ResearcherWebAddress"); i5.hasNext(); )
                {
                    org.dom4j.Element e5 = (org.dom4j.Element) i5.next();

                    String sURL = e5.getText();

                    byte[] bytes = sURL.getBytes();
                    String sAuxxx =
                            path + "\\" + Integer.toHexString(MurmurHash.hash(bytes, 5));

                    if(file.getPath().equals(sAuxxx.replace("\\\\", "\\")))
                    {    
                        b = true;
                        File dirFinalFiles = new File(file.getPath());
                        File[] afinalFiles = dirFinalFiles.listFiles();                                            

                        //Search all clean files
                        for( File finalFile : afinalFiles)
                        {
                            if(finalFile.getName().contains("clean_"))
                            {
                                //FIXME
                                if(!finalFile.getName().contains("pub"))
                                if(finalFile.exists())
                                {
                                    rootOut.addElement("infoblock")
                                           .addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT, sStaffIndentifier)
                                           .addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_TEXTMININGPARSER_ATT, DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER)
                                           .addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ANNOTATIONRECOLLECTING, DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC)
                                           .addText(finalFile.getAbsolutePath());
                                }
                            }
                        }
                    }
                }
                if(!b) Logger.getLogger("MyLog").warning("FILE MISSED: " + file.getPath());
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
        FileOutputStream fileOS = new java.io.FileOutputStream(dest_file_xml, false);
        OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
        BufferedWriter bw = new java.io.BufferedWriter(writer);
        String sOut = docOut.asXML();
        bw.write(sOut);
        bw.close();
        Logger.getLogger("MyLog").info(dest_file_xml + " export!.");
    }

}
