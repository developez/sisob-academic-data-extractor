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

package eu.sisob.uma.restserver.restservices;

import eu.sisob.uma.restserver.AuthorizationManager;
import eu.sisob.uma.restserver.TheResourceBundle;
import eu.sisob.uma.restserver.UserAttributes;
import eu.sisob.uma.restserver.services.communications.OutputAuthorizationResult;
import java.io.StringWriter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
@Path("/authorization")
public class RESTSERVICEAuthorization 
{
    @Context
    private UriInfo context;

    /** Creates a new instance of HelloWorld */
    public RESTSERVICEAuthorization() {

    }        

    /**
     * Checking access
     * @param user 
     * @param pass      
     * @return If the authorization is correct
     */
    @GET
    @Produces("application/json")
    public OutputAuthorizationResult authorization(@QueryParam("user") String user, @QueryParam("pass") String pass) 
    {        
        StringWriter message = new StringWriter();    
        UserAttributes user_att = new UserAttributes();        
        boolean valid = false;
        
        OutputAuthorizationResult r = new OutputAuthorizationResult();   
        //Security
        if(user.contains("'") || pass.contains("'"))
        {
            r.success = false;
            r.message = "Please, do not try sql inyection or similar shit :S";
            return r;
        }        
        
        synchronized(AuthorizationManager.getLocker(user))
        {
            
            valid = AuthorizationManager.validateAccess(user, pass, user_att, message);
        }
        
        if(valid)
        {            
            r.success = true;
            r.account_type = user_att.getAccountType();
        }
        else
        {
            r.success = false;
        }
        
        r.message = message.toString();
        
        return r;
    }    
}
