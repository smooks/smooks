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
package org.milyn.ect.ecore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.milyn.archive.Archive;

public class SchemaConverter {

	/**
	 * Singleton instance for convinience
	 */
	public static final SchemaConverter INSTANCE = new SchemaConverter();

	public static final String PLUGIN_XML_ENTRY = "plugin.xml";

	private static final String MANIFEST = "META-INF/MANIFEST.MF";

	private static final SimpleDateFormat qualifierFormat = new SimpleDateFormat(
			"yyyyMMdd-HHmm");

	private static final Log log = LogFactory.getLog(SchemaConverter.class);

	protected SchemaConverter() {
		// noop
	}

	/**
	 * Convert directory given as {@link InputStream} to the resulting archive
	 * 
	 * @param directoryInputStream
	 */
	public Archive createArchive(Set<EPackage> packages, String pluginID, String pathPrefix)
			throws IOException {
		String qualifier = qualifierFormat.format(Calendar.getInstance()
				.getTime());
		ResourceSet rs = prepareResourceSet();

		Archive archive = new Archive(pluginID + "_1.0.0.v" + qualifier
				+ ".jar");
		StringBuilder pluginBuilder = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
						+ "<?eclipse version=\"3.0\"?>\n" + "<plugin>\n");
		StringBuilder ecoreExtension = new StringBuilder(
				"\t<extension point=\"org.eclipse.emf.ecore.dynamic_package\">\n");
		StringBuilder xmlExtension = new StringBuilder(
				"\t<extension point=\"org.eclipse.wst.xml.core.catalogContributions\"><catalogContribution>\n");

		for (EPackage pkg : packages) {
			Resource resource = addSchemaResource(rs, pkg);
			EObject obj = resource.getContents().get(0);
			String fileName = resource.getURI().lastSegment();
			String ecoreEntryPath = pathPrefix + "/" + fileName;
			xmlExtension.append(saveSchema(archive, ecoreEntryPath, resource,
					((XSDSchema) obj).getTargetNamespace()));
			// Save memory
			resource.unload();
		}

		ecoreExtension.append("\t</extension>\n");
		xmlExtension.append("\t</catalogContribution></extension>\n");
		pluginBuilder.append(ecoreExtension);
		pluginBuilder.append(xmlExtension);
		pluginBuilder.append("</plugin>");
		archive.addEntry(PLUGIN_XML_ENTRY, pluginBuilder.toString());

		archive.addEntry(MANIFEST, generateManifest(pluginID, qualifier));

		return archive;
	}

	private Resource addSchemaResource(ResourceSet rs, EPackage pkg) {
		String message = pkg.getName();
		// Creating XSD resource
		log.debug(pkg.getName() + " schema generation start");
		Resource xsd = null;
		long start = System.currentTimeMillis();
		try {
			CustomSchemaBuilder schemaBuilder = new CustomSchemaBuilder(
					ExtendedMetaData.INSTANCE);
			XSDSchema schema = schemaBuilder.getSchema(pkg);
			xsd = rs.createResource(URI.createFileURI(message
					+ ".xsd"));
			if (!xsd.getContents().isEmpty()) {
				throw new RuntimeException("Duplicate schema "
						+ xsd.getURI());
			}
			xsd.getContents().add(schema);
		} catch (Exception e) {
			log.error("Failed to generate schema for " + pkg.getNsURI(), e);
		}
		log.info(pkg.getName() + " schema generation took " + (System.currentTimeMillis() - start) / 1000f
				+ " sec.");
		return xsd;
	}

	private String saveSchema(Archive archive, String entryPath,
			Resource resource, String ns) {
		StringBuilder result = new StringBuilder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		log.info("Saving XML Schema " + ns);
		try {
			resource.save(out, null);
			if (archive.getEntries().containsKey(entryPath)) {
				throw new RuntimeException("Duplicate entry " + entryPath);
			}
			archive.addEntry(entryPath, out.toByteArray());
			result.append("\t<uri name=\"");
			result.append(ns);
			result.append("\" uri=\"");
			result.append(entryPath);
			result.append("\"/>\n");
		} catch (Exception e) {
			log.error("Failed to save XML Schema " + ns, e);
		}
		return result.toString();
	}

	private String generateManifest(String pluginID, String qualfier) {
		StringBuilder result = new StringBuilder();
		result.append("Manifest-Version: 1.0\n");
		result.append("Bundle-ManifestVersion: 2\n");
		result.append("Bundle-Name: " + pluginID + "\n");
		result.append("Bundle-SymbolicName: " + pluginID + ";singleton:=true\n");
		result.append("Bundle-Version: 1.0.0.v" + qualfier + "\n");
		result.append("Bundle-ClassPath: .\n");
		result.append("Bundle-ActivationPolicy: lazy\n");
		return result.toString();
	}

	private ResourceSet prepareResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		/*
		 * Register XML Factory implementation using DEFAULT_EXTENSION
		 */
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("xsd", new XSDResourceFactoryImpl());

		return resourceSet;
	}

}
