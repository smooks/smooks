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
package org.milyn.ecore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.ecore.EcoreXMLSchemaBuilder;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.milyn.archive.Archive;
import org.milyn.ect.formats.unedifact.UnEdifactSpecificationReader;

public class SchemaConverter {

	/**
	 * Singleton instance for convinience
	 */
	public static final SchemaConverter INSTANCE = new SchemaConverter();

	public static final String PLUGIN_XML_ENTRY = "plugin.xml";

	private static final String MANIFEST = "META-INF/MANIFEST.MF";

	private static final SimpleDateFormat qualifierFormat = new SimpleDateFormat(
			"yyyyMMdd-HHmm");

	protected SchemaConverter() {
		// noop
	}

	/**
	 * Convert directory given as {@link InputStream} to the resulting archive
	 * 
	 * @param directoryInputStream
	 */
	public Archive createArchive(InputStream directoryStream, String pluginID)
			throws IOException {
		String qualifier = qualifierFormat.format(Calendar.getInstance()
				.getTime());
		ZipInputStream zipInputStream = new ZipInputStream(directoryStream);
		UnEdifactSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(
				zipInputStream, false);
		ECoreGenerator ecoreGen = new ECoreGenerator();
		Set<EPackage> packages = ecoreGen
				.generatePackages(ediSpecificationReader);
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
		String pathPrefix = pluginID.replace(".", "/");

		for (EPackage pkg : packages) {
			String message = pkg.getName();
			// Creating ecore resource
			Resource resource = rs.createResource(URI.createFileURI(message
					+ ".ecore"));
			resource.getContents().add(pkg);
			// Creating XSD resource
			try {
				EcoreXMLSchemaBuilder schemaBuilder = new EcoreXMLSchemaBuilder();
				Collection<EObject> generate = schemaBuilder.generate(pkg);
				Resource xsd = rs.createResource(URI.createFileURI(message
						+ ".xsd"));
				if (!xsd.getContents().isEmpty()) {
					throw new RuntimeException("Duplicate schema "
							+ xsd.getURI());
				}
				XSDSchema schema = (XSDSchema) generate.iterator().next();
				xsd.getContents().add(schema);
			} catch (Exception e) {
				System.err.println("Failed to generate schema for "
						+ pkg.getNsURI());
			}
		}

		EList<Resource> resources = rs.getResources();
		for (Resource resource : resources) {
			EObject obj = resource.getContents().get(0);
			String fileName = resource.getURI().lastSegment();
			String ecoreEntryPath = pathPrefix + "/" + fileName;
			if (obj instanceof EPackage) {
				ecoreExtension.append(savePackage(archive, ecoreEntryPath,
						resource, ((EPackage) obj).getNsURI()));
			} else {
				xmlExtension.append(saveSchema(archive, ecoreEntryPath,
						resource, ((XSDSchema) obj).getTargetNamespace()));
			}
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

	private Object saveSchema(Archive archive, String entryPath,
			Resource resource, String ns) {
		StringBuilder result = new StringBuilder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, null);
			if (archive.getEntries().containsKey(entryPath)) {
				throw new RuntimeException("Duplicate entry " + entryPath);
			}
			archive.addEntry(entryPath, out.toByteArray());
			result.append("<uri name=\"");
			result.append(ns);
			result.append("\" uri=\"");
			result.append(entryPath);
			result.append("\"/>");
		} catch (Exception e) {
			System.err.println("Failed to save XML Schema " + ns);
			e.printStackTrace();
		}
		return result.toString();
	}

	private String savePackage(Archive archive, String ecoreEntryPath,
			Resource resource, String ns) {
		StringBuilder result = new StringBuilder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, null);
			// Add the generated mapping model to the archive...
			archive.addEntry(ecoreEntryPath, out.toByteArray());
			// Add dynamic package to plugin.xml
			result.append("\t\t<resource \n\t\t\tlocation=\"");
			result.append(ecoreEntryPath);
			result.append("\" \n\t\t\turi=\"");
			result.append(ns);
			result.append("\">\n\t\t</resource>\n");

		} catch (Exception e) {
			System.err.println("Failed to save package " + ns);
			e.printStackTrace();
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
				.put("ecore", new EcoreResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("xsd", new XSDResourceFactoryImpl());

		return resourceSet;
	}

}
