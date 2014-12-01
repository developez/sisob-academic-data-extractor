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

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import eu.sisob.uma.api.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */

public final class WebURLTupleBinding extends TupleBinding<WebURL> {

	@Override
	public WebURL entryToObject(TupleInput input) {
		WebURL webURL = new WebURL();
		webURL.setURL(input.readString());
		webURL.setDocid(input.readInt());
		webURL.setParentDocid(input.readInt());
		webURL.setDepth(input.readShort());
                webURL.setAssociateText(input.readString());
                webURL.setParentUrl(input.readString());
		return webURL;
	}

	@Override
	public void objectToEntry(WebURL url, TupleOutput output) {		
		output.writeString(url.getURL());
		output.writeInt(url.getDocid());
		output.writeInt(url.getParentDocid());
		output.writeShort(url.getDepth());
                output.writeString(url.getAssociateText());
                output.writeString(url.getParentUrl());
	}
}
