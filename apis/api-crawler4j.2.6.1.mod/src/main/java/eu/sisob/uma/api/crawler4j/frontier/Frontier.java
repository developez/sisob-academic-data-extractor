/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

package eu.sisob.uma.api.crawler4j.frontier;

import java.util.Iterator;
import java.util.List;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;

import eu.sisob.uma.api.crawler4j.crawler.Configurations;
import eu.sisob.uma.api.crawler4j.crawler.ProjectLogger;
import eu.sisob.uma.api.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public final class Frontier {

	private WorkQueues workQueues;
	private InProcessPagesDB inprocessPages;
        private DocIDServer refdocIDServer;

	private Object mutex;

	private Object waitingList;

	private boolean isFinished;

	private int maxPagesToFetch;

	private int scheduledPages;
        
        

        //init
	public Frontier(Environment env, boolean resumable, DocIDServer docIDServer) 
        {
		try 
                {                        
                        refdocIDServer = docIDServer;
                        mutex = env.getHome().getPath() + "_" + Frontier.class.toString() + "_Mutex";
                        waitingList = Frontier.class.toString() + "_WaitingList";
                        isFinished = false;
                        maxPagesToFetch = Configurations.getIntProperty("crawler.max_pages_to_fetch", -1);
                    
			workQueues = new WorkQueues(env, env.getHome().getPath() + "_" + Frontier.class.toString() + "_PendingURLsDB", resumable);
			if (resumable) {
				inprocessPages = new InProcessPagesDB(env);
				long docCount = inprocessPages.getLength();
				if (docCount > 0) {
					ProjectLogger.LOGGER.info("Rescheduling " + docCount + " URLs from previous crawl.");
					while (true) {
						List<WebURL> urls = inprocessPages.get(100);
						if (urls.size() == 0) {
							break;
						}
						inprocessPages.delete(urls.size());
						scheduleAll(urls);
					}
				}
			} else {
				inprocessPages = null;
				scheduledPages = 0;
			}			
		} 
                catch (DatabaseException e) {
			ProjectLogger.LOGGER.error("Error while initializing the Frontier: " + e.getMessage());
			workQueues = null;
		}
	}

	public void scheduleAll(List<WebURL> urls) {
		synchronized (mutex) {
			Iterator<WebURL> it = urls.iterator();
			while (it.hasNext()) {
				WebURL url = it.next();
				if (maxPagesToFetch < 0 || scheduledPages < maxPagesToFetch) {					
					try {
						workQueues.put(url);
						scheduledPages++;
					} catch (DatabaseException e) {
						ProjectLogger.LOGGER.error("Error while puting the url in the work queue.");
					}
				}
			}
			synchronized (waitingList) {
				waitingList.notifyAll();
			}
		}
	}

	public void schedule(WebURL url) {
		synchronized (mutex) {
			try {
				if (maxPagesToFetch < 0 || scheduledPages < maxPagesToFetch) {
					workQueues.put(url);
					scheduledPages++;
				}
			} catch (DatabaseException e) {
				ProjectLogger.LOGGER.error("Error while puting the url in the work queue.");
			}
		}
	}

	public void getNextURLs(int max, List<WebURL> result) 
        {
		while (true) {
			synchronized (mutex) {
				try {
					List<WebURL> curResults = workQueues.get(max);
					workQueues.delete(curResults.size());
					if (inprocessPages != null) {
						for (WebURL curPage : curResults) {
							inprocessPages.put(curPage);
						}
					}
					result.addAll(curResults);					
				} catch (DatabaseException e) {
					ProjectLogger.LOGGER.error("Error while getting next urls: " + e.getMessage());
					e.printStackTrace();
				}
				if (result.size() > 0) {
					return;
				}
			}
			try {
				synchronized (waitingList) {
                                        ProjectLogger.LOGGER.debug("Im waiting.");
					waitingList.wait();
                                        ProjectLogger.LOGGER.debug("Stop waiting.");
				}
			} catch (InterruptedException e) {
			}
			if (isFinished) {
				return;
			}
		}
	}
	
        //
	public void setProcessed(WebURL webURL) {
		if (inprocessPages != null) {
			if (!inprocessPages.removeURL(webURL)) {
				ProjectLogger.LOGGER.warn("Could not remove: " + webURL.getURL() + " from list of processed pages.");
			}
		}
	}

	public long getQueueLength() {
		return workQueues.getLength();
	}

	public long getNumberOfAssignedPages() {
		return inprocessPages.getLength();
	}
	
	public void sync() {
		workQueues.sync();
		refdocIDServer.sync();
	}

	public boolean isFinished() {
		return isFinished;
	}
	
	public void setMaximumPagesToFetch(int max) {
		maxPagesToFetch = max;
	}

	public void close() {
		sync();
		workQueues.close();
		refdocIDServer.close();
	}

	public void finish() {
		isFinished = true;
		synchronized (waitingList) {
			waitingList.notifyAll();
		}
	}

	public void notifyAllWaitingList() {
		synchronized (waitingList) {
			waitingList.notifyAll();
		}
	}

    /**
     * @return the docIDServer
     */
    public DocIDServer getDocIDServer() {
        return refdocIDServer;
    }

    /**
     * @param docIDServer the docIDServer to set
     */
    public void setDocIDServer(DocIDServer docIDServer) {
        this.refdocIDServer = docIDServer;
    }


    
}
