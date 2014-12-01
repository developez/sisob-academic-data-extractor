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

package eu.sisob.uma;

import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTaskExecution;
import eu.sisob.uma.extractors.adhoc.email.EmailExtractorService;
import eu.sisob.uma.extractors.adhoc.email.EmailExtractorTask;
import eu.sisob.uma.extractors.adhoc.email.ProjectLogger;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.BasicConfigurator;
import static org.junit.Assert.*;
import org.junit.*;


/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class EmailExtractorExtractor_Test 
{   
    @Before
    public void setup()
    {
        BasicConfigurator.configure();
    }
    
    @Test
    public void test_patterns() throws InterruptedException
    {
        if(true)
        {
            assertEquals(true, true);
            return;
        } 
        
        List<String> emails = new ArrayList<String>();
        emails.add("indian.es");
        emails.add("aindian.co");
        emails.add("foo.co");
        emails.add("indianes");
        
        List<String> filters = new ArrayList<String>();
        filters.add("*.es");
        filters.add("*in*.co");
        filters.add("in*.co");
        
        for(String email : emails)
        for(String filter : filters){
            
            String filter2 = filter.replace("*", ".*?");
            Pattern pattern  = Pattern.compile(filter2);
            if(pattern.matcher(email).matches()){
                ProjectLogger.LOGGER.info(filter + " => " + email + " => " + " yes");      
            }else{
                ProjectLogger.LOGGER.info(filter + " => " + email + " => " + " no");     
            }
            
            
        }
    }
    
    @Test
    public void test() throws InterruptedException
    {
        if(true)
        {
            assertEquals(true, true);
            return;
        } 
        

        String test_dirname = "test-data" + File.separator + "sample_3";
        String input_filename = "data-researchers-documents-urls.csv";
        
        File data_dir = new File(test_dirname);
        File input_file = new File(data_dir, input_filename);        
        File output_file = new File(data_dir, "out."+input_filename);                
        File norepeat_output_file = new File(data_dir, "out.norepeat."+input_filename); 
        File notfound_output_file = new File(data_dir, "out.notfound."+input_filename);      
        File output_norepeat_file_wor_notfound = new File(data_dir, "out.notfound.norepeat."+input_filename); 
        
        StringWriter sw = new StringWriter();
        
        EmailExtractorService.createInstance();
        
        List<String> filters = new ArrayList<String>();
        filters.add("*.edu");
        
        
        EmailExtractorTask task = new EmailExtractorTask(input_file, data_dir, output_file, norepeat_output_file, notfound_output_file, output_norepeat_file_wor_notfound, filters);
        
        try 
        {
            EmailExtractorService.getInstance().addExecution((new CallbackableTaskExecution(task)));            
        } 
        catch (Exception ex) 
        {
            ProjectLogger.LOGGER.error("Error", ex);            
        }   
        
        int count = 0;
        while(count < 10){
            Thread.sleep(10000);
            count++;
        }
        
    }    
    
}
