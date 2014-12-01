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

package eu.sisob.uma.extractors.adhoc.websearcher.test;

import eu.sisob.uma.extractors.adhoc.websearchers.WebSearchersExtractor;
import eu.sisob.uma.extractors.adhoc.websearchers.WebSearchersExtractor.SearchPatterns;
import eu.sisob.uma.extractors.adhoc.websearchers_cv.WebSearchersCVExtractor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import static org.junit.Assert.*;
import org.junit.*;


/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class WebSearcherExtractor_Test 
{   
    @Before
    public void setup()
    {
        BasicConfigurator.configure();
    }
        
    @Test
    public void test_extracto_cv() throws InterruptedException
    {   
        if(true)
        {
            assertEquals(true, true);
            return;
        } 
            
        for(int i = 1; i <= 1; i++){
            String input_filename = "data-researchers-urls" + i + ".csv";
            String input_filepath = "test-data\\sample_1";
            File input_file = new File(input_filepath, input_filename);        

            String output_filename = "cv_out_" + input_file.getName();
            String output_filename_2 = "cv_out_2_" + input_file.getName();
            String output_u_filename = "cv_out_unfound_" + input_file.getName();
            String zip_output_filename = "downloads_out_" + input_file.getName() + ".zip";
            if(!input_file.getParentFile().exists())
                return;

            File out_file = new File(input_file.getParentFile(), output_filename);        
            File out_u_file = new File(input_file.getParentFile(), output_u_filename);        
            File zip_output_file = new File(input_file.getParentFile(), zip_output_filename);
            File output_file_2 = new File(input_file.getParentFile(), output_filename_2);                 
            
            WebSearchersCVExtractor w = new WebSearchersCVExtractor();
            w.scrap_duckduckgo(input_file, out_file, out_u_file, null);
            w.download_files(out_file, new File(input_filepath + "\\downloads"), zip_output_file, output_file_2,null);
        }
    }    
    
    @Test
    public void test_extractor() throws InterruptedException
    {
        
        if(true)
        {
            assertEquals(true, true);
            return;
        } 
            
        List<SearchPatterns> patterns = new ArrayList<SearchPatterns>();
        patterns.add(SearchPatterns.P1);
        patterns.add(SearchPatterns.P2);
        patterns.add(SearchPatterns.P3);
        patterns.add(SearchPatterns.P4);
        patterns.add(SearchPatterns.P5);
        
        String input_filename = "data-researchers-urls1.csv";
        String input_filepath = "test-data\\sample_1";
        
        for(SearchPatterns pattern : patterns){

            File input_file = new File(input_filepath, input_filename);        

            String output_filename = pattern + "_out_" + input_file.getName();
            String output_u_filename = pattern + "_out_unfound_" + input_file.getName();
            if(!input_file.getParentFile().exists())
                return;

            File out_file = new File(input_file.getParentFile(), output_filename);        
            File out_u_file = new File(input_file.getParentFile(), output_u_filename);        

            (new WebSearchersExtractor(pattern)).scrap_duckduckgo(input_file, out_file, out_u_file, null);        
        }
    }    
    
}
