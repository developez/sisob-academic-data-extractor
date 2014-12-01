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

package eu.sisob.uma.NPL.Researchers.Data;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import eu.sisob.uma.NPL.Researchers.ProjectLogger;
import eu.sisob.uma.api.prototypetextmining.globals.CVItemExtracted;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ViewCreator_CSVandSheets {
    
    final static char csv_separator = ',';
    final static String agent_identification_file = "AgentIdentification.csv";
    final static String professional_activity_file = "ProfessionalActivity.csv";
    final static String accredited_university_studies_file = "AccreditedUniversityStudies.csv";
    final static String spreadsheet_w_all_data_file = "_DataExtracted.ods";    
        
    /*
     * Auxiliar class to keep the csv data.
     */
    private static class ViewsExporterUnit
    {
        public ViewsExporterUnit(String tag, String filepath)
        {
            this.cache = new ArrayList<String[]>();
            this.map = new LinkedHashMap <String, String>();        
            this.writer = null;
            this.tag = tag;
            this.filepath = filepath;
            this.lines = 0;            
        }
        
        public void createMapIndex()
        {
            map_index = new LinkedHashMap <String, Integer>();    
            int i = 0;
            for(String key : this.map.keySet())
            {
                map_index.put(key, i+1);
                i++;
            }
        }
        
        public void createNewCsv() throws IOException                
        {
            OutputStreamWriter fw1 = new OutputStreamWriter(new FileOutputStream(filepath),"UTF-8");
            writer = new CSVWriter(fw1, csv_separator); 
            String line[] = new String[map_index.size()+1];
            
            line[0] = "ID";
            for(String key : this.map.keySet())
            {
                line[map_index.get(key)] = map.get(key);            
            }
            writer.writeNext(line);            
            this.lines += 1;
        }
        
        public void AddCsv(String[] line)
        {
            writer.writeNext(line);
            this.lines += 1;
        }
        
        public void UpdateCsv(String id, String[] new_line)
        {
            String[] line_searched = null;
            for(String[] line : cache)
            {
                if(line[0].equals(id))   
                {
                    line_searched = line;
                    break;
                }                    
            }            
            
            if(line_searched != null)                
            {
                for(int i = 0; i < line_searched.length; i++)
                {
                    if(line_searched[i] == null || line_searched[i].equals(""))
                        line_searched[i] = new_line[i];
                }
            }
            else
            {
                cache.add(new_line);                
            }
        }        
        
        public String[] createNewLine(String id, LinkedHashMap <String, String> values)
        {
            String[] line = new String[map_index.size()+1];
            line[0] = id;
            for(String value_name : values.keySet())
            {
                if(map_index.containsKey(value_name))                    
                    line[map_index.get(value_name)] = values.get(value_name);
            }
            return line;
        }
        
        public void closeCsv() throws IOException
        {
            for(String[] line : cache)
            {
                writer.writeNext(line);
                this.lines += 1;
            }
            writer.close();
        }
        
        List<String[]> cache;
        LinkedHashMap <String, String> map;        
        LinkedHashMap <String, Integer> map_index;        
        
        CSVWriter writer;        
        String filepath;
        String tag;
        int lines;
    }
            
    /*
     * Create a set of CSV of researcher cv data, also can create a spreadsheet file with jopendocument in the destination folder given
     * @param document xml document with data extracted with gate (researcher cv data)
     */
    /**
     *
     * @param document
     * @param dest
     * @param create_spreadsheet
     * @param debug_mode
     */
    public static void createViewFilesFromDataExtracted(org.dom4j.Document document, File dest, boolean create_spreadsheet, boolean debug_mode)
    {
        List<ViewsExporterUnit> units = new ArrayList<ViewsExporterUnit>();        
        ViewsExporterUnit new_unit = null;
        //new_unit = new ViewsExporterUnit("AgentIdentification", dest.getAbsolutePath() + File.separator + agent_identification_file);
        new_unit = new ViewsExporterUnit(CVItemExtracted.AgentIdentification.class.getSimpleName(), dest.getAbsolutePath() + File.separator + agent_identification_file);
        
        //Put from XML. Remove "get"s and think that richer data machine will put new fields to xml                
        new_unit.map.put(CVItemExtracted.AgentIdentification.FirstFamilyName, "First Last Name"); //  "FirstFamilyName"
        new_unit.map.put(CVItemExtracted.AgentIdentification.SecondFamilyName, "Second Last Name");
        new_unit.map.put(CVItemExtracted.AgentIdentification.GivenName, "First Name");
        new_unit.map.put(CVItemExtracted.AgentIdentification.Gender, "Gender");
        new_unit.map.put(CVItemExtracted.AgentIdentification.Nationality, "Nationality");
        new_unit.map.put(CVItemExtracted.AgentIdentification.BirthCity, "Birth City");
        new_unit.map.put(CVItemExtracted.AgentIdentification.BirthRegion, "Birth Region");
        new_unit.map.put(CVItemExtracted.AgentIdentification.BirthCountry, "Birth Country");
        new_unit.map.put(CVItemExtracted.AgentIdentification.BirthDateDayMonthYear, "Birthday Day");
        new_unit.map.put(CVItemExtracted.AgentIdentification.BirthDateMonthYear, "Birthday Month");
        new_unit.map.put(CVItemExtracted.AgentIdentification.BirthDateYear, "Birthday Year");
        new_unit.map.put(CVItemExtracted.AgentIdentification.Email, "Email");
        new_unit.map.put(CVItemExtracted.AgentIdentification.Phone, "Phone");
        new_unit.createMapIndex();
        try {
            new_unit.createNewCsv();
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());
            return;
        }
        
        units.add(new_unit);
        //new_unit = new ViewsExporterUnit("ProfessionalActivity", dest.getAbsolutePath() + File.separator + professional_activity_file);        
        new_unit = new ViewsExporterUnit(CVItemExtracted.ProfessionalActivity.class.getSimpleName(), dest.getAbsolutePath() + File.separator + professional_activity_file);
        
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.Title_name, "Literal Position Name");        
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.Position, "Position Name"); //MISS IN GATE
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.PositionNumber, "Position Number");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.DateInit_dayMonthYear, "Start Date Day");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.DateInit_monthYear, "Start Date Month");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.DateInit_year, "Start Date Year");        
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.DateInit_duration, "Duration");        
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.Entity1_entityName, "Entity 1");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.Entity2_entityName, "Entity 2");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.Entity3_entityName, "Entity 3");
        //NEW
        //new_unit.map.put(CVItemExtracted.ProfessionalActivity., "Complete Instituion (e1+e2+e3)");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_city, "City");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_regionName, "Region");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_regionCode, "Region Code");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_countryName, "Country");
        new_unit.map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_countryCode, "Country Code");
        new_unit.createMapIndex();
        try {
            new_unit.createNewCsv();
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());
            return;
        }
        
        units.add(new_unit);
        //new_unit = new ViewsExporterUnit("AccreditedUniversityStudies", dest.getAbsolutePath() + File.separator + accredited_university_studies_file);
        new_unit = new ViewsExporterUnit(CVItemExtracted.AccreditedUniversityStudies.class.getSimpleName(), dest.getAbsolutePath() + File.separator + accredited_university_studies_file);
        
        //NEW        
        //new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies, "Type Degree");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.Title_name, "Literal Study Name");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.Position, "Study Name");
        //NEW
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.Hons, "hons"); 
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.DateTitle_dayMonthYear, "Acchievement Date Day");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.DateTitle_MonthYear, "Acchievement Date Month");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.DateTitle_year, "Acchievement Date Year");                
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.Entity1_entityName, "Entity 1");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.Entity2_entityName, "Entity 2");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.Entity3_entityName, "Entity 3");
        //NEW
        //new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies., "Complete Institution (e1+e2+e3)");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_city, "City");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_regionName, "Region");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_regionCode, "Region Code");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_countryName, "Country");
        new_unit.map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_countryCode, "Country Code");
        new_unit.createMapIndex();        
        try {
            new_unit.createNewCsv();
        } catch (IOException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());
            return;
        }
                
        units.add(new_unit);        

        org.dom4j.Element root = document.getRootElement();
        boolean bLock = false;
        int count = 0;        

        bLock = false;             
        for ( Iterator i = root.elementIterator("blockinfo"); i.hasNext(); )
        {             
            org.dom4j.Element elInfoBlock = (org.dom4j.Element) i.next();                 
//            <blockinfo id_annotationrecollecting="default" id_entity="3626" URL="file:/...">
//                <ProfessionalActivityNoCurrent>
//                <Content>Research Fellow, University of Leicester (1984</Content>
//                <DateInit_year>1984</DateInit_year>
//                <Pattern>ProfessionalActivityPattern1</Pattern>
//                <Entity_entityName>University of Leicester</Entity_entityName>
//                <Title_name>Research Fellow</Title_name>
//                </ProfessionalActivityNoCurrent>
//                ...
//            </blockinfo>
            String id_entity = elInfoBlock.attributeValue(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT);            
                            
            int countElements = 0;
            for(Object oCVNItem : elInfoBlock.elements())
            {
                countElements++;            
                org.dom4j.Element elCVNItem = (org.dom4j.Element)oCVNItem;
                String className = elCVNItem.getName();                    

                ViewsExporterUnit unit_ref = null;

                for(ViewsExporterUnit unit : units)
                {                        
                    if(className.startsWith(unit.tag))
                    {
                        unit_ref = unit;
                        break;
                    }   
                }

                if(unit_ref == null)
                {
                    ProjectLogger.LOGGER.info(className + " has not views exporter unit");
                }
                else
                {                   
                    LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
                    for(Object oCVNItemField : elCVNItem.elements())
                    {
                        org.dom4j.Element elCVNItemField = (org.dom4j.Element)oCVNItemField;
                        String methodName = elCVNItemField.getName();
                        String value = "";
                        String value_name = "";

                        if(methodName.equals("Pattern"))
                        {
                            if(debug_mode)
                                value = elCVNItemField.getText();   
                        }
                        else if(methodName.equals("Content"))
                        {
                            if(debug_mode)
                                value = elCVNItemField.getText();                                           
                        }
                        else if((methodName.equals("Domain")))
                        {
                            if(debug_mode)
                                value = elCVNItemField.getText();                                           
                        }
                        else
                        {
                            value_name = elCVNItemField.getName();                                               
                            value = elCVNItemField.getText();            
                            values.put(value_name, value);
                        }
                    }

                    if(elCVNItem.attributeValue("action_mode").equals("") || elCVNItem.attributeValue("action_mode").equals("add"))
                    {                            
                        try
                        {   
                            String[] line = unit_ref.createNewLine(id_entity, values);
                            unit_ref.AddCsv(line);
                        }
                        catch (Exception ex)
                        {
                            ProjectLogger.LOGGER.error(ex.getMessage());
                        }
                    }
                    else if(elCVNItem.attributeValue("action_mode").equals("overwrite"))
                    {
                        try
                        {   
                            String[] line = unit_ref.createNewLine(id_entity, values);
                            unit_ref.UpdateCsv(id_entity, line);
                        }
                        catch (Exception ex)
                        {
                            ProjectLogger.LOGGER.error(ex.getMessage());
                        }
                    }
                }
            }          
        }
        
        int max_r = 0;
        int max_c = 0;        
        
        for(ViewsExporterUnit unit : units)
        {                 
            if(max_c < unit.map_index.size()+1)            
                max_c = unit.map_index.size()+1;
            
            if(max_r < unit.lines)            
                max_r = unit.lines;
            
            try {
                unit.closeCsv();
            } catch (IOException ex) {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }
        }
        
        if(create_spreadsheet)
        {        
            //Create open document spread sheet
            TableModel model = new DefaultTableModel(max_r, max_c);          

            // Save the data to an ODS file and open it.        
            final File file = new File(dest.getAbsolutePath() + File.separator + spreadsheet_w_all_data_file);

            try 
            {            
                SpreadSheet.createEmpty(model).saveAs(file);

                SpreadSheet spread_sheet = SpreadSheet.createFromFile(file);                                    

                int i = 0;
                for(ViewsExporterUnit unit : units)
                { 
                    Sheet sheet = null;
                    if(i > 0) 
                    {
                        sheet = spread_sheet.addSheet(i, unit.tag);
                    }
                    else
                    {
                        sheet = spread_sheet.getSheet(i);
                        sheet.setName(unit.tag);
                    }

                    sheet.setRowCount(unit.lines);
                    sheet.setColumnCount(unit.map_index.size()+1);

                    InputStreamReader fw1 = new InputStreamReader(new FileInputStream(unit.filepath),"UTF-8");
                    CSVReader reader = new CSVReader(fw1, csv_separator); 

                    String[] line = null;
                    int r = 0;

                    while((line = reader.readNext()) != null)
                    {                       
                        for(int c = 0; c < line.length; c++)                    
                        {                        
                            sheet.setValueAt(line[c], c, r);                        
                        }                    
                        r++;
                        //System.out.println(r);
                    }                
                    i++;
                }


                spread_sheet.saveAs(file);      
                

            } 
            catch (FileNotFoundException ex) 
            {
                ProjectLogger.LOGGER.error(ex.getMessage());
            } 
            catch (IOException ex) 
            {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }
            catch (Exception ex) 
            {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }
            finally{
                
            }
        }
        
    }
           
}
