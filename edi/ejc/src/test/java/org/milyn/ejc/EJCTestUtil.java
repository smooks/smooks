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
package org.milyn.ejc;

import java.io.*;
import java.lang.reflect.Method;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.milyn.archive.Archive;
import org.milyn.archive.ArchiveClassLoader;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.io.StreamUtils;
import org.milyn.test.ant.AntRunner;
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EJCTestUtil {

    private static final String ORG_SMOOKS_EJC_TEST = "org.smooks.ejc.test";

    public static void assertEquals(ClassModel model, InputStream expectedModel) throws IOException, IllegalNameException, ClassNotFoundException {
        StringWriter writer = new StringWriter();

        BeanWriter.writeBeans(model, writer);
        BindingWriter.writeBindingConfig(model, writer);
        
        String expected = StreamUtils.readStreamAsString(expectedModel);
        String actual = writer.toString();

        System.out.println(actual);
		
        TestCase.assertEquals("Expected mapping model not the same as actual.", StreamUtils.normalizeLines(expected, true), StreamUtils.normalizeLines(actual, true));
	}

    public static void dumpModel(InputStream mappingModel) throws EDIConfigurationException, ClassNotFoundException, IOException, SAXException, IllegalNameException {
        EJC ejc = new EJC();

        ClassModel model = ejc.compile(mappingModel, ORG_SMOOKS_EJC_TEST);

        Writer writer = new PrintWriter(System.out);
        BeanWriter.writeBeans(model, writer);
        BindingWriter.writeBindingConfig(model, writer);
    }

    public static Archive buildModelArchive(InputStream mappingModel, String modelJavaPackage) throws EDIConfigurationException, ClassNotFoundException, IOException, SAXException, IllegalNameException {
        BeanWriter.setGenerateFromEDINR(true);

        try {
            AntRunner antRunner = new AntRunner("build.xml");
            EJC ejc = new EJC();

            antRunner.run("delete");
            ejc.compile(mappingModel, modelJavaPackage, "./target/ejc/src");
            antRunner.run("compile");

            return new Archive(new ZipInputStream(new FileInputStream("./target/ejc/ejc.jar")));
        } finally {
            BeanWriter.setGenerateFromEDINR(false);
        }
    }

    public static void testModel(String ediMappingModelFile, String ediMessageFile, String factoryClassName) throws EDIConfigurationException, IOException, SAXException, IllegalNameException {
        testModel(ediMappingModelFile, ediMessageFile, factoryClassName, false);
    }
    
    public static void testModel(String ediMappingModelFile, String ediMessageFile, String factoryClassName, boolean dump) throws EDIConfigurationException, IOException, SAXException, IllegalNameException {
        StackTraceElement[] thisStack = Thread.currentThread().getStackTrace();
        Class callerClass = null;

        for(int i = 0; i < thisStack.length; i++) {
            if(thisStack[i].getClassName().equals(EJCTestUtil.class.getName())) {
                try {
                    callerClass = Class.forName(thisStack[i + 1].getClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    TestCase.fail("Exception resolving caller class: " + e.getMessage());
                }
            }
        }

        if(callerClass == null) {
            TestCase.fail("Failed to resolve caller class.");
        }

        Archive archive = null;
        try {
            archive = buildModelArchive(callerClass.getResourceAsStream(ediMappingModelFile), ORG_SMOOKS_EJC_TEST);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            TestCase.fail("Exception building model archive: " + e.getMessage());
        }

        ArchiveClassLoader classLoader = new ArchiveClassLoader(archive);
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class factoryClass = null;
            Object factoryInstance = null;
            try {
                factoryClass = classLoader.loadClass(ORG_SMOOKS_EJC_TEST + "." + factoryClassName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                TestCase.fail("Exception loading model Factory class: " + e.getMessage());
            }
            try {
                factoryInstance = factoryClass.getMethod("getInstance").invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
                TestCase.fail("Exception creating Factory class instance: " + e.getMessage());
            }

            String ediMessage = StreamUtils.readStreamAsString(callerClass.getResourceAsStream(ediMessageFile));
            Object modelInstance = null;

            try {
                modelInstance = findFromEDIMethod(factoryClass).invoke(factoryInstance, new StringReader(ediMessage));
            } catch (Exception e) {
                e.printStackTrace();
                TestCase.fail("Exception invoking 'fromEDI' method on Factory class instance: " + e.getMessage());
            }

            StringWriter ediOut = new StringWriter();
            try {
                findToEDIMethod(factoryClass).invoke(factoryInstance, modelInstance, ediOut);
            } catch (Exception e) {
                e.printStackTrace();
                TestCase.fail("Exception invoking 'fromEDI' method on Factory class instance: " + e.getMessage());
            }

            if(dump) {
                System.out.println("\n==== Serialized EDI Model ====");
                System.out.println(ediOut.toString());
                System.out.println("==============================\n");
            }

            TestCase.assertEquals(StreamUtils.normalizeLines(ediMessage, false).trim(), ediOut.toString().trim());
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader.getParent());
        }
    }

    private static Method findFromEDIMethod(Class factoryClass) throws NoSuchMethodException {
        return factoryClass.getMethod("fromEDINR", new Class[] {Reader.class});
    }

    private static Method findToEDIMethod(Class factoryClass) throws NoSuchMethodException {
        Method[] methods = factoryClass.getDeclaredMethods();

        for(Method method : methods) {
            if(method.getName().equals("toEDI")) {
                return method;
            }
        }

        throw new NoSuchMethodException("Failed to find 'toEDI' method on factory class '" + factoryClass.getName() + "'.");
    }
}
