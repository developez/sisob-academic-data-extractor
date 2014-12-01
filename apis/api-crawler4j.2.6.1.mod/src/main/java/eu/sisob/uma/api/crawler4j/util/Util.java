/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

package eu.sisob.uma.api.crawler4j.util;

/**
 * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
 */


public final class Util {
	
	public static void longIntoByteArray(long l, byte[] array, int offset) {
        int i, shift;                  
        for(i = 0, shift = 56; i < 8; i++, shift -= 8)
        array[offset+i] = (byte)(0xFF & (l >> shift));
    }
	
	public static byte[] long2ByteArray(long l) {
		byte[] array = new byte[8];
        int i, shift;                  
        for(i = 0, shift = 56; i < 8; i++, shift -= 8) {
        	array[i] = (byte)(0xFF & (l >> shift));
        }
        return array;
    }
    
    public static long byteArrayIntoLong(byte [] bytearray) {
        return byteArrayIntoLong(bytearray, 0);
    }
    
    public static long byteArrayIntoLong(byte [] bytearray,
            int offset) {
        long result = 0;
        for (int i = offset; i < 8 /*Bytes in long*/; i++) {
            result = (result << 8 /*Bits in byte*/) |
                (0xff & (byte)(bytearray[i] & 0xff));
        }
        return result;
    }
    
    public static byte[] int2ByteArray(int value) {
    	byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
    
    public static int byteArray2Int(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
    
    public static long byteArray2Long(byte[] b) {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            int shift = (8 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
    
}
