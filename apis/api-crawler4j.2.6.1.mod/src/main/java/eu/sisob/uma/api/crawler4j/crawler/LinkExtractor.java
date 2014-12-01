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

import eu.sisob.uma.api.crawler4j.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.parser.Attribute;
import it.unimi.dsi.parser.BulletParser;
import it.unimi.dsi.parser.Element;
import it.unimi.dsi.parser.callback.DefaultCallback;
import it.unimi.dsi.util.TextPattern;

import java.util.Map;
import java.util.Set;

/**
 * This class is almost a copy/paste of
 * it.unimi.dsi.parser.callback.LinkExtractor but with support for extracting
 * image sources. The original class didn't allow overriding
 */

public class LinkExtractor extends DefaultCallback {
	/**
	 * The pattern prefixing the URL in a <samp>META </samp> <samp>HTTP-EQUIV
	 * </samp> element of refresh type.
	 */
	private static final TextPattern URLEQUAL_PATTERN = new TextPattern("URL=",
			TextPattern.CASE_INSENSITIVE);

	/** The URLs resulting from the parsing process. */
	public final Set<Pair<String, String>> urls = new ObjectLinkedOpenHashSet<Pair<String, String>>();

	/**
	 * The URL contained in the first <samp>META </samp> <samp>HTTP-EQUIV
	 * </samp> element of refresh type (if any).
	 */
	private String metaRefresh = null;

	/**
	 * The URL contained in the first <samp>META </samp> <samp>HTTP-EQUIV
	 * </samp> element of location type (if any).
	 */
	private String metaLocation = null;

	/** The URL contained in the first <samp>BASE </samp> element (if any). */
	private String base = null;

        private boolean includeImagesSources = false;

        private String associateText = "";

	/**
	 * Configure the parser to parse elements and certain attributes.
	 * 
	 * <p>
	 * The required attributes are <samp>SRC </samp>, <samp>HREF </samp>,
	 * <samp>HTTP-EQUIV </samp>, and <samp>CONTENT </samp>.
	 * 
	 */

	public void configure(final BulletParser parser) 
        {
                parser.parseText(true);
		parser.parseTags(true);                
		parser.parseAttributes(true);
                
		parser.parseAttribute(Attribute.SRC);
		parser.parseAttribute(Attribute.HREF);
		parser.parseAttribute(Attribute.HTTP_EQUIV);
		parser.parseAttribute(Attribute.CONTENT);
                parser.parseAttribute(Attribute.TITLE);
                parser.parseAttribute(Attribute.ALT);
                parser.parseAttribute(Attribute.VALUE);                
	}

	public void startDocument() {
		urls.clear();
		base = metaLocation = metaRefresh = null;
	}

        public final MutableString text = new MutableString();        
        private Element lastElement = null;
        private MutableString lastTitle = null;
        private Pair<String, String> lastPair = null;

        public boolean characters( final char[] characters, final int offset, final int length, final boolean flowBroken )
        {
            if((lastElement == Element.A || lastElement == Element.OPTION) && lastPair != null)
            {
                String s = new String(characters, offset, length);
                if(s.trim().equals("") && lastTitle != null)
                    s = lastTitle.toString();
                //Always "" in the first time!
                lastPair.setObject2(lastPair.getObject2() + s);
            }
            
            return true;
        }

        public boolean endElement(final Element element)
        {
             if(element == Element.BR ||
                element == Element.B ||
                element == Element.SMALL ||
                element == Element.BIG ||
                element == Element.STRONG ||
                element == Element.FONT ||
                element == Element.STRIKE ||
                element == Element.SPAN ||
                element == Element.EM ||
                element == Element.SCRIPT)
             {
                    return true;
             }
             
             if((lastElement == Element.A || lastElement == Element.OPTION) && lastPair != null)
             {
                //System.out.println("ADDED: " + lastPair.getObject1() + " - " + lastPair.getObject2());
                urls.add(lastPair);
             }

             lastElement = null;
             lastPair = null;
             lastTitle = null;
             return true;
        }

        @Override
	public boolean startElement(final Element element,
			final Map<Attribute, MutableString> attrMap) 
        {
                Object s;

                if(element == Element.A)
                {
                    s = attrMap.get(Attribute.HREF);
                    if (s != null) {
                            lastPair = new Pair<String, String>(s.toString(), "");
                            lastElement = Element.A;
                            lastTitle = attrMap.get(Attribute.TITLE);
                    }
                    //takeText = true;
                    return true;
                }

                if(element == Element.OPTION)
                {
                    s = attrMap.get(Attribute.VALUE);
                    if (s != null) {
                        if(s.toString().startsWith("/") || s.toString().startsWith("http:"))
                        {
                            lastPair = new Pair<String, String>(s.toString(), "");
                            lastElement = Element.OPTION;
                            lastTitle = attrMap.get(Attribute.VALUE);
                        }
                    }
                    //takeText = true;
                    return true;
                }

                if((element == Element.IMG) &&
                   (lastElement == Element.A && lastPair != null))
                {
                    lastTitle = attrMap.get(Attribute.TITLE);
                    if(lastTitle != null && lastPair.getObject2().equals(""))
                    {
                        lastPair.setObject2(lastTitle.toString());
                    }
                    else
                    {
                        lastTitle = attrMap.get(Attribute.ALT);
                        if(lastTitle != null && lastPair.getObject2().equals(""))
                        {
                            lastPair.setObject2(lastTitle.toString());
                        }
                    }
                    return true;
                }

                if(element == Element.BR ||
                   element == Element.B ||
                   element == Element.SMALL ||
                   element == Element.BIG ||
                   element == Element.STRONG ||
                   element == Element.FONT ||
                   element == Element.STRIKE ||
                   element == Element.SPAN ||
                   element == Element.EM ||
                   element == Element.SCRIPT) //LAST CHANGE
                {
                    return true;
                }    
		
                lastElement = null;
                lastPair = null;
                lastTitle = null;

		if (element == Element.AREA
            	 || element == Element.LINK) {
			s = attrMap.get(Attribute.HREF);
                        if (s != null) {
				urls.add(new Pair<String, String>(s.toString(), ""));
			}                        
			return true;
		} else if (includeImagesSources && element == Element.IMG) {
			s = attrMap.get(Attribute.SRC);
			if (s != null) {
				urls.add(new Pair<String, String>(s.toString(), ""));
			}
			return true;
		}

		// IFRAME or FRAME + SRC
		if (element == Element.IFRAME || element == Element.FRAME
				|| element == Element.EMBED) {
			s = attrMap.get(Attribute.SRC);
			if (s != null) {
				urls.add(new Pair<String, String>(s.toString(), associateText));
			}
			return true;
		}

		// BASE + HREF (change context!)
		if (element == Element.BASE && base == null) {
			s = attrMap.get(Attribute.HREF);
			if (s != null) {
				base = s.toString();
			}
		}

		// META REFRESH/LOCATION
		if (element == Element.META) {
			final MutableString equiv = attrMap.get(Attribute.HTTP_EQUIV);
			final MutableString content = attrMap.get(Attribute.CONTENT);
			if (equiv != null && content != null) {
				equiv.toLowerCase();

				// http-equiv="refresh" content="0;URL=http://foo.bar/..."
				if (equiv.equals("refresh") && (metaRefresh == null)) {

					final int pos = URLEQUAL_PATTERN.search(content);
					if (pos != -1)
						metaRefresh = content.substring(
								pos + URLEQUAL_PATTERN.length()).toString();
				}

				// http-equiv="location" content="http://foo.bar/..."
				if (equiv.equals("location") && (metaLocation == null))
					metaLocation = attrMap.get(Attribute.CONTENT).toString();
			}
		}

		return true;
	}

	/**
	 * Returns the URL specified by <samp>META </samp> <samp>HTTP-EQUIV </samp>
	 * elements of location type. More precisely, this method returns a non-
	 * <code>null</code> result iff there is at least one <samp>META HTTP-EQUIV
	 * </samp> element specifying a location URL (if there is more than one, we
	 * keep the first one).
	 * 
	 * @return the first URL specified by a <samp>META </samp> <samp>HTTP-EQUIV
	 *         </samp> elements of location type, or <code>null</code>.
	 */
	public String metaLocation() {
		return metaLocation;
	}

	/**
	 * Returns the URL specified by the <samp>BASE </samp> element. More
	 * precisely, this method returns a non- <code>null</code> result iff there
	 * is at least one <samp>BASE </samp> element specifying a derelativisation
	 * URL (if there is more than one, we keep the first one).
	 * 
	 * @return the first URL specified by a <samp>BASE </samp> element, or
	 *         <code>null</code>.
	 */
	public String base() {
		return base;
	}

	/**
	 * Returns the URL specified by <samp>META </samp> <samp>HTTP-EQUIV </samp>
	 * elements of refresh type. More precisely, this method returns a non-
	 * <code>null</code> result iff there is at least one <samp>META HTTP-EQUIV
	 * </samp> element specifying a refresh URL (if there is more than one, we
	 * keep the first one).
	 * 
	 * @return the first URL specified by a <samp>META </samp> <samp>HTTP-EQUIV
	 *         </samp> elements of refresh type, or <code>null</code>.
	 */
	public String metaRefresh() {
		return metaRefresh;
	}

	public boolean isIncludeImagesSources() {
		return includeImagesSources;
	}

	public void setIncludeImagesSources(boolean includeImagesSources) {
		this.includeImagesSources = includeImagesSources;
	}

        /**
         * Aux var for store associateText for parsed page
         */
        public void setAssociateText(String s)
        {
            associateText = s;
        }
}
