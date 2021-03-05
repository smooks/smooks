/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.support;

import java.io.*;

/**
 * Character utilities.
 * @author tfennelly
 */
public abstract class CharUtils {

    /**
	 * Compares the 2 Strings.
	 * @param s1 Stream 1.
	 * @param s2 Stream 2.
	 * @return True if the streams are equal not including leading and trailing
	 * whitespace on each line and blank lines, otherwise returns false.
	 */
    public static boolean compareStrings(String s1, String s2) {
        return compareCharStreams(new ByteArrayInputStream(s1.getBytes()), new ByteArrayInputStream(s2.getBytes()));
    }

    /**
	 * Compares the 2 streams.
	 * <p/>
	 * Calls {@link #trimLines(InputStream)} on each stream before comparing.
	 * @param s1 Stream 1.
	 * @param s2 Stream 2.
	 * @return True if the streams are equal not including leading and trailing 
	 * whitespace on each line and blank lines, otherwise returns false.
	 */
	public static boolean compareCharStreams(InputStream s1, InputStream s2) {
		StringBuffer s1Buf, s2Buf;
		
		try {
			s1Buf = trimLines(s1);
			s2Buf = trimLines(s2);
			
			return s1Buf.toString().equals(s2Buf.toString());
		} catch (IOException e) {
			// fail the comparison
		}
		
		return false;
	}
	
	/**
	 * Read the lines lines of characters from the stream and trim each line
	 * i.e. remove all leading and trailing whitespace.
	 * @param charStream Character stream.
	 * @return StringBuffer containing the line trimmed stream.
	 * @throws IOException
	 */
	public static StringBuffer trimLines(InputStream charStream) throws IOException {
		StringBuffer stringBuf = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(charStream));
		String line;

		while((line = reader.readLine()) != null) {
			stringBuf.append(line.trim());
		}
		
		return stringBuf;
	}
}
