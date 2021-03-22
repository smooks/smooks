/*-
 * ========================LICENSE_START=================================
 * Commons
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

import org.smooks.support.StreamUtils;
import org.smooks.support.ClassUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;

/**
 * XSD resolver for local XSD's.
 *
 * @author tfennelly
 */
public abstract class LocalEntityResolver implements EntityResolver {

    /**
     * Entity classpath prefix.
     */
    private String entityCPLocation;

    /**
	 * Local Entity folder.
	 */
	private File localEntityFolder;

	/**
	 * Entity entity lookup table. <p/> Contains preread Entity entity byte arrays.
	 */
	private static final Hashtable<String,byte[]> entities = new Hashtable<String,byte[]>();

    /**
     * Document type.  This is a bit of a hack.  There's a way of getting the DOM
     * parser to populate the DocumentType.
     */
    private String docType;

    /**
	 * Public Constructor.
     * @param entityCPLocation Entity classpath location.
	 */
	public LocalEntityResolver(String entityCPLocation) {
        this.entityCPLocation = entityCPLocation;
    }

	/**
	 * Public default Constructor <p/> This constructor allows specification of
	 * a local file system folder from which Entitys can be loaded.
	 *
	 * @param localEntityFolder
	 *            Local Entity folder.
	 */
	public LocalEntityResolver(File localEntityFolder) {
		if (localEntityFolder == null) {
			throw new IllegalStateException(
					"Cannot resolve local entity.  Local entity folder arg 'null'.");
		}
		if (!localEntityFolder.exists()) {
			throw new IllegalStateException(
					"Cannot resolve local entity.  Local entity folder not present: ["
							+ localEntityFolder.getAbsolutePath() + "].");
		}
		this.localEntityFolder = localEntityFolder;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		byte[] cachedBytes = entities.get(systemId);
		InputSource entityInputSource = null;

        if (cachedBytes == null) {
			URL systemIdUrl = new URL(systemId);
			String entityPath = systemIdUrl.getHost() + systemIdUrl.getFile();
			String entityName = (new File(entityPath)).getName();
			File fileSysEntity = null;
			InputStream entityStream = null;

			// First try locate the file in the Entity folder based on the files
			// full path.
			// If this fails, try locate it in the root of the Entity folder
			// directly. If
			// this too fails try the classpath - specifically the org.smooks.engine.dtd
			// package.
			if (localEntityFolder != null) {
				fileSysEntity = new File(localEntityFolder, entityPath);
				if (!fileSysEntity.exists()) {
					fileSysEntity = new File(localEntityFolder, entityName);
				}
			}
			if (localEntityFolder != null && fileSysEntity.exists()) {
				entityStream = new FileInputStream(fileSysEntity);
			} else if (entityCPLocation != null) {
				entityStream = ClassUtil.getResourceAsStream(entityCPLocation + entityName, getClass());
				if (entityStream == null) {
					return null;
				}
			} else {
                throw new SAXException("Unable to resolve entity. " + getClass().getName() + " is not configured with a valid entity file or classpath location.");
            }

			// Read the entity stream and store it in the cache.
			cachedBytes = StreamUtils.readStream(entityStream);
            entities.put(systemId, cachedBytes);
		}

		entityInputSource = new InputSource(new ByteArrayInputStream(cachedBytes));
		entityInputSource.setPublicId(publicId);
		entityInputSource.setSystemId(systemId);

		return entityInputSource;
	}

	/**
	 * Clear the entity cache.
	 */
	public static void clearEntityCache() {
		entities.clear();
	}

    /**
     * Get the document type.
     * <p/>
     * This is a bit of a hack.  There's a way of getting the DOM
     * parser to populate the DocumentType.
     *
     * @return The Document type (systemId).
     */
    public String getDocType() {
        return docType;
    }

    /**
     * Set the document type for the resolver.
     * @param docType Primary document type.
     */
    public void setDocType(String docType) {
        this.docType = docType;
    }
}
