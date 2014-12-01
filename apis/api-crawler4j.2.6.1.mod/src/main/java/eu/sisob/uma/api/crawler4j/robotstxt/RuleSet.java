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

import java.util.SortedSet;
import java.util.TreeSet;

public class RuleSet extends TreeSet<String> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean add(String str) {
		SortedSet<String> sub = headSet(str);
		if (!sub.isEmpty() && str.startsWith((String) sub.last())) {
			// no need to add; prefix is already present
			return false;
		}
		boolean retVal = super.add(str);
		sub = tailSet(str + "\0");
		while (!sub.isEmpty() && ((String) sub.first()).startsWith(str)) {
			// remove redundant entries
			sub.remove(sub.first());
		}
		return retVal;
	}
	
	public boolean containsPrefixOf(String s) {
		SortedSet<String> sub = headSet(s);
		// because redundant prefixes have been eliminated,
		// only a test against last item in headSet is necessary
		if (!sub.isEmpty() && s.startsWith((String) sub.last())) {
			return true; // prefix substring exists
		} 
		// might still exist exactly (headSet does not contain boundary)
		return contains(s); 
	}
}
