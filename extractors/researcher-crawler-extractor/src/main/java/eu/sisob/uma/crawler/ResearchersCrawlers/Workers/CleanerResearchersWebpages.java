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

import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.IteratorReseachersFile;
import eu.sisob.uma.crawler.ResearchersCrawlers.ResearchersPagePostProcessor;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.LocalFormatType;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.XMLTags;
import java.io.File;
import java.util.Iterator;
import org.dom4j.Element;

/**
 * This class cleans html webpages saved in cache with org.htmlcleaner
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CleanerResearchersWebpages extends IteratorReseachersFile
{   
    long lTimerAux;
    int hitsTable[][];
    
    /**
     * 
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @param local_format_type  
     */
    public CleanerResearchersWebpages(File source_file_xml, File downloadPagesDir, LocalFormatType local_format_type)
    {
        IteratorReseachersFile(source_file_xml, downloadPagesDir, local_format_type);         
    }
    
    /**
     * 
     */
    protected void beginActions() 
    {
        hitsTable = new int[2][5];
        lTimerAux = java.lang.System.currentTimeMillis();
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
     * @return  
     */
    @Override
    protected boolean actionsInResearcherNode(Element elementResearcher, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description, String sResearchGroupDescription, ResearcherNameInfo researcherNameInfo, String sStaffIndentifier)
    {        
        for ( Iterator i5 = elementResearcher.elementIterator("ResearcherWebAddress"); i5.hasNext(); )
        {
            org.dom4j.Element e5 = (org.dom4j.Element) i5.next();

            String url = e5.getText();
            if(!url.equals(""))
            {                                
                String ext = e5.attributeValue(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT);
                if(ext == null || ext == "") ext = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT_VALUE_DEFAULT_HTML;
                String type = e5.attributeValue(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE);
                if(type == null || type == "") ext = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_DEFAULT_CV;      

                String filename = ResearchersPagePostProcessor.getHashFileName(type, url, ext);

                ResearchersPagePostProcessor.cleanFile(ResearchersPagePostProcessor.getCleanerProperties(), path, filename, filename);    
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
    }

}
