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

package eu.sisob.uma.restserver;

import java.text.MessageFormat;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class TheResourceBundle 
{
    private static TheResourceBundle globalBundle = null;
    
    private java.util.ResourceBundle bundle;
    
    private TheResourceBundle()
    {
        bundle = java.util.ResourceBundle.getBundle("Bundle"); 
    }
    
    /**
     * 
     * @param key
     * @return
     */
    public static String getString(String key)
    {
        if(globalBundle == null)
        {
            globalBundle = new TheResourceBundle();            
        }
        return globalBundle.bundle.getString(key);
    }
    
    /**
     * 
     * @param key
     * @param params
     * @return
     */
    public static String getString(String key, Object... params  ) 
    {
        if(globalBundle == null)
        {
            globalBundle = new TheResourceBundle();
        }
        
        return MessageFormat.format(globalBundle.getString(key), params);
    }
}
