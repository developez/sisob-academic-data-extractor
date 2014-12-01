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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class TextMiningSimpleParserMonitor {

    Thread[] parsersThreads;
    TextMiningParser[] parsers;        
    private DataInputRepository repInput;
    private DataOutputRepository repOutput;
    
    public TextMiningSimpleParserMonitor(TextMiningParser... parsers) throws Exception
    {
        this.parsers = parsers;
        this.parsersThreads = new Thread[parsers.length];
        for(int i = 0; i < parsers.length; i++)
        {            
            this.parsers[i].iniActions();
            parsersThreads[i] = new Thread(this.parsers[i]);
        }        
    }   

    public void launchParsers() throws InterruptedException, Exception
    {                
        for(int i = 0; i < parsersThreads.length; i++)
        {                     
            parsersThreads[i].start();
        }
    }
    
    public void startParsing() throws InterruptedException, Exception
    {    
        for(int i = 0; i < parsersThreads.length; i++)
        {
            parsers[i].setStop(false);
        }
        
        boolean end = false;
        
        while(!end)
        {
            Thread.sleep(1000);
            end = true;
            for(int i = 0; i < parsersThreads.length; i++)
            {                
                if(parsers[i].isStop())
                {
                    end = end && true;
                }
                else
                {
                    if(parsersThreads[i].getState().equals(Thread.State.TERMINATED) && parsers[i].getMustRunning())
                    {   
                        ProjectLogger.LOGGER.info("Relaunch Parser Thread");
                        parsers[i].cleanAllForRelaunch();
                        parsersThreads[i] = new Thread(this.parsers[i]);
                        parsers[i].setStop(false);
                        parsersThreads[i].start();
                        end = false;
                    }
                    else
                    {
                        end = false;
                    }
                }               
            }
        }       
    }
    
    public synchronized void killParsers()
    {
        for(int i = 0; i < parsersThreads.length; i++)
        {
            parsers[i].setMustRunning(false);
        }
    }
    
    public synchronized void doTask(List<MiddleData> lstMiddleData)
    {        
        int n = lstMiddleData.size();
        
        for(MiddleData md : lstMiddleData)        
        {
            getRepInput().addData(md);
        }        
    }

    /**
     * @return the repInput
     */
    public DataInputRepository getRepInput() {
        return repInput;
    }
    

    /**
     * @return the repOutput
     */
    public DataOutputRepository getRepOutput() {
        return repOutput;
    }
   
}
