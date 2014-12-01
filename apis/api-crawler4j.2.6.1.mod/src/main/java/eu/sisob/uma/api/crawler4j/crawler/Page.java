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

import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import eu.sisob.uma.api.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public class Page {

	private WebURL url;

	private String html;

	// Data for textual content
	private String text;
	private String title;

	// binary data (e.g, image content)
	// It's null for html pages
	private byte[] binaryData;

	private List<WebURL> urls;

	private ByteBuffer bBuf;

	private final static String defaultEncoding = Configurations
			.getStringProperty("crawler.default_encoding", "UTF-8");

	public boolean load(final InputStream in, final int totalsize,
			final boolean isBinary) {
		if (totalsize > 0) {
			this.bBuf = ByteBuffer.allocate(totalsize + 1024);
		} else {
			this.bBuf = ByteBuffer.allocate(PageFetcher.MAX_DOWNLOAD_SIZE);
		}
		final byte[] b = new byte[1024];
		int len;
		double finished = 0;
		try {
			while ((len = in.read(b)) != -1) {
				if (finished + b.length > this.bBuf.capacity()) {
					break;
				}
				this.bBuf.put(b, 0, len);
				finished += len;
			}
		} catch (final BufferOverflowException boe) {
			System.out.println("Page size exceeds maximum allowed.");
			return false;
		} catch (final Exception e) {
			System.err.println(e.getMessage());
			return false;
		}

		this.bBuf.flip();
		if (isBinary) {
			binaryData = new byte[bBuf.limit()];
			bBuf.get(binaryData);
		} else {
			this.html = "";
			this.html += Charset.forName(defaultEncoding).decode(this.bBuf);
			this.bBuf.clear();
			if (this.html.length() == 0) {
				return false;
			}
		}
		return true;
	}

	public Page(WebURL url) {
		this.url = url;
	}

	public String getHTML() {
		return this.html;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<WebURL> getURLs() {
		return urls;
	}

	public void setURLs(List<WebURL> urls) {
		this.urls = urls;
	}

	public WebURL getWebURL() {
		return url;
	}

	public void setWebURL(WebURL url) {
		this.url = url;
	}

	// Image or other non-textual pages
	public boolean isBinary() {
		return binaryData != null;
	}

	public byte[] getBinaryData() {
		return binaryData;
	}

}
