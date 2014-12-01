
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
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class Utils {
    
    /*
     * Return JsonObject of a REST call of url
     * 
     * @param url   
     * @return  JsonObject of the result
     * @throws IOException
     */
    public static JsonObject doRESTCall(String url) throws IOException
    {
        HttpClient httpclient = new DefaultHttpClient();   
        HttpResponse response = httpclient.execute(new HttpGet(url));      

        JsonParser parser = new JsonParser();
        JsonObject json_data = (JsonObject)parser.parse(EntityUtils.toString(response.getEntity()));
        
        return json_data;
    }
    
      
    
    /*
     * Get the results JsonArray of Freebase API Search Call   
     * 
     * @param query     query
     * @param key       API key
     * @param params        extra params to be added to the call
     * @return
     */
    public static JsonArray getResultsFromAPISearchCall(String query, String key, String params)
    {
        String service_url = "https://www.googleapis.com/freebase/v1/search";                               
        String url = "";        
        JsonObject json_data = null;
        try             
        {
            url = service_url + "?query=" + URLEncoder.encode(query, "UTF-8")
                                + params 
                                + "&key=" + key + 
                                  "&limit=1";     
      
            json_data = doRESTCall(url);            
        }
        catch (UnsupportedEncodingException ex) 
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
            json_data = null;
        }
        catch (IOException ex) 
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
            json_data = null;
        }
        
        JsonArray results = null;
        if(json_data != null)
        {
            results = (JsonArray)json_data.get("result");
        }
        
        return results;
    }
   
    /**
     * Get canonical name in Freebase of expression given 
     * @param expression
     * @param key
     * @param params        extra params to be added to the call
     * @return
     * @throws IOException
     */
    public static String[] getCanonicalInfoFromEntity(String expression, String key, String params) 
    {   
        JsonArray results = getResultsFromAPISearchCall(expression, key, params);
        String[] info = new String[2];
        if(results != null)
        {
            for (Object planet : results) 
            {
                info[0] = ((JsonObject)planet).get("name").getAsJsonPrimitive().getAsString();
                info[1] = ((JsonObject)planet).get("mid").getAsJsonPrimitive().getAsString();
                break;
            }
        }      

        return info;        
    }
    
    /**
     * Get entity mid in Freebase of expression given 
     * @param expression
     * @param key
     * @param params        extra params to be added to the call
     * @return
     * @throws IOException
     */
//    public static String getCanonicalMidFromEntity(String expression, String key, String params)
//    {   
//        JsonArray results = getResultsFromAPISearchCall(expression, key, params);
//        String name = "";
//        if(results != null)
//        {
//            for (Object planet : results) 
//            {
//                name = ((JsonObject)planet).get("mid").getAsJsonPrimitive().getAsString();
//                break;
//            }
//        }      
//
//        return name;        
//    }
        
     /**
     * Get the results JsonArray of Freebase MQL Read API call
     * 
     * @param query
     * @return
     */
    public static JsonArray getResultsFromMQLReadCall(String query)
    {
        String service_url = "https://www.googleapis.com/freebase/v1/mqlread";                               
        String url;        
        JsonObject json_data = null;
        try             
        {
            url = service_url    + "?query=" +  URLEncoder.encode(query, "UTF-8")
                                 + "&key=AIzaSyBwYBI9bKtHDKRLfCjCx1p78-zsbGldD7Y";            
            json_data = doRESTCall(url);            
        }
        catch (UnsupportedEncodingException ex) 
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
            json_data = null;
        }
        catch (IOException ex) 
        {
            ProjectLogger.LOGGER.error(ex.getMessage());
            json_data = null;
        }
        
        JsonArray results = null;
        if(json_data != null)
        {
            results = (JsonArray)json_data.get("result");
        }
        
        return results;
    }   
}
