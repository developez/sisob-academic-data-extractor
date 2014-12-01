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

package eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;


/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ConversorFromXMLtoCSV extends IteratorReseachersFile 
{   
    
    boolean refuseExecution;
    CSVWriter writer;
    File csvfilename;
    
    CSVWriter writer_nofound;
    File csvfilename_nofound;
    boolean first;
    boolean last;    
            
    /**
     * 
     * @param sourceXmlDocument 
     * @param destDir
     */
    public ConversorFromXMLtoCSV(org.dom4j.Document sourceXmlDocument, File csvfilename, File csvfilename_nofound)
    {
        IteratorReseachersFile(sourceXmlDocument, new File(""), LocalFormatType.PLAIN_DIRECTORY);      
        
        this.refuseExecution = false;
        this.writer = null;
        this.writer_nofound = null;
        this.csvfilename = csvfilename;
        this.csvfilename_nofound = csvfilename_nofound;
        this.first = true;
        this.last = true;
    }
    
    /**
     * 
     * @param sourceXmlDocument 
     * @param destDir
     */
    public ConversorFromXMLtoCSV(org.dom4j.Document sourceXmlDocument, CSVWriter writer, CSVWriter writer_nofound, boolean first, boolean last)
    {
        IteratorReseachersFile(sourceXmlDocument, new File(""), LocalFormatType.PLAIN_DIRECTORY);      
        
        this.refuseExecution = false;
        this.writer = writer;
        this.writer_nofound = writer_nofound;
        this.csvfilename = null;
        this.csvfilename_nofound = null;
        this.first = first;
        this.last = last;
    }
    
    /**
     * 
     * @throws Exception
     */
    @Override
    protected void beginActions() throws Exception
    {
        if(writer == null && this.csvfilename != null && first)
        {
            try
            {
                writer = new CSVWriter(new FileWriter(this.csvfilename), ';');
            }
            catch(Exception ex)
            {
                refuseExecution = true;
                writer.close();            
                writer = null;
                return;
            }
            
            try
            {
                writer_nofound = new CSVWriter(new FileWriter(this.csvfilename_nofound), ';');
            }
            catch(Exception ex)
            {
                refuseExecution = true;
                writer_nofound.close();            
                writer_nofound = null;
                return;
            }
        }   
            
        if(first)
        {
            try
            {
                String[] entries = new String[11];
                entries[0] = FileFormatConversor.CSV_COL_ID;
                entries[1] = FileFormatConversor.CSV_COL_NAME;
                entries[2] = FileFormatConversor.CSV_COL_FIRSTNAME;
                entries[3] = FileFormatConversor.CSV_COL_LASTNAME;
                entries[4] = FileFormatConversor.CSV_COL_INITIALS;
                entries[5] = FileFormatConversor.CSV_COL_SUBJECT;
                entries[6] = FileFormatConversor.CSV_COL_INSTITUTION_NAME;
                entries[7] = FileFormatConversor.CSV_COL_INSTITUTION_URL;
                entries[8] = FileFormatConversor.CSV_COL_RESEARCHER_PAGE_URL;
                entries[9] = FileFormatConversor.CSV_COL_RESEARCHER_PAGE_TYPE;
                entries[10] = FileFormatConversor.CSV_COL_RESEARCHER_PAGE_EXT;
                writer.writeNext(entries);                
            }
            catch(Exception ex)
            {
                refuseExecution = true;
                writer.close();            
                writer = null;
            }
            
            try
            {
                String[] entries = new String[11];
                entries[0] = FileFormatConversor.CSV_COL_ID;
                entries[1] = FileFormatConversor.CSV_COL_NAME;
                entries[2] = FileFormatConversor.CSV_COL_FIRSTNAME;
                entries[3] = FileFormatConversor.CSV_COL_LASTNAME;
                entries[4] = FileFormatConversor.CSV_COL_INITIALS;
                entries[5] = FileFormatConversor.CSV_COL_SUBJECT;
                entries[6] = FileFormatConversor.CSV_COL_INSTITUTION_NAME;
                entries[7] = FileFormatConversor.CSV_COL_INSTITUTION_URL;
                this.writer_nofound.writeNext(entries);                
            }
            catch(Exception ex)
            {
                refuseExecution = true;
                writer_nofound.close();            
                writer_nofound = null;
            }
        }        
    }
    
    /**
     * 
     * @throws Exception
     */
    @Override
    protected void endActions() throws Exception
    {        
        if(refuseExecution) return;
        
        if(writer != null && writer_nofound != null && last)
        {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getRootLogger().error(ex.getMessage());
            }
            writer = null;
            
            try {
                writer_nofound.close();
            } catch (IOException ex) {
                Logger.getRootLogger().error(ex.getMessage());
            }
            writer_nofound = null;
        }
    }      
    
    @Override
    protected boolean actionsInResearcherNode(org.dom4j.Element elementResearcher, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description, String sResearchGroupDescription, ResearcherNameInfo researcherNameInfo, String sStaffIndentifier){
        boolean has = false;
        for ( Iterator i5 = elementResearcher.elementIterator(XMLTags.RESEARCHER_WEB_ADDRESS); i5.hasNext();){
            org.dom4j.Element e5 = (org.dom4j.Element) i5.next();
            String url = e5.getText();
            if(!url.equals(""))
            {  
                has = true;
                break;  
            }
        }
        
        if(!has){
            String[] entries = new String[7];
            entries[0] = sStaffIndentifier;
            entries[1] = researcherNameInfo.whole_name;
            entries[2] = researcherNameInfo.first_name;
            entries[3] = researcherNameInfo.last_name;
            entries[4] = researcherNameInfo.initial;
            entries[5] = sUnitOfAssessment_Description;
            entries[6] = sInstitutionName;
            
            this.writer_nofound.writeNext(entries); 
        }
        return true;
    }
    
    
    /**
     * 
     * @param elementResearcher
     * @param path
     * @param sInstitutionName
     * @param sWebAddress
     * @param sUnitOfAssessment_Description
     * @param sResearchGroupDescription
     * @param sResearchInitials
     * @param sStaffIndentifier
     * @return  
     */
    @Override
    protected boolean actionsInResearcherWebPageNode(org.dom4j.Element elementResearcher, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description, String sResearchGroupDescription, ResearcherNameInfo researcherNameInfo, String sStaffIndentifier, String url, String ext, String type)
    {
        if(refuseExecution) return false;
        
        try
        {
            String[] entries = new String[11];
            entries[0] = sStaffIndentifier;
            entries[1] = researcherNameInfo.whole_name;
            entries[2] = researcherNameInfo.first_name;
            entries[3] = researcherNameInfo.last_name;
            entries[4] = researcherNameInfo.initial;
            entries[5] = sUnitOfAssessment_Description;
            entries[6] = sInstitutionName;
            entries[7] = sWebAddress;
            entries[8] = url;
            entries[9] = ext;
            entries[10] = type;
            writer.writeNext(entries);        
        }
        catch(Exception ex)
        {
            if(writer != null)
            {
                try {
                    writer.close();
                } catch (IOException ex1) {
                    Logger.getRootLogger().error(ex.getMessage());
                }
                writer = null;
            }
            return false;
        }
        
        return true;
    }
}
