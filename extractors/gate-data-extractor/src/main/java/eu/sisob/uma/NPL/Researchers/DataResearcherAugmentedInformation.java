
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

package eu.sisob.uma.NPL.Researchers;

import eu.sisob.uma.NPL.Researchers.Data.TraductionTablesOperations;
import eu.sisob.uma.NPL.Researchers.Freebase.LocationDataResolver;
import eu.sisob.uma.api.h2dbpool.H2DBPool;
import eu.sisob.uma.api.prototypetextmining.globals.CVItemExtracted;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 * Class with heuristic to able to extract more information from information extracted.
 *      For example:
 *          - Extract 
 * 
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class DataResearcherAugmentedInformation 
{
    /*
     * Enrich researchers data information with other modules
     *  - Location of universities and organizations
     * 
     * @param doc
     * @param resolver 
     */
    /**
     *
     * @param doc
     * @param resolver
     */
    public static void resolveLocationOfEntities(org.dom4j.Document doc, LocationDataResolver resolver)
    {       
        boolean verbose = resolver.verbose;
        org.dom4j.Element root = doc.getRootElement();        

        for ( Iterator i = root.elementIterator("blockinfo"); i.hasNext(); )
        {
             org.dom4j.Element ib = (org.dom4j.Element) i.next();          

             // Professional activities
             List<org.dom4j.Element> profs = new ArrayList<org.dom4j.Element>();
             for(Object obj : ib.elements())
             {   
                 org.dom4j.Element prof = (org.dom4j.Element) obj;         
                 if(prof.getName().startsWith(CVItemExtracted.ProfessionalActivity.class.getSimpleName()))
                    profs.add(prof);
             }
             
             for(org.dom4j.Element prof : profs)
             {                 
                 String entity_name = "";           
                 String element_name = "";

                 /* */

                 /*
                  * Trying to extract more information about the organization detected, like the location for example
                  * 
                  * Location searchs: 
                  *     Normally, Entity3_entityName contains Entity2_entityName and so on, so the heurístic will try
                  *     to resolve the date first for the 3, next for the 2, and next for the 1.
                  * 
                  *     Once time the location will searched, the algoritm will take the first occurrence of each entity (cities, regions, countries).
                  *     But after, the algoritm will eliminate regions with the same name in cities, and regions with the same name in countries.
                  */
                 org.dom4j.Element ent_name_3 = prof.element(CVItemExtracted.ProfessionalActivity.Entity3_entityName);
                 org.dom4j.Element ent_type_3 = prof.element(CVItemExtracted.ProfessionalActivity.Entity3_type);

                 if(ent_name_3 != null && ent_type_3 != null)
                 {
                     if(ent_type_3.getText().equals(eu.sisob.uma.api.prototypetextmining.gatedataextractor.Literals.EntityType_University))
                     {  
                         //"University of Massachusetts"
                         entity_name = ent_name_3.getText();       
                         element_name = ent_name_3.getName();
                     }
                 }
                 else
                 {
                     org.dom4j.Element ent_name_2 = prof.element(CVItemExtracted.ProfessionalActivity.Entity2_entityName);
                     org.dom4j.Element ent_type_2 = prof.element(CVItemExtracted.ProfessionalActivity.Entity2_type);

                     if(ent_name_2 != null && ent_type_2 != null)
                     {
                         if(ent_type_2.getText().equals(eu.sisob.uma.api.prototypetextmining.gatedataextractor.Literals.EntityType_University))
                         {
                            entity_name = ent_name_2.getText();
                            element_name = ent_name_2.getName();
                         }
                     }
                     else
                     {
                         org.dom4j.Element ent_name_1 = prof.element(CVItemExtracted.ProfessionalActivity.Entity1_entityName);
                         org.dom4j.Element ent_type_1 = prof.element(CVItemExtracted.ProfessionalActivity.Entity1_type);

                         if(ent_name_1 != null && ent_type_1 != null)
                         {
                             if(ent_type_1.getText().equals(eu.sisob.uma.api.prototypetextmining.gatedataextractor.Literals.EntityType_University))
                             {
                                 entity_name = ent_name_1.getText();                                 
                                 element_name = ent_name_1.getName();
                             }
                         }
                     }                         
                 }

                 entity_name = entity_name.replace("  ", " ").trim();

                 if(!entity_name.equals(""))
                 {      
                    ProjectLogger.LOGGER.info("\tTry to resolve => " + entity_name);                         
                    LocationDataResolver.LocationTupleWithEntity location = resolver.resolve(entity_name);                     
                    if(location != null)
                    {
                        ProjectLogger.LOGGER.info("\tLocation solved => " + entity_name + " = " + location);                                                       

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_city, "city");
                        map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_regionName, "region");
                        map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_regionCode, "region_code");
                        map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_countryName, "country");
                        map.put(CVItemExtracted.ProfessionalActivity.PlaceJob_countryCode, "country_code");                        
                        map.put(element_name, "canonic_name");

                        Element place = null;

                        // Update locations and entity name using map object
                        for(String key : map.keySet())       
                        {                            
                            String value = location.getByName(map.get(key));

                            place = prof.element(key);
                            if(place == null) {
                                prof.addElement(key).setText(value);
                            } else {
                            ProjectLogger.LOGGER.info("\tChange '" + key + "' with '" + place.getText() + "' by " + value);   
                            place.setText(value);
                            }
                        }
                    } 
                 }
             }
             
             // Accredited Studies
             List<org.dom4j.Element> studies = new ArrayList<org.dom4j.Element>();
             for(Object obj : ib.elements())
             {   
                 org.dom4j.Element study = (org.dom4j.Element) obj;         
                 if(study.getName().startsWith(CVItemExtracted.AccreditedUniversityStudies.class.getSimpleName()))
                    studies.add(study);
             }
             
             for(org.dom4j.Element study : studies)
             {                 
                 String entity_name = "";           
                 String element_name = "";

                 /* */

                 /*
                  * Trying to extract more information about the organization detected, like the location for example
                  * 
                  * Location searchs: 
                  *     Normally, Entity3_entityName contains Entity2_entityName and so on, so the heurístic will try
                  *     to resolve the date first for the 3, next for the 2, and next for the 1.
                  * 
                  *     Once time the location will searched, the algoritm will take the first occurrence of each entity (cities, regions, countries).
                  *     But after, the algoritm will eliminate regions with the same name in cities, and regions with the same name in countries.
                  */
                 org.dom4j.Element ent_name_3 = study.element(CVItemExtracted.AccreditedUniversityStudies.Entity3_entityName);
                 org.dom4j.Element ent_type_3 = study.element(CVItemExtracted.AccreditedUniversityStudies.Entity3_type);

                 if(ent_name_3 != null && ent_type_3 != null)
                 {
                     if(ent_type_3.getText().equals(eu.sisob.uma.api.prototypetextmining.gatedataextractor.Literals.EntityType_University))
                     {  
                         //"University of Massachusetts"
                         entity_name = ent_name_3.getText();       
                         element_name = ent_name_3.getName();
                     }
                 }
                 else
                 {
                     org.dom4j.Element ent_name_2 = study.element(CVItemExtracted.AccreditedUniversityStudies.Entity2_entityName);
                     org.dom4j.Element ent_type_2 = study.element(CVItemExtracted.AccreditedUniversityStudies.Entity2_type);

                     if(ent_name_2 != null && ent_type_2 != null)
                     {
                         if(ent_type_2.getText().equals(eu.sisob.uma.api.prototypetextmining.gatedataextractor.Literals.EntityType_University))
                         {
                            entity_name = ent_name_2.getText();
                            element_name = ent_name_2.getName();
                         }
                     }
                     else
                     {
                         org.dom4j.Element ent_name_1 = study.element(CVItemExtracted.AccreditedUniversityStudies.Entity1_entityName);
                         org.dom4j.Element ent_type_1 = study.element(CVItemExtracted.AccreditedUniversityStudies.Entity1_type);

                         if(ent_name_1 != null && ent_type_1 != null)
                         {
                             if(ent_type_1.getText().equals(eu.sisob.uma.api.prototypetextmining.gatedataextractor.Literals.EntityType_University))
                             {
                                 entity_name = ent_name_1.getText();                                 
                                 element_name = ent_name_1.getName();
                             }
                         }
                     }                         
                 }

                 entity_name = entity_name.replace("  ", " ").trim();

                 if(!entity_name.equals(""))
                 {      
                    ProjectLogger.LOGGER.info("\tTry to resolve => " + entity_name);                         
                    LocationDataResolver.LocationTupleWithEntity location = resolver.resolve(entity_name);                     
                    if(location != null)
                    {
                        ProjectLogger.LOGGER.info("\tLocation solved => " + entity_name + " = " + location);                                                       

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_city, "city");
                        map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_regionName, "region");
                        map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_regionCode, "region_code");
                        map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_countryName, "country");
                        map.put(CVItemExtracted.AccreditedUniversityStudies.PlaceTitle_countryCode, "country_code");                                                
                        map.put(element_name, "canonic_name");

                        Element place = null;

                        // Update locations and entity name using map object
                        for(String key : map.keySet())       
                        {                            
                            String value = location.getByName(map.get(key));

                            place = study.element(key);
                            if(place == null) {
                                study.addElement(key).setText(value);
                            } else {
                            ProjectLogger.LOGGER.info("\tChange '" + key + "' with '" + place.getText() + "' by " + value);   
                            place.setText(value);
                            }
                        }
                    } 
                 }
             }             
             
        }
    }    
    
    
    /*
     * Enrich researchers data information with other modules
     *  - From professional activities and university studies get the standar academic position
     *    F.E:  Lect. of Chemistry => Lecturer, 4
     * 
     * @param doc
     * @param resolver 
     */
    /**
     *
     * @param doc
     * @param dbpool_academic_trad_tables
     */
    public static void resolveAcademicPosistion(org.dom4j.Document doc, H2DBPool dbpool_academic_trad_tables)
    {   
        org.dom4j.Element root = doc.getRootElement();              
        
        Connection cnn = null;
        
        try {
            cnn = dbpool_academic_trad_tables.getConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getRootLogger().error(ex.toString());
            cnn = null;
            return;
        } catch (SQLException ex) {
            Logger.getRootLogger().error(ex.toString());
            cnn = null;
            return;
        }

        for ( Iterator i = root.elementIterator("blockinfo"); i.hasNext(); )
        {
             org.dom4j.Element ib = (org.dom4j.Element) i.next();          

             // Professional activities
             List<org.dom4j.Element> profs = new ArrayList<org.dom4j.Element>();
             for(Object obj : ib.elements())
             {   
                 org.dom4j.Element prof = (org.dom4j.Element) obj;         
                 if(prof.getName().startsWith(CVItemExtracted.ProfessionalActivity.class.getSimpleName()))
                    profs.add(prof);
             }
             
             for(org.dom4j.Element prof : profs)
             {                 
                 String title_name = "";                            

                 /* */

                 /*
                  * Try to get the standar cademic position of prof acti
                  */
                 org.dom4j.Element title_name_element = prof.element(CVItemExtracted.ProfessionalActivity.Title_name);                 

                 if(title_name_element != null)
                 {
                    title_name = title_name_element.getText();       
                 }                 

                 while(title_name.contains("  "))
                    title_name = title_name.replace("  ", " ").trim();

                 if(!title_name.equals(""))
                 {      
                    ProjectLogger.LOGGER.info("\tTry to resolve => " + title_name);
                    
                    Integer id_type = TraductionTablesOperations.getTypeListFromTraductionTable(cnn, title_name, TraductionTablesOperations.TRAD_TABLE_PROF_ACTIVITIES, "cvn_trad_", "id_");                    
                                        
                    if(id_type != null)
                    {
                        String standard_type = TraductionTablesOperations.getProfActivityStandardName(cnn, id_type);                   
                        ProjectLogger.LOGGER.info("\tResolve => " + title_name + " => " + standard_type);
                        String key = CVItemExtracted.ProfessionalActivity.Position;
                        String value = standard_type;
                        Element position = prof.element(key);
                        if(position == null) {
                            prof.addElement(key).setText(standard_type);
                        } else {                                
                            position.setText(standard_type);
                            ProjectLogger.LOGGER.info("\tChange '" + key + "' with '" + position.getText() + "' by " + value);   
                        }                       
                    }
                 }
             }
             
            /*
             * Try to get the standard cademic position of univ study
             */
             List<org.dom4j.Element> studies = new ArrayList<org.dom4j.Element>();
             for(Object obj : ib.elements())
             {   
                 org.dom4j.Element prof = (org.dom4j.Element) obj;         
                 if(prof.getName().startsWith(CVItemExtracted.AccreditedUniversityStudies.class.getSimpleName()))
                    profs.add(prof);
             }
             
             for(org.dom4j.Element study : studies)
             {                 
                 String title_name = "";                            

                 /* */

                 /*
                  * 
                  */
                 org.dom4j.Element title_name_element = study.element(CVItemExtracted.AccreditedUniversityStudies.Title_name);                 

                 if(title_name_element != null)
                 {
                    title_name = title_name_element.getText();       
                 }                 

                 while(!title_name.contains("  "))
                    title_name = title_name.replace("  ", " ").trim();

                 if(!title_name.equals(""))
                 {      
                    ProjectLogger.LOGGER.info("\tTry to resolve => " + title_name);
                    
                    Integer id_type = TraductionTablesOperations.getTypeListFromTraductionTable(cnn, title_name, TraductionTablesOperations.TRAD_TABLE_UNIVERSITY_STUDIES, "cvn_trad_", "id_");                    
                    
                    if(id_type != null)
                    {
                        String standard_type = TraductionTablesOperations.getUniversityStudyStandardName(cnn, id_type); 
                        ProjectLogger.LOGGER.info("\tResolve => " + title_name + " => " + standard_type);
                        String key = CVItemExtracted.AccreditedUniversityStudies.Position;
                        String value = standard_type;
                        Element position = study.element(key);
                        if(position == null) {
                            study.addElement(key).setText(standard_type);
                        } else {                                
                            position.setText(standard_type);
                            ProjectLogger.LOGGER.info("\tChange '" + key + "' with '" + position.getText() + "' by " + value);   
                        }                       
                    }
                 }
             }
             
        }
    } 
}
