/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.edi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.milyn.util.ClassUtil;

/**
 * Extension of Xerces {@link XMLEntityManager} that implements
 * {@link XMLEntityResolver} which additinally able to resolve Smooks XML
 * Schemas
 * 
 * @author zubairov
 */
public class EDISchemaEntityManager extends XMLEntityManager {

	private final Map<String, String> catalog;

	private static final Log log = LogFactory.getLog(EDISchemaEntityManager.class);

	/**
	 * Factory method to create a new instance
	 * 
	 * @return
	 * @throws IOException
	 */
	public static EDISchemaEntityManager createInstance() throws IOException {
		List<URL> urnFiles = ClassUtil.getResources("/fragment.xml",
				EDISchemaEntityManager.class);
		Map<String, String> catalog = new HashMap<String, String>();
		log.debug("Loading XML schemas information from " + urnFiles);
		for (URL url : urnFiles) {
			InputStream in = url.openStream();
			try {
				if (in != null) {
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(in);
					@SuppressWarnings("unchecked")
					Iterator<Element> it = document
							.getDescendants(new ElementFilter("uri"));
					while (it.hasNext()) {
						Element next = it.next();
						String uri = next.getAttributeValue("uri");
						String name = next.getAttributeValue("name");
						// URI is now something like platform:/fragment/org.milyn.edi.unedifact.d99a-mapping/path/path/file.xsd
						// we need only /path/path/file.xsd
						// cut platform:/fragment/
						uri = uri.substring(19);
						// cut after first '/'
						uri = uri.substring(uri.indexOf('/'));
						catalog.put(name, uri);
					}
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		}
		// One resource we have to add manually
		catalog.put("urn:org.milyn.edi.unedifact.v41",
				"/META-INF/schema/v41-segments.xsd");
		log.debug("Loaded " + catalog.size() + " entries");
		return new EDISchemaEntityManager(catalog);
	}

	/**
	 * Constructor called from {@link #createInstance()} with Map NamespaceURI
	 * --> ResourceLocation
	 * 
	 * @param catalog
	 */
	protected EDISchemaEntityManager(Map<String, String> catalog) {
		super();
		this.catalog = catalog;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
			throws IOException, XNIException {
		XMLInputSource result = super.resolveEntity(resourceIdentifier);
		if (result.getPublicId() == null && result.getSystemId() == null
				&& result.getCharacterStream() == null
				&& result.getByteStream() == null) {
			String ns = resourceIdentifier.getNamespace();
			if (ns != null) {
				log.debug("Resolving schema to namespace: " + ns);
				if (catalog.containsKey(ns)) {
					result = new XMLInputSource(resourceIdentifier);
					String location = catalog.get(ns);
					InputStream stream = EDISchemaEntityManager.class
							.getResourceAsStream(location);
					if (stream == null) {
						throw new XNIException(
								"Smooks Entity Manager was unable to find resource "
										+ "with location: " + location);
					}
					result.setByteStream(stream);
				} else {
					throw new XNIException("Can't find schema with NS: " + ns);
				}
			}
		}
		return result;
	}

}
