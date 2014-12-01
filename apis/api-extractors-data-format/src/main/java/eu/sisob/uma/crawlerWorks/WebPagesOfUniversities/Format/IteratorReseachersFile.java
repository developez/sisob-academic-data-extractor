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

import java.io.File;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * This abstract class iterate along the folder structure made for store copies of researchers webpages.
 * 
 * The structure is something like this:
 *      (base_directory => Universities folders => Discipline folders => Researchers foldes => webpages folders (hash of url)
 * Example:
 *  download_pages\UniversityofBirmingham\Chemistry\Worth#G\3767dadc 
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
abstract public class IteratorReseachersFile 
{    
    /**
     * 
     */
    protected File source_file_xml;
    /**
     * 
     */
    protected org.dom4j.Document sourceXmlDocument;
    /**
     * 
     */
    protected org.dom4j.Document document;
    /**
     * 
     */
    protected org.dom4j.Element root;
    /**
     * 
     */
    protected File work_dir;
    /**
     * 
     */
    //protected boolean createFolders;
    
    
    
    
    protected LocalFormatType local_format_type;
    
    /**
     * 
     * @param createFolders
     * @param sourceXmlFile
     * @param destDir
     */
    public void IteratorReseachersFile(File source_file_xml, File work_dir, LocalFormatType local_format_type)
    {
        this.source_file_xml = source_file_xml;
        this.sourceXmlDocument = null;
        this.root = null;
        this.work_dir = work_dir;                
        //this.createFolders = createFolders;
        this.local_format_type = local_format_type;
    }
    
    /**
     * 
     * @param createFolders
     * @param sourceXmlDocument 
     * @param local_format_type 
     * @param destDir
     */
    public void IteratorReseachersFile(org.dom4j.Document sourceXmlDocument, File work_dir, LocalFormatType local_format_type)
    {
        this.source_file_xml = null;
        this.sourceXmlDocument = sourceXmlDocument;
        this.root = null;
        this.work_dir = work_dir;                
        //this.createFolders = createFolders;
        this.local_format_type = local_format_type;
    }
    
    /**
     * 
     * @param createFolders
     * @param root 
     * @param sourceXmlDocument 
     * @param local_format_type 
     * @param destDir
     */
    public void IteratorReseachersFile(org.dom4j.Element root, File work_dir, LocalFormatType local_format_type)
    {
        this.source_file_xml = null;
        this.sourceXmlDocument = null;
        this.root = root;
        this.work_dir = work_dir;                
        //this.createFolders = createFolders;
        this.local_format_type = local_format_type;
    }
    
    /**
     * 
     * @throws Exception
     */
    public boolean iterate() throws Exception
    {
        if(source_file_xml == null)            
        {
            document = sourceXmlDocument;
            root = document.getRootElement(); 
        }
        else if(sourceXmlDocument != null)
        {
            org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
            document = reader.read(source_file_xml);               
            root = document.getRootElement(); 
        }
        else if(root != null)
        {
            root = root; 
        }
        else
        {
            return false;
        }
        
        String sInstitutionName = "";
        String sWebAddress = "";
        String sUnitOfAssessment_Description = "";        
        String sResearchGroupDescription = "";
        String sResearchName = "";
        String sResearchFirstName = "";
        String sResearchLastName = "";
        String sResearchInitials = "";
        String sStaffIndentifier = ""; 
        
        String dirBase = work_dir + "\\";
        
        boolean end = false;
        
        try
        {
            beginActions();
        }
        catch(Exception ex)
        {
            end = true;
            Logger.getLogger("root").error("", ex);
        }
        
        for ( Iterator i1 = root.elementIterator(XMLTags.INSTITUTION); i1.hasNext() && !end;)
        {
            org.dom4j.Element e1 = (org.dom4j.Element) i1.next();

            sInstitutionName = e1.element(XMLTags.INSTITUTION_NAME).getText();
            sWebAddress = e1.element(XMLTags.INSTITUTION_WEBADDRESS).getText();

            String dirI = "";
            if(local_format_type.equals(LocalFormatType.TREE_DIRECTORY))   
                dirI = dirBase + "\\" + sInstitutionName.replaceAll("[^a-z^A-Z]","") + "\\";
            else if(local_format_type.equals(LocalFormatType.PLAIN_DIRECTORY))   
                dirI = dirBase;
            
            end = !actionsInInstitutionNode(e1, dirI, sInstitutionName, sWebAddress);                                                

            for ( Iterator i2 = e1.elementIterator(XMLTags.UNIT_OF_ASSESSMENT); i2.hasNext() && !end; )
            {
                org.dom4j.Element e2 = (org.dom4j.Element) i2.next();

                sUnitOfAssessment_Description = e2.element(XMLTags.UNIT_OF_ASSESSMENT_DESCRIPTION).getText();                

                String dirUAD = "";
                if(local_format_type.equals(LocalFormatType.TREE_DIRECTORY))   
                    dirUAD = dirI + "\\" + sUnitOfAssessment_Description.replaceAll("[^a-z^A-Z]","") + "\\";  
                else if(local_format_type.equals(LocalFormatType.PLAIN_DIRECTORY))   
                    dirUAD = dirBase;
                
                end = !actionsInUnitOfAssessmentNode(e2, dirUAD, sInstitutionName, sWebAddress, sUnitOfAssessment_Description);                                                
                
                for ( Iterator i3 = e2.elementIterator(XMLTags.RESEARCHGROUP); i3.hasNext() && !end; )
                {
                    org.dom4j.Element e3 = (org.dom4j.Element) i3.next();
                    sResearchGroupDescription = e3.element(XMLTags.RESEARCHGROUP_DESCRIPTION).getText();

                    for ( Iterator i4 = e3.elementIterator(XMLTags.RESEARCHER); i4.hasNext() && !end; )
                    {
                        org.dom4j.Element e4 = (org.dom4j.Element) i4.next();

                        sResearchLastName = e4.element(XMLTags.RESEARCHER_LASTNAME).getText();
                        sResearchInitials = e4.element(XMLTags.RESEARCHER_INITIALS).getText();                        
                        sResearchFirstName = e4.element(XMLTags.RESEARCHER_FIRSTNAME) == null ? "" : e4.element(XMLTags.RESEARCHER_FIRSTNAME).getText();
                        sResearchName = e4.element(XMLTags.RESEARCHER_NAME) == null ? "" : e4.element(XMLTags.RESEARCHER_NAME).getText();                                    
                        
                        sStaffIndentifier = e4.element(XMLTags.RESEARCHER_STAFFIDENTIFIER).getText();                        
                        
                        String sAux = sResearchLastName.replaceAll("[^a-z^A-Z]","") + "#" + sResearchInitials.replaceAll("[^a-z^A-Z]","");                        

                        String dirR = "";
                        if(local_format_type.equals(LocalFormatType.TREE_DIRECTORY))   
                            dirR = dirUAD + "\\" + sAux + "\\";
                        else if(local_format_type.equals(LocalFormatType.PLAIN_DIRECTORY))   
                            dirR = dirBase;
                        
                        ResearcherNameInfo rsi = new ResearcherNameInfo(sResearchLastName, sResearchInitials, sResearchFirstName, sResearchName);  
                        end = !actionsInResearcherNode(e4, dirR, sInstitutionName, sWebAddress, sUnitOfAssessment_Description, sResearchGroupDescription, rsi, sStaffIndentifier);                                                
                                                
                        for ( Iterator i5 = e4.elementIterator(XMLTags.RESEARCHER_WEB_ADDRESS); i5.hasNext() && !end; )
                        {
                            org.dom4j.Element e5 = (org.dom4j.Element) i5.next();

                            String url = e5.getText();
                            if(!url.equals(""))
                            {                               
                                
                                String ext = e5.attributeValue(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT);
                                if(ext == null || ext == "") ext = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_EXT_VALUE_DEFAULT_HTML;
                                String type = e5.attributeValue(XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE);
                                if(type == null || type == "") ext = XMLTags.RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_DEFAULT_CV;                            
                           
                                end = !actionsInResearcherWebPageNode(e4, dirR, sInstitutionName, sWebAddress, sUnitOfAssessment_Description, sResearchGroupDescription, rsi, sStaffIndentifier, url, ext, type);                                                
                            }
                        }                        
                    }
                }
            }
        }
        
        try
        {
            endActions();   
        }
        catch(Exception ex)
        {
            Logger.getLogger("root").error("", ex);
        }
            
        
        return !end;
    }
    
    /**
     * 
     * @throws Exception
     */
    protected void beginActions() throws Exception
    {
    }
    
    /**
     * 
     * @param elementUnitOfAssessment
     * @param path
     * @param sInstitutionName
     * @param sWebAddress     
     * @return  
     */
    protected boolean actionsInInstitutionNode(org.dom4j.Element elementUnitOfAssessment, String path, String sInstitutionName, String sWebAddress)
    {        
        return true;
    }
    
    /**
     * 
     * @param elementUnitOfAssessment
     * @param path
     * @param sInstitutionName
     * @param sWebAddress
     * @param sUnitOfAssessment_Description     
     * @return  
     */
    protected boolean actionsInUnitOfAssessmentNode(org.dom4j.Element elementUnitOfAssessment, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description)
    {        
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
     * @param sResearchWholeName 
     * @param sResearchFirstName 
     * @param sResearchLastName 
     * @param sResearchInitials
     * @param sStaffIndentifier
     * @return  
     */
    protected boolean actionsInResearcherNode(org.dom4j.Element elementResearcher, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description, String sResearchGroupDescription, ResearcherNameInfo researcherNameInfo, String sStaffIndentifier)
    {
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
     * @param sResearchWholeName 
     * @param sResearchFirstName 
     * @param sResearchLastName 
     * @param sResearchInitials
     * @param sStaffIndentifier
     * @return  
     */
    protected boolean actionsInResearcherWebPageNode(org.dom4j.Element elementResearcher, String path, String sInstitutionName, String sWebAddress, String sUnitOfAssessment_Description, String sResearchGroupDescription, ResearcherNameInfo researcherNameInfo, String sStaffIndentifier, String url, String ext, String type)
    {
        return true;
    }
    
    /**
     * 
     * @throws Exception
     */
    protected void endActions() throws Exception
    {
    }
}
