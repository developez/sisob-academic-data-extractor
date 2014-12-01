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

package eu.sisob.uma.NPL.Researchers;

import static eu.sisob.uma.NPL.Researchers.GateDataExtractorService.KEYWORDS_PATH;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * This class split in blocks a CV
 * 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CVBlocks 
{
    /**
     *
     */
    public static final Integer CVBLOCK_PROFESSIONAL_ACTIVITY   = 0;  //"Profesional Activity";
    /**
     *
     */
    public static final Integer CVBLOCK_UNIVERSITY_STUDIES      = 1;  //"University Studies";
    /**
     *
     */
    public static final Integer CVBLOCK_PUBLICATIONS            = 2;  //"Publications";
    /**
     *
     */
    public static final Integer CVBLOCK_PERSONAL                = 3;  //"Personal";    
    /**
     *
     */
    public static final Integer CVBLOCK_OTHERS                  = 4;  //"Others";   
    /**
     *
     */
    public static final Integer CVBLOCK_COUNT_WITH_FILES        = 5;  //"Others";    

    /**
     *
     */
    public static final Integer CVBLOCK_REST                    = 5;  //"Others";    
    /**
     *
     */
    public static final Integer CVBLOCK_COUNT_TOTAL             = 6;  //"Others"; 
    
    /**
     *
     */
    public static final String[] CVBLOCK_DESCRIPTIONS = new String[] { "CVBLOCK_PROFESSIONAL_ACTIVITY", 
                                                                       "CVBLOCK_UNIVERSITY_STUDIES", 
                                                                       "CVBLOCK_PUBLICATIONS", 
                                                                       "CVBLOCK_PERSONAL", 
                                                                       "CVBLOCK_OTHERS",
                                                                       "CVBLOCK_REST"};  
    
    /**
     *
     */
    public static final String[] CVBLOCK_FILENAMES = new String[] {    "cv_blocks_keywords_pa", 
                                                                       "cv_blocks_keywords_us", 
                                                                       "cv_blocks_keywords_pub", 
                                                                       "cv_blocks_keywords_per", 
                                                                       "cv_blocks_keywords_other"}; 
    
    /**
     *
     * @param keywords_dir
     * @return
     */
    public static HashMap<String, String[]> getCVBlocksAndKeywords(File keywords_dir)
    {
        HashMap<String, String[]> blocks_and_keywords = new HashMap<String, String[]>();
         
        try 
        {  
            List<String>[] lists = new List[CVBLOCK_COUNT_WITH_FILES];
            for(Integer i = 0; i < lists.length; i++){
                lists[i] = new ArrayList<String>();
                String content = FileUtils.readFileToString(new File(keywords_dir, CVBLOCK_FILENAMES[i]), "UTF-8");
                for(String line : content.split("\r\n"))
                    if(!line.equals("")){
                        line = line.toLowerCase().replaceAll("[^A-Za-z0-9 ]","").trim(); //FIXME
                        lists[i].add(line);
                    }
            }            
            
            for(Integer i = 0; i < lists.length; i++)
                blocks_and_keywords.put(i.toString(), lists[i].toArray(new String[lists[i].size()]));
        }         
        catch (FileNotFoundException ex) 
        {
            ProjectLogger.LOGGER.error(ex);
        }
        catch (IOException ex) 
        {
            ProjectLogger.LOGGER.error(ex);
        }
         
        /*
        blocks_and_keywords.put(CVBLOCK_PROFESSIONAL_ACTIVITY, new String[] { "Positions", "Professional Experience", "Employment", "Career", "Teaching", "Laboral"});
        blocks_and_keywords.put(CVBLOCK_UNIVERSITY_STUDIES, new String[] {"Education", "University Studies"});
        blocks_and_keywords.put(CVBLOCK_PERSONAL, new String[] {"Personal", "Information Personal"});
        blocks_and_keywords.put(CVBLOCK_PUBLICATIONS, new String[] {"Publications", "Selected Publications"});
        blocks_and_keywords.put(CVBLOCK_OTHERS, new String[] {"RESEARCH RECORD", "Research Area", "Teaching Experience", "Lab members", "Awards", "Research funding", "Research Grants", "Research funding"});
        */
         
        return blocks_and_keywords;
    }
}
