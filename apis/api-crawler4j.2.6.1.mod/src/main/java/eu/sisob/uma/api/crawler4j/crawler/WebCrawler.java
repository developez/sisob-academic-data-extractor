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

import java.util.*;

import org.apache.log4j.Logger;

import eu.sisob.uma.api.crawler4j.robotstxt.RobotstxtServer;
import eu.sisob.uma.api.crawler4j.url.URLCanonicalizer;
import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.api.crawler4j.util.Pair;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public class WebCrawler implements Runnable {	

	private Thread myThread;

	private final static int PROCESS_OK = -12;

	private HTMLParser htmlParser;

	int myid;

	private CrawlController myController;

	private static short MAX_CRAWL_DEPTH = Configurations.getShortProperty("crawler.max_depth", (short) -1);
	private static boolean IGNORE_BINARY_CONTENT = !Configurations.getBooleanProperty("crawler.include_binary_content", true);
	private static final boolean FOLLOW_REDIRECTS = Configurations.getBooleanProperty("fetcher.follow_redirects", true);
        private boolean finish;
        private static boolean tracePageName = false;
        private static boolean traceLinkName = false;
        
        public static void setTraceLinkName(boolean b)
        {
            traceLinkName = b;
        }
        
        public static void setTracePageName(boolean b)
        {
            tracePageName = b;
        }

	public CrawlController getMyController() {
		return myController;
	}

	public void setMyController(CrawlController myController) 
        {            
            this.myController = myController;
	}

	public WebCrawler() {
                finish = false;
		htmlParser = new HTMLParser();
	}

	public WebCrawler(int myid) {
                finish = false;
		this.myid = myid;
	}

	public void setMyId(int myid) {
		this.myid = myid;
	}

	public int getMyId() {
		return myid;
	}

	public void onStart() {

	}

	public void onBeforeExit() {

	}

	public Object getMyLocalData() {
		return null;
	}

	public void run() {
		onStart();
		while (true)
                {
			List<WebURL> assignedURLs = new ArrayList<WebURL>(50);
                        ProjectLogger.LOGGER.debug("Take next URLS.");
			//frontier.getNextURLs(50, assignedURLs);
                        this.getMyController().getFrontier().getNextURLs(50, assignedURLs);
                        ProjectLogger.LOGGER.info("URLS " + assignedURLs.size() + " taked.");
			if (assignedURLs.size() == 0) {
				if (this.getMyController().getFrontier().isFinished()) {
                                        ProjectLogger.LOGGER.info("Frontier finish!.");
					return;
				}
				try {
                                        ProjectLogger.LOGGER.debug("Go to sleep!.");
					Thread.sleep(3000);
                                        ProjectLogger.LOGGER.debug("Wake up!.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				for (WebURL curURL : assignedURLs)
                                {
					if (curURL != null) {
						if (curURL.getDocid() == 14) {
							System.out.println();
						}
						if(!isFinish()) {
                                                    processPage(curURL);
                                                }
						this.getMyController().getFrontier().setProcessed(curURL);
					}
				}
			}                        
		}
	}

	public boolean shouldVisit(WebURL url) {
		return true;
	}

	public void visit(Page page) {
		// Should be implemented in sub classes
	}

	private int processPage(WebURL curURL) {
                
                int returnValue = PROCESS_OK;
		if (curURL == null) {
                        //ProjectLogger.LOGGER.debug("Exit processPage");
			return -1;
		}
                if(tracePageName) ProjectLogger.LOGGER.info("Process " + curURL.getURL());
		Page page = new Page(curURL);
                String lastURL = curURL.getURL();
		int statusCode = PageFetcher.fetch(page, IGNORE_BINARY_CONTENT, this.getMyController().getDocIDServer());
		// The page might have been redirected. So we have to refresh curURL
		curURL = page.getWebURL();
		int docid = curURL.getDocid();
		if (statusCode != PageFetchStatus.OK) 
                {
			if (statusCode == PageFetchStatus.Moved) 
                        {
				if (FOLLOW_REDIRECTS) 
                                {
                                    String movedToUrl = curURL.getURL();
                                    if (movedToUrl == null) 
                                    {
                                            //ProjectLogger.LOGGER.info("Exit processPage");
                                            return PageFetchStatus.MovedToUnknownLocation;
                                    }
                                    String movedToUrlAux = URLCanonicalizer.getCanonicalURL(movedToUrl);

                                    if(movedToUrlAux == null) 
                                    {

                                    } 
                                    else 
                                    {
                                        movedToUrl = movedToUrlAux;
                                    }

                                    int newdocid = this.getMyController().getDocIDServer().getDocID(movedToUrl);
                                    if (newdocid > 0) 
                                    {
                                            //ProjectLogger.LOGGER.info("Exit processPage");
                                            return PageFetchStatus.RedirectedPageIsSeen;
                                    } 
                                    else 
                                    {
                                            WebURL webURL = new WebURL();
                                            webURL.setURL(movedToUrl);
                                            webURL.setParentDocid(curURL.getParentDocid());
                                            webURL.setDepth((short) (curURL.getDepth()));
                                            webURL.setDocid(-1);
                                            webURL.setAssociateText(curURL.getAssociateText());
                                            webURL.setParentUrl(curURL.getParentUrl());
                                            if (shouldVisit(webURL) && RobotstxtServer.allows(webURL, this.getMyController().getDocIDServer())) {
                                                    webURL.setDocid(this.getMyController().getDocIDServer().getNewDocID(movedToUrl));	
                                                    this.getMyController().getFrontier().schedule(webURL);
                                            }
                                    }
				}
                                //ProjectLogger.LOGGER.info("Exit processPage");
				return PageFetchStatus.Moved;
			} 
                        else if (statusCode == PageFetchStatus.PageTooBig) 
                        {
				ProjectLogger.LOGGER.error("Page was bigger than max allowed size: " + curURL.getURL());
			}
                        //ProjectLogger.LOGGER.info("Exit processPage");
			return statusCode;
		}

		try 
                {
                    if (!page.isBinary()) 
                    {
                            htmlParser.parse(page.getHTML(), curURL.getURL(), curURL.getAssociateText());
                            page.setText(htmlParser.getText());
                            page.setTitle(htmlParser.getTitle());

                            if (page.getText() == null) 
                            {
                                    //ProjectLogger.LOGGER.info("Exit processPage");
                                    return PageFetchStatus.NotInTextFormat;
                            }

                            Iterator<Pair<String, String>> it = htmlParser.getLinks().iterator();
                            List<WebURL> toSchedule = new ArrayList<WebURL>();
                            List<WebURL> toList = new ArrayList<WebURL>();    

                            while (it.hasNext()) 
                            {
                                Pair<String, String> pair = it.next();                                        
                                String url = pair.getObject1();
                                String sAssociateText =  pair.getObject2();
                                if (url != null) 
                                {
                                    int newdocid = this.getMyController().getDocIDServer().getDocID(url);
                                    if (newdocid > 0) 
                                    {
                                        if (newdocid != docid) 
                                        {
                                            WebURL webURL = new WebURL();
                                            webURL.setURL(url);
                                            webURL.setDocid(newdocid);
                                            webURL.setAssociateText(sAssociateText);
                                            webURL.setParentUrl(curURL.getURL());

                                            if(traceLinkName) 
                                                ProjectLogger.LOGGER.info("Link " + webURL.toStringContent());

                                            toList.add(webURL);
                                        }                                                        
                                    } 
                                    else 
                                    {
                                        WebURL webURL = new WebURL();
                                        webURL.setURL(url);
                                        webURL.setDocid(-1);
                                        webURL.setParentDocid(docid);
                                        webURL.setDepth((short) (curURL.getDepth() + 1));
                                        webURL.setAssociateText(sAssociateText);
                                        webURL.setParentUrl(curURL.getURL());

                                        if(traceLinkName) 
                                            ProjectLogger.LOGGER.info("Link " + webURL.toStringContent());

                                        if (shouldVisit(webURL) && RobotstxtServer.allows(webURL, this.getMyController().getDocIDServer())) 
                                        {
                                            if (MAX_CRAWL_DEPTH == -1 || curURL.getDepth() < MAX_CRAWL_DEPTH) 
                                            {
                                                webURL.setDocid(this.getMyController().getDocIDServer().getNewDocID(url));
                                                toSchedule.add(webURL);
                                                toList.add(webURL);
                                            }
                                        }
                                    }
                                }
                            }
                            this.getMyController().getFrontier().scheduleAll(toSchedule);
                            page.setURLs(toList);
                    }                        
                    visit(page);
		} 
                catch (Exception e) 
                {
                    e.printStackTrace();
                    ProjectLogger.LOGGER.error(e.getMessage() + ", while processing: " + curURL.getURL());
		}
//                finally
//                {
//                    //ProjectLogger.LOGGER.info("Exit processPage");
//                }
                
		return PROCESS_OK;
	}

	public Thread getThread() {
		return myThread;
	}

	public void setThread(Thread myThread) {
		this.myThread = myThread;
	}

	public static void setMaximumCrawlDepth(short depth) {
		MAX_CRAWL_DEPTH = depth;
	}

        protected synchronized void setFinish()
        {
            finish = true;
        }
        
        public boolean isFinish()
        {
            return finish;
        }
}


