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

import org.apache.log4j.Logger;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import eu.sisob.uma.api.crawler4j.crawler.ProjectLogger;

import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.api.crawler4j.util.Util;

/**
 * This class maintains the list of pages which are
 * assigned to crawlers but are not yet processed.
 * It is used for resuming a previous crawl. 
 * 
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public final class InProcessPagesDB extends WorkQueues {
		
	public InProcessPagesDB(Environment env) throws DatabaseException {
		super(env, "InProcessPagesDB", true);
		long docCount = getLength();
		if (docCount > 0) {
			ProjectLogger.LOGGER.info("Loaded " + docCount + " URLs that have been in process in the previous crawl.");
		}
	}

	public boolean removeURL(WebURL webUrl) {
		synchronized (mutex) {
			try {
				DatabaseEntry key = new DatabaseEntry(Util.int2ByteArray(webUrl.getDocid()));				
				Cursor cursor = null;
				OperationStatus result = null;
				DatabaseEntry value = new DatabaseEntry();
				Transaction txn = env.beginTransaction(null, null);
				try {
					cursor = urlsDB.openCursor(txn, null);
					result = cursor.getSearchKey(key, value, null);
					
					if (result == OperationStatus.SUCCESS) {
						result = cursor.delete();
						if (result == OperationStatus.SUCCESS) {
							return true;
						}
					}
				} catch (DatabaseException e) {
					if (txn != null) {
						txn.abort();
						txn = null;
					}
					throw e;
				} finally {
					if (cursor != null) {
						cursor.close();
					}
					if (txn != null) {
						txn.commit();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
