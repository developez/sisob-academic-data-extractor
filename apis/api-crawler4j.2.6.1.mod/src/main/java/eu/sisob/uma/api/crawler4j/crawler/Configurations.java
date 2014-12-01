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

import java.util.Properties;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public final class Configurations {

	private static Properties prop = new Properties();

	public static String getStringProperty(String key, String defaultValue) {
		if (prop == null || prop.getProperty(key) == null) {
			return defaultValue;
		}
		return prop.getProperty(key);
	}

	public static int getIntProperty(String key, int defaultValue) {
		if (prop == null || prop.getProperty(key) == null) {
			return defaultValue;
		}
		return Integer.parseInt(prop.getProperty(key));
	}
	
	public static short getShortProperty(String key, short defaultValue) {
		if (prop == null || prop.getProperty(key) == null) {
			return defaultValue;
		}
		return Short.parseShort(prop.getProperty(key));
	}

	public static long getLongProperty(String key, long defaultValue) {
		if (prop == null || prop.getProperty(key) == null) {
			return defaultValue;
		}
		return Long.parseLong(prop.getProperty(key));
	}
	
	public static boolean getBooleanProperty(String key, boolean defaultValue) {
		if (prop == null || prop.getProperty(key) == null) {
			return defaultValue;
		}
		return prop.getProperty(key).toLowerCase().trim().equals("true");
	}

	static {
		try {
			prop.load(Configurations.class.getClassLoader()
					.getResourceAsStream("crawler4j.properties"));
		} catch (Exception e) {
			prop = null;
			System.err.println("WARNING: Could not find crawler4j.properties file in class path. I will use the default values.");
		}
	}
}
