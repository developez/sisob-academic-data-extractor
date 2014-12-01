
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

package eu.sisob.uma.NPL.Researchers.Data;

import eu.sisob.uma.NPL.Researchers.ProjectLogger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class TraductionTablesOperations 
{   
    /*     
     */        
    final static int OPERATOR1_EQUALS = 1;
    final static int OPERATOR2_START_WITH = 2;
    final static int OPERATOR3_END_WITH = 3;
    final static int OPERATOR4_CONTAINS = 4;
    final static int OPERATOR5_REGULAR_EXPRESSION = 5;   
    
    /**
     *
     */
    public final static String TRAD_TABLE_UNIVERSITY_STUDIES = "university_study_type";
    /**
     *
     */
    public final static String TRAD_TABLE_PROF_ACTIVITIES = "prof_activity_type";
            
    /**
     *
     * @param cnn
     * @param value
     * @param tradTable
     * @param prefixOfTradTable
     * @param prefixOfId
     * @return
     */
    public static Integer getTypeListFromTraductionTable(Connection cnn, String value, String tradTable, String prefixOfTradTable, String prefixOfId)            
    {   
        Integer id_type = null;
        
        value = get_canonical_name(value);       
                
        Statement statement = null;
        ResultSet rs = null;
        try
        {                                    
            String query = "select " + prefixOfId + tradTable + ", expression, operator " + 
                           "from " + prefixOfTradTable + tradTable + " " + 
                           "";    
            
            statement = cnn.createStatement();
            rs = statement.executeQuery(query);
            Integer best_id = null;
            int aux = -1;

            while(rs.next())
            {                           
                Integer id = rs.getInt(1);
                String expression = rs.getString(2);
                Integer operator = rs.getInt(3);

                switch(operator)
                {
                    case OPERATOR1_EQUALS:
                        {                                
                            if(value.equals(expression))
                            {
                                if(aux < expression.length())
                                {
                                    aux = expression.length();
                                    best_id = id;
                                }
                            }  
                        }
                        break;
                    case OPERATOR2_START_WITH:
                        {                                
                            if(value.startsWith(expression + " ") ||
                                    value.equals(expression))
                            {
                                if(aux < expression.length())
                                {
                                    aux = expression.length();
                                    best_id = id;
                                }
                            }  
                        }
                        break;
                    case OPERATOR3_END_WITH:
                        {
                            if(value.endsWith(" " + expression) ||
                               value.equals(expression))
                            {
                                if(aux < expression.length())
                                {
                                    aux = expression.length();
                                    best_id = id;
                                }
                            }  
                        }
                        break;
                    case OPERATOR4_CONTAINS:   
                        {                     

                            if(value.contains(" " + expression + " ") ||
                               value.startsWith(expression + " ") ||
                               value.endsWith(" " + expression) ||
                               value.equals(expression))
                            {
                                if(aux < expression.length())
                                {
                                    System.out.println(value + " MATCH with " + expression);
                                    aux = expression.length();
                                    best_id = id;
                                }
                            }  
                        }
                        break;
                    case OPERATOR5_REGULAR_EXPRESSION:   
                        {
//                                value = value.toLowerCase();
//                                if(value.contains(" " + expression))
//                                {
//                                    if(aux < expression.length())
//                                    {
//                                        aux = expression.length();
//                                        best_id = id;
//                                    }
//                                }  
                        }
                        break;
                }  
            }

            if(best_id != null)
            {
                id_type = best_id;
            }                
            else
            {   
                id_type = null;
            }
        }  
        catch (SQLException ex)                             
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
        } 
        finally
        {
            if(statement != null) 
            try 
            {                        
                statement.close();
            } 
            catch (SQLException ex) {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }

            if(rs != null) 
            try 
            {
                rs.close();
            } 
            catch (SQLException ex) 
            {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }     

            statement = null;
            rs = null;
        }  
        
        return id_type;
    }
    
    /**
     *
     * @param cnn
     * @param id
     * @return
     */
    public static String getProfActivityStandardName(Connection cnn, int id)
    {
        String name = "";
        Statement statement = null;
        ResultSet rs = null;
        try
        {                                    
            String query = "SELECT NAME, RANK_INDEX_1 ,RANK_INDEX_2  FROM CVN_PROF_ACTIVITY_TYPE " + 
                           "WHERE ID_PROF_ACTIVITY_TYPE = " + id +
                           "";    
            
            statement = cnn.createStatement();
            rs = statement.executeQuery(query);
            if(rs.next())
            {
                name = rs.getString(1) + " [" + rs.getInt(2) + "." + rs.getInt(3) + "]";
            }
        
        }  
        catch (SQLException ex)                             
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
        } 
        finally
        {
            if(statement != null) 
            try 
            {                        
                statement.close();
            } 
            catch (SQLException ex) {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }

            if(rs != null) 
            try 
            {
                rs.close();
            } 
            catch (SQLException ex) 
            {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }     

            statement = null;
            rs = null;
        }  
        
        return name;
    }
    
    /**
     *
     * @param cnn
     * @param id
     * @return
     */
    public static String getUniversityStudyStandardName(Connection cnn, int id)
    {
        String name = "";
        Statement statement = null;
        ResultSet rs = null;
        try
        {                                    
            String query = "SELECT NAME  FROM CVN_UNIVERSITY_STUDY_TYPE  " + 
                           "WHERE ID_UNIVERSITY_STUDY_TYPE = " + id +
                           "";    
            
            statement = cnn.createStatement();
            rs = statement.executeQuery(query);
            if(rs.next())
            {
                name = rs.getString(1);
            }
        
        }  
        catch (SQLException ex)                             
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
        } 
        finally
        {
            if(statement != null) 
            try 
            {                        
                statement.close();
            } 
            catch (SQLException ex) {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }

            if(rs != null) 
            try 
            {
                rs.close();
            } 
            catch (SQLException ex) 
            {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }     

            statement = null;
            rs = null;
        }  
        
        return name;
    }
    
    /*
     * Canonice the string (no accentuation and lower case)
     * @param input
     * @return 
     */
    /**
     *
     * @param input
     * @return
     */
    public static String get_canonical_name(String input) 
    {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ.:-/()";
        
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC    ()";
        
        String output = input;
        
        for (int i=0; i<original.length(); i++) 
        {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        
        output = output.toLowerCase();
        
        return output;
    }
    
}
