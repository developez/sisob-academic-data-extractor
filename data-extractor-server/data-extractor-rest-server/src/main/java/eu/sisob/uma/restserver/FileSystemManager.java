
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

package eu.sisob.uma.restserver;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class FileSystemManager {
    
    /**
     *
     * @param code_task_folder_dir
     * @param filename_prefix
     * @param filename_ext
     * @return
     */
    public static File getFileIfExists(File code_task_folder_dir, final String filename_prefix, final String filename_ext){
        
        List<File> list = null;
        if(code_task_folder_dir.exists()){
            
            list = Arrays.asList(code_task_folder_dir.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(filename_prefix) && name.endsWith(filename_ext); 
            }}));
        }
        
        return (list != null && list.size() == 1 ? list.get(0) : null);
        
    }
    
    /**
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static File createFileAndIfExistsDelete(String path) throws IOException{        
        
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdir();
        else 
        { 
            FileUtils.deleteDirectory(dir);            
            dir.mkdir();                    
        }   
        
        return dir;
    }
}
