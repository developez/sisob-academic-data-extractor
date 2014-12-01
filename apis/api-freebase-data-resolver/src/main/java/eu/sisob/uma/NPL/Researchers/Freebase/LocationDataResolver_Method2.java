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

package eu.sisob.uma.NPL.Researchers.Freebase;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.sisob.uma.api.h2dbpool.H2DBCredentials;
import eu.sisob.uma.api.h2dbpool.H2DBPool;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/** 
 *  Data resolver Method 2:
 * 
 *      - Take the canonic name and the mid from Freebase using API Search.
 *      - Take the location from Freebase. The location comes in multiple tuples (Malaga, Andalusia, España), (Malaga, España, EU), ...
 *      - Take the tuples and try to reduce to one possibility using a city-region-country database using H2 DB Tecnology.
 *      - Once time the tuples are reduced to one possibility, we save the solution into a cache to reuse the solution.
 * 
 * NOTES:
 * 
 *      Cache table:
 * 
 *          EXPRESSION, TYPE, FREEBASE_NAME, FREEBASE_MID, CITY, REGION, REGION_CODE, COUNTRY, COUNTRY_CODE
 * 
 *          The TYPE file indicates the kind of the entity, this value is used by freebase, for example "/education/university" (a internal class to store the values is FREEBASE_TYPES)
 * 
 * TODO: 
 * 
 *      At the beginning, to search the location in Freebase, the heuristic uses only the expression and no keywords.
 *      (f.e: University of Madrid could has keywords Spain or EEUU), this case is not referred.
 * 
 */
public class LocationDataResolver_Method2 extends LocationDataResolver
{               
    /*     
     * 
     */
    public static class FREEBASE_TYPES
    {
        public static final String EDUCATION__UNIVERSITY = "/education/university";        
    }
    
    H2DBPool dbpool;
            
    public LocationDataResolver_Method2(boolean verbose, H2DBCredentials credentials)
    {
        super(verbose);
        try {
            dbpool = new H2DBPool(credentials);
        } catch (SQLException ex) {
            dbpool = null;
        }
    }
            
    /*
     * METHOD 2
     * 
     *
     */
    
     /*
     * Obtain the location from Freebase and H2 Database tablets of locations (city, region, country with iso codes)
     * 
     * - check "query" local variable to see the query format.     
     * - check "getLocationsFromJsonObject1" for see the format parsed.
     * @return 
     *      cityRegionCountry : String with this format: city, region, country OR city,,country OR city,, ...
     *
     * @param mid - Mid in Freebase format of Entity
     */
    private locationSet getLocationFromMid(String mid)
    {   
         locationSet location = null;
         
         String query = "[{" + 
                          "\"name\":[]," +                          
                          "\"mid\":\"" + mid + "\"," +                                                 
                          "\"/location/location/containedby\":" + 
                          "[{" + 
                            "\"id\": null," + 
                            "\"name\": null," +
                            "\"/common/topic/alias\":[]," +
                            "\"type\": \"/location/citytown\"," +  
                            "\"/location/location/containedby\":" +  
                            "[{" +  
                             "\"id\": null," +  
                             "\"name\": null," + 
                             "\"/common/topic/alias\":[]," +
                             "\"type\": \"/location/administrative_division\"," +  
                             "\"/location/location/containedby\":" +  
                             "[{" +  
                               "\"id\": null," +  
                               "\"name\": null," + 
                               "\"/common/topic/alias\":[]," +
                               "\"type\": \"/location/country\"" +  
                             "}]" +  
                            "}]" +  
                          "}]" + 
                        "}]";           
        
        JsonArray results = Utils.getResultsFromMQLReadCall(query);        
        
        if(results != null)
        { 
            //We assume one result
            if(results.size() == 0)
            {
            }
            else if(results.size() == 1)
            {
                JsonObject result = results.iterator().next().getAsJsonObject();
                location = getLocationsFromJsonObject1(result);
            }                
            else
            {
                int k = 0;
                k = 0;
            }
        }        
        
        
        return location;
    }


    

    

    /**
     * Object to store all location of entity and its alias of each location (city, region o country)
     */
    public class locationSet
    {
        /**
         * 
         */
        public Map<String, String[]> alias;
        /**
         * 
         */
        public List<String[]> cityRegionCountryList;                
        
        public LocationTuple location_solved;
        
        /**
         * 
         */
        public locationSet()
        {
            alias = new HashMap<String, String[]>();
            cityRegionCountryList = new ArrayList<String[]>();
            location_solved = null;
        }
    }
    
    /**
     * Fill a locationSet objet (fill alias of locations, and fill all possible row type city, region, country
     * 
     * Format of the object to parse
     * 
     * "result": [    
     *      {
     *      "/location/location/containedby": [
     *        {
     *          "/location/location/containedby": [
     *            {
     *              "/location/location/containedby": [
     *                {
     *                  "type": "/location/country",
     *                  "id": "/en/united_states",
     *                  "/common/topic/alias": [
     *                    "America",
     *                    "U.S.",
     *                    "USA",
     *                    "United States",
     *                    "United States of America",
     *                    "US",
     *                    "the states"
     *                  ],
     *                  "name": "United States of America"
     *                }
     *              ],
     *              "type": "/location/administrative_division",
     *              "id": "/en/massachusetts",
     *              "/common/topic/alias": [
     *                "Mass.",
     *                "Bay State",
     *                "Commonwealth of Massachusetts",
     *                "MA",
     *                "Mass"
     *              ],
     *              "name": "Massachusetts"
     *            }
     *          ],
     *          "type": "/location/citytown",
     *          "id": "/en/boston_massachusetts",
     *          "/common/topic/alias": [
     *            "Beantown",
     *            "Boston, Massachusetts",
     *            "City on the Hill",
     *            "The Hub",
     *            "The Hub of the Universe",
     *            "Athens of America",
     *            "Suffolk County / Boston city",
     *            "Boston [Mass."
     *          ],
     *          "name": "Boston"
     *        },
     *        ...
     *      }       
     * 
     * @param result
     * @return
     */
    private locationSet getLocationsFromJsonObject1(JsonObject result)
    {        
        locationSet locations_set = new locationSet();
        
        JsonArray  countainers_cities = result.getAsJsonObject().getAsJsonArray("/location/location/containedby");                    
                    
        if(countainers_cities != null)
        {                        
            for (JsonElement countainer_cities : countainers_cities) 
            {
                JsonArray  countainers_regions = countainer_cities.getAsJsonObject().getAsJsonArray("/location/location/containedby");                    

                if(countainers_regions != null)
                {                        
                    for (JsonElement countainer_regions : countainers_regions) 
                    {
                        JsonArray countainers_countries = countainer_regions.getAsJsonObject().getAsJsonArray("/location/location/containedby");                    

                        if(countainers_countries != null)
                        {                        
                            for (JsonElement countainer_countries : countainers_countries)
                            {
                                
                                String city = countainer_cities.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                                String region = countainer_regions.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                                String country = countainer_countries.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                                
                                String[] cityRegionCountry  = new String[] {
                                                            city,
                                                            region,
                                                            country
                                                          };
                                
                                if(!locations_set.alias.containsKey(city))
                                {
                                    
                                    Iterator<JsonElement> it = countainer_cities.getAsJsonObject().getAsJsonArray("/common/topic/alias").iterator();                                                                                                        
                                    ArrayList<String> aux = new ArrayList<String>();
                                    while(it.hasNext())
                                    {
                                        String s = it.next().getAsString();
                                        aux.add(s);
                                    }
                                    locations_set.alias.put(city, aux.toArray(new String[aux.size()]));
                                }
                                
                                if(!locations_set.alias.containsKey(region))
                                {
                                    
                                    Iterator<JsonElement> it = countainer_regions.getAsJsonObject().getAsJsonArray("/common/topic/alias").iterator();                                                                                                        
                                    ArrayList<String> aux = new ArrayList<String>();
                                    while(it.hasNext())
                                    {
                                        String s = it.next().getAsString();
                                        aux.add(s);
                                    }
                                    locations_set.alias.put(region, aux.toArray(new String[aux.size()]));
                                }
                                
                                if(!locations_set.alias.containsKey(country))
                                {                                    
                                    Iterator<JsonElement> it = countainer_countries.getAsJsonObject().getAsJsonArray("/common/topic/alias").iterator();                                                                                                        
                                    ArrayList<String> aux = new ArrayList<String>();
                                    while(it.hasNext())
                                    {
                                        String s = it.next().getAsString();
                                        aux.add(s);
                                    }
                                    locations_set.alias.put(country, aux.toArray(new String[aux.size()]));
                                }
                                
                                locations_set.cityRegionCountryList.add(cityRegionCountry);                                   
                            }
                        }

                    }
                }

            }
        }
        
        return locations_set;
    }
    
     /*
     * Function that try to resolve the location (this mean, to obtain a standar ISO city name, region code and country code)
     * from a array that containts tree values with name of city, name of region and name of country.
     * 
     * The function uses tree data table stored in H2 database. The tables are:
     *  - ISO3661_COUNTRY           => standar iso (ISO3661) codes and names of countries
     *  - ISO3661_FIPS_10_4_CITY    => standar iso (FIPS_10_4) codes and names of cities
     *  - ISO3661_FIPS_10_4_REGION  => standar iso (ISO3661 for US and CANADA, FIPS_10_4 the rest) codes and names of regions    
     * 
     * The function also uses a map object that contains alias of some location (for example: United States in key has US, U.S, United States of América, values in value.
     * 
     * TODO: Try to search first ISO code in alias in order to locate region code and country code
     * 
     * @param String[] cityRegionCountry - location to search
     * @param Map<String, String[]> alias - map with alias of location
     */
    private boolean resolveLocationWithCityRegionGive(locationSet location_set, int index, Connection cnn, boolean verbose)
    {
        boolean sucess = false;                
        String city = location_set.cityRegionCountryList.get(index)[0];
        String region = location_set.cityRegionCountryList.get(index)[1];
        String country = location_set.cityRegionCountryList.get(index)[2];
        
        if(!city.equals(""))
        {
            boolean finish_country = false;
            int alias_index_country = 0;
            String country_param = country;            
            String country_code = "";                            
            while(!finish_country)
            {             
                Statement statement = null;
                ResultSet rs = null;
                try
                {                                    
                    String query = "SELECT COUNTRY_CODE FROM ISO3661_COUNTRY where NAME = '" + country_param + "'";
                    if(verbose) ProjectLogger.LOGGER.info("\tQuery for country: " + query);
                    statement = cnn.createStatement();
                    rs = statement.executeQuery(query);
                    if(rs.next()) 
                    {
                        country_code = rs.getString("COUNTRY_CODE");                                    
                        finish_country = true;
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
                
                if(!finish_country)
                {
                    if(alias_index_country < location_set.alias.get(country).length)
                    {
                        country_param = location_set.alias.get(country)[alias_index_country];
                        alias_index_country++;
                    }
                    else
                    {
                        finish_country = true;
                    }
                }
            }

            if(country_code != "")
            {                                
                boolean finish_city = false;
                boolean searching_with_city_alias = false;
                int alias_index_city = 0;                
                String city_param = getCanonicalName2SearchInDb(city);
                List<String[]> city_results = new ArrayList<String[]>();
                
                while(!finish_city)
                {             
                    city_results = new ArrayList<String[]>();
                    Statement statement = null;
                    ResultSet rs = null;
                    
                    try 
                    {                            
                        String query = "SELECT COUNTRY_CODE, REGION_CODE,ISO_8859_1_CITY_NAME, ACCENT_CITY_NAME " + 
                                       "FROM ISO3661_FIPS_10_4_CITY " + 
                                       " where ISO_8859_1_CITY_NAME = '" + city_param + "' and COUNTRY_CODE = '" + country_code + "'";
                        if(verbose) ProjectLogger.LOGGER.info("\tQuery for city: " + query);
                        statement = cnn.createStatement();
                        rs = statement.executeQuery(query);

                        while (rs.next()) 
                        {
                            finish_city = true;
                            city_results.add(new String[]{ rs.getString("COUNTRY_CODE") , 
                                                           rs.getString("REGION_CODE"),
                                                           rs.getString("ISO_8859_1_CITY_NAME"),
                                                           rs.getString("ACCENT_CITY_NAME")
                                                }
                                    );                                        
                            if(verbose) ProjectLogger.LOGGER.info("\t\t" + rs.getString("COUNTRY_CODE") + ", " + rs.getString("ISO_8859_1_CITY_NAME") + ", " + rs.getString("ACCENT_CITY_NAME") + ", " + rs.getString("REGION_CODE"));           
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

                    if(city_results.size() > 1)
                    {
                        if(!region.equals(""))
                        {
                            String region_code = "";                        
                            {
                                {
                                    boolean finish_region = false;
                                    int alias_index_region = 0;
                                    String region_param = region;   
                                    List<String[]> region_results = new ArrayList<String[]>();

                                    while(!finish_region)
                                    {   
                                        statement = null;
                                        rs = null;

                                        try
                                        {
                                            String query = "SELECT REGION_CODE FROM ISO3661_FIPS_10_4_REGION where NAME = '" + region_param + "' AND COUNTRY_CODE = '" + country_code + "'";
                                            if(verbose) ProjectLogger.LOGGER.info("\tQuery for region: " + query);                                                    
                                            statement = cnn.createStatement();
                                            rs = statement.executeQuery(query);
                                            if(rs.next()) 
                                            {
                                                region_code = rs.getString("REGION_CODE");
                                                finish_region = true;
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

                                        if(!finish_region)
                                        {
                                            if(alias_index_region < location_set.alias.get(region).length)
                                            {
                                                region_param = location_set.alias.get(region)[alias_index_region];
                                                alias_index_region++;
                                            }
                                            else
                                            {
                                                finish_region = true;
                                            }
                                        }
                                    }
                                }
                            }

                            if(region_code != "")
                            {       
                                boolean find = false;
                                for(String city_result[] : city_results)
                                {
                                    if(city_result[1].equals(region_code))
                                    {
                                        find = true;
                                        break;
                                    }
                                }

                                if(find)
                                {
                                    if(verbose) ProjectLogger.LOGGER.info("\t\t => FOUND [" + city + "," + region_code + "," + country_code + "]");   
                                    
                                    location_set.location_solved = new LocationTuple(city, region, region_code, country, country_code);                                    
                                    
                                    sucess = true;
                                }
                                else
                                {
                                    if(verbose) ProjectLogger.LOGGER.info(" => REGION FAIL");
                                    finish_city = false;
                                }
                            }
                            else
                            {
                                if(verbose) ProjectLogger.LOGGER.info(" => REGION FAIL");
                                finish_city = false;
                            }
                        }
                        else
                        {
                            String region_code = city_results.get(0)[1];                                        
                            if(verbose) ProjectLogger.LOGGER.info("\t\t => FOUND [" + city + "," + region_code + "," + country_code + "]"); 
                            location_set.location_solved = new LocationTuple(city, region, region_code, country, country_code);                            
                            finish_city = true;
                            sucess = true;                            
                        }
                    }
                    else if(city_results.size() == 1)
                    {      
                        //In the case, that we are detected city with city alias, we need check the region
                        if(searching_with_city_alias)
                        {
                            String region_code = "";                        
                            {
                                {
                                    boolean finish_region = false;
                                    int alias_index_region = 0;
                                    String region_param = region;   
                                    List<String[]> region_results = new ArrayList<String[]>();

                                    while(!finish_region)
                                    {   
                                        statement = null;
                                        rs = null;

                                        try
                                        {
                                            String query = "SELECT REGION_CODE FROM ISO3661_FIPS_10_4_REGION where NAME = '" + region + "' AND COUNTRY_CODE = '" + country_code + "'";
                                            if(verbose) ProjectLogger.LOGGER.info("\tQuery for region: " + query);                                                    
                                            statement = cnn.createStatement();
                                            rs = statement.executeQuery(query);
                                            if(rs.next()) 
                                            {
                                                region_code = rs.getString("REGION_CODE");
                                                finish_region = true;
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

                                        if(!finish_region)
                                        {
                                            if(alias_index_region < location_set.alias.get(region).length)
                                            {
                                                region_param = location_set.alias.get(region)[alias_index_region];
                                                alias_index_region++;
                                            }
                                            else
                                            {
                                                finish_region = true;
                                            }
                                        }
                                    }
                                }
                            }

                            if(region_code != "")
                            {       
                                boolean find = false;
                                for(String city_result[] : city_results)
                                {
                                    if(city_result[1].equals(region_code))
                                    {
                                        find = true;
                                        break;
                                    }
                                }

                                if(find)
                                {
                                    if(verbose) ProjectLogger.LOGGER.info("\t\t => FOUND [" + city + "," + region_code + "," + country_code + "]");   
                                    location_set.location_solved = new LocationTuple(city, region, region_code, country, country_code);                                           
                                    sucess = true;
                                }
                                else
                                {
                                    if(verbose) ProjectLogger.LOGGER.info(" => REGION FAIL");
                                    finish_city = false;
                                }
                            }
                            else
                            {
                                if(verbose) ProjectLogger.LOGGER.info(" => REGION FAIL");
                                finish_city = false;
                            }                            
                        }   
                        else
                        {
                            String region_code = city_results.get(0)[1];                                        
                            if(verbose) ProjectLogger.LOGGER.info("\t\t => FOUND [" + city + "," + region_code + "," + country_code + "]"); 
                            location_set.location_solved = new LocationTuple(city, region, region_code, country, country_code);                                                    
                            finish_city = true;
                            sucess = true;                                        
                        }
                    }   
                    else
                    {
                        if(verbose) ProjectLogger.LOGGER.info(" => CITY FAIL ");                        
                    }
                    
                    if(!finish_city)
                    {
                        if(alias_index_city < location_set.alias.get(city).length)
                        {                            
                            city_param = getCanonicalName2SearchInDb(location_set.alias.get(city)[alias_index_city]);
                            alias_index_city++;
                            
                            searching_with_city_alias = true;                            
                        }
                        else
                        {
                            finish_city = true;
                        }
                    }        
                }
                
            }
            else
            {
                if(verbose) ProjectLogger.LOGGER.info(" => COUNTRY FAIL ");                
            }      
        }
        
        return sucess;
    }
    
    /*
     * Canonice the string (no accentuation and lower case)
     * @param input
     * @return 
     */
    private String getCanonicalName2SearchInDb(String input) 
    {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        
        String output = input;
        
        for (int i=0; i<original.length(); i++) 
        {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        
        output = output.toLowerCase();
        
        return output;
    }
    
    public Connection openConnection()
    {
        Connection conn = null;
        try {
            conn = this.dbpool.getConnection();        
        } catch (ClassNotFoundException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());
        } catch (SQLException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());
        }
        
        return conn;        
    }
    
    public void closeConnection(Connection conn)
    {
        //Close
        try {
            conn.close();                
        } catch (SQLException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());
        } finally {
            conn = null;
        }      
    }
    
    /*
     * END METHOD 2     
     */
    @Override
    public LocationTupleWithEntity resolve(String expression) 
    {
         //open database
        Connection conn = this.openConnection();
        
        LocationTupleWithEntity ltwe = this.resolve(expression, conn);
        
        this.closeConnection(conn);
        
        return ltwe;
    }
    
    
    public LocationTupleWithEntity resolve(String expression, Connection conn) 
    {   
        LocationTupleWithEntity location = null;
        expression = expression.replace("  ", " ").replace(" \t", " ").replace("\t", " ").trim();
        
       
        
        if(conn != null)
        {
            //Check if exist in cache
            location = getLocationFromCache(conn, expression, FREEBASE_TYPES.EDUCATION__UNIVERSITY);

            if(location == null)
            {
                //
                String freebase_type = "&type=/education/university";
                
                String[] info = Utils.getCanonicalInfoFromEntity(expression, "AIzaSyBwYBI9bKtHDKRLfCjCx1p78-zsbGldD7Y", freebase_type);                
                
                String freebase_name = info[0];                
                String freebase_mid = info[1];
                
                if(freebase_mid != null)
                {
                    locationSet location_set = getLocationFromMid(freebase_mid);                
                    boolean resolved = false;

                    if(location_set != null)
                    {
                        List<String[]> cityRegionCountryList = location_set.cityRegionCountryList;                    

                        int i = 0;
                        while(i < location_set.cityRegionCountryList.size()) {                            
                        
                            if(verbose) ProjectLogger.LOGGER.info("\tRESOLVING: " + Arrays.toString(cityRegionCountryList.get(i)));
                            resolved = resolveLocationWithCityRegionGive(location_set, i, conn, verbose);                                                                                                                   
                            if(resolved) {                                 
                                break;
                            }
                            i++;
                        }

                        // Update db cache
                        if(resolved)
                        {
                            location = new LocationTupleWithEntity(location_set.location_solved, freebase_name, freebase_mid, freebase_type);
                            
                            //UPDATE CACHE  
                            if(verbose) ProjectLogger.LOGGER.info("\tLocation '" + expression + "' solved => '" + freebase_name + "', " + location_set.location_solved);    
                            
                            saveLocationInChache(conn, expression, FREEBASE_TYPES.EDUCATION__UNIVERSITY, freebase_name, freebase_mid, location_set.location_solved);                            
                        }
                        else
                        {
                            if(verbose) ProjectLogger.LOGGER.info("\tLocation '" + expression + "' does not solved");
                            location = new LocationTupleWithEntity(new LocationTuple("","","","",""), 
                                                                   freebase_name, 
                                                                   freebase_mid, 
                                                                   FREEBASE_TYPES.EDUCATION__UNIVERSITY);  
                            saveLocationInChache(conn, expression, FREEBASE_TYPES.EDUCATION__UNIVERSITY, freebase_name, freebase_mid, new LocationTuple("","","","",""));                            
                        }
                    }
                    else
                    {
                        if(verbose) ProjectLogger.LOGGER.info("\tLocation '" + expression + "' not found in freebase but yes canonical info");    
                        location = new LocationTupleWithEntity(new LocationTuple("","","","",""), 
                                                               freebase_name, 
                                                               freebase_mid, 
                                                               FREEBASE_TYPES.EDUCATION__UNIVERSITY);           
                        saveLocationInChache(conn, expression, FREEBASE_TYPES.EDUCATION__UNIVERSITY, freebase_name, freebase_mid, new LocationTuple("","","","",""));  
                    }
                }
                else
                {
                    if(verbose) ProjectLogger.LOGGER.info("\tLocation '" + expression + "' not found in freebase");                                         
                }
                
            }                
            else {
                if(verbose) ProjectLogger.LOGGER.info("\tLocation '" + expression + "' found in cache");    
            }           
            
        }
        
        return location;
    }

    private LocationTupleWithEntity getLocationFromCache(Connection conn, String name, String type) {
        
        LocationTupleWithEntity location = null;
        String query_select = "SELECT FREEBASE_NAME, CITY, REGION_NAME, REGION_CODE, COUNTRY_NAME, COUNTRY_CODE FROM LOCATION_CACHE WHERE EXPRESSION = ? AND FREEBASE_TYPE = ?";
        PreparedStatement pst = null;
        try {
            pst = conn.prepareStatement(query_select);
            pst.setString(1, name);
            pst.setString(2, type);
            ResultSet rs = pst.executeQuery();
            if(rs.next())
            {
                //FIXME GET LOCATION
                location = new LocationTupleWithEntity(rs.getString("CITY"), rs.getString("REGION_NAME"), rs.getString("REGION_CODE"), rs.getString("COUNTRY_NAME"), rs.getString("COUNTRY_CODE"), 
                                                       rs.getString("FREEBASE_NAME"), "", "");                  
            }            
        } catch (SQLException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());        
            location = null;
        } catch (Exception ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());            
            location = null;
        }   
        finally {
            try {
                pst.close();
            } catch (SQLException ex) {
                ProjectLogger.LOGGER.error(ex.getMessage());            
            }
            pst = null;
        }

        return location;
    }

    private boolean saveLocationInChache(Connection conn, String expression, String type, String canonic_name, String mid, LocationTuple location_solved) 
    {
        boolean success = false;
        String query = "INSERT INTO location_cache (EXPRESSION, KEYWORDS, FREEBASE_TYPE, FREEBASE_NAME, FREEBASE_MID, CITY, REGION_NAME, REGION_CODE, COUNTRY_NAME, COUNTRY_CODE) VALUES " + 
                                           "(?,?,?,?,?,?,?,?,?,?)"; 
        PreparedStatement pstmt = null;
        try {

            pstmt = conn.prepareStatement(query);

            pstmt.setString(1, expression);                                
            pstmt.setString(2, "");
            pstmt.setString(3, type);
            pstmt.setString(4, canonic_name);
            pstmt.setString(5, mid);
            pstmt.setString(6, location_solved.city);
            pstmt.setString(7, location_solved.region);
            pstmt.setString(8, location_solved.region_code);
            pstmt.setString(9, location_solved.country);
            pstmt.setString(10, location_solved.country_code);                                                  
                
            pstmt.executeUpdate();
            
            if(verbose) ProjectLogger.LOGGER.info("\t'" + expression + "' saved to cache with '" + canonic_name + "', " + location_solved);    

        } catch (SQLException ex) {
            ProjectLogger.LOGGER.error(ex.getMessage());
        }
        finally
        {
            try {
                pstmt.close();
            } catch (SQLException ex) {
                ProjectLogger.LOGGER.error(ex.getMessage());            
            }
            pstmt = null;
        }
        
        return success;

        //return success;
    }           
   
}
