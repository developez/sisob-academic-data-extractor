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

package eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class XMLTags 
{
    public final static String INSTITUTION = "Institution";
    public final static String INSTITUTION_NAME = "InstitutionName";
    public final static String INSTITUTION_WEBADDRESS = "WebAddress";
    
    public final static String UNIT_OF_ASSESSMENT = "UnitOfAssessment";
        public final static String UNIT_OF_ASSESSMENT_DESCRIPTION = "UnitOfAssessment_Description";
        public final static String DEPARTMENT_WEB_ADDRESS = "DepartamentWebAddress";
        
    public final static String RESEARCHGROUP = "ResearchGroup";
    public final static String RESEARCHGROUP_DESCRIPTION = "ResearchGroupDescription";
    
    public final static String RESEARCHER = "Researcher";
        public final static String RESEARCHER_STAFFIDENTIFIER = "StaffIdentifier";
        public final static String RESEARCHER_NAME = "Name";
        public final static String RESEARCHER_FIRSTNAME = "FirstName";
        public final static String RESEARCHER_LASTNAME = "LastName";
        public final static String RESEARCHER_INITIALS = "Initials";
        public final static String RESEARCHER_WEB_ADDRESS = "ResearcherWebAddress";       
            public final static String RESEARCHER_WEB_ADDRESS_ATTR_TYPE = "T";       
            public final static String RESEARCHER_WEB_ADDRESS_ATTR_EXT = "E";           
            
            //This values are used in uma researcher crawler            
            public final static String RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_PUB = "PUB";                              
            public final static String RESEARCHER_WEB_ADDRESS_ATTR_TYPE_VALUE_DEFAULT_CV = "CV";
            
            public final static String RESEARCHER_WEB_ADDRESS_ATTR_EXT_VALUE_DEFAULT_HTML = "html";
}
