/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 02/10/2001
 *
 *  $Id: OffsetComparator.java 12006 2009-12-01 17:24:28Z thomas_heitz $
 *
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

package eu.sisob.uma.api.prototypetextmining.globals;

import java.util.Comparator;

import gate.Annotation;

/**
 * Compares annotations by start offset
 */
public class OffsetBeginEndComparator implements Comparator<Annotation> {

  public int compare(Annotation a1, Annotation a2){
    int result;

    // compare start offsets
    result = a1.getStartNode().getOffset().compareTo(
        a2.getStartNode().getOffset());

    // if start offsets are equal compare end offsets
    if(result == 0) {
      result = (-1)*
              (a1.getEndNode().getOffset().compareTo(a2.getEndNode().getOffset()));
    } // if

    return result;
  }
}