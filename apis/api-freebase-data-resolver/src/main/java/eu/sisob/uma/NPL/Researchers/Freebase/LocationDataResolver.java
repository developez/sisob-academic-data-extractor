
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public abstract class LocationDataResolver {
    
    public static class LocationTuple
    {        
        public String city;
        public String region;
        public String region_code;
        public String country_code;
        public String country;
        
        public LocationTuple(String city, String region, String region_code, String country, String country_code)
        {            
            this.city = city;
            this.region = region;
            this.region_code = region_code;            
            this.country = country;        
            this.country_code = country_code;
        }
        
        public String getByName(String name)
        {
            if(name.equals("city")) {
                return city;
            } else if(name.equals("region")) {            
                return region;
            } else if(name.equals("region_code")) {            
                return region_code;
            } else if(name.equals("country_code")) {            
                return country_code;
            } else if(name.equals("country")) {            
                return country;
            } else {
                return "";
            }
        }
        
        @Override
        public String toString()
        {
            return city + ", " + region + ", " + region_code + ", " + country + ", " + country_code;
        }
    }
    
    public static class LocationTupleWithEntity extends LocationTuple
    {
        public String canonic_name;
        public String mid;
        public String type;
        
        public LocationTupleWithEntity(LocationTuple location, String canonic_name, String mid, String type)
        { 
            super(location.city, location.region, location.region_code, location.country, location.country_code);
            this.canonic_name = canonic_name;
            this.mid = mid;
            this.type = type;
        }
        
        public LocationTupleWithEntity(String city, String region, String region_code, String country, String country_code, String canonic_name, String mid, String type)
        { 
            super(city, region, region_code, country, country_code);
            this.canonic_name = canonic_name;
            this.mid = mid;
            this.type = type;
        }
        
        public String getByName(String name)
        {
            if(name.equals("canonic_name")) {
                return canonic_name;
            } else if(name.equals("mid")) {            
                return mid;
            } else if(name.equals("type")) {            
                return type;
            } else {
                return super.getByName(name);
            }
        }
        
        @Override
        public String toString()
        {
            return canonic_name + ", " + mid + ", " + type + ", " + super.toString();
        }
    }
    
    public boolean verbose;
    public LocationDataResolver(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    public abstract LocationTupleWithEntity resolve(String name);
    
}
