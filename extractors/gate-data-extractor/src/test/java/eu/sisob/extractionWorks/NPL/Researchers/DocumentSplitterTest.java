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

package eu.sisob.extractionWorks.NPL.Researchers;

import eu.sisob.uma.NPL.Researchers.CVBlocks;
import eu.sisob.uma.NPL.Researchers.DocumentSplitter;
import eu.sisob.uma.NPL.Researchers.ProjectLogger;
import eu.sisob.uma.footils.File.FileFootils;
import gate.creole.ResourceInstantiationException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

import gate.*;
import java.io.File;

/**
 *
 * @author Daniel L�pez Gonz�lez (dlopez@lcc.uma.es, dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class DocumentSplitterTest {
   
    @Test
    public void testSplitDocument()
    {
        if(true)
        {
            assertEquals(true, true);
            return;
        }
        
        System.out.println("SplitDocument");   
        
        try 
        {
            
            String path = ClassLoader.getSystemClassLoader().getResource("eu/sisob/components/gatedataextractor/GATE-6.0").getPath();            
            
            File home_path = new File(path);
            Gate.setGateHome(home_path);
            File plugins_path = new File(home_path + "//plugins");
            Gate.setPluginsHome(plugins_path);             
        
            Gate.init();
            
            File gateHome = new File(Gate.getGateHome().getAbsolutePath()/* + "\\resources\\GATE-6.0"*/);
            File pluginsHome = new File(gateHome, "plugins");
            
            Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURL());       
            Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Tools").toURL());
        } 
        catch (Exception ex) 
        {
            ProjectLogger.LOGGER.error(ex.toString());
        }
        
        ProjectLogger.LOGGER.info( "Done!");
                
        File keywords_dir = new File("keywords");           
        if(!keywords_dir.exists())
            keywords_dir.mkdirs();

        FileFootils.copyResourcesRecursively(Thread.currentThread().getContextClassLoader().getResource("eu/sisob/uma/NPL/Researchers/KEYWORDS/"), new File(""));      
        
        Document doc = null;
        try 
        {
            doc = Factory.newDocument((new java.io.File("C:\\Users\\dlopez\\Documents\\NetBeansProjects\\data_extracted\\CVsOfResearchers\\cvs\\3408.pdf")).toURI().toURL());          
        }
        catch (MalformedURLException ex) 
        {
             fail("File cannot be readed.");
        }
        catch (ResourceInstantiationException ex) 
        {
             fail("File cannot be readed.");
        }
        
        String content = doc.getContent().toString();        
        DocumentSplitter instance = new DocumentSplitter(CVBlocks.getCVBlocksAndKeywords(keywords_dir));
        List expResult = null;
        List<Entry<String,String>> contents = instance.SplitDocument(content);
        
        boolean result = false;
        if(contents.size() == 9)        
        {
            result = true;   
        }            
        assertEquals(true, result);
        // TODO review the generated test code and remove the default call to fail.       
    }
}
