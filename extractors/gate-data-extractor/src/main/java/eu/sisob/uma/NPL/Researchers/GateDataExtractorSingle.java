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

import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.dom4j.DocumentException;
import gate.Gate;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.log4j.*;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import gate.Document;
import java.io.FileReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map.Entry;
import au.com.bytecode.opencsv.CSVReader;
import eu.sisob.uma.api.prototypetextmining.DataInputRepository;
import eu.sisob.uma.api.prototypetextmining.MiddleData;
import eu.sisob.uma.api.prototypetextmining.RepositoryPreprocessDataMiddleData;
import eu.sisob.uma.api.prototypetextmining.TextMiningParserMonitor;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.FileFormatConversor;
import java.net.URL;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 * Data extraction tasks (prototype). See: DataExtractionPrototype_1.pdf.
 * INCLUDE: PROCESS STEPS 4
 */
public class GateDataExtractorSingle 
{ 
    /**
     *
     * @param path
     * @throws GateException
     * @throws MalformedURLException
     */
    public static void GateUp(String path) throws GateException, MalformedURLException
    {
         ProjectLogger.LOGGER.info( "Initialising GATE ...");         
         File home_path = new File(path);
         Gate.setGateHome(home_path);
         File plugins_path = new File(home_path + "//plugins");
         Gate.setPluginsHome(plugins_path);
         Gate.init();             
         File gateHome = new File(Gate.getGateHome().getAbsolutePath()/* + "\\resources\\GATE-6.0"*/);
         File pluginsHome = new File(gateHome, "plugins");
         Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURL());
         Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Tools").toURL());
         ProjectLogger.LOGGER.info( "Done!");
    }
    
    /**
     * PROCESS STEPS 4
     * The Data Extractor uses GATE (Cunningham et al., 2011) for processing and annotating the
     * provided data, in order to extract useful information about the researchers.
     * Inputs:
     *  - Data in the form of blocks of information useful or interesting for extraction
     *    obtained from the third module.
     *  Format XML:
     *  <root>
     *  <infoblock id=researcherid type=I_INDEX_DATA_TYPE>content or URL<7infoblock>
     * Outputs:
     *  - Processed and annotated useful data stored in a repository. See TextMiningParserGateResearcher.iniAnnotatorCollectors
     * @param infoblocksXmlFile 
     * @param verbose 
     * @param verbose_dir 
     * @return RepositoryCVN filled with extracted data
     * @throws DocumentException  
     */
    public static RepositoryPreprocessDataMiddleData createPreprocessRepositoryFromXml(File infoblocksXmlFile, boolean verbose, File verbose_dir) throws DocumentException
    {
         RepositoryPreprocessDataMiddleData preprocessedRep = new RepositoryPreprocessDataMiddleData();             

         org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
         org.dom4j.Document document = reader.read(infoblocksXmlFile); //("ResearcherPagesMonkeyTask.xml");

         org.dom4j.Element root = document.getRootElement();
         boolean bLock = false;
         int N_MAX = 100, count = 0;
         Random randomGenerator = new Random();             

         bLock = false;     
         for ( Iterator i = root.elementIterator("infoblock" ); i.hasNext(); )
         {
            org.dom4j.Element ib = (org.dom4j.Element) i.next();           

            MiddleData aoPreProcessData = new MiddleData(ib.attributeValue(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT),
                                                         ib.attributeValue(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_TEXTMININGPARSER_ATT),
                                                         ib.attributeValue(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ANNOTATIONRECOLLECTING),
                                                         ib.getText(),
                                                         null,
                                                         verbose,
                                                         verbose_dir);                                      
            {
               //if(N_MAX > count)
               //if(ib.getText().contains("2f21a5ff"))
               {

                   preprocessedRep.addData(aoPreProcessData);
                   bLock = true;
                   count++;
               }                    
            }
         }             

         ProjectLogger.LOGGER.info( count + " documents added");
         return preprocessedRep;
    }    
    
    /**
     *
     * @param reader
     * @param separator
     * @param data_dir
     * @param verbose
     * @param verbose_dir
     * @param split_by_keyword
     * @param blocks_and_keywords
     * @return
     * @throws IOException
     */
    public static RepositoryPreprocessDataMiddleData createPreprocessRepositoryFromCSV(CSVReader reader, char separator, File data_dir, boolean verbose, File verbose_dir, boolean split_by_keyword, HashMap<String, String[]> blocks_and_keywords, File dest_dir) throws IOException    
    {   
        RepositoryPreprocessDataMiddleData preprocessedRep = new RepositoryPreprocessDataMiddleData();             
        
        String [] nextLine;
        
        int idStaffIdentifier= -1;int idName= -1;int idFirstName= -1;int idLastName= -1;int idInitials= -1;int idUnitOfAssessment_Description= -1;
        int idInstitutionName= -1;int idWebAddress= -1;int idResearchGroupDescription= -1;int idResearcherWebAddress = -1;int idResearcherWebAddressType = -1;int idResearcherWebAddressExt = -1;
        if ((nextLine = reader.readNext()) != null)
        {
            for(int i = 0; i < nextLine.length; i++)            
            {
                String column_name = nextLine[i];
                if(column_name.equals(FileFormatConversor.CSV_COL_ID))
                        idStaffIdentifier = i;
                else if(column_name.equals(FileFormatConversor.CSV_COL_NAME))
                        idName = i;
                else if(column_name.equals(FileFormatConversor.CSV_COL_FIRSTNAME))
                        idFirstName = i;
                else if(column_name.equals(FileFormatConversor.CSV_COL_LASTNAME))
                        idLastName = i;
                else if(column_name.equals(FileFormatConversor.CSV_COL_INITIALS))
                        idInitials = i;
                else if(column_name.equals(FileFormatConversor.CSV_COL_SUBJECT))
                        idUnitOfAssessment_Description = i;
                else if(column_name.equals(FileFormatConversor.CSV_COL_INSTITUTION_NAME))
                        idInstitutionName = i;
                else if(column_name.equals(FileFormatConversor.CSV_COL_INSTITUTION_URL))
                        idWebAddress = i;                
                else if(column_name.equals(FileFormatConversor.CSV_COL_RESEARCHER_PAGE_URL))
                        idResearcherWebAddress = i;          
                else if(column_name.equals(FileFormatConversor.CSV_COL_RESEARCHER_PAGE_TYPE))
                        idResearcherWebAddressType = i;          
                else if(column_name.equals(FileFormatConversor.CSV_COL_RESEARCHER_PAGE_EXT))
                        idResearcherWebAddressExt = i;          
            }                
        }                         

        if(idResearcherWebAddress != -1 && idStaffIdentifier != -1 && idLastName != -1 && idInitials != -1)
        {
            Pattern p1 = Pattern.compile("([a-zA-Z0-9#._-]+)+");
            
            int count = 0;
            while ((nextLine = reader.readNext()) != null)
            {
                String file_reference = nextLine[idResearcherWebAddress];
                String researcher_page_url = nextLine[idResearcherWebAddress];
                
                if(p1.matcher(researcher_page_url).matches())
                {                    
                    File f = new File(data_dir, researcher_page_url);                                        
                    if(!f.exists())
                    {                        
                        throw new FileNotFoundException(researcher_page_url  + " not found in the folder.");
                    }                    
                    researcher_page_url = f.toURI().toURL().toString();                                       
                }
                
                String id = nextLine[idStaffIdentifier];
                String lastname = nextLine[idLastName];
                String initials = nextLine[idInitials];

                String name = idFirstName != -1 ? nextLine[idFirstName] : "";
                String firstname = idName != -1 ? nextLine[idName] : "";                
                
                if(split_by_keyword && blocks_and_keywords != null && blocks_and_keywords != null)                
                {
                    DocumentSplitter spliter = new DocumentSplitter(blocks_and_keywords);
                    Document doc = null;
                    boolean document_loaded = false;
                    try 
                    {
                        ProjectLogger.LOGGER.info("Opening " + researcher_page_url);
                        doc = gate.Factory.newDocument(new URL(researcher_page_url));
                        document_loaded = true;
                    }
                    catch (Exception ex) 
                    {
                        ProjectLogger.LOGGER.error("Document not loaded ", ex);
                    }
                    
                    if(document_loaded)
                    {
                        List<Entry<String, String>> blocks = spliter.SplitDocument(doc.getContent().toString());
                        for(Entry<String, String> block : blocks)
                        {
                            //if(block.getKey().equals(CVBlocks.CVBLOCK_PROFESSIONAL_ACTIVITY.toString()) ||
                            //           block.getKey().equals(CVBlocks.CVBLOCK_UNIVERSITY_STUDIES.toString()) ||
                            //          block.getKey().equals(CVBlocks.CVBLOCK_PERSONAL.toString()) ||
                            //           block.getKey().equals(""))
                            {
                                String desc = "";
                                try{
                                    desc = CVBlocks.CVBLOCK_DESCRIPTIONS[Integer.parseInt(block.getKey())];
                                }catch(Exception ex){
                                    desc = CVBlocks.CVBLOCK_DESCRIPTIONS[CVBlocks.CVBLOCK_REST];
                                }
                                
                                String output_filename = file_reference;
                                int index_slash = file_reference.substring(0, file_reference.length()-1).lastIndexOf("/");
                                if(index_slash != -1){
                                    output_filename = file_reference.substring(index_slash);
                                }
                                
                                output_filename = desc + "-" + id + "-" + output_filename.replaceAll("[^A-Za-z0-9]","-");
                                File output_file = new File(dest_dir, output_filename);
                                FileUtils.write(output_file, block.getValue(), "UTF-8", false);
                                String output_fileurl = output_file.toURI().toURL().toString();
                                HashMap<String, String> extra_data = new HashMap<String, String>();
                                extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_LASTNAME, lastname);
                                extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_INITIALS, initials);
                                extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_NAME, name);
                                extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_FIRSTNAME, firstname);
                                extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_DOCUMENT_NAME, desc + "-" + id + "-" + output_filename.replaceAll("[^A-Za-z0-9]","-"));
                                extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_BLOCK_TYPE, block.getKey());
                                
                                MiddleData md = new MiddleData(id,
                                                               DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER,
                                                               DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC,                                                                 
                                                               output_fileurl,//block.getValue(),
                                                               extra_data,
                                                               verbose,
                                                               verbose_dir);                                                                                                                            
                                preprocessedRep.addData(md);    
                            }                            
                        }
                    }                   
                }
                else
                {   
                    //extra_data.put("block_type", "WHOLE_CV");
                    HashMap<String, String> extra_data = new HashMap<String, String>();
                    extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_LASTNAME, lastname);
                    extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_INITIALS, initials);
                    extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_NAME, name);
                    extra_data.put(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_FIRSTNAME, firstname);
                    MiddleData md = new MiddleData(id,
                                                   DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER,
                                                   DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC,                                                                 
                                                   researcher_page_url,
                                                   extra_data,
                                                   verbose,
                                                   verbose_dir);
                    preprocessedRep.addData(md);    
                }               
                
                count++;
            }
            
            ProjectLogger.LOGGER.info( count + " documents added");
        }
        else
        {
            ProjectLogger.LOGGER.info( "Miss some fields in the csv content");
        }        
        
        return preprocessedRep;
    }
    
    /**
     *
     * @param csv_filepath
     * @param separator
     * @param data_dir
     * @param verbose
     * @param verbose_dir
     * @param split_by_keyword
     * @param blocks_and_keywords
     * @return
     * @throws IOException
     */
    public static RepositoryPreprocessDataMiddleData createPreprocessRepositoryFromCSVFile(File csv_filepath, char separator, File data_dir, boolean verbose, File verbose_dir, boolean split_by_keyword, HashMap<String, String[]> blocks_and_keywords, File dest_dir) throws IOException   
    {   
        //CSVReader reader = new CSVReader(new StringReader(csv_content), separator);
        CSVReader reader = new CSVReader(new FileReader(csv_filepath), separator);
        
        return createPreprocessRepositoryFromCSV(reader, separator, data_dir, verbose, verbose_dir, split_by_keyword, blocks_and_keywords, dest_dir);
    }
    
    /**
     *
     * @param csv_filepath
     * @param separator
     * @param data_dir
     * @return
     * @throws IOException
     */
    public static RepositoryPreprocessDataMiddleData createPreprocessRepositoryFromCSVFile(File csv_filepath, char separator, File data_dir) throws IOException    
    {   
        //CSVReader reader = new CSVReader(new StringReader(csv_content), separator);
        CSVReader reader = new CSVReader(new FileReader(csv_filepath), separator);
        
        return createPreprocessRepositoryFromCSV(reader, separator, data_dir, false, null, false, null, null);
    }
    
    /**
     *
     * @param csv_content
     * @param separator
     * @param data_dir
     * @return
     * @throws IOException
     */
    public static RepositoryPreprocessDataMiddleData createPreprocessRepositoryFromCSVContent(String csv_content, char separator, File data_dir) throws IOException    
    {           
        //CSVReader reader = new CSVReader(new StringReader(csv_content), separator);
        CSVReader reader = new CSVReader(new StringReader(csv_content), separator);
        
        return createPreprocessRepositoryFromCSV(reader, separator, data_dir, false, null, false, null, null);
    }    
    
    /**
     *
     * @param preprocessedRep
     * @param processedRep
     * @return
     */
    public static DataInputRepository extractInformation(RepositoryPreprocessDataMiddleData preprocessedRep, DataInputRepository processedRep)
    {       
        try
        {       
             TextMiningParserMonitor monitor = new TextMiningParserMonitor(1, 
                                                                           TextMiningParserGateResearcher.class, 
                                                                           DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER, 
                                                                           processedRep,
                                                                           5,
                                                                           preprocessedRep);                             
             monitor.launchParsers();     
             
             ProjectLogger.LOGGER.info("PROCESS DONE!");            
             
             return processedRep;             
        }
        catch(org.dom4j.DocumentException spe )
        {
            StringBuffer sb = new StringBuffer( spe.toString() );
            sb.append("\nLocMSG: " + spe.getLocalizedMessage());
            sb.append("\nMSG: " + spe.getMessage() );
            System.out.println( sb.toString() );
            return null;
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error( "Error: " + ex.getMessage());
            return null;            
        }
    }  
    
    /*
     * Read txt like:
     * 
     * ID1 ID2
     * ID1 ID2
     * ID1 ID2
     * 
     * make repository like:
     * 
     * 
     * 
     */
    /**
     *
     * @param csv_file
     * @param cvs_directory
     * @param withSplitter
     * @param verbose
     * @param verbose_dir
     * @return
     */
    public static RepositoryPreprocessDataMiddleData createPreprocessRepositoryOfCVsFromTxt(File csv_file, File cvs_directory, boolean withSplitter, boolean verbose, File verbose_dir)            
    {
        RepositoryPreprocessDataMiddleData preprocessedRep = new RepositoryPreprocessDataMiddleData();
         
        String path = csv_file.getParentFile().getAbsolutePath();
        
        HashMap<String, String> idToCvPath = new HashMap<String, String>();
        
        HashMap<String, String> id2Toid1 = new HashMap<String, String>();
        
        if(cvs_directory.exists())
        {
            for(File f : cvs_directory.listFiles())
            {
                if(f.isFile())
                {
                    String name = f.getName().substring(0, f.getName().lastIndexOf("."));
                    try
                    {
                        Integer.parseInt(name);
                        idToCvPath.put(name, f.getAbsolutePath());
                    }
                    catch(Exception ex)
                    {
                        ProjectLogger.LOGGER.error( "Not file: " + ex.getMessage());
                    }
                }
            }            
            

            FileReader fr;
            CSVReader reader;
            try 
            {
                fr = new FileReader(csv_file);
                reader = new CSVReader(fr, '\t');
                String [] nextLine;
                while ((nextLine = reader.readNext()) != null) 
                {
                    String id_researcher_1 = nextLine[0];
                    String id_researcher_2 = nextLine[1];

                    id2Toid1.put(id_researcher_2, id_researcher_1);
                }
            } 
            catch (FileNotFoundException ex) 
            {
                ProjectLogger.LOGGER.error("Error", ex);
            }
            catch (IOException ex) 
            {
                ProjectLogger.LOGGER.error("Error", ex);
            }
            
            
            for(String id2 : idToCvPath.keySet())
            {
                if(id2Toid1.containsKey(id2))
                {
                    File cv_file = new File(idToCvPath.get(id2));

                    boolean document_loaded = false;
                    if(cv_file.exists())
                    {                   
                         if(withSplitter)
                         {
                             DocumentSplitter spliter = new DocumentSplitter(CVBlocks.getCVBlocksAndKeywords(new File("keywords")));
                             Document doc = null;
                             try 
                             {
                                 ProjectLogger.LOGGER.info("Opening " + cv_file.getName());
                                 doc = gate.Factory.newDocument((cv_file).toURI().toURL());          
                                 document_loaded = true;
                             }
                             catch (MalformedURLException ex) 
                             {
                                 ProjectLogger.LOGGER.error("Document not loaded ", ex);
                             }
                             catch (ResourceInstantiationException ex) 
                             {
                                ProjectLogger.LOGGER.error("Document not loaded ", ex);
                             }                         
                             catch (Exception ex)
                             {
                                 ProjectLogger.LOGGER.error("Document not loaded ", ex);
                             }
                            
                             if(document_loaded)
                             {
                                 List<Entry<String, String>> blocks = spliter.SplitDocument(doc.getContent().toString());

                                 //System.out.println("||||||||||||||||||||||||||||||||||||||||||||||1");
                                 //System.out.println(cv_file.getName());
                                 //System.out.println("||||||||||||||||||||||||||||||||||||||||||||||1");

                                 for(Entry<String, String> block : blocks)
                                 {
                                    //TESTING                                     
                                    //if(block.getKey().equals(""))                                    
                                    //    System.out.println("GENERAL");
                                    //else
                                    //    System.out.println(CVBlocks.CVBLOCK_DESCRIPTIONS[Integer.parseInt(block.getKey())]);

                                    //System.out.println("----------------------------------------------");
                                    //System.out.println(block.getValue());
                                    //System.out.println("----------------------------------------------");

                                    if(block.getKey().equals(CVBlocks.CVBLOCK_PROFESSIONAL_ACTIVITY.toString()) ||
                                       block.getKey().equals(CVBlocks.CVBLOCK_UNIVERSITY_STUDIES.toString()) ||
                                       block.getKey().equals(CVBlocks.CVBLOCK_PERSONAL.toString()) ||
                                       block.getKey().equals(""))
                                    {
                                        MiddleData aoPreProcessData = new MiddleData(id2Toid1.get(id2),
                                                                                     DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER,
                                                                                     DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC,
                                                                                     block.getValue(),
                                                                                     null,
                                                                                     verbose,verbose_dir);
                                                                                     //cv_file.getAbsolutePath());                                                      
                                        preprocessedRep.addData(aoPreProcessData);
                                    }
                                 }
                             }
                        }
                        else
                        {
                            Document doc = null;
                             try 
                             {
                                 ProjectLogger.LOGGER.info("Opening " + cv_file.getName());
                                 doc = gate.Factory.newDocument((cv_file).toURI().toURL());      
                                 doc.setName(cv_file.getName());
                                 document_loaded = true;
                             }
                             catch (MalformedURLException ex) 
                             {
                                 ProjectLogger.LOGGER.error("Document not loaded ", ex);
                             }
                             catch (ResourceInstantiationException ex) 
                             {
                                ProjectLogger.LOGGER.error("Document not loaded ", ex);
                             }                         
                             catch (Exception ex)
                             {
                                 ProjectLogger.LOGGER.error("Document not loaded ", ex);
                             }
                            
                             if(document_loaded)
                             {
                                 
                                //String content = doc.getContent().toString().replace("\r\n\r\n", "\r\n");
                                       
                                MiddleData aoPreProcessData = new MiddleData(id2Toid1.get(id2),
                                                                         DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER,
                                                                         DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC,                                                                                     
                                                                         doc.getContent().toString(),
                                                                         null,
                                                                         verbose,verbose_dir);                                                     
                                preprocessedRep.addData(aoPreProcessData);
                             }
                        }
                    }
                }
            }    
        }
        
        return preprocessedRep;
    } 
}
