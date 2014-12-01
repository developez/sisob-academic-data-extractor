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

import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.IteratorReseachersFile;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.LocalFormatType;
import java.io.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
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
public class ResearcherXMLFileSplitter extends IteratorReseachersFile
{        
    String prefix_file;    
    
    /**
     * 
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @param destXmlFile
     */
    public ResearcherXMLFileSplitter(org.dom4j.Document sourceXmlDocument, File work_dir, String prefix_file)
    {
         IteratorReseachersFile(sourceXmlDocument, work_dir, LocalFormatType.PLAIN_DIRECTORY);     
         this.prefix_file = prefix_file;
    }

    /**
     * 
     */
    @Override
    protected void beginActions() 
    {        
        if(!work_dir.exists())
        {
            work_dir.mkdir();
        }
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
    protected boolean actionsInInstitutionNode(org.dom4j.Element elementInstitution, String path, String sInstitutionName, String sWebAddress)
    {   
        Document split_doc = DocumentHelper.createDocument();
        Element split_root = split_doc.addElement( "root" );
        split_root.add((Element) elementInstitution.clone());
        File split_file = new File(work_dir, prefix_file + sInstitutionName.replaceAll("\\W+", "").toLowerCase());
        try {
            FileUtils.write(split_file, split_doc.asXML(), "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger("roor").error("Cannot create xml file for " + split_file.getPath());
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
       
    }

}
