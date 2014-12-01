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

package eu.sisob.uma.api.prototypetextmining;

import java.util.List;
import java.util.Iterator;
import java.util.TreeMap;
import org.apache.log4j.Logger;


/**
 * FIXME, Secure set and get methods against run 
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public abstract class TextMiningParser implements Runnable
{
    /**
     *
     */
    protected final String literalIdParser;
    private TreeMap tmAnnColl;    
    private DataInputRepository repInput;
    private DataOutputRepository repOutput;
    private int nInfoblocks;
    private boolean mustRunning;
    private boolean infinite;
    private boolean stop;
    private boolean loaded;
    
    /**
     *
     * @param literalIdParser
     * @param repInput
     * @param nInfoblocks
     * @param repOutput
     */
    public TextMiningParser(String literalIdParser,
                            DataInputRepository repInput,
                            int nInfoblocks,
                            DataOutputRepository repOutput,
                            boolean infinite)
    {        
        this.literalIdParser = literalIdParser;
        this.repInput = repInput;
        this.nInfoblocks = nInfoblocks;
        this.repOutput = repOutput;
        this.tmAnnColl = new TreeMap();
        this.iniAnnotatorCollectors(tmAnnColl);        
        this.infinite = infinite;
        this.loaded = false;         
        this.stop = true;
        this.mustRunning = true;
    }

    /**
     * For define annotator collector acoording index and fill lstAnnColl_ object.
     *   a = new AnnotatorCollector(DataExchangeLiterals.ID_TEXTMININGPARSER_GATERESEARCHER_DEFAULTANNREC_1)
     *   {
     *       @Override
     *       public void collect(Object doc, MiddleData aoData)
     *       {
     *          ...
     *       }
     *    }
     *    lstAnnColl_.put(a.type, a);
     * @param lstAnnColl_
     */
    protected abstract void iniAnnotatorCollectors(TreeMap lstAnnColl_);

    /**
     * Create an filled corpus list from data_ini properties of MiddleData objects.
     * @param lstContent
     * @return
     * @throws Exception
     */
    protected abstract List getFilledCorpus(List<MiddleData> lstContent) throws Exception;

    /**
     * Process created corpus list
     * @param corpus created corpus list
     * @param lstContent MiddleData list object for use inside
     * @throws Exception
     */
    protected abstract void processCorpus(List corpus, List<MiddleData> lstContent) throws Exception;

    /**
     * Release corpus elements from list
     * @param corpus
     */
    protected abstract void cleanCorpusElements(List corpus);

    /**
     * Release corpus list
     * @param corpus
     */
    protected abstract void cleanCorpus(List corpus);

    /**
     * Initialize action block code
     * @throws Exception
     */
    public abstract void iniActions() throws Exception;

    /**
     * End actions block code before end the parser process
     * @throws Exception
     */
    protected abstract void endActions() throws Exception;

    /**
     *
     * @throws Exception
     */
    public void cleanAllForRelaunch() throws Exception
    {
        if(this.getMustRunning())
        {
            endActions();
        }
    }

    public void run()
    {
        
        List corpus = null;  
        try
        {            
            this.setMustRunning(true);
            this.setStop(false);
            //iniActions();      

            while(mustRunning)
            {
                if(this.isStop())
                {
                    Thread.sleep(2000);
                }
                else
                {
                    if(!this.repOutput.isEmpty())
                    {
                        //[0]: int identification, |1]: E_TYPE_, [2]: Content, [3]: Out
                        ProjectLogger.LOGGER.info("Prepare for creating GATE's documents from repository)");
                        List<MiddleData> lst = this.getRepOutput().getData(literalIdParser, this.nInfoblocks);
                        ProjectLogger.LOGGER.info("Creating " + lst.size() + " GATE's documents from repository)");
                        long lTimerAux = java.lang.System.currentTimeMillis();
                        //Corpus corpus = null;

                        corpus = getFilledCorpus(lst); //corpus = getFilledCorpus(lst, MiddleData.I_INDEX_DATA_IN);
                        long lTimeForCreateDocument = java.lang.System.currentTimeMillis() - lTimerAux;
                        ProjectLogger.LOGGER.info("Done!");

                        if(corpus.size() > 0)
                        {
                            lTimerAux = java.lang.System.currentTimeMillis();

                            processCorpus(corpus, lst);
                            parseAnnotations(corpus, lst);
                            postProcessCorpus(corpus);
                            long lTimeForParsing = java.lang.System.currentTimeMillis() - lTimerAux;
                            ProjectLogger.LOGGER.info("Done!");
                            double d = lTimeForParsing;
                            ProjectLogger.LOGGER.info("Parsing documents in: " + lTimeForParsing + " ms (" + (d / corpus.size()) + " ms each document)");

                            for(MiddleData ao : lst)
                            {
                                try
                                {
                                    this.getRepInput().addData( ao );
                                }
                                catch(Exception ex)
                                {
                                    ProjectLogger.LOGGER.error("ERROR: " + ex.getMessage());
                                }
                            }

                            ProjectLogger.LOGGER.info("Release corpus from memory");
                            cleanCorpusElements(corpus);
                        }

                        cleanCorpus(corpus);
                        corpus = null;
                        ProjectLogger.LOGGER.info("Done!");
                    }
                    else
                    {    
                        if(infinite)
                        {
                            this.setStop(true);                     
                        }
                        else
                        {
                            this.setMustRunning(false);
                        }
                    }
                }
            }
        }
        catch(Exception ex)
        {   
            ProjectLogger.LOGGER.error("ERROR: " + ex.getMessage());
            mustRunning = false;
            if(corpus != null) 
            {
                cleanCorpusElements(corpus);
                cleanCorpus(corpus);
            }
        }

        if(infinite)
        {
            ProjectLogger.LOGGER.info("Begin end actions!");        
            try
            {
                endActions();
            }
            catch (Exception ex)
            {
                ProjectLogger.LOGGER.error(ex.getMessage());
            }
            ProjectLogger.LOGGER.info("Done!");        
        }
    }

    private void parseAnnotations(List corpus, List<MiddleData> lstOut)
    {
        //Iterator<gate.Document> itDoc = corpus.iterator();
        //Iterator<Object[]> itSource = lstOut.iterator();
        ProjectLogger.LOGGER.info("Collecting annotation ...");
        Iterator itDoc = corpus.iterator();
        Iterator<MiddleData> itSource = lstOut.iterator();

        while(itDoc.hasNext() && itSource.hasNext())
        {
             Object doc = (Object) itDoc.next();

             MiddleData aoData = itSource.next();             

             ((AnnotatorCollector)tmAnnColl.get(aoData.getId_annotationrecollecting())).
                                                collect(doc, aoData);
        }     
    }
    
    /*
     * Clean function to implement postprocessing corpus actions
     */
    private void postProcessCorpus(List corpus)
    {
        
    }

    /**
     * @return the stop
     */
    public synchronized boolean isStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    public synchronized void setStop(boolean stop) {
        this.stop = stop;
    }

    /**
     * @return the repInput
     */
    public DataInputRepository getRepInput() {
        return repInput;
    }

    /**
     * @param repInput the repInput to set
     */
    public void setRepInput(DataInputRepository repInput) {
        this.repInput = repInput;
    }

    /**
     * @return the repOutput
     */
    public DataOutputRepository getRepOutput() {
        return repOutput;
    }

    /**
     * @param repOutput the repOutput to set
     */
    public void setRepOutput(DataOutputRepository repOutput) {
        this.repOutput = repOutput;
    }
    
    /**
     *
     * @return if parser must running
     */
    public synchronized boolean getMustRunning()
    {
        return mustRunning;
    }

    /**
     *
     * @param b - mustrunning value
     */
    public synchronized void setMustRunning(boolean b)
    {
        mustRunning = b;
    }
}
