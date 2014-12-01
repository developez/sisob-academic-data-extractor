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

package eu.sisob.uma.api.crawler4j.robotstxt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.sisob.uma.api.crawler4j.crawler.Configurations;
import eu.sisob.uma.api.crawler4j.crawler.Page;
import eu.sisob.uma.api.crawler4j.crawler.PageFetchStatus;
import eu.sisob.uma.api.crawler4j.crawler.PageFetcher;
import eu.sisob.uma.api.crawler4j.frontier.DocIDServer;
import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.api.crawler4j.frontier.DocIDServer;
import eu.sisob.uma.api.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */


public class RobotstxtServer {
	
	private static Map<String, HostDirectives> host2directives = new HashMap<String, HostDirectives>();
	
	private static final String USER_AGENT_NAME = Configurations.getStringProperty("fetcher.user_agent_name", "crawler4j");
	private static final int MAX_MAP_SIZE = Configurations.getIntProperty("crawler.robotstxt.max_map_size", 100);
	private static boolean active = Configurations.getBooleanProperty("crawler.obey_robotstxt", false);
	private static final Object mutex = RobotstxtServer.class.toString() + "_MUTEX"; 
	
	public static boolean allows(WebURL webURL, DocIDServer refdocIDServer) {
		if (!active) {
			return true;
		}
		try {
			URL url = new URL(webURL.getURL());
			String host = url.getHost().toLowerCase();
			String path = url.getPath();
			
			HostDirectives directives = host2directives.get(host);
			if (directives == null) {
				directives = fetchDirectives(host, refdocIDServer);
			} 
			return directives.allows(path);			
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		}
		return true;
	}
	
	public static void setActive(boolean active) {
		RobotstxtServer.active = active;
	}
	
	private static HostDirectives fetchDirectives(String host, DocIDServer refdocIDServer) {
		WebURL robotsTxt = new WebURL();
		robotsTxt.setURL("http://" + host + "/robots.txt");
		Page page = new Page(robotsTxt);
		int statusCode = PageFetcher.fetch(page, true, refdocIDServer);
		HostDirectives directives = null;
		if (statusCode == PageFetchStatus.OK) {
			directives = RobotstxtParser.parse(page.getHTML(), USER_AGENT_NAME);			
		}
		if (directives == null) {
			// We still need to have this object to keep track of the time we fetched it
			directives = new HostDirectives();
		}
		synchronized (mutex) {
			if (host2directives.size() == MAX_MAP_SIZE) {
				String minHost = null;
				long minAccessTime = Long.MAX_VALUE;
				for (Entry<String, HostDirectives> entry : host2directives.entrySet()) {
					if (entry.getValue().getLastAccessTime() < minAccessTime) {
						minAccessTime = entry.getValue().getLastAccessTime();
						minHost = entry.getKey();
					}					
				}
				host2directives.remove(minHost);
			}
			host2directives.put(host, directives);
		}
		return directives;
	}

}
