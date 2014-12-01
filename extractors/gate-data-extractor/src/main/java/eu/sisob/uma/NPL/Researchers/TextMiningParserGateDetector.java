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
import java.util.List;
import java.util.TreeMap;
import eu.sisob.uma.api.prototypetextmining.AnnotatorCollector;
import eu.sisob.uma.api.prototypetextmining.DataInputRepository;
import eu.sisob.uma.api.prototypetextmining.DataOutputRepository;
import eu.sisob.uma.api.prototypetextmining.MiddleData;
import eu.sisob.uma.api.prototypetextmining.gatedataextractor.TextMiningParserGate;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import org.apache.log4j.Logger;

/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class TextMiningParserGateDetector extends TextMiningParserGate
{   
    /**
     *
     * @param literalIdParser
     * @param repInput_
     * @param nInfoblocks
     * @param repOutput_
     */
    public TextMiningParserGateDetector(String literalIdParser,
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
     * Especific for extract info from CV and personal web page of researchers
     * @throws Exception
     */
    @Override
    public void iniActions() throws Exception
    {
        TextMiningParserGate.iniMutex.acquire();
        ProjectLogger.LOGGER.info("Load SerialAnalyserController Pipe (Researchers) (" + Gate.genSym() + ").");
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
            //String sListDefFilePath = (new java.io.File("resources\\GATE-6.0\\plugins\\ANNIE\\resources\\gazetteer\\lists_researchers.def")).toURI().toString();
            //params.put("listsURL", sListDefFilePath);
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
            //file:/C:/Users/dlopez/Documents/NetBeansProjects/TextExtractionPrototypes/extractionsWorksGate/resources/GATE-6.0/plugins/ANNIE/resources/gazetteer/lists.def
            String sListDefFilePath = (new java.io.File("resources\\GATE-6.0\\plugins\\ANNIE\\resources\\NE\\main_detector.jape")).toURI().toString();
            params.put("grammarURL", sListDefFilePath);
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
            ProjectLogger.LOGGER.info("Load SerialAnalyserController done.");
            TextMiningParserGate.iniMutex.release();
        }

    }

   /**
    * Define annotator collector acoording index
    * I_TYPE_CONTENT_ENTIRE_WEB_PAGE => Extract info from CV and personal web page of researchers
    * @param lstAnnColl_ list of annotator collector
    */
    @Override
    protected void iniAnnotatorCollectors(TreeMap lstAnnColl_)
    {
        AnnotatorCollector a = null;

        a = new AnnotatorCollector(DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC)
        {
            @Override
            public void collect(Object doc, MiddleData aoData)
            {
                org.dom4j.Element eOut = org.dom4j.DocumentFactory.getInstance().createElement("blockinfo");
                eOut.addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ANNOTATIONRECOLLECTING, aoData.getId_annotationrecollecting()); // aoData[MiddleData.I_INDEX_DATA_TYPE].toString());
                eOut.addAttribute(DataExchangeLiterals.MIDDLE_ELEMENT_XML_ID_ENTITY_ATT, aoData.getId_entity()); // aoData[MiddleData.I_INDEX_DATA_ID].toString());

                gate.Document docGate = (gate.Document) doc;
                AnnotationSet annoset = docGate.getAnnotations();
                List<Annotation> anns = new ArrayList<Annotation>();
                
                //Expressions
                //anns.addAll(annoset.get("JobTitleTest"));
                //anns.addAll(annoset.get("DegreeTest"));
                anns.addAll(annoset.get("OrgTest"));

                //Collections.sort(anns, new OffsetBeginEndComparator());

                //need to bee order
                if(anns.size() > 0)
                {                    
                    for(Annotation an : anns)
                    {
                        String cvnItemName = an.getType();
                        org.dom4j.Element eAux = new org.dom4j.DocumentFactory().createElement(cvnItemName);
                        //eAux.addElement("Domain").addText(gate.Utils.stringFor(docGate,
                        //                                  an.getStartNode().getOffset() > 100 ? an.getStartNode().getOffset() - 100 : an.getStartNode().getOffset(),
                        //                                  an.getEndNode().getOffset() + 100 < docGate.getContent().size() ? an.getEndNode().getOffset() + 100 :  an.getEndNode().getOffset()));
                        eAux.addText(gate.Utils.stringFor(docGate, an));                        
                        eOut.add(eAux);
                    }
                }               

                Logger.getLogger("MyLog").info(String.format("%3d expressions in %s : ", eOut != null ? eOut.elements().size() : 0, docGate.getSourceUrl())); // + docXML.asXML()

                aoData.setData_out(eOut);
            }
        };
        lstAnnColl_.put(a.type, a);
    }
}
