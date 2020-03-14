package org.smooks.ect.ecore;

import static org.smooks.ect.ecore.ECoreConversionUtils.toJavaName;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import static org.junit.Assert.*;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.smooks.ect.formats.unedifact.UnEdifactSpecificationReader;

public class ECoreGenerationTest {

	private static final ExtendedMetaData metadata = ExtendedMetaData.INSTANCE;

	@Test
	public void testECoreGeneration() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/D99A.zip");
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);

		UnEdifactSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(
				zipInputStream, false, false);
		ECoreGenerator generator = new ECoreGenerator();
		Set<EPackage> packages = generator
				.generatePackages(ediSpecificationReader.getEdiDirectory());
		for (EPackage pkg : packages) {
			validatePackage(pkg);
			if ("cuscar".equals(pkg.getName())) {
				checkCUSCAR(pkg);
			}
			if ("common".equals(pkg.getName())) {
				assertEquals("Common namespace don't match", "urn:org.milyn.edi.unedifact:un:d99a:common", pkg.getNsURI());
			}
		}
	}

	private void checkCUSCAR(EPackage pkg) {
		assertEquals("Namespace don't match", "urn:org.milyn.edi.unedifact:un:d99a:cuscar", pkg.getNsURI());
		EClass clazz = (EClass) pkg.getEClassifier("CUSCAR");
		assertNotNull(clazz);
		assertEquals(13, clazz.getEStructuralFeatures().size());
		assertEquals(13, clazz.getEAllContainments().size());
		assertEquals("CUSCAR", metadata.getName(clazz));
	}

	private void validatePackage(EPackage pkg) {
		assertNotNull(pkg.getName() + " has document root",
				metadata.getDocumentRoot(pkg));
		EList<EClassifier> classifiers = pkg.getEClassifiers();
		Set<String> names = new HashSet<String>();
		for (EClassifier classifier : classifiers) {
			if (classifier instanceof EClass) {
				EClass clazz = (EClass) classifier;
				String location = pkg.getName() + "#" + clazz.getName();
				if (!"DocumentRoot".equals(clazz.getName())) {
					String metadataName = metadata.getName(clazz);
					boolean same = clazz.getName().equals(metadataName)
							|| clazz.getName().equals(
									toJavaName(metadataName, true));
					assertTrue(
							location + " metadata missmatch " + clazz.getName()
									+ "<>" + metadataName, same);
					assertTrue(location + " duplicate",
							names.add(clazz.getName()));
				}
			}
		}
	}
	
	@Test
	public void testMissingSegmentNames() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/d96b.zip");
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);

		UnEdifactSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(
				zipInputStream, false);
		ECoreGenerator generator = new ECoreGenerator();
		Set<EPackage> packages = generator
				.generatePackages(ediSpecificationReader.getEdiDirectory());
		boolean found = false;
		for (EPackage pkg : packages) {
			validatePackage(pkg);
			if ("cusdec".equals(pkg.getName())) {
				checkCUSDEC(pkg);
				found = true;
			}
		}
		assertTrue("Can't find cusdec package", found);
	}

	private void checkCUSDEC(EPackage pkg) {
		EClass root = (EClass) pkg.getEClassifier("CUSDEC");
		assertNotNull(root);
		assertEquals(23, root.getEStructuralFeatures().size());
		assertNotNull(root.getEStructuralFeature("UNS1"));
		assertNotNull(root.getEStructuralFeature("UNS2"));
		assertNull(root.getEStructuralFeature("UNS"));
	}

}
