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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import eu.sisob.uma.api.prototypetextmining.AnnotatorCollector;
import eu.sisob.uma.api.prototypetextmining.DataInputRepository;
import eu.sisob.uma.api.prototypetextmining.DataOutputRepository;
import eu.sisob.uma.api.prototypetextmining.MiddleData;
import eu.sisob.uma.api.prototypetextmining.gatedataextractor.TextMiningParserGate;
import eu.sisob.uma.api.prototypetextmining.globals.DataExchangeLiterals;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class TextMiningParserGateResearcher extends TextMiningParserGate
{   
    String gate_path;  
    
    /**
     *
     * @param literalIdParser
     * @param repInput_
     * @param nInfoblocks
     * @param repOutput_
     * @param infinite
     * @param gate_path
     */
    public TextMiningParserGateResearcher(String literalIdParser, 
                                          DataInputRepository repInput_,
                                          int nInfoblocks,
                                          DataOutputRepository repOutput_,
                                          boolean infinite,
                                          String gate_path)     
                            
    {
        super(literalIdParser, repInput_, nInfoblocks, repOutput_, infinite);
        this.gate_path = gate_path;  
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
            
            //String gate_path = ClassLoader.getSystemClassLoader().getResource("eu/sisob/components/gatedataextractor/GATE-6.0").getPath();                        
            //String gate_path = gate_path;
            
            String plugins_gate_path = (gate_path + "//plugins");            
                        
            params = Factory.newFeatureMap();
            pr = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            //params.put("caseSensitive", false);
            pr = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", params);
            annieController.add(pr);

            params = Factory.newFeatureMap();
            //file:/C:/Users/dlopez/Documents/NetBeansProjects/TextExtractionPrototypes/extractionsWorksGate/resources/GATE-6.0/plugins/ANNIE/resources/gazetteer/lists.def
            String sListDefFilePathGazEurope = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "gazetteer" + File.separator + "gazetters_locations" + File.separator + "gaz_locations_for_cv_extractor_europe_us_canada.def")).toURI().toString();                                                           
            params.put("listsURL", sListDefFilePathGazEurope);
            params.put("caseSensitive", false);
            //pr = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params);                        
            pr = (ProcessingResource) Factory.createResource("com.ontotext.gate.gazetteer.HashGazetteer", params);                        
            annieController.add(pr);            
            
            params = Factory.newFeatureMap();
            String sListDefFilePathGazNames = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "gazetteer" + File.separator + "gazetters_names" + File.separator + "gaz_names_genres.def")).toURI().toString();                                                           
            params.put("listsURL", sListDefFilePathGazNames);            
            pr = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params);            
            annieController.add(pr); 
            
            params = Factory.newFeatureMap();
            String sListDefFilePathGazResearchers = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "gazetteer" + File.separator + "gaz_researchers.def")).toURI().toString();                                                           
            params.put("listsURL", sListDefFilePathGazResearchers);            
            pr = (ProcessingResource) Factory.createResource("com.ontotext.gate.gazetteer.HashGazetteer", params);            
            annieController.add(pr); 

            //ANNIE/resources/sentenceSplitter/gazetteer/lists.def
            //ANNIE/resources/sentenceSplitter/grammar/main.jape
            params = Factory.newFeatureMap();
            String def_gazetteerListsURL_file = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "sentenceSplitter" + File.separator + "gazetteer"  + File.separator + "lists.def")).toURI().toString();                                                           
            params.put("gazetteerListsURL", def_gazetteerListsURL_file);            
            String def_transducerURL_file = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "sentenceSplitter" + File.separator + "grammar" + File.separator + "main.jape")).toURI().toString();                                                           
            params.put("transducerURL", def_transducerURL_file);            
            //pr = (ProcessingResource) Factory.createResource("gate.creole.splitter.RegexSentenceSplitter", params);
            pr = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter", params);
            annieController.add(pr);

            //ANNIE/resources/heptag/lexicon
            //ANNIE/resources/heptag/ruleset
            params = Factory.newFeatureMap();
            String def_lexicon_file = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "heptag" + File.separator + "lexicon")).toURI().toString();                                                           
            params.put("lexiconURL", def_lexicon_file);            
            String def_rules_file = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "heptag" + File.separator + "ruleset")).toURI().toString();                                                           
            params.put("rulesURL", def_rules_file);            
            pr = (ProcessingResource) Factory.createResource("gate.creole.POSTagger", params);
            annieController.add(pr);

            //Tools/resources/morph/default.rul
            params = Factory.newFeatureMap();
            String def_morph_file = (new java.io.File(plugins_gate_path + "" + File.separator + "Tools" + File.separator + "resources" + File.separator + "morph" + File.separator + "default.rul")).toURI().toString();                                                           
            params.put("rulesFile", def_morph_file);            
            pr = (ProcessingResource) Factory.createResource("gate.creole.morph.Morph", params);
            annieController.add(pr);

            //ANNIE/resources/NE/main.jape
            params = Factory.newFeatureMap();
            String def_transducer_file = (new java.io.File(plugins_gate_path + "" + File.separator + "ANNIE" + File.separator + "resources" + File.separator + "NE" + File.separator + "main.jape")).toURI().toString();                                                           
            params.put("grammarURL", def_transducer_file);            
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
        a = new GateResearcherAnnCollector(DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC);
        lstAnnColl_.put(a.type, a);        
    }        
}
