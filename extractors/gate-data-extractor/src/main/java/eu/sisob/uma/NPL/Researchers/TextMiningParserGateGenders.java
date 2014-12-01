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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import gate.*;
import gate.creole.SerialAnalyserController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import eu.sisob.uma.api.prototypetextmining.AnnotatorCollector;
import eu.sisob.uma.api.prototypetextmining.DataInputRepository;
import eu.sisob.uma.api.prototypetextmining.DataOutputRepository;
import eu.sisob.uma.api.prototypetextmining.MiddleData;
import eu.sisob.uma.api.prototypetextmining.gatedataextractor.OffsetBeginEndComparator;
import eu.sisob.uma.api.prototypetextmining.gatedataextractor.TextMiningParserGate;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import org.apache.log4j.Logger;

/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class TextMiningParserGateGenders extends TextMiningParserGate {
    
    /*
     * John Smith bla bla bla, and bla bla bla, receivede blablablabla.
     */
    static final int I_TYPE_LIST_OF_PERSONS = 0;
   

    /**
     *
     * @param literalIdParser
     * @param repInput_
     * @param nInfoblocks
     * @param repOutput_
     */
    public TextMiningParserGateGenders(String literalIdParser,
                                       DataInputRepository repInput_,
                                       int nInfoblocks,
                                       DataOutputRepository repOutput_)
                            
    {
        super(literalIdParser, repInput_, nInfoblocks, repOutput_, false);
    }

    /**
     * Initialise the ANNIE system by default. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     * Especific for recognize genders of name from list
     * @throws Exception
     */
    @Override
    public void iniActions() throws Exception
    {
        TextMiningParserGate.iniMutex.acquire();
        ProjectLogger.LOGGER.info("Load SerialAnalyserController Pipe (Genders) (" + Gate.genSym() + ").");
        try
        {
            // create a serial analyser controller to run ANNIE with
            annieController =
              (SerialAnalyserController) Factory.createResource(
                "gate.creole.SerialAnalyserController", Factory.newFeatureMap(),
                Factory.newFeatureMap(), "ANNIE_" + Gate.genSym()
              );

            FeatureMap params = null;
            ProcessingResource pr = null;

            params = Factory.newFeatureMap();
            pr = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            //params.put("caseSensitive", false);
            pr = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            //file:/C:/Users/dlopez/Documents/NetBeansProjects/TextExtractionPrototypes/extractionsWorksGate/resources/GATE-6.0/plugins/ANNIE/resources/gazetteer/lists.def
            String sListDefFilePath = new java.io.File("resources\\GATE-6.0\\plugins\\ANNIE\\resources\\gazetteer\\lists_naming.def").toURI().toString();
            params.put("listsURL", sListDefFilePath);
            pr = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            pr = (ProcessingResource) Factory.createResource("gate.creole.splitter.RegexSentenceSplitter", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            pr = (ProcessingResource) Factory.createResource("gate.creole.POSTagger", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            pr = (ProcessingResource) Factory.createResource("gate.creole.morph.Morph", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            pr = (ProcessingResource) Factory.createResource("gate.creole.ANNIETransducer", params);
            annieController.add(pr);

        }
//        catch (GateException ex)
//        {
//            annieController = null;
//            throw new Exception(ex.getMessage());
//        }
        finally
        {
            ProjectLogger.LOGGER.error("Load SerialAnalyserController done.");
            TextMiningParserGate.iniMutex.release();
        }
    }

   /**
     * Define annotar collector acoording index
     * I_TYPE_LIST_OF_PERSONS => Extract name from text
     * @param lstAnnColl_ list of annotator collector
    */
    @Override
    protected void iniAnnotatorCollectors(TreeMap lstAnnColl_)
    {
        AnnotatorCollector a = null;       

        a = new AnnotatorCollector(DataExchangeLiterals.ID_TEXTMININGPARSER_GATEGUESSER_DEFAULTANNREC)
        {
            public void collect(Object doc, MiddleData aoData)
            {
                org.dom4j.Element eOut = org.dom4j.DocumentFactory.getInstance().createElement("blockinfo");
                eOut.addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ANNOTATIONRECOLLECTING, aoData.getId_annotationrecollecting()); // aoData[MiddleData.I_INDEX_DATA_TYPE].toString());
                eOut.addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT, aoData.getId_entity()); // aoData[MiddleData.I_INDEX_DATA_ID].toString());
                
                gate.Document docGate = (gate.Document) doc;
                AnnotationSet annoset = docGate.getAnnotations();
                List<Annotation> anns = new ArrayList<Annotation>();

                anns.addAll(annoset.get("Person"));
                anns.addAll(annoset.get("FirstPerson"));
                anns.addAll(annoset.get("Token"));

                Collections.sort(anns, new OffsetBeginEndComparator());

                String out = "";
                //need to bee order
                boolean bLastSplit = true;
                for(Annotation an : anns)
                {
                    try
                    {
                        //org.dom4j.Element el = docXML.addElement(an.getType());
                        if(an.getType().equals("Person") || an.getType().equals("FirstPerson"))
                        {
                            if(an.getFeatures().get("rule") != null && (an.getFeatures().get("rule").equals("PersonFinal") || an.getFeatures().get("rule").equals("FirstName"))
                               && bLastSplit)
                            {
                                //out += cUtils.getContentOfAnnotation(docGate, an);
                                Object gender = an.getFeatures().get("gender");

                                if(gender!=null)
                                {
                                    char c = gender.toString().toUpperCase().charAt(0);
                                    if(c == 'F' || c == 'M')
                                    {
                                        out += c;
                                    }
                                }
                                else
                                {
                                    out += "A";
                                }                                
                            }
                            bLastSplit= false;
                        }
                        else if(an.getType().equals("Token"))
                        {
                            if(an.getFeatures().get("category") != null && an.getFeatures().get("category").equals("#"))
                            {
                                out += "\n";                                
                                bLastSplit = true;
                            }
                        }
                    } 
                    //catch (InvalidOffsetException ex)
                    //{
                    //    Logger.getLogger(TextMiningParserGate.class.getName()).log(Level.SEVERE, null, ex);
                    //}
                    catch (Exception ex)
                    {
                        ProjectLogger.LOGGER.error(ex.getMessage());                        
                    }
                }

                eOut.addElement("Genders").addText(out);
                ProjectLogger.LOGGER.info(eOut.elements().size() + " expressions in " +  docGate.getSourceUrl());// + " ==> " + docXML.asXML());

                aoData.setData_out(eOut);
            }
        };  
        lstAnnColl_.put(a.type, a);
    }
}
