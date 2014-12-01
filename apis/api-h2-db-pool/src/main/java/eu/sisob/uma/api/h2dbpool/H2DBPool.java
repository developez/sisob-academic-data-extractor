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

package eu.sisob.uma.api.h2dbpool;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class H2DBPool 
{    
    H2DBCredentials credentials;        
    
    //String dbname = "system";        
    //private JdbcConnectionPool instance;
    public H2DBPool(H2DBCredentials credentials) throws SQLException
    {   
        this.credentials = credentials;
        //;IFEXIST=TRUE
        //instance = JdbcConnectionPool.create("jdbc:h2:file:" + bdpath + ";IFEXISTS=TRUE;DB_CLOSE_DELAY=-1;MULTI_THREADED=1;", "sa", "sa");                    
        //instance.getConnection().close();        
    }

    //FIX THIS AND IMPLEMENT POOL
    public Connection getConnection() throws ClassNotFoundException, SQLException
    {        
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:file:" + credentials.connpath + ";IFEXISTS=TRUE;", credentials.user, credentials.pass);
        return conn;
    }                
}
