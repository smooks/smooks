/*
 Milyn - Copyright (C) 2006 - 2010

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License (version 2.1) as published by the Free Software 
 Foundation.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 
 See the GNU Lesser General Public License for more details:    
 http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.xml;

import java.util.Hashtable;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.milyn.util.ClassUtil;

/**
 * 
 * @author Tom Fennelly
 */

public abstract class HTMLEntityLookup {

	/**
	 * Table providing the entity reference to character-code mappings.
	 */
	private static Hashtable m_nameMap = new Hashtable();

	/**
	 * Table providing the character-code to entity reference mappings.
	 */
	private static Hashtable m_codeMap = new Hashtable();

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
