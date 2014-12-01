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

package eu.sisob.uma.footils.Web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import org.jsoup.Jsoup;

/**
 *
 * @author Daniel L�pez Gonz�lez (dlopez@lcc.uma.es, dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class Downloader 
{
    /*
     * Get jsoup document from URL (try N times)
     */
    public static org.jsoup.nodes.Document tryToConnect(String sURL, int times) throws InterruptedException
    {
        boolean b = true;
        int iMaxError = 0;
        org.jsoup.nodes.Document doc = null;
        while(b)
        {
            try
            {
                doc = Jsoup.connect(sURL).get();
                b = false;
            }
            catch(Exception ex)
            {
                if(!ex.getMessage().contains("404"))
                {
                    iMaxError++;                                        
                    Thread.sleep(1000);
                    b = true;
                }
                else
                {
                    b = false;
                    iMaxError = 21;
                    doc = null;
                }
            }

            if(iMaxError >= 20)
            {
                b = false;
                doc = null;
            }
        }

        return doc;
    }

    /*
     * Get jsoup document from URL (try N times)
     */
    public static org.dom4j.Document tryToConnectDomj4(org.dom4j.io.SAXReader reader, String sURL, int times) throws InterruptedException
    {
        boolean b = true;
        int iMaxError = 0;
        org.dom4j.Document doc = null;
        while(b)
        {
            try
            {
                doc = reader.read(new URL(sURL));
                b = false;
            }
            catch(Exception ex)
            {
                if(!ex.getMessage().contains("404"))
                {
                    iMaxError++;                    
                    Thread.sleep(1000);
                    b = true;
                }
                else
                {
                    b = false;
                    iMaxError = times + 1;
                    doc = null;
                }
            }

            if(iMaxError >= times)
            {
                b = false;
                doc = null;
            }
        }

        return doc;
    }
    
    public static void downloadFile(String sURL, String filePath) throws MalformedURLException, IOException
    {
      
        URL url  = new URL(sURL);
        URLConnection urlC = url.openConnection();
        
        InputStream is = url.openStream();                

        System.out.flush();

        String localFile = filePath;
        FileOutputStream fos = null;
        fos = new FileOutputStream(localFile);

        int oneChar, count=0;
        while ((oneChar=is.read()) != -1)
        {
         fos.write(oneChar);
         count++;
        }
        is.close();
        fos.close();                
    }
    
    /**
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static URL fetchURL( String url ) throws IOException 
    {
        
        URL dest = new URL(url);
        HttpURLConnection yc =  (HttpURLConnection) dest.openConnection();
        yc.setInstanceFollowRedirects( false );
        yc.setUseCaches(false);

        System.out.println( "url = " + url );

        int responseCode = yc.getResponseCode();
        if ( responseCode >= 300 && responseCode < 400 ) { // brute force check, far too wide
            URL url_3 = fetchURL( yc.getHeaderField( "Location") );    
            yc.getInputStream().close();
            yc.disconnect();
            return url_3;
        }
        URL url_2 = yc.getURL();
        yc.getInputStream().close();
        yc.disconnect();
        return url_2;
    }   
}
