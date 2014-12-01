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

package eu.sisob.uma.api.prototypetextmining;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DATA STEP 4
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class RepositoryPreprocessDataMiddleData implements DataRepository
{    
    private List<MiddleData> lstPreProcessData;

    /**
     *
     */
    public RepositoryPreprocessDataMiddleData()
    {
        lstPreProcessData = new ArrayList<MiddleData>();
    }

    /**
     *
     * @return
     */
    public synchronized boolean isEmpty()
    {
        return this.lstPreProcessData.isEmpty();
    }

    /**
     * Get data with according "id_textminingparser"
     * @param id_textminingparser
     * @param iSize
     * @return List with appropiate MiddleData object
     */
    public synchronized List<MiddleData> getData(String id_textminingparser, int iSize)
    {
        List<MiddleData> lst = new ArrayList<MiddleData>();

        boolean exit = false;
        Iterator<MiddleData> it = lstPreProcessData.iterator();

        while(it.hasNext() && iSize > 0)
        {
            MiddleData md = it.next();
            if(md.getId_textminingparser().equals(id_textminingparser))
            {
                lst.add(md);
                it.remove();
                iSize--;
            }
        }

        return lst;
    }

    /**
     *
     * @param aoData
     */
    public synchronized void addData(MiddleData aoData) 
    {
        lstPreProcessData.add(aoData);
    }
    
   /**
     *
     */
    public synchronized void clearData() 
    {
        lstPreProcessData.clear();
    }    
     
    public synchronized List<MiddleData>  getMiddleDataList(){
        return lstPreProcessData;
    }      
    
}
