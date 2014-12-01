
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

package eu.sisob.uma.NPL.Researchers;

import eu.sisob.uma.api.prototypetextmining.AnnotatorCollector;
import eu.sisob.uma.api.prototypetextmining.MiddleData;
import eu.sisob.uma.api.prototypetextmining.globals.CVItemExtracted;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class GateResearcherAnnCollector_Deprecated extends AnnotatorCollector 
{
    /**
     *
     * @param type
     */
    public GateResearcherAnnCollector_Deprecated(String type)
    {
        super(type);
    }
        
    /**
     *
     * @param doc
     * @param aoData
     */
    @Override
    public void collect(Object doc, MiddleData aoData)
    {
        int n_expressions = 0;
        org.dom4j.Element eOut = org.dom4j.DocumentFactory.getInstance().createElement("blockinfo");
        eOut.addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ANNOTATIONRECOLLECTING, aoData.getId_annotationrecollecting()); // aoData[MiddleData.I_INDEX_DATA_TYPE].toString());
        eOut.addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT, aoData.getId_entity()); // aoData[MiddleData.I_INDEX_DATA_ID].toString());

        gate.Document docGate = (gate.Document) doc;   
        
        HashMap<String,String> extra_data = null;
        try{
            extra_data = (HashMap<String,String>) aoData.getData_extra();
        } catch(Exception ex){
            extra_data = null;
        }
        boolean collect_expressions = true;
        if(extra_data != null){
            if(extra_data.containsKey(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_BLOCK_TYPE)){
                String block_type = extra_data.get(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_BLOCK_TYPE);
                if(block_type.equals(CVBlocks.CVBLOCK_OTHERS)){
                    collect_expressions = false;
                }
            }else{
                
            }            
        }
        
        eOut.addAttribute("URL", docGate.getSourceUrl() != null ? docGate.getSourceUrl().toString() : "");

        if(collect_expressions)
        {
            AnnotationSet annoset = docGate.getAnnotations();
            List<Annotation> anns = new ArrayList<Annotation>();

            //Expressions
            anns.addAll(annoset.get("ProfessionalActivityCurrent"));
            anns.addAll(annoset.get("ProfessionalActivityNoCurrent"));
            anns.addAll(annoset.get("AccreditedUniversityStudiesOtherPostGrade"));
            anns.addAll(annoset.get("AccreditedUniversityStudiesDegree"));
            anns.addAll(annoset.get("AccreditedUniversityStudiesPhDStudies"));

            //Collections.sort(anns, new OffsetBeginEndComparator());

            //need to bee order
            if(anns.size() > 0)
            {                    
                for(Annotation an : anns)
                {
                    String cvnItemName = an.getType();
                    org.dom4j.Element eAux = new org.dom4j.DocumentFactory().createElement(cvnItemName);
    //                        eAux.addElement("Domain").addText(gate.Utils.stringFor(docGate,
    //                                                          an.getStartNode().getOffset() > 100 ? an.getStartNode().getOffset() - 100 : an.getStartNode().getOffset(),
    //                                                          an.getEndNode().getOffset() + 100 < docGate.getContent().size() ? an.getEndNode().getOffset() + 100 :  an.getEndNode().getOffset()));
                    eAux.addAttribute("action_mode", "add");
                    eAux.addElement("Content").addText(gate.Utils.stringFor(docGate, an));
                    FeatureMap fmap = an.getFeatures();
                    for(Object key : fmap.keySet())
                    {
                        String fieldName = key.toString();
                        eAux.addElement(fieldName).addText(fmap.get(key).toString());
                    }
                    eOut.add(eAux);
                }
            }

            n_expressions += eOut.elements().size();                

            anns = new ArrayList<Annotation>();
            anns.addAll(annoset.get("AgentIdentification"));

            if(anns.size() > 0)
            {                    
                String lastname = "";
                String initials = "";
                String name = "";
                String firstname = "";

                if(extra_data != null){
                    lastname = extra_data.get(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_LASTNAME);
                    initials = extra_data.get(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_INITIALS);
                    name = extra_data.get(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_NAME);
                    firstname = extra_data.get(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_FIRSTNAME);
                }

                if(firstname.equals("")){
                    firstname = initials;
                }

                for(Annotation an : anns)
                {
                    String cvnItemName = an.getType();
                    org.dom4j.Element eAux = new org.dom4j.DocumentFactory().createElement(cvnItemName);
    //                        eAux.addElement("Domain").addText(gate.Utils.stringFor(docGate,
    //                                                          an.getStartNode().getOffset() > 100 ? an.getStartNode().getOffset() - 100 : an.getStartNode().getOffset(),
    //                                                          an.getEndNode().getOffset() + 100 < docGate.getContent().size() ? an.getEndNode().getOffset() + 100 :  an.getEndNode().getOffset()));
                    eAux.addAttribute("action_mode", "overwrite");
                    eAux.addAttribute("extra_gets", "getInformation");
                    eAux.addElement("Content").addText(gate.Utils.stringFor(docGate, an));
                    FeatureMap fmap = an.getFeatures();
                    for(Object key : fmap.keySet())
                    {
                        String fieldName = key.toString();
                        eAux.addElement(fieldName).addText(fmap.get(key).toString());
                    }
                    eAux.addElement(CVItemExtracted.AgentIdentification.GivenName).addText(firstname);
                    eAux.addElement(CVItemExtracted.AgentIdentification.FirstFamilyName).addText(lastname);

                    eOut.add(eAux);
                }
            }

            n_expressions += eOut.elements().size();

            ProjectLogger.LOGGER.info(String.format("%3d expressions in %s : ", n_expressions, docGate.getSourceUrl())); // + docXML.asXML()
            if(eOut == null) 
                ProjectLogger.LOGGER.info("Output is null"); // + docXML.asXML()

            aoData.setData_out(eOut);
        }

        if(aoData.getVerbose())
        {
            File dest_dir = aoData.getVerboseDir();
            File path = null;
            String fileName = "";
            
            if(extra_data != null &&
               extra_data.containsKey(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_DOCUMENT_NAME)){
                
                fileName = extra_data.get(DataExchangeLiterals.MIDDLE_ELEMENT_XML_EXTRADATA_DOCUMENT_NAME);
                
            }else{
                
                URL url = docGate.getSourceUrl();
                try 
                {
                   path = new File(url.toURI());
                   fileName = path.getName();
                }
                catch(Exception e) 
                {         
                   String filename;
                    try {
                        filename = URLEncoder.encode(url.toString(), "UTF-8") + ".html";
                    } catch (Exception ex) {
                        filename = docGate.getName() + ".html";
                    }
                   path = new File(filename);
                   fileName = path.getName();         
                }

                if(!fileName.equals(""))
                {
                    fileName = fileName.substring(0,fileName.lastIndexOf("."));
                }
                
            }

            File file_result = new File(dest_dir, fileName + "_verbose.html");
      
            try
            {
                writeResultsInHTMLFile(docGate, file_result);
            }
            catch(Exception ex){
                Logger.getRootLogger().error("Error writing verbose results. " + ex.toString());
            }
                
        }
    }
    
    @SuppressWarnings("unchecked")
    private void writeResultsInHTMLFile(Document doc, File file_result)
    {                 
        
      String startTagPart_1 = "<br><span GateID=\"";
      String startTagPart_2 = "\" title=\"";
      String startTagPart_3 = "\" style=\"background:LightBlue;\">";
      String endTag =         "</span><br>";      
            
      AnnotationSet defaultAnnotSet = doc.getAnnotations();
      Set annotTypesRequired = new HashSet();
      
      annotTypesRequired.add("ProfessionalActivityCurrent");                  
      annotTypesRequired.add("ProfessionalActivityNoCurrent");
      annotTypesRequired.add("AccreditedUniversityStudiesOtherPostGrade");
      annotTypesRequired.add("AccreditedUniversityStudiesDegree");
      annotTypesRequired.add("AccreditedUniversityStudiesPhDStudies");                  
      annotTypesRequired.add("AgentIdentification");          

      Set<Annotation> peopleAndPlaces =
        new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));

      FeatureMap features = doc.getFeatures();
      String originalContent = doc.getContent().toString();
        //(String) features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
        //RepositioningInfo info = (RepositioningInfo)
        //  features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);                       

      String xmlDocument = doc.toXml(peopleAndPlaces, true);

      String css_code = "<style type=\"text/css\">" + 
                        "span.AgentIdentification" + 
                        "{" + 
                        "   background-color: #808080;" + 
                        "} " + 
                        "span.AccreditedUniversityStudiesPhDStudies" + 
                        "{" + 
                        "   background-color: #FFFFCC;" + 
                        "} " + 
                        "span.AccreditedUniversityStudiesDegree" + 
                        "{" + 
                        "   background-color: #CCFFCC;" + 
                        "} " + 
                        "span.AccreditedUniversityStudiesOtherPostGrade" + 
                        "{" + 
                        "   background-color: #C17128;" + 
                        "} " + 
                        "span.ProfessionalActivityNoCurrent" + 
                        "{" + 
                        "   background-color: #99CCCC;" + 
                        "} " + 
                        "span.ProfessionalActivityCurrent" + 
                        "{" + 
                        "   background-color: #FF99CC;" + 
                        "} " + 
                        ".fixed {position:fixed !important; right:0px; top:0px; z-index:10 !important; background-color: #ffffff;} " +
                        "</style>";

      String legend = "<div class=\"fixed\">NOTES:<br>";
      legend = legend + "|1| = <span class=\"AccreditedUniversityStudiesPhDStudies\">AccreditedUniversityStudiesPhDStudies";
      legend = legend + "</span><br>";

      legend = legend + "|2| = <span class=\"AccreditedUniversityStudiesDegree\">AccreditedUniversityStudiesDegree";
      legend = legend + "</span><br>";

      legend = legend + "|3| = <span class=\"AccreditedUniversityStudiesOtherPostGrade\">AccreditedUniversityStudiesOtherPostGrade";
      legend = legend + "</span><br>";

      legend = legend + "|4| = <span class=\"ProfessionalActivityNoCurrent\">ProfessionalActivityNoCurrent";
      legend = legend + "</span><br>";

      legend = legend + "|5| = <span class=\"ProfessionalActivityCurrent\">ProfessionalActivityCurrent";
      legend = legend + "</span><br>";   
      
      legend = legend + "|6| = <span class=\"AgentIdentification\">AgentIdentification";
      legend = legend + "</span></div><br><br><br><br><br>";   
      
      int index1 = xmlDocument.indexOf("</head>");
      if(index1 > 0)
      {
          xmlDocument = xmlDocument.replace("</head>","</head>" + css_code + legend);
      }
      else
      {
          xmlDocument = css_code + legend + xmlDocument;
      }      
      
      {
          org.jsoup.nodes.Document docjsoup = org.jsoup.Jsoup.parse(xmlDocument);
          org.jsoup.select.Elements elements = docjsoup.select("AccreditedUniversityStudiesDegree");
          if(elements != null)
          {
              for(org.jsoup.nodes.Element element : elements)
              {
                  String s = element.html();
                  s = s;
                  
              }
          }
      }
            
//      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesPhDStudies","<b>#SP#</b><span class=\"AccreditedUniversityStudiesPhDStudies\"");
//      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesPhDStudies>","</span><b>#SP#</b>");
//
//      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesDegree","<b>#SD#</b><span class=\"AccreditedUniversityStudiesDegree\"");
//      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesDegree>","</span><b>#SD#</b>");
//
//      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesOtherPostGrade","<b>#SO#</b><span class=\"AccreditedUniversityStudiesPhDStudies\"");
//      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesOtherPostGrade>","</span><b>#SO#</b>");
//
//      xmlDocument = xmlDocument.replace("<ProfessionalActivityNoCurrent","<b>#</b><span class=\"ProfessionalActivityNoCurrent\"");
//      xmlDocument = xmlDocument.replace("</ProfessionalActivityNoCurrent>","</span><b>#PN#</b>");
//
//      xmlDocument = xmlDocument.replace("<ProfessionalActivityCurrent","<b>#</b><span class=\"ProfessionalActivityCurrent\"");
//      xmlDocument = xmlDocument.replace("</ProfessionalActivityCurrent>","</span><b>#PC#</b>");   
      
//      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesPhDStudies","<span class=\"AccreditedUniversityStudiesPhDStudies\"");
//      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesPhDStudies>","</span>");
//
//      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesDegree","<span class=\"AccreditedUniversityStudiesDegree\"");
//      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesDegree>","</span>");
//
//      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesOtherPostGrade","<span class=\"AccreditedUniversityStudiesPhDStudies\"");
//      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesOtherPostGrade>","</span>");
//
//      xmlDocument = xmlDocument.replace("<ProfessionalActivityNoCurrent","<span class=\"ProfessionalActivityNoCurrent\"");
//      xmlDocument = xmlDocument.replace("</ProfessionalActivityNoCurrent>","</span>");
//
//      xmlDocument = xmlDocument.replace("<ProfessionalActivityCurrent","<span class=\"ProfessionalActivityCurrent\"");
//      xmlDocument = xmlDocument.replace("</ProfessionalActivityCurrent>","</span>");      
      
      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesPhDStudies","<b>|1|</b><span class=\"AccreditedUniversityStudiesPhDStudies\"");
      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesPhDStudies>","</span><b>|1|</b>");

      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesDegree","<b>|2|</b><span class=\"AccreditedUniversityStudiesDegree\"");
      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesDegree>","</span><b>|2|</b>");

      xmlDocument = xmlDocument.replace("<AccreditedUniversityStudiesOtherPostGrade","<b>|3|</b><span class=\"AccreditedUniversityStudiesPhDStudies\"");
      xmlDocument = xmlDocument.replace("</AccreditedUniversityStudiesOtherPostGrade>","</span><b>|3|</b>");

      xmlDocument = xmlDocument.replace("<ProfessionalActivityNoCurrent","<b>|4|</b><span class=\"ProfessionalActivityNoCurrent\"");
      xmlDocument = xmlDocument.replace("</ProfessionalActivityNoCurrent>","</span><b>|4|</b>");

      xmlDocument = xmlDocument.replace("<ProfessionalActivityCurrent","<b>|5|</b><span class=\"ProfessionalActivityCurrent\"");
      xmlDocument = xmlDocument.replace("</ProfessionalActivityCurrent>","</span><b>|5|</b>");       
      
      xmlDocument = xmlDocument.replace("<AgentIdentification","<b>|6|</b><span class=\"AgentIdentification\"");
      xmlDocument = xmlDocument.replace("</AgentIdentification>","</span><b>|6|</b>");             
      
      xmlDocument = xmlDocument.replace("\n", "<br>");      
      try 
      {
          FileUtils.write(file_result, xmlDocument, "UTF-8");          
      }
      catch (IOException ex) {
          ProjectLogger.LOGGER.error("The verbose file can not be created " + file_result.getPath(), ex);
      }
    }  
}
