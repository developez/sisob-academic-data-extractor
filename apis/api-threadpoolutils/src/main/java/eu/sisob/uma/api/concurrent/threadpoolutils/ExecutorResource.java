/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
    Copyright (c) 2014 "(IA)2 Research Group. Universidad de M�laga"
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

package eu.sisob.uma.api.concurrent.threadpoolutils;

/**
 *
 * @author Daniel L�pez Gonz�lez (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class ExecutorResource 
{
    private Object resource;
    private boolean free;

    /**
     * @return the resource
     */
    public Object getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(Object resource) {
        this.resource = resource;
    }

    /**
     * @return the free
     */
    public synchronized boolean isFree() {
        return free;
    }

    /**
     * @param free the free to set
     */
    public synchronized void setFree(boolean free) {
        this.free = free;
    }
}
