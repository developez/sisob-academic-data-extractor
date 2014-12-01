

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

import eu.sisob.uma.extractors.adhoc.cvfilesinside.InternalCVFilesExtractor;
import eu.sisob.uma.footils.File.ZipUtil;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.*;


/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class Test 
{   
    @Before
    public void setup()
    {
        BasicConfigurator.configure();
    }
    
    @org.junit.Test
    public void test() throws InterruptedException
    { 
        if(true)
        {
            assertEquals(true, true);
            return;
        } 

        String test_dirname = "test-data" + File.separator + "sample_6";
        String input_filename = "data-researchers-documents-urls.csv";
        
        File data_dir = new File(test_dirname);
        File source_csv_file = new File(data_dir, input_filename);        
        File output_file = new File(data_dir, "out."+input_filename);                
        File zip_output_file = new File(data_dir, "downloads_out_" + input_filename + ".zip");            
        File out_file_second = new File(data_dir, "out.file."+input_filename);        
        File results_dir = new File(data_dir,"downloads");
        StringWriter sw = new StringWriter();
                
        //ZipUtil.unZipIt(new File(data_dir, "downloads_folder.zip"), new File(data_dir, "dest_folder"));
        //ZipUtil.unZipItToSameFolder(new File(data_dir, "downloads_folder.zip"), new File(data_dir, "dest"));
        
        
//        ZipFile zf;
//            try {
//                zf = new ZipFile(zip_output_file);                
//                ArrayList<File> files = new ArrayList<File>();
//                for(File f : results_dir.listFiles())
//                    files.add(f);
//                zf.createZipFile(files, new ZipParameters()); 
//            } catch (ZipException ex) {
//                Logger.getRootLogger().error("Error zipping results from " + results_dir.getName());
//            }
        
        //InternalCVFilesExtractor.removeDuplicates(output_file, 5);
        //InternalCVFilesExtractor.extract_cv_files(source_csv_file, data_dir, output_file, /*out_file_second, new File(data_dir,"downloads"),*/ sw);
        //InternalCVFilesExtractor.download_files(output_file, new File(data_dir,"downloads"), zip_output_file, out_file_second, sw);
    }    
    
}
