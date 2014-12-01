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

package eu.sisob.uma.api.prototypetextmining.gatedataextractor;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class Literals 
{    

    //VALUES
    public final static String STRING_OTHERS = "OTHERS";

    //ExperienceType
    public final static String ExperienceType_Teaching_Experience     = "000";
    public final static String ExperienceType_Academic_Experience     = "010";
    public final static String ExperienceType_Research_Experience     = "020";
    public final static String ExperienceType_Professional_Experience = "030";
    
    //ResultType
    public final static String ResultType_Production    = "000";
    public final static String ResultType_Activity      = "010";
    
    //FreeNorm
    public final static String FreeNorm_Free = "000";
    public final static String FreeNorm_Norm = "010";
    
    //TitleCategory
    public final static String TitleCategory_Full_Title             = "000";
    public final static String TitleCategory_Short_Title            = "010";
    public final static String TitleCategory_Key_Title              = "020";
    public final static String TitleCategory_Uniform_Title          = "030";
    public final static String TitleCategory_Approved_Title         = "040";
    public final static String TitleCategory_TranslationOfTitle     = "050";
    public final static String TitleCategory_Variations_Form_Title  = "060";
    public final static String TitleCategory_Others                 = STRING_OTHERS;
        
    //EntityType 
    public final static String EntityType_University                                    = "000";
    public final static String EntityType_Research_Institute                            = "010";
    public final static String EntityType_University_Centers_And_Structures_And_Similar = "020"; 
    public final static String EntityType_University_Department                         = "030";
    public final static String EntityType_Foundation                                    = "040";
    public final static String EntityType_Agency                                        = "050";
    public final static String EntityType_Public_Research_Organization                  = "060";
    public final static String EntityType_Other_Organization_DEPRECATED                 = "070";
    public final static String EntityType_Business_Entity                               = "080";
    public final static String EntityType_Managers_Of_The_National_Health_System        = "090";
    public final static String EntityType_Health_Institutions                           = "100";
    public final static String EntityType_Technology_Center                             = "110";
    public final static String EntityType_IAndD_Center                                  = "120";
    public final static String EntityType_Associations_And_Groups                       = "130";
    public final static String EntityType_Ciber                                         = "140";
    public final static String EntityType_Centers_Of_Innovation_And_Technology          = "150";
    public final static String EntityType_Others                                        = STRING_OTHERS;
    
    //MomentDateType
    public final static String MomentDateType_Creation      = "000";
    public final static String MomentDateType_Modification  = "010";
    public final static String MomentDateType_Application   = "020";
    public final static String MomentDateType_Concession    = "030";
    public final static String MomentDateType_Priority      = "040";
    public final static String MomentDateType_Homologation  = "050";
    public final static String MomentDateType_Others        = STRING_OTHERS;
    public final static String MomentDateType_Publication   = "SISOB_000";
    
    	
    //MomentPlaceType
    public final static String MomentPlaceType_Place                = "000";
    public final static String MomentPlaceType_Country_Extended     = "010";
    public final static String MomentPlaceType_Country_Solicitude   = "020";
    
    //QualityType
    public final static String QualityType_Expected_Impact_Rate = "000";
    public final static String QualityType_Number_Of_Citations  = "010";
    public final static String QualityType_Position_In_Ranking  = "020";
    public final static String QualityType_Textual_Rating       = "030";
    public final static String QualityType_Numerical_Rating     = "040";
    public final static String QualityType_Logical_Value        = "050";
    public final static String QualityType_Observed_Impact_Rate = "060";
    public final static String QualityType_Rate_H               = "070";
    
    //DurationType
    public final static String DurationType_Hours               = "000";
    public final static String DurationType_Years_Months_Days   = "010"; // Years/Months/Days)
    
    //EconomicDimensionType
    public final static String EconomicDimensionType_Total_Amount               = "000";
    public final static String EconomicDimensionType_Amount_Of_The_Subproject   = "010";
    public final static String EconomicDimensionType_Percentage_of_Subsidy      = "020";
    public final static String EconomicDimensionType_In_Credit                  = "030";
    public final static String EconomicDimensionType_In_Mixed                   = "040";
    public final static String EconomicDimensionType_Average_Anunual_Budget     = "050";
    public final static String EconomicDimensionType_Others                     = STRING_OTHERS;
	
    //EconomicDimensionType_CurrencyType
    public final static String ED_CurrencyType_Euro    = "EUR";
    public final static String ED_CurrencyType_Libra   = "GDP";
    public final static String ED_CurrencyType_Dolar   = "USD";

    //CollaboratorType
    public final static String CollaboratorType_Absolute   = "010";
    public final static String CollaboratorType_Relative   = "020";
    
    //CategoryType
    public final static String CategoryType_Personal_Author = "000";
    public final static String CategoryType_Institutions    = "010";
    public final static String CategoryType_Countries       = "020";
    
    //ExternalPKType
    public final static String ExternalPKType_Proyect_Code          = "000";
    public final static String ExternalPKType_ISSN                  = "010";
    public final static String ExternalPKType_ISBN                  = "020";
    public final static String ExternalPKType_DL                    = "030";
    public final static String ExternalPKType_DOI                   = "040";
    public final static String ExternalPKType_Explotation_License   = "050";
    public final static String ExternalPKType_Aplication_Number     = "060"; 
    public final static String ExternalPKType_Reference_Code        = "070"; // Patentes
    public final static String ExternalPKType_Register_Number       = "080"; // Patentes
    public final static String ExternalPKType_CVNET                 = "090";
    public final static String ExternalPKType_Group_Id              = "100";
    public final static String ExternalPKType_Group_Code            = "110";
    public final static String ExternalPKType_Others                = STRING_OTHERS;
    public final static String ExternalPKType_Patent_Number         = "SISOB_000";
    public final static String ExternalPKType_Publication_Number    = "SISOB_010";
    public final static String ExternalPKType_Priority_Number       = "SISOB_020";
    
    
    //FilterTypeType
    public final static String FilterTypeType_Participation_Type                = "000";
    public final static String FilterTypeType_Modality_Of_Industrial_Property   = "010";
    public final static String FilterTypeType_Modality_Of_Intelectual_Property  = "020";
    public final static String FilterTypeType_Validity_State                    = "030";
    public final static String FilterTypeType_Type_Of_Modality                  = "040";
    public final static String FilterTypeType_Reason_Of_Stay                    = "050";
    public final static String FilterTypeType_Collaboration_Type                = "060"; 
    public final static String FilterTypeType_Subject_Type                      = "070"; 
    public final static String FilterTypeType_Evaluation_Type                   = "080"; 
    public final static String FilterTypeType_Announcement                      = "090";  // Convocatoria
    public final static String FilterTypeType_Program_Type                      = "100";
    public final static String FilterTypeType_Management_Type                   = "110";  // Gestion
    public final static String FilterTypeType_System_Access_Type                = "120";
    public final static String FilterTypeType_Finality                          = "130";
    public final static String FilterTypeType_Current_Activity_Type             = "140";
    public final static String FilterTypeType_Labor_Situation                   = "150";
    public final static String FilterTypeType_Titulation_Type                   = "160";
    public final static String FilterTypeType_Teaching_Type                     = "170";
    public final static String FilterTypeType_Proyect_Type                      = "180";
    public final static String FilterTypeType_Group_Profile                     = "190";
    public final static String FilterTypeType_Duration_Type                     = "200";    
    public final static String FilterTypeType_Others                            = STRING_OTHERS;
    public final static String FilterTypeType_Reason_Of_Employment_Break        = "SISOB_000";
    public final static String FilterTypeType_Profesional_Activity_Position     = "SISOB_010";
    public final static String FilterTypeType_Accredited_University_Position    = "SISOB_020";
 
    
    // EditionType
    public final static String EditionType_Participation_Type                   = "000";
    public final static String EditionType_Edition_Of_Event                     = "010";
    public final static String EditionType_Citation_Of_Edition_Of_Publication   = "020";    
    public final static String EditionType_Others                               = STRING_OTHERS;
    public final static String EditionType_Academic_Cycle                       = "SISOB_000";
    
    
    // PhysicalDimensionalType
    public final static String PhysicalDimensionalType_Credits  = "000";
    public final static String PhysicalDimensionalType_Hours    = "010";
    public final static String PhysicalDimensionalType_Volume   = "020";    
    public final static String PhysicalDimensionalType_Pages    = "030"; 
    public final static String PhysicalDimensionalType_Tesis    = "040"; 
    public final static String PhysicalDimensionalType_Posdoc   = "050"; 
    public final static String PhysicalDimensionalType_Others   = STRING_OTHERS;

}
