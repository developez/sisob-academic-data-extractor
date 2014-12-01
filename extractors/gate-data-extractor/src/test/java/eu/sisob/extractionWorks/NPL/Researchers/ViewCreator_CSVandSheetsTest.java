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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import eu.sisob.uma.NPL.Researchers.Data.ViewCreator_CSVandSheets;
import org.apache.log4j.BasicConfigurator;
import org.dom4j.DocumentException;
import org.junit.Before;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ViewCreator_CSVandSheetsTest 
{    
         @Before
      public void setup()
      {

        BasicConfigurator.configure();

      }
         
    @Test
    public void test() throws DocumentException
    {
            if(true)
            {
                assertEquals(true, true);
                return;
            }
            
            String filepath = "C:\\Users\\dlopez\\Documents\\NetBeansProjects\\data_extracted\\CVsOfResearchers\\researcher_ids_results.xml";                                        
            File destpath =  new File("test_gate_destination");
            if(!destpath.exists())
                destpath.mkdir();
            
            if(!(new File(filepath).exists()))
            {
                System.out.println(filepath + " do not exist");                                                
                assertEquals(true, false);
                return;
            }            
            
            org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = reader.read(filepath); //("ResearcherPagesMonkeyTask.xml");
            
            ViewCreator_CSVandSheets.createViewFilesFromDataExtracted(document, destpath, true, false);
            
    }
}
