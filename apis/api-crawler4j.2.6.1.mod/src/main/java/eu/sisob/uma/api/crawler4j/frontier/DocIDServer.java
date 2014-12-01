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

import com.sleepycat.je.*;
import eu.sisob.uma.api.crawler4j.crawler.ProjectLogger;

import eu.sisob.uma.api.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public final class DocIDServer {

	private Database docIDsDB = null;
	
	private Object mutex;

	private int lastDocID;
	private boolean resumable;

	public DocIDServer(Environment env, boolean resumable) throws DatabaseException 
        {
                mutex = env.getHome().getPath() + "_" + Frontier.class.toString() + "_Mutex";		
                resumable = resumable;
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTransactional(resumable);
		dbConfig.setDeferredWrite(!resumable); //FIXME !resumable
                
		docIDsDB = env.openDatabase(null, env.getHome().getPath() + "_" + Frontier.class.toString() + "_DocIDs", dbConfig);
		if (resumable) {
			int docCount = getDocCount();
			if (docCount > 0) {
				ProjectLogger.LOGGER.info("Loaded " + docCount + " URLs that had been detected in previous crawl.");
				lastDocID = docCount;
			}
		} else {
			lastDocID = 0;
		}
	}

	/**
	 * Returns the docid of an already seen url.
	 * If url is not seen before, it will return -1
	 */
	public int getDocID(String url) {
		synchronized (mutex) {
			if (docIDsDB == null) {
				return -1;
			}
			OperationStatus result = null;
			DatabaseEntry value = new DatabaseEntry();
			try {
				DatabaseEntry key = new DatabaseEntry(url.getBytes());
				result = docIDsDB.get(null, key, value, null);

				if (result == OperationStatus.SUCCESS && value.getData().length > 0) {
					return Util.byteArray2Int(value.getData());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
	}

	public int getNewDocID(String url) {
		synchronized (mutex) {
			try {
				// Make sure that we have not already assigned a docid for this URL
				int docid = getDocID(url);
				if (docid > 0) {
					return docid;
				}

				lastDocID++;
				docIDsDB.put(null, new DatabaseEntry(url.getBytes()), new DatabaseEntry(Util.int2ByteArray(lastDocID)));
				return lastDocID;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
	}

	public int getDocCount() {
		try {
			return (int) docIDsDB.count();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void sync() {
		if (resumable) {
			return;
		}
		if (docIDsDB == null) {
			return;
		}
		try {
			docIDsDB.sync();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
        
        public String getDatabaseName()
        {
            
                    String name = docIDsDB.getDatabaseName();
                    return name;
        }

	public void close() {
		try {
			docIDsDB.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
}
