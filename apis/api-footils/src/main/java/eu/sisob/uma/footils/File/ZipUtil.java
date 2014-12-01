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

package eu.sisob.uma.footils.File;

import eu.sisob.uma.footils.ProjectLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
 
/**
 * Utility to Zip and Unzip nested directories recursively.
 * @author Robin Spark
 */
public class ZipUtil {
    
    /**
     * Unzip it
     * @param zipFile input zip file
     * @param outputFolder      
     */
    public static boolean unZipItToSameFolder(File zipFile, File outputFolder)
    {
        boolean success = false;
        byte[] buffer = new byte[1024];
 
        //get the zip file content
        ZipInputStream zis = null;
        //get the zipped file list entry
        ZipEntry ze = null;
        FileOutputStream fos = null;
        
        try
        {
            //get the zip file content
            zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ze = zis.getNextEntry();
            
            while(ze!=null)
            {

                String fileName = ze.getName();
                
                if(!fileName.contains("__MACOSX"))
                {
                    int index = fileName.lastIndexOf("/");
                    File newFile = null;
                    if(index == -1){
                        newFile = new File(outputFolder, fileName); 
                    }else{
                        if(index == fileName.length() - 1){
                            ze = zis.getNextEntry();
                            continue;
                        } else
                            newFile = new File(outputFolder, fileName.substring(index+1)); 
                    }

                    ProjectLogger.LOGGER.info("file unzip : "+ newFile.getAbsoluteFile());

                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    new File(newFile.getParent()).mkdirs();

                    fos = new FileOutputStream(newFile);   

                    int len;
                    while ((len = zis.read(buffer)) > 0) 
                    {
                        fos.write(buffer, 0, len);
                    }

                    fos.close(); 
                    fos = null;
                }
                ze = zis.getNextEntry();
            }

            success = true; 
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
        }
        finally
        {
            if(zis != null) 
            {   
                try 
                {
                    zis.closeEntry();
                    zis.close();   	            
                } 
                catch (IOException ex) 
                {
                    ProjectLogger.LOGGER.error(ex.getMessage());
                }                
            }            
        }
     
        return success;
    }    
 
   /**
     * Unzip it
     * @param zipFile input zip file
     * @param outputFolder      
     */
    /*
    public static boolean unZipIt(File zipFile, File outputFolder)
    {
        boolean success = false;
        byte[] buffer = new byte[1024];
 
        //get the zip file content
        ZipInputStream zis = null;
        //get the zipped file list entry
        ZipEntry ze = null;
        FileOutputStream fos = null;
        
        try
        {
            //get the zip file content
            zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ze = zis.getNextEntry();
            
            while(ze!=null)
            {
                String fileName = ze.getName();
                
                
                if(!fileName.contains("__MACOSX"))
                {
                    File newFile = new File(outputFolder, fileName);

                    ProjectLogger.LOGGER.info("file unzip : "+ newFile.getAbsoluteFile());

                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    new File(newFile.getParent()).mkdirs();

                    fos = new FileOutputStream(newFile);   

                    int len;
                    while ((len = zis.read(buffer)) > 0) 
                    {
                        fos.write(buffer, 0, len);
                    }

                    fos.close(); 
                    fos = null;
                }
                ze = zis.getNextEntry();
            }

            success = true; 
        }
        catch(Exception ex)
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
        }
        finally
        {
            if(zis != null) 
            {   
                try 
                {
                    zis.closeEntry();
                    zis.close();   	            
                } 
                catch (IOException ex) 
                {
                    ProjectLogger.LOGGER.error(ex.getMessage());
                }                
            }            
        }
     
        return success;
    }    
    * */
}
