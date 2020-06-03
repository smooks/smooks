/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.xml;

import java.util.Hashtable;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.smooks.util.ClassUtil;

/**
 * 
 * @author Tom Fennelly
 */

public abstract class HTMLEntityLookup {

	/**
	 * Table providing the entity reference to character-code mappings.
	 */
	private static Hashtable<String,Character>  m_nameMap = new Hashtable<String, Character>();

	/**
	 * Table providing the character-code to entity reference mappings.
	 */
	private static Hashtable<Character,String>  m_codeMap = new Hashtable<Character,String>();

	/**
	 * Load the entities.
	 */
	static {

		// Lifted from XERCES - org.apache.xml.serialize.HTMLdtd.initialize();

		InputStream is = null;
		BufferedReader reader = null;
		int index;
		String name;
		String value;
		int code;
		String line;

		try {
			is = ClassUtil.getResourceAsStream("HTML.ent",
					HTMLEntityLookup.class);
			try {
				reader = new BufferedReader(new InputStreamReader(is));
			} catch (Exception except) {
				throw new IllegalStateException(
						"Illegal State ["
								+ except.getMessage()
								+ "]: HTML.ent not in classpath or in wrong package.  Should be in package "
								+ HTMLEntityLookup.class.getPackage().getName());
			}
			line = reader.readLine();
			while (line != null) {
				if (line.length() == 0 || line.charAt(0) == '#') {
					line = reader.readLine();
					continue;
				}
				index = line.indexOf(' ');
				if (index > 1) {
					name = line.substring(0, index);
					++index;

					if (index < line.length()) {
						value = line.substring(index);
						index = value.indexOf(' ');

						if (index > 0) {
							value = value.substring(0, index);
						}

						code = Integer.parseInt(value);
						defineEntity(name, (char) code);
					}
				}

				line = reader.readLine();
			}

			is.close();
		} catch (Exception except) {
			except.printStackTrace();
			throw new IllegalStateException(
					"Fatal ERROR: HTML.ent could not load: "
							+ except.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception except) {
				}
			}
		}
	}

	/**
	 * Defines a new character reference. The reference's name and value are
	 * supplied. Nothing happens if the character reference is already defined.
	 * <P>
	 * Unlike internal entities, character references are a string to single
	 * character mapping. They are used to map non-ASCII characters both on
	 * parsing and printing, primarily for HTML documents. '&lt;amp;' is an
	 * example of a character reference.
	 * 
	 * @param entityName
	 *            The entity's name
	 * @param charCode
	 *            The entity's value
	 */
	private static void defineEntity(String entityName, char charCode) {

		// This code was lifted from XERCES -
		// org.apache.xml.serialize.HTMLdtd.defineEntity(...);

		if (m_nameMap.get(entityName) == null) {
			m_nameMap.put(entityName, new Character(charCode));
			m_codeMap.put(new Character(charCode), entityName);
		}
	}

	/**
	 * Get the character code for the given entity reference name.
	 * 
	 * @param entityName
	 *            The entity name for the character code being sought.
	 * @return The character code for the entity, or null if it doesn't exist in
	 *         the table.
	 */
	public static Character getCharacterCode(String entityName) {
		return (Character) m_nameMap.get(entityName);
	}

	/**
	 * Get the entity reference name for the given character code.
	 * 
	 * @param charCode
	 *            The character code of the entity reference name being sought.
	 * @return The entity reference name, or null if it doesn't exist in the
	 *         table.
	 */
	public static String getEntityRef(char charCode) {
		return (String) m_codeMap.get(new Character(charCode));
	}
}
