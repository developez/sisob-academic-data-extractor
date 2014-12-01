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

package eu.sisob.uma.restserver.services.communications;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */

@XmlRootElement
public class OutputTaskStatus 
{
    /**
     * 
     */
    public static final String TASK_STATUS_TO_EXECUTE = "TO EXECUTE";
    /**
     * 
     */
    public static final String TASK_STATUS_EXECUTING = "EXECUTING";
    /**
     * 
     */
    public static final String TASK_STATUS_EXECUTED = "EXECUTED";
    
    /**
     * 
     */
    public static final String TASK_STATUS_NO_ACCESS = "NO ACCESS";    
    
    /**
     * 
     */
    public static final String TASK_STATUS_NO_AUTH = "NO AUTH"; 
    
    /**
     *
     */
    public OutputTaskStatus()
    {
        status = "";      
        message = "";
        task_code = "";   
        name = "";   
        kind = "";

        date_created = "";
        date_started = "";
        date_finished = "";

        result = "";      
        source = "";
        verbose = "";
        errors = "";

        feedback = "";   
    }
    
    /**
     *
     */
    public String status;      
    /**
     *
     */
    public String message;
    /**
     *
     */
    public String task_code;   
    /**
     *
     */
    public String name;   
    /**
     *
     */
    public String kind;
    /**
     *
     */
    public String params;

    /**
     *
     */
    public String date_created;
    /**
     *
     */
    public String date_started;
    /**
     *
     */
    public String date_finished;

    /**
     *
     */
    public String result;      
    /**
     *
     */
    public String source;  
    /**
     *
     */
    public String verbose;  
    /**
     *
     */
    public String errors;

    /**
     *
     */
    public String feedback;   
}

    
