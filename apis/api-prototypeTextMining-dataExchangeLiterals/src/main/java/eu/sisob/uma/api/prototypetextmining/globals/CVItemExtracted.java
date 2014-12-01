
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

package eu.sisob.uma.api.prototypetextmining.globals;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class CVItemExtracted 
{
    	public static class AgentIdentification
        {            
            public static final String FirstFamilyName = "FirstFamilyName";
            public static final String SecondFamilyName = "SecondFamilyName";
            public static final String GivenName = "GivenName";
            public static final String Gender = "Gender";
            public static final String Nationality = "Nationality";
            public static final String BirthCity = "BirthCity";
            public static final String BirthRegion = "BirthRegion";
            public static final String BirthCountry = "BirthCountry";
            public static final String BirthDateDayMonthYear = "BirthDateDayMonthYear";
            public static final String BirthDateMonthYear = "BirthDateMonthYear";
            public static final String BirthDateYear = "BirthDateYear";
            public static final String Email = "Email";
            public static final String Phone = "Phone";
        }
		
        public static class ProfessionalActivity
        {
            public static final String Title_name = "Title_name";
            public static final String Position = "Position";
            public static final String PositionNumber = "PositionNumber";
            public static final String DateInit_dayMonthYear = "DateInit_dayMonthYear";
            public static final String DateInit_monthYear = "DateInit_monthYear";
            public static final String DateInit_year = "DateInit_year";
            public static final String DateInit_duration = "DateInit_duration";
            
            public static final String Entity1_entityName = "Entity1_entityName";            
            public static final String Entity2_entityName = "Entity2_entityName";            
            public static final String Entity3_entityName = "Entity3_entityName";
            
            public static final String Entity1_type = "Entity1_type";
            public static final String Entity2_type = "Entity2_type";
            public static final String Entity3_type = "Entity3_type";
            
            public static final String PlaceJob_city = "PlaceJob_city";
            public static final String PlaceJob_regionName = "PlaceJob_regionName";
            public static final String PlaceJob_regionCode = "PlaceJob_regionCode";
            public static final String PlaceJob_countryName = "PlaceJob_countryName";
            public static final String PlaceJob_countryCode = "PlaceJob_countryCode";
        }
		
        public static class AccreditedUniversityStudies
        {
            //public static final String xxx = "";
            public static final String Title_name = "Title_name";
            public static final String Position = "Position";
            public static final String Hons = "Hons";
            public static final String DateTitle_dayMonthYear = "DateTitle_dayMonthYear";
            public static final String DateTitle_MonthYear = "DateTitle_MonthYear";
            public static final String DateTitle_year = "DateTitle_year";     
            public static final String Entity1_entityName = "Entity1_entityName";
            public static final String Entity2_entityName = "Entity2_entityName";
            public static final String Entity3_entityName = "Entity3_entityName";    
            
            public static final String Entity1_type = "Entity1_type";
            public static final String Entity2_type = "Entity2_type";
            public static final String Entity3_type = "Entity3_type";            
            
            public static final String PlaceTitle_city = "PlaceTitle_city";
            public static final String PlaceTitle_regionName = "PlaceTitle_regionName";
            public static final String PlaceTitle_regionCode = "PlaceTitle_regionCode";
            public static final String PlaceTitle_countryName = "PlaceTitle_countryName";
            public static final String PlaceTitle_countryCode = "PlaceTitle_countryCode";
    }
}
