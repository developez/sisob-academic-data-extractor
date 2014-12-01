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

package eu.sisob.uma.crawler.ResearchersCrawlers;

import eu.sisob.uma.crawler.ProjectLogger;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CandidateTypeURL
{
    public CandidateTypeURL(String sType_, String sText_, String sURL_, String sFromURL_)
    {
        this.sText = sText_;
        this.sURL = sURL_;
        this.sFromURL = sFromURL_;
        this.sType = sType_;
        this.sSubType = "";
        this.sExt = "";
        this.data = null;
    }
    
    public CandidateTypeURL(String sType_, String sSubType_, String sExt, String sText_, String sURL_, String sFromURL_, Object data)
    {
        this.sText = sText_;
        this.sURL = sURL_;
        this.sFromURL = sFromURL_;
        this.sType = sType_;
        this.sSubType = sSubType_;        
        this.sExt = sExt;
        this.data = null;
    }

    @Override
    public String toString()
    {
        return " TEXT " + sText + " DATA " + data + "URL " + sURL + " TYPE " + sType + " SUBTYPE " + sSubType + " FROM " + sFromURL;
    }
    public String sText;
    public String sURL;
    public String sFromURL;
    public String sType;
    public String sSubType;
    public String sExt;
    public Object data;
    
    public static boolean checkURLExistInList(List<CandidateTypeURL> lst, String url)
    {
        if(lst != null)
        for(CandidateTypeURL ctu : lst)
        {
            //if(ctu.sURL.toLowerCase().equals(url.toLowerCase()))
            if(ctu.sURL.equals(url))
            {
                return true;
            }
        }
        return false;
    }    
    
    public static boolean checkIfURLExistsInList(List<CandidateTypeURL> lst, String url)
    {
        url = url;
        if(lst != null)
        for(CandidateTypeURL ctu : lst)
        {
            if(ctu.sURL.equals(url))
            {
                return true;
            }
        }
        return false;
    }    
    
    /**
     * For see what is happening after each crawling action
     * @param sTitle
     * @param t
     */
    public static void printResults(String sTitle, TreeMap<String, List<CandidateTypeURL>> t)
    {        
        ProjectLogger.LOGGER.info(sTitle);
        Iterator<String> it = t.keySet().iterator();
        while(it.hasNext())
        {
            String s = it.next();
            List<CandidateTypeURL> lst = t.get(s);
            ProjectLogger.LOGGER.info(s);
            for(CandidateTypeURL ss: lst)
            {
                ProjectLogger.LOGGER.info("\t" + ss.toString());
            }
        }        
    }
    
    public static boolean containsTheWorkInLink(String associate_text, String word)
    {
        boolean success = false;
        
        if(word.length() < 3)
        {
            success = word.equals(associate_text);
        }
        else
        {
            if(
                    associate_text.contains(" " + word) ||
                    associate_text.contains(word + " ") ||
                    associate_text.equals(word)
              )
                success = true;
            
            //for(String s : associate_text.split(" "))
            //    if(s.equals(word)){            
            //        success = true;
            //        break;
            //    }            
        }
        return success;
    }
    
    /*
     * Canonice the string (no accentuation and lower case)
     * @param input
     * @return 
     */
    public static String getCanonicalName(String input) 
    {        
        //Super accent cleaner
        // Canonic descomposition
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);        
        Pattern pattern = Pattern.compile("\\P{ASCII}");
        String output = pattern.matcher(normalized).replaceAll("");
        //~Super accent cleaner
       
        output = output.replace(". ", ".");
        
        String symbols1 = "_().,|<>-";
        for (char c : symbols1.toCharArray())                     
            output = output.replace(c, ' ');
        
//        String symbols2 = "-";
//        for (int i=0; i<symbols2.length(); i++)                     
//            output = output.replace(symbols2.charAt(i), ' ');
        
        output = output.toLowerCase();        
        
        /*
         * Clean associate_text
         */  
        output = output.trim().replace("\r\n", " ").replace("\r", " ").replace("\n", " ").replace("\t", " ");
        while(output.contains("  ")) output = output.replace("  ", " ");
        
        while(output.contains("  ")) 
        {
            output = output.replace("  ", " ");
        }              
        
        return output.trim();
    }
}
