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

package eu.sisob.uma.api.crawler4j.crawler;

import java.io.File;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import eu.sisob.uma.api.crawler4j.frontier.DocIDServer;
import eu.sisob.uma.api.crawler4j.frontier.Frontier;
import eu.sisob.uma.api.crawler4j.robotstxt.RobotstxtServer;
import eu.sisob.uma.api.crawler4j.url.URLCanonicalizer;
import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.api.crawler4j.util.IO;
import java.util.Iterator;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public class CrawlController {
	
        public static final String SEED_TAG = "#SEED_TAG#";

	private Environment env;
	private List<Object> crawlersLocalData = new ArrayList<Object>();        
        protected List<String> seeds;

	public List<Object> getCrawlersLocalData() {
		return crawlersLocalData;
	}

	List<Thread> threads;
        
        //Fixme
        private Frontier frontier;
        private DocIDServer docIDServer;

	public CrawlController(String storageFolder) throws Exception {
            
		this(storageFolder, Configurations.getBooleanProperty("crawler.enable_resume", false));                 
	}
	
	public CrawlController(String storageFolder, boolean resumable) throws Exception {
		File folder = new File(storageFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		envConfig.setTransactional(resumable);
		envConfig.setLocking(resumable);
                
		File envHome = new File(storageFolder + File.separator + "frontier");
		if (!envHome.exists()) {
			envHome.mkdir();
		}
                
		if (!resumable) {                        
			IO.deleteFolderContents(envHome);                        
		}

		env = new Environment(envHome, envConfig);
                //Frontier.init(env, resumable);
                //DocIDServer(env, resumable);
                frontier = new Frontier(env, resumable, null);          
		docIDServer = new DocIDServer(env, resumable);                
                frontier.setDocIDServer(docIDServer);
                
		//PageFetcher.startConnectionMonitorThread();
                seeds = new ArrayList<String>();
	}

	public <T extends WebCrawler> void start(Class<T> _c, int numberOfCrawlers) {
		try {
			crawlersLocalData.clear();
			threads = new ArrayList<Thread>();
			List<T> crawlers = new ArrayList<T>();
			int numberofCrawlers = numberOfCrawlers;
			for (int i = 1; i <= numberofCrawlers; i++) {
				T crawler = _c.newInstance();
				Thread thread = new Thread(crawler, "Crawler " + i);
				crawler.setThread(thread);
				crawler.setMyId(i);
				crawler.setMyController(this);
				thread.start();
				crawlers.add(crawler);
				threads.add(thread);
				ProjectLogger.LOGGER.info("Crawler " + i + " started.");
			}
			while (true)
                        {
                                //ProjectLogger.LOGGER.info("Wait 10 seconds.");
				sleep(10);
				boolean someoneIsWorking = false;
                                //ProjectLogger.LOGGER.info("Check if any thread is working and runnable.");
				for (int i = 0; i < threads.size(); i++) {
					Thread thread = threads.get(i);
					if (!thread.isAlive()) {
						ProjectLogger.LOGGER.info("Thread " + i + " was dead, I'll recreate it.");
						T crawler = _c.newInstance();
						thread = new Thread(crawler, "Crawler " + (i + 1));
						threads.remove(i);
						threads.add(i, thread);
						crawler.setThread(thread);
						crawler.setMyId(i + 1);
						crawler.setMyController(this);
						thread.start();
						crawlers.remove(i);
						crawlers.add(i, crawler);
					} else if (thread.getState() == State.RUNNABLE) {
						someoneIsWorking = true;
                                                //ProjectLogger.LOGGER.info("Thread " + i + " is working.");
					}
				}
				if (!someoneIsWorking)
                                {
                                        ProjectLogger.LOGGER.info("No one crawler seems working. Check if all are finished by condition.");
                                        Iterator<WebCrawler> it = (Iterator<WebCrawler>) crawlers.iterator();
                                        boolean bAllFinish = true;
                                        while(it.hasNext())
                                        {
                                            WebCrawler wb = it.next();
                                            bAllFinish = bAllFinish && wb.isFinish();
                                        }

                                        if(bAllFinish)
                                        {
                                            ProjectLogger.LOGGER.info("All of the crawlers are finished by some condition. Finishing the process...");
                                            for (T crawler : crawlers) {
                                                    crawler.onBeforeExit();
                                                    crawlersLocalData.add(crawler.getMyLocalData());
                                            }

                                            // At this step, frontier notifies the threads that were waiting for new URLs and they should stop
                                            // We will wait a few seconds for them and then return.
                                            //getFrontier().finish(); //FIXME
                                            ProjectLogger.LOGGER.info("Waiting for 10 seconds before final clean up...");
                                            sleep(10);

                                            //getFrontier().close();  //FIXME
                                            //PageFetcher.stopConnectionMonitorThread();
                                            return;
                                        }

					// Make sure again that none of the threads are alive.
					ProjectLogger.LOGGER.info("It looks like no thread is working, waiting for 5 seconds to make sure...");
					sleep(5);                                        
                                        
					if (!isAnyThreadWorking())
                                        {
						long queueLength = getFrontier().getQueueLength();
						if (queueLength > 0) {
							continue;
						}
						ProjectLogger.LOGGER.info("No thread is working and no more URLs are in queue waiting for another 5 seconds to make sure...");
						sleep(5);
						queueLength = getFrontier().getQueueLength();
						if (queueLength > 0) {
							continue;
						}
						ProjectLogger.LOGGER.info("All of the crawlers are stopped. Finishing the process...");
						for (T crawler : crawlers) {
							crawler.onBeforeExit();
							crawlersLocalData.add(crawler.getMyLocalData());
						}

						// At this step, frontier notifies the threads that were waiting for new URLs and they should stop
						// We will wait a few seconds for them and then return.
						
						
						//getFrontier().finish(); //FIXME
                                                ProjectLogger.LOGGER.info("Waiting for 10 seconds before final clean up...");
                                                sleep(10);
                                                //getFrontier().close();  //FIXME						
						//PageFetcher.stopConnectionMonitorThread();
						return;
					}
				}
			}                       
		} catch (Exception e) {
                    ProjectLogger.LOGGER.error(e.toString(), e);
		}
                finally
                {
                    releaseResources();
                }
	}
        
        public void releaseResources()
        {
            if(frontier != null)
            {
                ProjectLogger.LOGGER.info("Releasing frontier");
                try {
                    frontier.finish();
                    frontier.close();                    
                } catch(Exception ex) {
                    ProjectLogger.LOGGER.error(ex.toString());
                }
            }
            
            if(docIDServer != null)
            {
                ProjectLogger.LOGGER.info("Releasing doc id server");
                try {
                   docIDServer.close();
                } catch(Exception ex) {
                    ProjectLogger.LOGGER.error(ex.toString());
                }
            }
            
            if(env != null)
            {
                ProjectLogger.LOGGER.info("Releasing crawler controller env");
                try {
                    for(String database : env.getDatabaseNames()) 
                    {
                        env.removeDatabase(null, database);
                    }
                    
                    File f = new File(env.getHome().getAbsolutePath());
                    env.close();
                    
                    IO.deleteFolderContents(f);
                    IO.deleteFolder(f);
                } catch(Exception ex) {
                    ProjectLogger.LOGGER.error(ex.toString());
                }
            }

            frontier = null;
            docIDServer = null;
            env = null;
        }

	private void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (Exception e) {
		}
	}

	private boolean isAnyThreadWorking() {
                ProjectLogger.LOGGER.info("Check if any thread is working and runnable.");
		boolean someoneIsWorking = false;
		for (int i = 0; i < threads.size(); i++) {
			Thread thread = threads.get(i);
			if (thread.isAlive() && thread.getState() == State.RUNNABLE) {
                                ProjectLogger.LOGGER.info("Thread " + i + " is working.");
				someoneIsWorking = true;
			}
		}
		return someoneIsWorking;
	}

	public void addSeed(String pageUrl) 
        {            
            String canonicalUrl = URLCanonicalizer.getCanonicalURL(pageUrl);
            if (canonicalUrl == null) {
                    ProjectLogger.LOGGER.error("Invalid seed URL: " + pageUrl);
                    return;
            }
            seeds.add(canonicalUrl);
            int docid = docIDServer.getDocID(canonicalUrl);
            if (docid > 0) {
                    // This URL is already seen.
                    return;
            }

            WebURL webUrl = new WebURL();
            webUrl.setURL(canonicalUrl);
            docid = docIDServer.getNewDocID(canonicalUrl);
            webUrl.setDocid(docid);
            webUrl.setDepth((short) 0);
            webUrl.setAssociateText(CrawlController.SEED_TAG);
            if (!RobotstxtServer.allows(webUrl, this.getDocIDServer())) {
                    ProjectLogger.LOGGER.info("Robots.txt does not allow this seed: " + pageUrl);
            } else {
                    getFrontier().schedule(webUrl);
            }
	}

	public void setPolitenessDelay(int milliseconds) {
		if (milliseconds < 0) {
			return;
		}
		if (milliseconds > 10000) {
			milliseconds = 10000;
		}
		PageFetcher.setPolitenessDelay(milliseconds);
	}

	public void setMaximumCrawlDepth(int depth) throws Exception {
		if (depth < -1) {
			throw new Exception("Maximum crawl depth should be either a positive number or -1 for unlimited depth.");
		}
		if (depth > Short.MAX_VALUE) {
			throw new Exception("Maximum value for crawl depth is " + Short.MAX_VALUE);
		}
		WebCrawler.setMaximumCrawlDepth((short) depth);
	}

	public void setMaximumPagesToFetch(int max) {
		getFrontier().setMaximumPagesToFetch(max);
	}

	public void setProxy(String proxyHost, int proxyPort) {
		PageFetcher.setProxy(proxyHost, proxyPort);
	}

	public static void setProxy(String proxyHost, int proxyPort, String username, String password) {
		PageFetcher.setProxy(proxyHost, proxyPort, username, password);
	}

    /**
     * @return the frontier
     */
    public Frontier getFrontier() {
        return frontier;
    }

    /**
     * @param frontier the frontier to set
     */
    public void setFrontier(Frontier frontier) {
        this.frontier = frontier;
    }
    
        /**
     * @return the frontier
     */
    public DocIDServer getDocIDServer() {
        return docIDServer;
    }

    /**
     * @param frontier the frontier to set
     */
    public void setDocIDServer(DocIDServer docIDServer) {
        this.docIDServer = docIDServer;
    }
    
    /**
     * @return the env
     */
    public Environment getEnv() {
        return env;
    }    
}
