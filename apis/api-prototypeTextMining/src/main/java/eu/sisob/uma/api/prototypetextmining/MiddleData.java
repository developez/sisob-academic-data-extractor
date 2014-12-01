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

package eu.sisob.uma.api.prototypetextmining;

import java.io.File;

/**
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class MiddleData
{
    private String id_entity = null;
    private String id_textminingparser = null;
    private String id_annotationrecollecting = null;
    private Object data_in = null;
    private Object data_out = null;
    private Object data_extra = null;
    private boolean verbose = false;
    private File verbose_dir = null;
    
    public MiddleData(String id_entity,
                      String id_textminingparser,
                      String id_annotationrecollecting,
                      Object data_in,
                      Object data_extra)
    {
        this.id_entity = id_entity;
        this.id_textminingparser = id_textminingparser;
        this.id_annotationrecollecting = id_annotationrecollecting;
        this.data_in = data_in;
        this.data_extra = data_extra;
        this.data_out = null;        
        this.verbose = false;
        this.verbose_dir = null;
    }
    
    public MiddleData(String id_entity,
                      String id_textminingparser,
                      String id_annotationrecollecting,
                      Object data_in,
                      Object data_extra,
                      boolean verbose,
                      File verbose_dir)
    {
        this.id_entity = id_entity;
        this.id_textminingparser = id_textminingparser;
        this.id_annotationrecollecting = id_annotationrecollecting;
        this.data_in = data_in;
        this.data_extra = data_extra;
        this.data_out = null;        
        this.verbose = verbose;
        this.verbose_dir = verbose_dir;
    }

    /**
     * @return the id_entity
     */
    public String getId_entity() {
        return id_entity;
    }

    /**
     * @return the id_textminingparser
     */
    public String getId_textminingparser() {
        return id_textminingparser;
    }

    /**
     * @return the id_annotationrecollecting
     */
    public String getId_annotationrecollecting() {
        return id_annotationrecollecting;
    }

    /**
     * @return the data_in
     */
    public Object getData_in() {
        return data_in;
    }

    /**
     * @return the data_out
     */
    public Object getData_out() {
        return data_out;
    }
    
    /**
     * @return the verbose
     */
    public boolean getVerbose() {
        return verbose;
    }
    
    /**
     * @param verbose_dir
     */
    public File getVerboseDir() {
        return verbose_dir;
    }

    /**
     * @param data_out the data_out to set
     */
    public void setData_out(Object data_out) {
        this.data_out = data_out;
    }

    /**
     * @return the data_extra
     */
    public Object getData_extra() {
        return data_extra;
    }

    /**
     * @param data_extra the data_extra to set
     */
    public void setData_extra(Object data_extra) {
        this.data_extra = data_extra;
    }
    
    /**
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * @param verbose_dir
     */
    public void setVerboseDir(File verbose_dir) {
        this.verbose_dir = verbose_dir;
    }

//    public static final int I_INDEX_DATA_ID = 0;
//    public static final int I_INDEX_DATA_TYPE = 1;
//    public static final int I_INDEX_DATA_IN = 2;
//    public static final int I_INDEX_DATA_OUT = 3;
//    public static final int I_INDEX_COUNT = 4;
    
}
