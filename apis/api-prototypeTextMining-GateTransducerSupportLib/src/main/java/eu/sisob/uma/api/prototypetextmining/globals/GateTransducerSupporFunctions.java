/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 * Check: http://gate.ac.uk/sale/tao/splitch8.html#sec:jape:javarhsoverview
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

import java.util.*;
import gate.*;
import gate.annotation.AnnotationImpl;

public class GateTransducerSupporFunctions
{
    public static List<long[]> mergeCoextensiveAnnotations(gate.AnnotationSet varResearcherDegree)
    {
        List annList = new ArrayList((gate.AnnotationSet)varResearcherDegree);
        Collections.sort(annList, new OffsetBeginEndComparator());

        List<long[]> lst = new ArrayList<long[]>();
        Long begin_offset = null, end_offset = null;
        AnnotationImpl a;

        for(int i = 0; i < annList.size(); i++)
        {
            gate.Annotation varAnn = (Annotation)annList.get(i);

            //System.out.println(varAnn.getStartNode().getOffset() + " " + varAnn.getEndNode().getOffset());

            if(begin_offset == null) 
            {
                begin_offset = varAnn.getStartNode().getOffset();
                end_offset = varAnn.getEndNode().getOffset();
            }
            else
            {
                if(end_offset.equals(varAnn.getStartNode().getOffset()))
                {
                    end_offset = varAnn.getEndNode().getOffset();
                }
                else
                {
                    gate.FeatureMap features = Factory.newFeatureMap();
                    features.put("rule","ResearcherDegreePattern");
                    try
                    {
                        //outputAS.add(begin_offset, end_offset, sPrefix + "_" + "Degree", features); //System.out.println(" => " + begin_offset + " " + end_offset + " ANN");
                        //System.out.println(" => " + begin_offset + " " + end_offset + " ANN");
                        long[] al = new long[2];
                        al[0] = begin_offset.longValue();
                        al[1] = end_offset.longValue();
                        lst.add(al);
                    }
                    catch(Exception ex)
                    {
                    }
                    begin_offset = varAnn.getStartNode().getOffset();
                    end_offset = varAnn.getEndNode().getOffset();
                }
            }
        }

        gate.FeatureMap features = Factory.newFeatureMap();
        features.put("rule","ResearcherDegreePattern");
        try
        {
            long[] al = new long[2];
            al[0] = begin_offset.longValue();
            al[1] = end_offset.longValue();
            lst.add(al);
            //System.out.println(" => " + begin_offset + " " + end_offset + " ANN");
        }
        catch(Exception ex)
        {
        }

        return lst;
    }

    public static void exportAnnotationForJobOrgExpression(gate.AnnotationSet varDateCT,
                                                           gate.AnnotationSet varJob,
                                                           gate.AnnotationSet varDiscipline,
                                                           gate.AnnotationSet varOrg,
                                                           gate.AnnotationSet varLoc,
                                                           gate.AnnotationSet varDate)
    {        
        if(varDateCT != null)
        {
            
        }
        else
        {

        }
    }

}
