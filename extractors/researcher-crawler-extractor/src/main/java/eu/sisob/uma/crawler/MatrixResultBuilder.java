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

import eu.sisob.uma.crawler.ResearchersCrawlers.CandidateTypeURL;
import eu.sisob.uma.crawler.ResearchersCrawlers.CrawlerResearchesPagesV3Controller;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.IteratorReseachersFile;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.LocalFormatType;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.XMLTags;
import java.io.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

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
public class MatrixResultBuilder extends IteratorReseachersFile
{        
    
    //Used to build results matrix, build another iterator to build the table (with split too)
    HashMap<String, Integer> universities_axis;
    HashMap<String, Integer> dept_axis;    
    HashMap<String, HashMap<String, Map.Entry<Integer, Integer>>> resultsMatrix;    
    
    /**
     * 
     * @param sourceXmlFile
     * @param downloadPagesDir
     * @param destXmlFile
     */
    public MatrixResultBuilder(org.dom4j.Document sourceXmlDocument, File work_dir, HashMap<String, Integer> universities_axis, HashMap<String, Integer> dept_axis, HashMap<String, HashMap<String, Map.Entry<Integer, Integer>>> resultsMatrix)
    {
         IteratorReseachersFile(sourceXmlDocument, work_dir, LocalFormatType.PLAIN_DIRECTORY);     
         this.universities_axis = universities_axis;
         this.dept_axis = dept_axis;
         this.resultsMatrix = resultsMatrix;         
    }

    /**
     * 
     */
    @Override
    protected void beginActions() 
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
        if(!this.universities_axis.containsKey(sInstitutionName))
            this.universities_axis.put(sInstitutionName, universities_axis.size());
        
        if(!this.resultsMatrix.containsKey(sInstitutionName))
            this.resultsMatrix.put(sInstitutionName, new HashMap<String, Map.Entry<Integer, Integer>>());
        
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
        if(!this.dept_axis.containsKey(sUnitOfAssessment_Description))
            this.dept_axis.put(sUnitOfAssessment_Description, dept_axis.size());
        
        int counterTotal = 0;
        int counterSuccess = 0;
        for ( Iterator<org.dom4j.Element> research_group_it = elementUnitOfAssessment.elementIterator(XMLTags.RESEARCHGROUP); research_group_it.hasNext(); )
        {
            org.dom4j.Element research_group_element = research_group_it.next();

            for ( Iterator<org.dom4j.Element> researcher_it = research_group_element.elementIterator(XMLTags.RESEARCHER); researcher_it.hasNext(); )
            {
                org.dom4j.Element researcher_element = researcher_it.next();
                
                counterTotal++;
                if(researcher_element.elements(XMLTags.RESEARCHER_WEB_ADDRESS).size() > 0)
                    counterSuccess++;
            }
        }
                
        this.resultsMatrix.get(sInstitutionName).put(sUnitOfAssessment_Description, new AbstractMap.SimpleEntry<Integer, Integer>(counterSuccess, counterTotal));                
        
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
    
    public static void writeResultsMatrix(File file, HashMap<String, Integer> universities_axis, HashMap<String, Integer> dept_axis, HashMap<String, HashMap<String, Map.Entry<Integer, Integer>>> resultsMatrix)
    {        
        Map.Entry<Integer, Integer>[][] matrix = (Map.Entry<Integer, Integer>[][])new Map.Entry[universities_axis.size()][dept_axis.size()];
        String[] cols = new String[universities_axis.size()];
        String[] rows = new String[dept_axis.size()];
        
        int total_researchers = 0;
        int success_researchers = 0;
        
        for(String j : resultsMatrix.keySet())
        {                
            cols[universities_axis.get(j)] = j;
            for(String k : resultsMatrix.get(j).keySet())
            {
                rows[dept_axis.get(k)] = k;                
            }
        }
        
        for(String j : resultsMatrix.keySet())
        {        
            for(String k : resultsMatrix.get(j).keySet())
            {                
                Map.Entry<Integer, Integer> r = resultsMatrix.get(j).get(k);

                int x = universities_axis.get(j);
                int y = dept_axis.get(k);

                matrix[x][y] = r;                
                    
                success_researchers += r.getKey();
                total_researchers += r.getValue();
            }            
        }
        
        try {
            FileUtils.write(file, "RESULTS TABLE\r\n", "UTF-8", false);
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error("Error writing results matrix");
        }        
        
        String s = "#\t";
        for(int i = 0; i < rows.length; i++)
        {   
            s += rows[i] + "\t";
        }
        s += "\r\n";            
        for(int i = 0; i < matrix.length; i++)
        {   
            s += cols[i] + "\t";
            for(int j = 0; j < matrix[i].length; j++)
            {
                if(matrix[i][j] != null)
                    s += matrix[i][j].getKey() + "/" + matrix[i][j].getValue() + "\t";
                else 
                    s += "\t";
            }                
            s += "\r\n";            
            if(s.length() > 1000)
            {
                try {
                FileUtils.write(file, s, "UTF-8", true);
                } catch (IOException ex) {
                    ProjectLogger.LOGGER.error("Error writing matrix results");
                }
                s = "";
            }            
        }
        
        if(!s.equals(""))
        {
            try {
            FileUtils.write(file, s, "UTF-8", true);
            } catch (IOException ex) {
                ProjectLogger.LOGGER.error("Error writing matrix results");
            }
        }
        
        s = "\r\nTOTAL RESEARCHER = " + total_researchers + " - RESEARCHERS WITH RESULTS: " + success_researchers;          
        try {
            FileUtils.write(file, s, "UTF-8", true);
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error("Error writing matrix results");
        }
    }

    public static void writeResultsList(File file, HashMap<String, Integer> universities_axis, HashMap<String, Integer> dept_axis, HashMap<String, HashMap<String, Map.Entry<Integer, Integer>>> resultsMatrix)
    {        
        Map.Entry<Integer, Integer>[][] matrix = (Map.Entry<Integer, Integer>[][])new Map.Entry[universities_axis.size()][dept_axis.size()];
        String[] cols = new String[universities_axis.size()];
        String[] rows = new String[dept_axis.size()];
        
        int total_researchers = 0;
        int success_researchers = 0;
        
        for(String j : resultsMatrix.keySet())
        {                
            cols[universities_axis.get(j)] = j;
            for(String k : resultsMatrix.get(j).keySet())
            {
                rows[dept_axis.get(k)] = k;                
            }
        }
        
        for(String j : resultsMatrix.keySet())
        {        
            for(String k : resultsMatrix.get(j).keySet())
            {                
                Map.Entry<Integer, Integer> r = resultsMatrix.get(j).get(k);

                int x = universities_axis.get(j);
                int y = dept_axis.get(k);

                matrix[x][y] = r;                
                    
                success_researchers += r.getKey();
                total_researchers += r.getValue();
            }            
        }
        
        String s = "\"UNIVERSITY\"\t\"SUBJECT\"\t\"RESEARCHERS FOUND\"\t\"TOTAL RESEARCHERS\"";        
        s += "\r\n";            
        
        try {
            FileUtils.write(file, s, "UTF-8", false);
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error("Error writing results list");
        }        
        
        s = "";
        
        for(int i = 0; i < matrix.length; i++)
        {   
            for(int j = 0; j < matrix[i].length; j++)
            {
                if(matrix[i][j] != null)
                {
                    s += "\"" + cols[i] + "\"\t\"" + rows[j] + "\"\t" + matrix[i][j].getKey() + "\t" + matrix[i][j].getValue();
                    s += "\r\n";            
                }
            }                
            
            if(s.length() > 1000)
            {
                try {
                FileUtils.write(file, s, "UTF-8", true);
                } catch (IOException ex) {
                    ProjectLogger.LOGGER.error("Error writing matrix results");
                }
                s = "";
            }            
        }
        
        s += "\r\nRESEARCHERS FOUND\t" + success_researchers;          
        s += "\r\nNUMBER OF RESEARCHERS\t" + total_researchers;          
        
        if(!s.equals(""))
        {
            try {
            FileUtils.write(file, s, "UTF-8", true);
            } catch (IOException ex) {
                ProjectLogger.LOGGER.error("Error writing matrix results");
            }
        }
    }
}
