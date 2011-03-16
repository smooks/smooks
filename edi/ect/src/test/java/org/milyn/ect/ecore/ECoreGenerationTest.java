package org.milyn.ect.ecore;

import static org.milyn.ect.ecore.ECoreConversionUtils.toJavaName;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.milyn.ect.formats.unedifact.UnEdifactSpecificationReader;

public class ECoreGenerationTest extends TestCase {

	private static final ExtendedMetaData metadata = ExtendedMetaData.INSTANCE;

	public void testECoreGeneration() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/D99A.zip");
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);

		UnEdifactSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(
				zipInputStream, false);
		ECoreGenerator generator = new ECoreGenerator();
		Set<EPackage> packages = generator
				.generatePackages(ediSpecificationReader);
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

}
