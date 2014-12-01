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

package eu.sisob.uma.api.crawler4j.url;

import java.io.*;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */


@Entity
public final class WebURL implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	private String url;
	private int docid;
	private int parentDocid;
	private short depth;
        private String associateText;
        private String parentUrl;

        public WebURL()
        {
            url = "";
            parentUrl = "";
            associateText = "";
        }
	
	public int getDocid() {
		return docid;
	}

	public void setDocid(int docid) {
		this.docid = docid;
	}

	public String getURL() {
		return url;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		WebURL url2 = (WebURL) o;
		if (url == null) {
			return false;
		}
		return url.equals(url2.getURL());

	}

	public String toString() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}
	
	public int getParentDocid() {
		return parentDocid;
	}

	public void setParentDocid(int parentDocid) {
		this.parentDocid = parentDocid;
	}
	
	public short getDepth() {
		return depth;
	}

	public void setDepth(short depth) {
		this.depth = depth;
	}

        public String getAssociateText()
        {
            return associateText;
        }

        public void setAssociateText(String s)
        {
            associateText = s;
        }
        
        public String getParentUrl()
        {
            return parentUrl;
        }

        public void setParentUrl(String s)
        {
            parentUrl = s;
        }

        public String toStringContent()
        {
            return "[" + associateText.replace("\r\n", "").replace("\r", "") + "] => [" + url + "]";
        }
}
