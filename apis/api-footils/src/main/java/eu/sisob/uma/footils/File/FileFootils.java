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

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Daniel L�pez Gonz�lez (dlopez@lcc.uma.es, dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class FileFootils 
{
    
    public static File[] finderByName(String dirName, final String fileName)
    {
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() 
        { 
                 public boolean accept(File dir, String filename)
                 { 
                     return filename.endsWith(fileName + ".*");
                 }
        } );

    }
    
    public static String readStream(InputStream is, String encoding) 
    {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, encoding);
            int c = 0;
            while (c != -1) {
                c = r.read();
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
    
    /**
     *
     * @param content
     * @param name
     * @param encode
     */
    public static void writeFile(String content, String name, String encode) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {        
        File fField = new File(name);
        FileOutputStream fileOS;
        fileOS = new java.io.FileOutputStream(fField, false);
        OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,encode);
        BufferedWriter bw = new java.io.BufferedWriter(writer);
        String sOut = content;
        bw.write(sOut);
        bw.close();           
    }

    /**
     *
     * @param sPath
     */
    public static void deleteDir(String sPath)
    {

        File dir = new File(sPath);
        if(dir.exists())
            deleteDir(dir);
        dir.delete();
    }

    /**
     *
     * @param dir
     */
    public static void deleteDir(File dir)
    {
        File[] files = dir.listFiles();

        for (int x=0; x<files.length; x++)
        {
            if (files[x].isDirectory())
            {
                deleteDir(files[x]);
            }
            files[x].delete();
        }
    }
    
    public static void copyfile(File  f1, File f2) throws FileNotFoundException, IOException
    {      
          InputStream in = new FileInputStream(f1);

          //For Append the file.
          //OutputStream out = new FileOutputStream(f2,true);

          //For Overwrite the file.
          OutputStream out = new FileOutputStream(f2);

          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
          }
          in.close();
          out.close();                
      }
    
     public static void copyFolder(File src, File dest)
    	throws IOException{
 
    	if(src.isDirectory()){
 
    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();    		   
    		}
 
    		//list all the directory contents
    		String files[] = src.list();
 
    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   //recursive copy
    		   copyFolder(srcFile,destFile);
    		}
 
    	}else{
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		InputStream in = new FileInputStream(src);
    	        OutputStream out = new FileOutputStream(dest); 
 
    	        byte[] buffer = new byte[1024];
 
    	        int length;
    	        //copy the file content in bytes 
    	        while ((length = in.read(buffer)) > 0){
    	    	   out.write(buffer, 0, length);
    	        }
 
    	        in.close();
    	        out.close();    	 
    	}
    }
     
     public static int getNumberOfLinesFromFile(String filepath) throws FileNotFoundException, IOException
     {
        File f = new File(filepath);
        return getNumberOfLinesFromFile(f);
     }
     
     public static int getNumberOfLinesFromFile(File f) throws FileNotFoundException, IOException
     {
        BufferedReader reader = null;
        int lines = 0;
        
        reader = new BufferedReader(new FileReader(f));            
        while (reader.readLine() != null) lines++;
        reader.close();           
        
        return lines;
     }        
    
      public static boolean copyFile(final File toCopy, final File destFile) {
        try {
          return FileFootils.copyStream(new FileInputStream(toCopy),
              new FileOutputStream(destFile));
        } catch (final FileNotFoundException e) {
          e.printStackTrace();
        }
        return false;
      }

      private static boolean copyFilesRecusively(final File toCopy,
          final File destDir) {
        assert destDir.isDirectory();

        if (!toCopy.isDirectory()) {
          return FileFootils.copyFile(toCopy, new File(destDir, toCopy.getName()));
        } else {
          final File newDestDir = new File(destDir, toCopy.getName());
          if (!newDestDir.exists() && !newDestDir.mkdir()) {
            return false;
          }
          for (final File child : toCopy.listFiles()) {
            if (!FileFootils.copyFilesRecusively(child, newDestDir)) {
              return false;
            }
          }
        }
        return true;
      }

      public static boolean copyJarResourcesRecursively(final File destDir,
          final JarURLConnection jarConnection) throws IOException {

        final JarFile jarFile = jarConnection.getJarFile();

        for (final Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
          final JarEntry entry = e.nextElement();
          if (entry.getName().startsWith(jarConnection.getEntryName())) {
            final String filename = StringUtils.removeStart(entry.getName(), //
                jarConnection.getEntryName());

            final File f = new File(destDir, filename);
            if (!entry.isDirectory()) {
              final InputStream entryInputStream = jarFile.getInputStream(entry);
              if(!FileFootils.copyStream(entryInputStream, f)){
                return false;
              }
              entryInputStream.close();
            } else {
              if (!FileFootils.ensureDirectoryExists(f)) {
                throw new IOException("Could not create directory: "
                    + f.getAbsolutePath());
              }
            }
          }
        }
        return true;
      }

      public static boolean copyResourcesRecursively( //
          final URL originUrl, final File destination) {
        try {
          final URLConnection urlConnection = originUrl.openConnection();
          if (urlConnection instanceof JarURLConnection) {
            return FileFootils.copyJarResourcesRecursively(destination,
                (JarURLConnection) urlConnection);
          } else {
            return FileFootils.copyFilesRecusively(new File(originUrl.getPath()),
                destination);
          }
        } catch (final IOException e) {
          e.printStackTrace();
        }
        return false;
      }

      private static boolean copyStream(final InputStream is, final File f) {
        try {
          return FileFootils.copyStream(is, new FileOutputStream(f));
        } catch (final FileNotFoundException e) {
          e.printStackTrace();
        }
        return false;
      }

      private static boolean copyStream(final InputStream is, final OutputStream os) {
        try {
          final byte[] buf = new byte[1024];

          int len = 0;
          while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
          }
          is.close();
          os.close();
          return true;
        } catch (final IOException e) {
          e.printStackTrace();
        }
        return false;
      }

      private static boolean ensureDirectoryExists(final File f) {
        return f.exists() || f.mkdir();
      }


}
