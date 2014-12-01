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

package eu.sisob.uma.extractors.adhoc.websearchers_cv;

import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTask;
import eu.sisob.uma.api.concurrent.threadpoolutils.ExecutorResource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class WebSearchersCVExtractorTask implements CallbackableTask
{       
    String user;    
    String pass;    
    String email;    
    String task_code;
    String task_code_folder;
    boolean from_meneame;
    File input_file;
    File output_file;
    File notfound_output_file;
    
    File results_dir;
    File zip_output_file;
    
    File output_file_2;
    
    /**
     *
     */
    protected StringWriter error_sw;     
    
    Boolean finished;
    
    /*
     * @param document - xml document with the information     
     */    
    /**
     *
     * @param input_file
     * @param output_file
     * @param notfound_output_file
     * @param results_dir
     * @param zip_output_file
     * @param output_file_2
     */
    public WebSearchersCVExtractorTask(File input_file, File output_file, File notfound_output_file, File results_dir, File zip_output_file, File output_file_2)
    {   
        this.input_file = input_file;
        this.output_file = output_file;
        this.notfound_output_file = notfound_output_file;
        this.results_dir = results_dir;
        this.zip_output_file = zip_output_file;
        this.output_file_2 = output_file_2;
        this.error_sw = new StringWriter();
        this.finished = false;
    }           
    
    /**
     *
     */
    @Override
    /*
     * Extract from news and its social indicators from some news portals
     */
    public void executeTask() {        
                        
        this.error_sw.getBuffer().setLength(0);
        WebSearchersCVExtractor w = new WebSearchersCVExtractor();
        w.scrap_duckduckgo(input_file, output_file, notfound_output_file, error_sw);
        w.download_files(output_file, results_dir, zip_output_file, output_file_2, error_sw);
    }    
    
    /*
     * Callback function to manage the results
     */
    /**
     *
     */
    public void executeCallBackOfTask() 
    {                        
        setFinished(true);
    }       

    /**
     *
     * @return
     */
    @Override
    public boolean isFinished()
    {
        synchronized(finished) 
        {
            return finished;
        }
    }

    /**
     *
     * @param b
     */
    public void setFinished(boolean b)
    {
        synchronized(finished) 
        {
            finished = b;
        }
    }

   
}

