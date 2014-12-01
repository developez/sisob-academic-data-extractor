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

package eu.sisob.uma.restserver.servlets;
import eu.sisob.uma.restserver.SystemManager;
import eu.sisob.uma.restserver.TheConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

/**
 * Server Context Listener used to initialization stuff
 * Set up gate services, crawler, etc
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ServletContextClass implements ServletContextListener
{  
    
    @Override
    public void contextInitialized(ServletContextEvent arg0) 
    {        
        try 
        {
            TheConfig.createInstance(arg0.getServletContext().getRealPath(""));
            
            SystemManager.getInstance().setup();            
        } 
        catch (Exception ex) {
            Logger.getLogger("error").error("System Manager has not initialized", ex);
        }        
    }


    @Override
    public void contextDestroyed(ServletContextEvent arg0) 
    {
        try 
        {
            SystemManager.getInstance().setdown();       
        }
        catch (Exception ex)         
        {
            Logger.getLogger("error").error("System Manager had errors in its setdown", ex);
        }
    }

}
