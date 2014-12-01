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

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CallbackableTaskPoolExecutorWithResources
{
    int poolSize;
 
    int maxPoolSize;
 
    long keepAliveTime;
 
    ThreadPoolExecutor threadPool = null;
 
    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
    
    ExecutorResource[] executorResources;
    
    protected static Semaphore resourceMutex = new Semaphore(1);
 
    public CallbackableTaskPoolExecutorWithResources(Object[] resources, int maxPoolSize, long keepAliveTime)
    {
        this.executorResources = new ExecutorResource[resources.length];
        
        for(int i = 0; i < resources.length; i++)        
        {
            this.executorResources[i] = new ExecutorResource();
            this.executorResources[i].setFree(true);
            this.executorResources[i].setResource(resources[i]);
        }
        
        threadPool = new ThreadPoolExecutor(resources.length, maxPoolSize,
                            keepAliveTime, TimeUnit.SECONDS, queue)
        {
             @Override
             protected void beforeExecute(Thread t, Runnable r) 
             {   
                 super.beforeExecute(t, r);
                 //Logger.getLogger("MyLog").info("Task begins");
                
                 //resourceMutex.acquire();  
                 ExecutorResource re = null;
                 while((re = getFreeExecutorResource()) == null)
                 {
                     //Logger.getLogger("MyLog").info("Need wait for a free resource");
                     try {
                         Thread.sleep(100);
                     } catch (InterruptedException ex) {                         
                         
                     }
                 }
                
                 ((CallbackableTaskExecutionWithResource)r).setExecutorResource(re);                                    
             }
            
             @Override
             public void afterExecute(Runnable r, Throwable t) 
             {                    
                 //First release resource!
                 ExecutorResource re = ((CallbackableTaskExecutionWithResource)r).getExecutorResource();
                 re.setFree(true);                    
                 ((CallbackableTaskExecutionWithResource)r).setExecutorResource(null);                         
                 
                 super.afterExecute(r, t);
                 
                 //Next, execute callback                 
                 
                 //Logger.getLogger("MyLog").info("Task end");
                 CallbackableTaskExecutionWithResource execution = (CallbackableTaskExecutionWithResource)r;
                 execution.executeCallBackOfTask();                 
             }
             
             @Override
             public void terminated()
             {
                 String s = "";
                 s ="dasasa";
             }
        };
    }
    
 
    public synchronized void runTask(CallbackableTaskExecutionWithResource task) throws InterruptedException
    {   
        //Logger.getLogger("MyLog").info("Task added to pool");
        threadPool.execute(task);
    }
 
    public void shutDown()
    {
        threadPool.shutdown();
    }
    
    public synchronized ExecutorResource getFreeExecutorResource()
    {        
        ExecutorResource re = null;
        for(ExecutorResource re_aux : executorResources)
        {
            if(re_aux.isFree())
            {
                re = re_aux;
                re.setFree(false);
                break;
            }
        }        
        return re;
    }   
    
}
