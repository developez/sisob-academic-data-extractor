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

package eu.sisob.uma.extractors.adhoc.email;

import eu.sisob.uma.api.concurrent.threadpoolutils.CallbackableTask;
import java.io.File;
import java.io.StringWriter;
import java.util.List;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class EmailExtractorTask implements CallbackableTask
{       
    String user;    
    String pass;    
    String email;    
    String task_code;
    String task_code_folder;
    boolean from_meneame;
    File input_file;
    File data_dir;
    File output_file;
    File norepeat_output_file;
    File notfound_output_file;
    File output_norepeat_file_wor_notfound;
    /**
     *
     */
    protected StringWriter error_sw;     
    List<String> filters;
    
    Boolean finished;
    
    /*
     * @param document - xml document with the information     
     */             
    //public EmailExtractorTask(File csv_researchers_input_file, File researchers_input_dir, File csv_researchers_output_file, File csv_researchers_output_wor_file, File csv_researchers_output_notfound_file)
    /**
     *
     * @param input_file
     * @param data_dir
     * @param output_file
     * @param norepeat_output_file
     * @param notfound_output_file
     * @param output_norepeat_file_wor_notfound
     * @param filters
     */
    public EmailExtractorTask(File input_file, File data_dir, File output_file, File norepeat_output_file, 
                              File notfound_output_file, File output_norepeat_file_wor_notfound, List<String> filters)
    {   
        this.input_file = input_file;
        this.data_dir = data_dir;
        this.output_file = output_file;
        this.norepeat_output_file = norepeat_output_file;
        this.notfound_output_file = notfound_output_file;
        this.output_norepeat_file_wor_notfound = output_norepeat_file_wor_notfound;
        this.error_sw = new StringWriter();
        this.filters = filters;
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
        EmailExtractor.extract_emails(input_file, data_dir, output_file, norepeat_output_file, notfound_output_file, output_norepeat_file_wor_notfound, filters, error_sw);
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

