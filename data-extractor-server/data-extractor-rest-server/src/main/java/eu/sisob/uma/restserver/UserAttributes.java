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

package eu.sisob.uma.restserver;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class UserAttributes 
{    
    private String account_type;
    private Integer n_tasks_allow;

    /**
     * @return the account_type
     */
    public String getAccountType() {
        return account_type;
    }

    /**
     * @param account_type the account_type to set
     */
    public void setAccountType(String account_type) {
        this.account_type = account_type;
    }

    /**
     * @return the n_tasks_allow
     */
    public Integer getNTasksAllow() {
        return n_tasks_allow;
    }

    /**
     * @param n_tasks_allow the n_tasks_allow to set
     */
    public void setNTasksAllow(Integer n_tasks_allow) {
        this.n_tasks_allow = n_tasks_allow;
    }
}
