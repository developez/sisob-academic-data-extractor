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

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import eu.sisob.uma.api.crawler4j.url.WebURL;
import eu.sisob.uma.api.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public class WorkQueues {

	protected Database urlsDB = null;
	protected Environment env;

	private boolean resumable;

	private WebURLTupleBinding webURLBinding;

	protected Object mutex = "WorkQueues_Mutex";

	public WorkQueues(Environment env, String dbName, boolean resumable) throws DatabaseException {
		this.env = env;
		this.resumable = resumable;
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTransactional(resumable);
		dbConfig.setDeferredWrite(!resumable);
		urlsDB = env.openDatabase(null, dbName, dbConfig);
		webURLBinding = new WebURLTupleBinding();
	}

	public List<WebURL> get(int max) throws DatabaseException {
		synchronized (mutex) {
			int matches = 0;
			List<WebURL> results = new ArrayList<WebURL>(max);

			Cursor cursor = null;
			OperationStatus result = null;
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry value = new DatabaseEntry();
			Transaction txn;
			if (resumable) {
				txn = env.beginTransaction(null, null);
			} else {
				txn = null;
			}
			try {
				cursor = urlsDB.openCursor(txn, null);
				result = cursor.getFirst(key, value, null);

				while (matches < max && result == OperationStatus.SUCCESS) {
					if (value.getData().length > 0) {
						WebURL curi = (WebURL) webURLBinding.entryToObject(value);
						results.add(curi);
						matches++;
					}
					result = cursor.getNext(key, value, null);
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
			return results;
		}
	}

	public void delete(int count) throws DatabaseException {
		synchronized (mutex) {
			int matches = 0;

			Cursor cursor = null;
			OperationStatus result = null;
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry value = new DatabaseEntry();
			Transaction txn;
			if (resumable) {
				txn = env.beginTransaction(null, null);
			} else {
				txn = null;
			}
			try {
				cursor = urlsDB.openCursor(txn, null);
				result = cursor.getFirst(key, value, null);

				while (matches < count && result == OperationStatus.SUCCESS) {
					cursor.delete();
					matches++;
					result = cursor.getNext(key, value, null);
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
		}
	}

	public void put(WebURL curi) throws DatabaseException {
		byte[] keyData = Util.int2ByteArray(curi.getDocid());
		DatabaseEntry value = new DatabaseEntry();
		webURLBinding.objectToEntry(curi, value);
		Transaction txn;
		if (resumable) {
			txn = env.beginTransaction(null, null);
		} else {
			txn = null;
		}
		urlsDB.put(txn, new DatabaseEntry(keyData), value);
		if (resumable) {
			txn.commit();
		}
	}

	public long getLength() {
		try {
			return urlsDB.count();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void sync() {
		if (resumable) {
			return;
		}
		if (urlsDB == null) {
			return;
		}
		try {
			urlsDB.sync();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			urlsDB.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
}
