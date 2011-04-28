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

import static org.milyn.ect.ecore.SmooksMetadata.INSTANCE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.milyn.edisax.model.EDIConfigDigester;
import org.milyn.edisax.model.internal.Edimap;

/**
 * Test case for conversion of build-in segment definitions
 * 
 * @author zubairov
 *
 */
public class ConvertBuildinSegmentsTest extends TestCase {

	private static final ExtendedMetaData METADATA = ExtendedMetaData.INSTANCE;

	public void testConversion() throws Exception {
		InputStream is = ConvertBuildinSegmentsTest.class.getResourceAsStream("/org/milyn/edisax/unedifact/handlers/r41/v41-segments.xml");
		assertNotNull("Can't find a v41-segments.xml", is);
		Edimap edimap = EDIConfigDigester.digestConfig(is);
		EPackage pkg = ECoreGenerator.INSTANCE.generateSinglePackage(edimap);
		assertEquals("urn:org.milyn.edi.unedifact.v41", pkg.getNsURI());
		assertEquals("unedifact", pkg.getNsPrefix());
		assertEquals(21, pkg.getEClassifiers().size());
		List<String> codz = new ArrayList<String>();
		for (EClassifier clazz : pkg.getEClassifiers()) {
			if (SmooksMetadata.INSTANCE.isSegment(clazz)) {
				codz.add(INSTANCE.getSegcode(clazz));
				
			}
		}
		Collections.sort(codz);
		assertEquals("[UNB, UNE, UNG, UNH, UNT, UNZ]", codz.toString());
		// Now we need to do a trick with Document Root
		EClass docRoot = METADATA.getDocumentRoot(pkg);
		assertEquals(1, docRoot.getEStructuralFeatures().size());
		// Fix name of the root element
		EStructuralFeature feature = docRoot.getEAllStructuralFeatures().get(0);
		assertNotNull("Can't find feature of DocumentRoot", feature);
		feature.setName("unEdifact");
		EReference ref = (EReference) docRoot.getEStructuralFeatures().get(0);
		EClassifier rootElementType = pkg.getEClassifier(ref.getEReferenceType().getName());
		METADATA.setName(rootElementType, "unEdifact");
		SchemaConverter.INSTANCE.convertEDIMap(pkg, new FileOutputStream(new File("./target/v41-segments.xsd")));
		saveECORE(pkg);
	}

	private void saveECORE(EPackage pkg) throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		/*
		 * Register XML Factory implementation using DEFAULT_EXTENSION
		 */
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("ecore", new EcoreResourceFactoryImpl());
		
		Resource resource = resourceSet.createResource(URI.createURI("buildin.ecore"));
		resource.getContents().add(pkg);
		resource.save(new FileOutputStream(new File("./target/buildin.ecore")), null);
	}
	
	
	
}
