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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.milyn.util.ClassUtil;
import org.milyn.io.StreamUtils;

import java.io.*;
import java.util.Hashtable;
import java.net.URL;

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
	private File localEntityFolder = null;

	/**
	 * Entity entity lookup table. <p/> Contains preread Entity entity byte arrays.
	 */
	private static Hashtable entities = new Hashtable();

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
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		byte cachedBytes[] = (byte[]) entities.get(systemId);
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
			// this too fails try the classpath - specifically the org.milyn.dtd
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
