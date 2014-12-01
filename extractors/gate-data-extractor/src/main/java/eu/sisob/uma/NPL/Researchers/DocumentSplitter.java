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

package eu.sisob.uma.NPL.Researchers;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;


/**
 * Object that splits one document in list of subdocuments (info blocks) (TODO FIXME, this will be an interface but now is a alone class)
 *  
 *** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class DocumentSplitter 
{   
    final int MAX_DIFF_IN_WORDS = 3;
    private HashMap<String, String[]> blocks_and_keywords;
    
    /**
     *
     * @param blocks_and_keywords
     */
    public DocumentSplitter(HashMap<String, String[]> blocks_and_keywords)
    {
        this.blocks_and_keywords = blocks_and_keywords;
    }
    
    /**
     *
     * @param line
     * @param line_number
     * @param separators
     */
    public void detectSeparator(String line, Integer line_number, List<Entry<String, Integer>> separators)
    {      
        String[] aux = line.split(":");
        if(aux.length > 0)
            line = aux[0];
        
        line = line.toLowerCase().replaceAll("[^A-Za-z0-9]"," ").trim();
        while(line.contains("  "))
            line = line.replace("  ", " ");       
        
        List<Entry<String, Integer>> separators_aux = new ArrayList<Entry<String, Integer>>();
        
        for(String info_block_name : blocks_and_keywords.keySet())
        {
            for(String separator : blocks_and_keywords.get(info_block_name))
            {   
                int number_words_line = line.split(" ").length;
                int info_block_name_line = separator.split(" ").length;
                
                if(info_block_name_line + MAX_DIFF_IN_WORDS >= number_words_line)
                {
                    if(line.contains(separator.toLowerCase()))
                    {
                        separators_aux.add(new AbstractMap.SimpleEntry<String, Integer>(info_block_name, line_number));
                        break;
                    }
                }                
                else if(info_block_name_line == number_words_line)
                {
                    if(line.contains(separator))
                    {
                        separators_aux.add(new AbstractMap.SimpleEntry<String, Integer>(info_block_name, line_number));
                        break;
                    }
                }       
            }
        }
        
        boolean has_others = false;
        for(Entry<String, Integer> entry : separators_aux)
        {
            if(entry.getKey().equals(CVBlocks.CVBLOCK_OTHERS.toString()))
            {
                has_others = true;
            }
        }
        
        for(Entry<String, Integer> entry : separators_aux)
        {
            if(has_others)
            {
                if(entry.getKey().equals(CVBlocks.CVBLOCK_OTHERS.toString()))
                {
                    separators.add(entry);
                }
            }
            else
            {
                separators.add(entry);
            }
        }        
    }
    
    private class EntryStringIntegerComparator implements Comparator<Entry<String, Integer>> 
    {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) 
        {
            if (o1.getValue() <= o2.getValue()) 
            {
                return -1;
            } 
            else 
            {
                return 1;
            }
        }
    }
    
    /**
     *
     * @param content
     * @return
     */
    public List<Entry<String, String>> SplitDocument(String content)
    {       
        List<Entry<String, String>> e = new ArrayList<Entry<String, String>>();        
        List<Entry<String, Integer>> separators = new ArrayList<Entry<String, Integer>>();        
        String[] lines = null;
        String newline = "";
        
        if(content.contains("\n")) newline = "\n";        
        if(content.contains("\r\n")) newline = "\r\n";        
        
        lines = content.split(newline);                
        
        while(content.contains(newline + newline + newline))
        {
            content = content.replace(newline + newline + newline, newline);
        }
        
        for(int i = 0; i < lines.length; i++)        
        {        
            detectSeparator(lines[i], i, separators);
        }
         
        Collections.sort(separators, new EntryStringIntegerComparator());
 
        //FIXME, This can be improved       
        /*
         * - It takes the blocks and it puts into the map, next, 
         * - we take the text that was not filtered and tag it with GENERIC BLOCK or something shit         
         * - In the comparision zone in order to indentify the kind of block, we can use "contains" in a line that seems a subsets (no many words, some : or .)
         */
        
        if(separators.size() > 0)
        {                           
            for(int i = 0; i < separators.size(); i++)
            {                
                if(i + 1 < separators.size())
                {                         
                    String kind = separators.get(i).getKey();
                    String sub_content = copyLinesToString(lines, separators.get(i).getValue(), separators.get(i+1).getValue(), newline);                    
                    e.add(new AbstractMap.SimpleEntry<String, String>(kind, sub_content)); 
                }
                else
                {
                    String kind = separators.get(i).getKey();
                    String sub_content = copyLinesToString(lines, separators.get(i).getValue(), null, newline);
                    e.add(new AbstractMap.SimpleEntry<String, String>(kind, sub_content)); 
                }                
            }
            
            int offset = 0;
            for(int i = 0; i < separators.size(); i++)
            {                
                if(i + 1 < separators.size())
                {                    
                    removeLines(lines, separators.get(i).getValue(), separators.get(i+1).getValue());                
                }
                else
                {
                    removeLines(lines, separators.get(i).getValue(), null);
                }
            }
            
            String rest_content = StringUtils.join(lines, newline);
            while(rest_content.contains(newline + newline + newline))
            {
                rest_content = rest_content.replace(newline + newline + newline, newline);
            }
            
            e.add(new AbstractMap.SimpleEntry<String, String>(CVBlocks.CVBLOCK_REST.toString(), rest_content));             
        }
        
        return e;
    }                        
    
    private String copyLinesToString(String[] lines, Integer ini, Integer end, String newline)
    {
        String sub_content = "";
        if(end == null)
        {
            end = lines.length;
        }
        
        for(int j = ini; j < end; j++)
        {
            sub_content += lines[j] + newline;
        }
        
        return sub_content;
    }
    
    private String removeLines(String[] lines, Integer ini, Integer end)
    {
        String sub_content = "";
        if(end == null)
        {
            end = lines.length;
        }
        
        for(int j = ini; j < end; j++)
        {
            lines[j] = "";
        }
        
        return sub_content;
    }
}
