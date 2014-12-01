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

import eu.sisob.uma.api.prototypetextmining.*;
import gate.*;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import gate.creole.SerialAnalyserController;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public abstract class TextMiningParserGate extends TextMiningParser {
   
    protected final int MAX_LENGTH_DOCUMENT = 50000;
    protected static Semaphore iniMutex = new Semaphore(1);
            
    protected SerialAnalyserController annieController = null;

    protected TextMiningParserGate(String literalIdParser,
                                   DataInputRepository repInput_,
                                   int nInfoblocks,
                                   DataOutputRepository repOutput_,
                                   boolean infinite)                            
    {
        super(literalIdParser, repInput_, nInfoblocks, repOutput_, infinite);
        annieController = null;
    }

    protected abstract void iniAnnotatorCollectors(TreeMap lstAnnColl_);

   /**
    * Create corpus from content list using index
    * @param lstContent    
    * @return
    * @throws Exception
    */
    @Override
    protected List getFilledCorpus(List<MiddleData> lstContent) throws Exception
    {
        Corpus result = null;
        try
        {
            // create a corpus with the above documents
            result = Factory.newCorpus("Corpus");
            
            String fileSeparator = "";
            if(System.getProperty("file.separator").equals("\\"))
                fileSeparator = System.getProperty("file.separator") + System.getProperty("file.separator");
            else
                fileSeparator = System.getProperty("file.separator");                                        
            
            Pattern p1 = Pattern.compile("([a-zA-Z]:)?(" + fileSeparator + "[a-zA-Z0-9#._-]+)+" + fileSeparator + "?");
            Pattern p2 = Pattern.compile("\\b(https?|ftp|file):/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");            

            for(MiddleData ao: lstContent)
            {
                Document doc = null;
                String name = "";
                try
                {
                    if(p1.matcher((String)ao.getData_in()).matches())
                    {
                        ProjectLogger.LOGGER.info((String)ao.getData_in());
                        java.io.File f = new java.io.File((String)ao.getData_in());
                        doc = Factory.newDocument((f).toURI().toURL()); //(new URL("file:///" + ((String)ao[iTextIndex]).replace("#", "%23")));                        
                        name = f.getName();
                    }
                    else if(p2.matcher((String)ao.getData_in()).matches())
                    {
                        ProjectLogger.LOGGER.info((String)ao.getData_in());
                        URL url = new URL((String)ao.getData_in());
                        doc = Factory.newDocument(url); //(new URL("file:///" + ((String)ao[iTextIndex]).replace("#", "%23")));
                        if(url.toString().startsWith("file:")){
                            int i = url.toString().lastIndexOf(File.separator);                            
                            name = (i == -1 ? url.toString() : url.toString().substring(i)).replaceAll("\\W+", "-").toLowerCase();                        
                        }else{
                            name = url.toString().replaceAll("\\W+", "-").toLowerCase();                        
                        }
                    }
                    else
                    {
                        doc = Factory.newDocument((String)ao.getData_in());
                        name = doc.getName();
                    }
                    
                    doc = Factory.newDocument(doc.getContent().toString());
                    doc.setName(name);
                }
                catch(Exception ex)
                {
                    ProjectLogger.LOGGER.error(ex.getMessage() + " file: " + ((String)ao.getData_in()).replace("#", "%23"));
                    doc = null;
                }               

                if(doc != null)
                {                    
                    //doc.setPreserveOriginalContent(false);                    
                    if(doc.getContent().toString().trim().equals(""))
                    {
                        URL url = doc.getSourceUrl();
                        doc = Factory.newDocument("#DOCUMENT ERROR IN LOADING#");
                        doc.setSourceUrl(url);
                        result.add(doc);
                    }
                    else if(doc.getContent().toString().length() > MAX_LENGTH_DOCUMENT)
                    {
                        URL url = doc.getSourceUrl();
                        doc = Factory.newDocument(doc.getContent().toString().subSequence(0, MAX_LENGTH_DOCUMENT).toString());                        
                        doc.setSourceUrl(url);
                        result.add(doc);
                        ProjectLogger.LOGGER.warn(((String)ao.getData_in()).replace("#", "%23") + " is empty."); 
                    }
                    else
                    {
                        result.add(doc);                
                    }
                }
                else
                {                    
                    URL url = (new java.io.File((String)ao.getData_in())).toURI().toURL();
                    doc = Factory.newDocument("#DOCUMENT ERROR IN LOADING#");
                    doc.setSourceUrl(url);
                    result.add(doc);
                    ProjectLogger.LOGGER.warn(((String)ao.getData_in()).replace("#", "%23") + " is too long.");                    
                }
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }

        return result;
    }

   /**
     * Process GATE corpus
     * @param corpus
     */ 
    protected void processCorpus(List corpus, List<MiddleData> lstContent) throws Exception
    {  
        ProjectLogger.LOGGER.info("Parsing with ANNIE  + extra JAPE + extra Gazetters (Running ANNIE)...");
        annieController.setCorpus((Corpus)corpus);
        annieController.execute();
        ProjectLogger.LOGGER.info("Done!");
    }

    /**
     * Clean GATE corpus elements after processing
     * @param corpus
     */
    protected void cleanCorpusElements(List corpus)
    {
        if(corpus != null)
        {
            Object[] aDocs = corpus.toArray();
            for(int i = 0; i < aDocs.length; i++)
            {
                if(aDocs[i] != null)
                {
                    Factory.deleteResource((gate.Document)aDocs[i]);
                }
            }
        }
    }

    /**
     * Clean GATE corpus after processing
     * @param corpus
     */
    protected void cleanCorpus(List corpus)
    {
        if(corpus != null)
        {
            Factory.deleteResource((gate.Corpus)corpus);
        }
    }
    
    /**
     * Initialise the ANNIE system by default. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     * @throws Exception
     */
    public void iniActions() throws Exception
    {
        iniMutex.acquire();
        ProjectLogger.LOGGER.info("Load SerialAnalyserController (default) (" + Gate.genSym() + ").");
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
            //params.put("caseSensitive", false);
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

//        }
//        catch (GateException ex)
//        {
//            annieController = null;
//            throw new Exception(ex.getMessage());
        }
        finally
        {
            ProjectLogger.LOGGER.info("Load SerialAnalyserController done.");
            iniMutex.release();
        }
    }

    private void cleanPRs(Collection PRs)
    {
        Object[] ao = PRs.toArray();

        for(int i = 0; i < ao.length; i++)
        {
            Factory.deleteResource((ProcessingResource)ao[i]);
        }
    }


    /**
     * Clean annie controller
     * @throws Exception
     */
    public void endActions() throws Exception
    {
        if(annieController != null)
        {
            cleanCorpusElements(annieController.getCorpus());
            cleanCorpus(annieController.getCorpus());
            
            try
            {
                iniMutex.acquire();            
                cleanPRs(annieController.getPRs());
                annieController.cleanup();
                Factory.deleteResource(annieController);
                annieController = null;
            }
            finally
            {
                iniMutex.release();   
            }            
        }
    }    
}
