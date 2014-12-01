/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

package eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ResearcherNameInfo 
{
    public String whole_name;
    public String last_name;
    public String first_name;
    public String initial;
    
    public ResearcherNameInfo(String last_name, String initial, String first_name, String whole_name)
    {
        this.whole_name = whole_name;
        this.last_name = last_name;
        this.first_name = first_name;
        this.initial = initial;        
    }
    
    public ResearcherNameInfo(ResearcherNameInfo rsi)
    {
        this.whole_name = rsi.whole_name;
        this.last_name = rsi.last_name;
        this.first_name = rsi.first_name;
        this.initial = rsi.initial;
    }
    
    @Override
    public ResearcherNameInfo clone()
    {
        ResearcherNameInfo rsi = new ResearcherNameInfo(this);
        return rsi;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.whole_name != null ? this.whole_name.hashCode() : 0);
        hash = 97 * hash + (this.last_name != null ? this.last_name.hashCode() : 0);
        hash = 97 * hash + (this.first_name != null ? this.first_name.hashCode() : 0);
        hash = 97 * hash + (this.initial != null ? this.initial.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj != null)
        {
            if(obj.getClass().equals(ResearcherNameInfo.class))
            {
                ResearcherNameInfo rsi = (ResearcherNameInfo)obj;
                if( (this.initial != null ? this.initial.equals(rsi.initial) : this.initial == rsi.initial) &&
                    (this.last_name != null ? this.last_name.equals(rsi.last_name)  : this.last_name == rsi.last_name) &&
                    (this.first_name != null ? this.first_name.equals(rsi.first_name) : this.first_name == rsi.first_name) &&
                    (this.whole_name != null ? this.whole_name.equals(rsi.whole_name) : this.whole_name == rsi.whole_name))
                {
                    return true;
                }
                else
                {
                    return false;
                }            
            }
            else
            {
                return false;
            }        
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        return this.last_name + ", " + this.initial;
    }
}
