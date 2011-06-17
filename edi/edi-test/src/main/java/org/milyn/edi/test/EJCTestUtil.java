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
package org.milyn.edi.test;

import java.io.*;
import java.lang.reflect.Method;
import java.util.zip.ZipInputStream;

import org.milyn.archive.Archive;
import org.milyn.archive.ArchiveClassLoader;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.ejc.BeanWriter;
import org.milyn.ejc.BindingWriter;
import org.milyn.ejc.ClassModel;
import org.milyn.ejc.EJC;
import org.milyn.ejc.IllegalNameException;
import org.milyn.io.StreamUtils;
import org.milyn.test.ant.AntRunner;
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EJCTestUtil {

    public static final String ORG_SMOOKS_EJC_TEST = "org.smooks.ejc.test";

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
        Class callerClass = getCallerClass();

        if(callerClass == null) {
            throw new IllegalStateException("Failed to resolve caller class.");
        }
        InputStream mappingModel = callerClass.getResourceAsStream(ediMappingModelFile);
        testModel(ediMessageFile, mappingModel, factoryClassName, dump, callerClass);

    }

    public static void testModel(String ediMessageFile, InputStream mappingModelStream, String factoryClassName, boolean dump, Class callerClass) throws IOException, SAXException, IllegalNameException {

        Archive archive = null;
        try {
            archive = buildModelArchive(mappingModelStream, ORG_SMOOKS_EJC_TEST);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException("Exception building model archive: " + e.getMessage());
        }

        testModel(archive, ediMessageFile, factoryClassName, dump, callerClass);
    }

    public static void testModel(Archive archive, String ediMessageFile, String factoryClassName, boolean dump, Class callerClass) throws IOException {

        ArchiveClassLoader classLoader = new ArchiveClassLoader(archive);
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            testModel(ediMessageFile, factoryClassName, dump);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader.getParent());
        }
    }

    public static void testModel(String ediMessageFile, String factoryClassName, boolean dump) throws IOException {
        Class callerClass = getCallerClass();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Class factoryClass = null;
        Object factoryInstance = null;
        try {
            factoryClass = classLoader.loadClass(ORG_SMOOKS_EJC_TEST + "." + factoryClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException("Exception loading model Factory class: " + e.getMessage());
        }
        try {
            factoryInstance = factoryClass.getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Exception creating Factory class instance: " + e.getMessage());
        }

        String ediMessage = StreamUtils.readStreamAsString(callerClass.getResourceAsStream(ediMessageFile));
        Object modelInstance = null;

        try {
            modelInstance = findFromEDIMethod(factoryClass).invoke(factoryInstance, new StringReader(ediMessage));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Exception invoking 'fromEDI' method on Factory class instance: " + e.getMessage());
        }

        StringWriter ediOut = new StringWriter();
        try {
            findToEDIMethod(factoryClass).invoke(factoryInstance, modelInstance, ediOut);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Exception invoking 'fromEDI' method on Factory class instance: " + e.getMessage());
        }

        if(dump) {
            System.out.println("\n==== Serialized EDI Model ====");
            System.out.println(ediOut.toString());
            System.out.println("==============================\n");
        }

        String messageIn = StreamUtils.normalizeLines(ediMessage, false).trim();
        String messageOut = ediOut.toString().trim();
        if (!messageIn.equals(messageOut)) {
            throw new IllegalStateException("Message in: \n" + messageIn + "\n is not equal to message out \n" + messageOut);
        }
    }

    private static Class getCallerClass() {
        StackTraceElement[] thisStack = Thread.currentThread().getStackTrace();
        Class callerClass = null;

        for(int i = 0; i < thisStack.length; i++) {
            if(thisStack[i].getClassName().equals(EJCTestUtil.class.getName())) {
                String callingClass = thisStack[i + 1].getClassName();

                if(callingClass.equals(EJCTestUtil.class.getName())) {
                    continue;
                }

                try {
                    callerClass = Class.forName(callingClass);
                    break;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Exception resolving caller class: " + e.getMessage());
                }
            }
        }

        return callerClass;
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
