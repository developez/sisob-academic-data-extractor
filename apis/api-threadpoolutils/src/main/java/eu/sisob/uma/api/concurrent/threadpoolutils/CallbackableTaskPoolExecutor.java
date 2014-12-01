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

package eu.sisob.uma.api.concurrent.threadpoolutils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CallbackableTaskPoolExecutor
{ 
    long keepAliveTime = Long.MAX_VALUE;
 
    ThreadPoolExecutor threadPool = null;
 
    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
 
    public CallbackableTaskPoolExecutor(int poolSize, int maxPoolSize)
    {
        
        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
                            keepAliveTime, TimeUnit.SECONDS, queue)
        {            
             @Override
             protected void beforeExecute(Thread t, Runnable r) 
             {
                 ProjectLogger.LOGGER.info("Begin task");
             }
            
             @Override
             public void afterExecute(Runnable r, Throwable t) 
             {   
                 ProjectLogger.LOGGER.info("Ending task");
                 if(t != null){
                    ProjectLogger.LOGGER.error("Error executing the task", t);
                 }
                     
                 try
                 {
                    CallbackableTaskExecution execution = (CallbackableTaskExecution)r;
                    execution.executeCallBackOfTask();                 
                 }catch(Exception ex){
                     ProjectLogger.LOGGER.error("Error executing call back of task", t);
                 }
             }
        };
 
    }
 
    public void runTask(CallbackableTaskExecution task) throws InterruptedException
    {   
        threadPool.execute(task);
    }
 
    public void shutDown()
    {
        threadPool.shutdown();
    }    
}
