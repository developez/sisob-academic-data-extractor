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

package eu.sisob.uma.crawler.ResearchersCrawlers.Reporting;

import au.com.bytecode.opencsv.CSVReader;
import eu.sisob.uma.footils.File.FileFootils;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Node;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ReportingUtils 
{
    /*
     * Sacamos las putas webs de los putos IDs, tiran pa un csv
     * 
     */
    public static void extractTextFromCSVID(String xmlfile, String csvfile) throws Exception
    {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        org.dom4j.Document document = reader.read(xmlfile);
        org.dom4j.Element root = document.getRootElement();
        
        CSVReader csvreader = new CSVReader(new FileReader(csvfile), ';');
        
        String [] nextLine;
        while ((nextLine = csvreader.readNext()) != null) 
        {
            int id = Integer.parseInt(nextLine[0]);
            
            //Node node = root.selectSingleNode("//*[contains(text(),'" + id + "')]");            
            List<Node> lstNodes = root.selectNodes("//*[@id_entity=" + id + "]");                        
            
            System.out.println("");            
            System.out.println("ID: " + id);            
                
            for(int i = 0; i < lstNodes.size(); i++)
            {
                
                System.out.println(" 1 ---------------------------------------------");            
                String url = lstNodes.get(i).getText();
                //org.jsoup.nodes.Document doc = Jsoup.parse(new File(url), "UTF-8","");
                //String nohtml = doc.body().text();
                
                if(url.endsWith(".html"))
                {                
                    net.htmlparser.jericho.Source htmlSource = new net.htmlparser.jericho.Source(new FileReader(url));               
                    net.htmlparser.jericho.Segment htmlSeg = new net.htmlparser.jericho.Segment(htmlSource, 0, htmlSource.length());
                    net.htmlparser.jericho.Renderer htmlRend = new net.htmlparser.jericho.Renderer(htmlSeg);
                    System.out.println(htmlRend.toString());                
                }
                else
                {
                    System.out.println(url);  
                }

                //System.out.println(nohtml);
     
                System.out.println("---------------------------------------------");                                                
            }           
                     
            
//            for(Object obj : lst)
//            {
//                
//            }
        }
    }
    
    /*
     * Sacamos la mierda que ha sacado del xml de extractionsWorkGate y lo visualizamos en plan guapo para contear bien     
     * DataResearcherCollect_report0222012_ITA.xml
     */
   
    public static void extractExpressionFromCSVID(String xmlfile, String csvfile, String outfile) throws Exception
    {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        org.dom4j.Document document = reader.read(xmlfile);
        org.dom4j.Element root = document.getRootElement();        
        
        CSVReader csvreader = new CSVReader(new FileReader(csvfile), ';');
        HashMap<String, List<String>> hm = new HashMap<String, List<String>>();
        String [] nextLine;
        while ((nextLine = csvreader.readNext()) != null) 
        {
            hm.put(nextLine[0],  new ArrayList<String>());
        }
        
        for ( Iterator i1 = root.elementIterator("blockinfo"); i1.hasNext();)
        {
            org.dom4j.Element e1 = (org.dom4j.Element) i1.next();            
            
            String id = e1.attribute("id_entity").getValue();            
            //if(!id.equals("786643")) continue;
            List<String> al = hm.get(id);
            if(al == null) continue;
            int PA = 0, AS = 0;
            if(al.size() != 0)
            {
                int last = al.size() - 1;
                String[] ss = al.get(last).split("\t");
                PA = Integer.parseInt(ss[0]);
                AS = Integer.parseInt(ss[1]);
                al.remove(last);                
            }
            
            for(Object obj : e1.elements())
            {
                org.dom4j.Element exp = (org.dom4j.Element) obj;                
                
                String aux_content = exp.element("Content").getText().replace("\n"," ").replace("\r\n"," ");
                if(!al.contains(exp.getName() + "\t\t" + aux_content + "\r\n"))
                {
                    if(exp.getName().contains("Activity")) PA++;
                    if(exp.getName().contains("Studies")) AS++;                    
                    al.add(exp.getName() + "\t\t" + aux_content + "\r\n");
                }               
                
            }   
            //content += "\tPA: " + PA + " AS: " + AS + "\r\n";
            al.add(PA + "\t" + AS);
            hm.put(id, al);                                   
        }
        
        csvreader = new CSVReader(new FileReader(csvfile), ';');
        
        while ((nextLine = csvreader.readNext()) != null) 
        {
            String key = nextLine[0];
            //System.out.println("ID: " + key + " " + hm.get(key));
            List<String> al = hm.get(key);
            int size = al.size();
            System.out.println(key + "\t" + (size != 0 ? al.get(size-1) : ""));
            //for(String s : hm.get(key))
              //  System.out.println(s);
        }
      
    }
    
    
    public static void extractExpressions(String xmlfile, String csvfile, String outfile) throws Exception
    {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        org.dom4j.Document document = reader.read(xmlfile);
        org.dom4j.Element root = document.getRootElement();        
        
        CSVReader csvreader = new CSVReader(new FileReader(csvfile), ';');
        HashMap<String, String> hm = new HashMap<String, String>();
        String [] nextLine;
        while ((nextLine = csvreader.readNext()) != null) 
        {
            //hm.put(nextLine[0], "");
        }
        
        for ( Iterator i1 = root.elementIterator("blockinfo"); i1.hasNext();)
        {
            org.dom4j.Element e1 = (org.dom4j.Element) i1.next();            
            
            String id = e1.attribute("id_entity").getValue();            
            //if(!id.equals("786643")) continue;
            
            int PA = 0, AS = 0;
            
            for(Object obj : e1.elements())
            {
                org.dom4j.Element exp = (org.dom4j.Element) obj;                
                
                if(exp.getName().contains("Activity"))
                {                    
                    hm.put(exp.element("Title_name").getText(), "");      
                }
            }   
                       
        }
        
        csvreader = new CSVReader(new FileReader(csvfile), ';');
        
        for(String key : hm.keySet())
        {
            System.out.println(key);
        }       
      
    }
    
    
    
    /*
     * Limpiamos de mierda los textos para poder imprimirlos
     * 
     */
    public static void cleanActionsFOOOO(String textinput, String textoutput)
    {
        // Stream to read file
        FileInputStream fin;		

        try
        {
            // Open an input stream
            fin = new FileInputStream (textinput);

            
            String content = FileFootils.readStream(new DataInputStream(fin), "ISO-8859-15");
            
            String publication = "\r\nPublications\r\n";
            String line = "\r\n---------------------------------------------\r\n";            
            
            //content = "ID: 786642\r\n\r\n 1 ---------------------------------------------\r\n\r\nMYCV\r\n\r\n\r\nPublicacions\r\n\r\ndasdas\r\nsf\r\n\r\nsad\r\n---------------------------------------------\r\n\r\nID: 786642\r\n\r\n 1 ---------------------------------------------MYCV\r\n\r\n\r\nPublicacions\r\n\r\ndasdas\r\nsf\r\n\r\nsad\r\n---------------------------------------------\r\n\r\n";
            
            int fromIndex = 1;
            
            int index = content.indexOf(publication, fromIndex);
            
            int indexLine = -1;
            
            
            while(index != -1)
            {
                indexLine = content.indexOf(line, index);
                
                if(indexLine != -1)
                {                    
                    content = content.substring(0, index) + content.substring(indexLine);
                    
                    index = content.indexOf(publication, index+5);
                }
            }
            // Close our input stream
            fin.close();		
            
            FileFootils.writeFile(content, textoutput, "ISO-8859-15");
        }
        // Catches any error conditions
        catch (IOException e)
        {
                System.err.println ("Unable to read from file");
                System.exit(-1);
        }
    }
    
    
    
    public static void Go()
    {
        try
        {
            extractExpressionFromCSVID("EXTRACTED_INFORMATION_FIX.xml","ITAREPORT01022012_IDS_FOR_STUDY.csv", "");
            
            //extractExpressions("DataResearcherCollect_report0222012_ITA.xml","ITAREPORT01022012_IDS_FOR_STUDY.csv", "FOO.csv");                        
        }
        catch(Exception ex)
        {
            System.out.print(" FDAS " + ex.getMessage());
        }
    }   
            
}
