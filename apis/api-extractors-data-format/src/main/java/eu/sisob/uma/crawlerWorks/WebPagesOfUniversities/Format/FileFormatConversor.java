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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class FileFormatConversor 
{    
    public static final String CSV_COL_ID = "ID"; //"StaffIdentifier"
    public static final String CSV_COL_NAME = "NAME"; //"Name"
    public static final String CSV_COL_FIRSTNAME = "FIRSTNAME"; //"FirstName"
    public static final String CSV_COL_LASTNAME = "LASTNAME"; //"LastName"
    public static final String CSV_COL_INITIALS = "INITIALS"; //"Initials"
    public static final String CSV_COL_EMAIL = "EMAIL"; //"Initials"
    public static final String CSV_COL_SUBJECT = "SUBJECT"; //"UnitOfAssessment_Description"
    public static final String CSV_COL_INSTITUTION_NAME = "INSTITUTION_NAME"; //"InstitutionName"
    public static final String CSV_COL_INSTITUTION_URL = "INSTITUTION_URL"; //"WebAddress"    
    public static final String CSV_COL_RESEARCHER_PAGE_URL = "RESEARCHER_PAGE_URL"; //"ResearcherWebAddress"
    public static final String CSV_COL_RESEARCHER_PAGE_TYPE = "RESEARCHER_PAGE_TYPE"; //"ResearcherWebAddress"
    public static final String CSV_COL_RESEARCHER_PAGE_EXT = "RESEARCHER_PAGE_EXT"; //"ResearcherWebAddress"
    public static final String CSV_COL_SCORE_URL = "SCORE_URL"; //"ResearcherWebAddress"
    public static final String CSV_COL_SCORE_EMAIL = "SCORE_EMAIL"; //"ResearcherWebAddress"
    public static final String CSV_COL_CV_FILE = "CV_FILE";
    public static final String CSV_COL_SCORE_CV_FILE = "SCORE_CV_FILE";
    
    /*
     * Create a Researcher XML File from Researcher CSV File (The researcher XML file will be used by crawler)
     * @param filePathCSV - filepath of input csv file
     * @param filePathXml - filepath of output xml file 
     * @return success indication
     */
    public static boolean createResearchersCSVFileFromXML(org.dom4j.Document sourceXmlDocument, File filePathCSV, File filePathCSV_nofound) throws FileNotFoundException, IOException
    {   
        boolean success = false;
        
        ConversorFromXMLtoCSV c = new ConversorFromXMLtoCSV(sourceXmlDocument, filePathCSV, filePathCSV_nofound);
        try {
            success = c.iterate();
        } catch (Exception ex) {
            Logger.getLogger("root").error("Error iterating document to create '" + filePathCSV + "'");
        }        
        return success;
    }
    
    /*
     * Create a Researcher XML File from Researcher CSV File (The researcher XML file will be used by crawler)
     * @param filePathCSV - filepath of input csv file
     * @param filePathXml - filepath of output xml file 
     * @return success indication
     */
    public static boolean createResearchersCSVFileFromXML(File[] fileXmlDocuments, File filePathCSV, File filePathCSV_nofound) throws FileNotFoundException, IOException
    {   
        boolean success = false;
        
        boolean first = true;
        boolean last = false;
        
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(filePathCSV), ';');
        }
        catch(Exception ex){
            Logger.getLogger("root").error("Error create writer to create '" + filePathCSV + "'");
            writer = null;                        
        }
        
        CSVWriter writer_nofound = null;
        try {
            writer_nofound = new CSVWriter(new FileWriter(filePathCSV_nofound), ';');
        }
        catch(Exception ex){
            Logger.getLogger("root").error("Error create writer to create '" + filePathCSV + "'");
            writer = null;                        
        }
        
        if(writer != null && writer_nofound != null)
        {
            for(int i = 0; i < fileXmlDocuments.length; i++) 
            {
                Document doc = null;                                
                org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();                
                
                try {
                    doc = reader.read(fileXmlDocuments[i]);
                } catch (DocumentException ex) {
                    Logger.getLogger("root").error("Error opening xml document '" + fileXmlDocuments[i].getPath() + "' to create '" + filePathCSV + "'");
                    doc = null;
                }                
                        
                if(i == fileXmlDocuments.length - 1) last = true;

                if(doc != null)
                {
                    ConversorFromXMLtoCSV c = new ConversorFromXMLtoCSV(doc, writer, writer_nofound, first, last);

                    try {
                        success = c.iterate();
                    } catch (Exception ex) {
                        Logger.getLogger("root").error("Error iterating document to create '" + filePathCSV + "'");
                    }        

                    if(first) first = false;
                }
            }
        }        
            
        return success;
    }
    
    /*
     * Create a Researcher XML File from Researcher CSV File (The researcher XML file will be used by crawler)
     * @param filePathCSV - filepath of input csv file
     * @param filePathXml - filepath of output xml file 
     * @return success indication
     */
    public static boolean createResearchersXMLFileFromCSV(File filePathCSV, File filePathXml) throws FileNotFoundException, IOException
    {   
        boolean sucess = false;
        
        CSVReader reader = new CSVReader(new FileReader(filePathCSV), ';');
        String [] nextLine;   
        int idStaffIdentifier= -1;int idName= -1;int idFirstName= -1;int idLastName= -1;int idInitials= -1;int idUnitOfAssessment_Description= -1;
        int idInstitutionName= -1;int idWebAddress= -1;int idResearchGroupDescription= -1;int idResearcherWebAddress = -1;int idResearcherWebAddressType = -1;int idResearcherWebAddressExt = -1;
        if ((nextLine = reader.readNext()) != null)
        {
            //Locate indexes            
            //Locate indexes                        
            for(int i = 0; i < nextLine.length; i++)            
            {
                String column_name = nextLine[i];
                if(column_name.equals(CSV_COL_ID))
                        idStaffIdentifier = i;
                else if(column_name.equals(CSV_COL_NAME))
                        idName = i;
                else if(column_name.equals(CSV_COL_FIRSTNAME))
                        idFirstName = i;
                else if(column_name.equals(CSV_COL_LASTNAME))
                        idLastName = i;
                else if(column_name.equals(CSV_COL_INITIALS))
                        idInitials = i;
                else if(column_name.equals(CSV_COL_SUBJECT))
                        idUnitOfAssessment_Description = i;
                else if(column_name.equals(CSV_COL_INSTITUTION_NAME))
                        idInstitutionName = i;
                else if(column_name.equals(CSV_COL_INSTITUTION_URL))
                        idWebAddress = i;                
                else if(column_name.equals(CSV_COL_RESEARCHER_PAGE_URL))
                        idResearcherWebAddress = i;          
                else if(column_name.equals(CSV_COL_RESEARCHER_PAGE_TYPE))
                        idResearcherWebAddressType = i;          
                else if(column_name.equals(CSV_COL_RESEARCHER_PAGE_EXT))
                        idResearcherWebAddressExt = i;          
            }                
        }        
           
        if(idLastName != -1 && idInitials != -1 && idStaffIdentifier != -1 && idWebAddress != -1 && idInstitutionName != -1 && idUnitOfAssessment_Description != -1)
        {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("root");            
            
            while ((nextLine = reader.readNext()) != null) 
            {      
                for(int k = 0; k < nextLine.length; k++)
                {
                    nextLine[k] = nextLine[k].replace("'", "");                
                }                
                
                Node n = root.selectSingleNode(XMLTags.INSTITUTION + "/" + XMLTags.INSTITUTION_NAME + "[text()='" + nextLine[idInstitutionName] + "']");
                Element eInstitution = null;
                if(n == null)
                {
                    eInstitution = root.addElement(XMLTags.INSTITUTION); 
                    eInstitution.addElement(XMLTags.INSTITUTION_NAME).addCDATA(nextLine[idInstitutionName]);
                    eInstitution.addElement(XMLTags.INSTITUTION_WEBADDRESS).addCDATA(idWebAddress == -1 ? "" : nextLine[idWebAddress]);                    
                }    
                else                
                {
                    eInstitution = n.getParent();
                }
                {
                    n = eInstitution.selectSingleNode(XMLTags.UNIT_OF_ASSESSMENT + "/" + XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION + "[text()='" + nextLine[idUnitOfAssessment_Description] + "']");
                    Element eUnitOfAssessment = null;
                    if(n == null)
                    {
                        eUnitOfAssessment = eInstitution.addElement(XMLTags.UNIT_OF_ASSESSMENT); 
                        eUnitOfAssessment.addElement(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).addCDATA(nextLine[idUnitOfAssessment_Description]);                        
                        //eUnitOfAssessment.addElement("DepartamentWebAddress").addCDATA("");                        
                    }    
                    else
                    {
                        eUnitOfAssessment = n.getParent();
                    }
                    {
                        String researchGroupDescription = "";
                        if(idResearchGroupDescription != -1)
                        {
                            researchGroupDescription = nextLine[idResearchGroupDescription];
                        }
                        n = eUnitOfAssessment.selectSingleNode(XMLTags.RESEARCHGROUP + "/" + XMLTags.RESEARCHGROUP_DESCRIPTION + "[text()='" + researchGroupDescription + "']");
                        Element eResearchGroup = null;
                        if(n == null)
                        {
                            eResearchGroup = eUnitOfAssessment.addElement(XMLTags.RESEARCHGROUP); 
                            eResearchGroup.addElement( XMLTags.RESEARCHGROUP_DESCRIPTION).addCDATA(researchGroupDescription);                                                        
                        }
                        else
                        {                               
                           eResearchGroup = n.getParent();
                        }
                        {
                            Element eResearcher = eResearchGroup.addElement(XMLTags.RESEARCHER); 
                            eResearcher.addElement(XMLTags.RESEARCHER_STAFFIDENTIFIER).addCDATA(nextLine[idStaffIdentifier]);                            
                            if(idName != -1)
                            {
                                eResearcher.addElement(XMLTags.RESEARCHER_NAME).addCDATA(nextLine[idName]);                            
                            }
                            
                            if(idFirstName != -1)
                            {
                                eResearcher.addElement(XMLTags.RESEARCHER_FIRSTNAME).addCDATA(nextLine[idFirstName]);                            
                            }
                            
                            eResearcher.addElement(XMLTags.RESEARCHER_LASTNAME).addCDATA(nextLine[idLastName]);                            
                            eResearcher.addElement(XMLTags.RESEARCHER_INITIALS).addCDATA(nextLine[idInitials]);       
                            
                            
                            if(idResearcherWebAddress != -1)
                            {
                                String researcherWebAddress = "";
                                String researcherWebAddressType = "";
                                String researcherWebAddressExt = "";
                                
                                researcherWebAddress = nextLine[idResearcherWebAddress].trim();
                                
                                if(!researcherWebAddress.equals(""))
                                {
                                
                                    if(idResearcherWebAddressType != -1)
                                        researcherWebAddressType = nextLine[idResearcherWebAddressType];
                                    else
                                        researcherWebAddressType = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_DEFAULT_CV;

                                    if(idResearcherWebAddressExt != -1)
                                        researcherWebAddressExt = nextLine[idResearcherWebAddressExt];
                                    else
                                        researcherWebAddressExt = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT_VALUE_DEFAULT_HTML;

                                    eResearcher.addElement(XMLTags.RESEARCHER_WEB_ADDRESS)              
                                                    .addAttribute(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE, researcherWebAddressType)
                                                    .addAttribute(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT, researcherWebAddressExt)
                                                    .addCDATA(researcherWebAddress);                            
                                }
                            }                            
                        }                        
                    }
                }
            }
            
            reader.close();
            
            FileOutputStream fileOS = new java.io.FileOutputStream(filePathXml, false);
            OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
            BufferedWriter bw = new java.io.BufferedWriter(writer);
            String sOut = document.asXML();
            bw.write(sOut);
            bw.close();     
            writer.close();
            fileOS.close();            
            
            sucess = true;
        }
        else
        {
            sucess = false;            
        }
        
        return sucess;    
        
    }    
    
    public static boolean checkResearchersCSV(File filePathCSV, boolean urls) throws FileNotFoundException, IOException
    {   
        boolean sucess = false;
        
        CSVReader reader = new CSVReader(new FileReader(filePathCSV), ';');
        String [] nextLine;   
        int idStaffIdentifier= -1;int idName= -1;int idFirstName= -1;int idLastName= -1;int idInitials= -1;int idUnitOfAssessment_Description= -1;
        int idInstitutionName= -1;int idWebAddress= -1;int idResearchGroupDescription= -1;int idResearcherWebAddress = -1;int idResearcherWebAddressType = -1;int idResearcherWebAddressExt = -1;
        if ((nextLine = reader.readNext()) != null)
        {
            //Locate indexes            
            //Locate indexes                        
            for(int i = 0; i < nextLine.length; i++)            
            {
                String column_name = nextLine[i];
                if(column_name.equals(CSV_COL_ID))
                        idStaffIdentifier = i;
                else if(column_name.equals(CSV_COL_NAME))
                        idName = i;
                else if(column_name.equals(CSV_COL_FIRSTNAME))
                        idFirstName = i;
                else if(column_name.equals(CSV_COL_LASTNAME))
                        idLastName = i;
                else if(column_name.equals(CSV_COL_INITIALS))
                        idInitials = i;
                else if(column_name.equals(CSV_COL_SUBJECT))
                        idUnitOfAssessment_Description = i;
                else if(column_name.equals(CSV_COL_INSTITUTION_NAME))
                        idInstitutionName = i;
                else if(column_name.equals(CSV_COL_INSTITUTION_URL))
                        idWebAddress = i;                
                else if(column_name.equals(CSV_COL_RESEARCHER_PAGE_URL))
                        idResearcherWebAddress = i;          
                else if(column_name.equals(CSV_COL_RESEARCHER_PAGE_TYPE))
                        idResearcherWebAddressType = i;          
                else if(column_name.equals(CSV_COL_RESEARCHER_PAGE_EXT))
                        idResearcherWebAddressExt = i;          
            }                
        }        
           
        if(idLastName != -1 && idInitials != -1 && idStaffIdentifier != -1 && 
           (!urls ? idWebAddress != -1 && idInstitutionName != -1 : idResearcherWebAddress != -1))
        { 
            sucess = true;
            while ((nextLine = reader.readNext()) != null) 
            {      
                if(!(idLastName < nextLine.length &&
                   idInitials < nextLine.length &&
                   idStaffIdentifier < nextLine.length))
                {
                    sucess = false;
                    break;
                }              
                
                if(urls){
                    if(!(idResearcherWebAddress < nextLine.length)){
                        sucess = false;
                        break;
                    }
                } else {
                    if(!(idWebAddress < nextLine.length &&
                       idInstitutionName < nextLine.length)){
                        sucess = false;
                        break;
                    }
                }
            }       
            
            reader.close();                       
        }
        else
        {
            sucess = false;
        }
        
        return sucess;    
        
    }    
}
