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

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public class HostDirectives {

	// If we fetched the directives for this host more than
	// 24 hours, we have to refetch it.
	private static final long EXPIRATION_DELAY = 24 * 60 * 1000L;

	private RuleSet disallows = new RuleSet();
	private RuleSet allows = new RuleSet();

	private long timeFetched;
	private long timeLastAccessed;

	public HostDirectives() {
		timeFetched = System.currentTimeMillis();
	}

	public boolean needsRefetch() {
		return (System.currentTimeMillis() - timeFetched > EXPIRATION_DELAY);
	}

	public boolean allows(String path) {
		timeLastAccessed = System.currentTimeMillis();
		if (disallows.containsPrefixOf(path)) {
			return allows.containsPrefixOf(path);
		}
		return true;
	}

	public void addDisallow(String path) {
		disallows.add(path);
	}

	public void addAllow(String path) {
		allows.add(path);
	}
	
	public long getLastAccessTime() {
		return timeLastAccessed;
	}
}