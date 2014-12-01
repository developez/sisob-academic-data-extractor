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
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * Hello world!
 *
 */
public class LocationDataResolver_Method1 extends LocationDataResolver
{   
    /*
     * METHOD 1
     * Get location from freebase without sort the locations
     *
     */
    
    /**
     * Get the locations from a entity
     * 
     *  - Problem: The locations are unsorted and some times are from different location (Madrid, Spain, Madrid, EEUU)
     * 
     * @param name - canonic entity name
     * @param cities - list of cities of the entity
     * @param regions - list of regions of the entity
     * @param countries - list of countries of the entity
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void getLocationFromCanonicalName(String name, List<String> cities, List<String> regions, List<String> countries, boolean verbose) throws UnsupportedEncodingException, IOException
    {        
        
        String query = "";
        
        query = "[{\"name\":\"" + name + "\",\"type\":\"/education/university\",\"/location/location/containedby\":[{}]}]";            
        
            String service_url = "https://www.googleapis.com/freebase/v1/mqlread";        
            //feed in your array (or convert your data to an array)
        
            String url = service_url    + "?query=" +  URLEncoder.encode(query, "UTF-8")
                                        + "&key=AIzaSyBwYBI9bKtHDKRLfCjCx1p78-zsbGldD7Y"
                                       ;


            HttpClient httpclient = new DefaultHttpClient();   
            HttpResponse response = httpclient.execute(new HttpGet(url));              
            

            JsonParser parser = new JsonParser();
            JsonObject json_data = (JsonObject)parser.parse(EntityUtils.toString(response.getEntity()));
            
            if(verbose) ProjectLogger.LOGGER.info("\t" + name);
            JsonArray results = (JsonArray)json_data.get("result");
            if(results != null)
            {                   
                for (JsonElement result : results) 
                {                    
                    JsonArray names_university = null;                      
                    
                    JsonArray containers = result.getAsJsonObject().getAsJsonArray("/location/location/containedby");                    
                    
                    if(containers != null)
                    {                        
                        for (JsonElement container : containers) 
                        {
                            JsonArray types = container.getAsJsonObject().getAsJsonArray("type");
                            
                            String name_location = container.getAsJsonObject().get("name").getAsString();                            
                            
                            if(types != null)
                            {
                                for (JsonElement type : types) 
                                {
                                    if(type.getAsString().equals("/location/citytown"))
                                    {   
                                        cities.add(name_location);  
                                        if(verbose) ProjectLogger.LOGGER.info("\t\t" + name_location + " => city");
                                    }
                                    
                                    if(type.getAsString().equals("/location/country"))
                                    {   
                                        countries.add(name_location);                                        
                                        if(verbose) ProjectLogger.LOGGER.info("\t\t" + name_location + " => country");
                                    }
                                    
                                    if(type.getAsString().equals("/location/administrative_division"))
                                    {                                           
                                        regions.add(name_location);   
                                        if(verbose) ProjectLogger.LOGGER.info("\t\t" + name_location + " => region");
                                    }                                    
                                }                                
                            }
                            else
                            {
                                if(verbose) ProjectLogger.LOGGER.info("\t\tNOTHING");
                            }                           
                            //String[] line2 =(id + "#" + name_alias).split("#");
                            
                        }
                    }                      
                }                   
            }           
        
    }  
    
    /*
     * Reduce to one solution the locations obtained from getLocationFromCanonicalName
     */
    private String[] reduceLocationOccs(List<String> cities, List<String> regions, List<String> countries)    
    {
        String[] cityRegionCountry = new String[3];
        List<String> cities_2 = new ArrayList<String>();
        List<String> countries_2 = new ArrayList<String>();
        
        //Eliminate regions with city names.
        for(String city : cities)
        {
            if(regions.contains(city))
            {
               regions.remove(city);
               cities_2.add(city);
            }
        }
        
        for(String city : cities)
        {
            if(!cities_2.contains(city))
            {
               cities_2.add(city);
            }
        }
        
        //Eliminate regions with city names.
        for(String country : countries)
        {
            if(regions.contains(country))
            {
               regions.remove(country);
               countries_2.add(country);
            }
        }
        
        for(String country : countries)
        {
            if(!countries_2.contains(country))
            {
               countries_2.add(country);
            }
        }
        
        if(cities_2.size() > 0)      
            cityRegionCountry[0] = cities_2.get(cities_2.size() - 1);
        
        if(regions.size() > 0)      
            cityRegionCountry[1] = regions.get(regions.size() - 1);
        
        if(countries_2.size() > 0)      
            cityRegionCountry[2] = countries_2.get(countries_2.size() - 1);
        
        return cityRegionCountry;
    }
    
    /*
     * END OF METHOD 1
     * Get location from freebase without sort the locations
     *
     */    
    
    public LocationDataResolver_Method1(boolean verbose)
    {
        super(verbose);
    }
    
   
    @Override
    public LocationTupleWithEntity resolve(String name) 
    {        
        List<String> cities = new ArrayList<String>();
        List<String> regions = new ArrayList<String>();
        List<String> countries = new ArrayList<String>();
        try 
        {   
            /* method 2 */
            String[] info = Utils.getCanonicalInfoFromEntity(name, "AIzaSyBwYBI9bKtHDKRLfCjCx1p78-zsbGldD7Y", "&type=/education/university");
            String canonical_name = info[0];
            getLocationFromCanonicalName(canonical_name, cities, regions, countries, verbose);      
        } 
        catch (IOException ex) 
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
        }    
        return null;
    }
}
