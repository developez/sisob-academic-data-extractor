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

package eu.sisob.uma.api.prototypetextmining.gatedataextractor;

import java.util.Comparator;

import gate.Annotation;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
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