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
package org.smooks.javabean.pojogen;

import org.smooks.assertion.AssertArgument;
import org.smooks.util.FreeMarkerTemplate;
import org.smooks.io.StreamUtils;

import java.io.Serializable;
import java.util.*;
import java.io.Writer;
import java.io.IOException;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CannotCompileException;

/**
 * Java POJO model.
 * @author bardl
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JClass {

    private String uniqueId;
    private String packageName;
    private String className;
    private Set<JType> rawImports = new LinkedHashSet<JType>();
    private Set<JType> implementTypes = new LinkedHashSet<JType>();
    private Set<JType> extendTypes = new LinkedHashSet<JType>();
    private Set<JType> annotationTypes = new LinkedHashSet<JType>();
    private Class<?> skeletonClass;
    private List<JNamedType> properties = new ArrayList<JNamedType>();
    private List<JMethod> constructors = new ArrayList<JMethod>();
    private List<JMethod> methods = new ArrayList<JMethod>();
    private boolean fluentSetters = true;
    private boolean serializable = false;
    private boolean finalized = false;

    private static FreeMarkerTemplate template;

    static {
        try {
            template = new FreeMarkerTemplate(StreamUtils.readStreamAsString(JClass.class.getResourceAsStream("JavaClass.ftl")));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load JavaClass.ftl FreeMarker template.", e);
        }
    }

    public JClass(String packageName, String className) {
        this(packageName, className, UUID.randomUUID().toString());
    }

    public JClass(String packageName, String className, String uniqueId) {
        AssertArgument.isNotNull(packageName, "packageName");
        AssertArgument.isNotNull(className, "className");
        AssertArgument.isNotNull(uniqueId, "uniqueId");
        this.packageName = packageName;
        this.className = className;
        this.uniqueId = uniqueId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Set<JType> getRawImports() {
        return rawImports;
    }

    public Set<JType> getImplementTypes() {
        return implementTypes;
    }

    public Set<JType> getExtendTypes() {
        return extendTypes;
    }

    public Set<JType> getAnnotationTypes() {
        return annotationTypes;
    }

    public void setFluentSetters(boolean fluentSetters) {
        this.fluentSetters = fluentSetters;
    }

    public Class<?> getSkeletonClass() {
        if(skeletonClass == null) {
            String skeletonClassName = packageName + "." + className;

            try {
                skeletonClass = Thread.currentThread().getContextClassLoader().loadClass(skeletonClassName);
            } catch (ClassNotFoundException e) {
                ClassPool pool = new ClassPool(true);
                CtClass cc = pool.makeClass(skeletonClassName);

                try {
                    skeletonClass = cc.toClass();
                } catch (CannotCompileException ee) {
                    throw new IllegalStateException("Unable to create runtime skeleton class for class '" + skeletonClassName + "'.", ee);
                } finally {
                    cc.detach();
                }
            }
        }
        
        return skeletonClass;
    }

    public JClass setSerializable() {
        this.serializable = true;
        implementTypes.add(new JType(Serializable.class));
        return this;
    }

    public boolean isSerializable() {
        return serializable;
    }

    public void addProperty(JNamedType property) {
        AssertArgument.isNotNull(property, "property");
        assertPropertyUndefined(property);

        properties.add(property);
    }

    public JClass addBeanProperty(JNamedType property) {
        addProperty(property);

        String propertyName = property.getName();
        String capitalizedPropertyName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

        // Add property getter method...
        JMethod getterMethod = new JMethod(property.getType(), "get" + capitalizedPropertyName);
        getterMethod.appendToBody("return " + property.getName() + ";");
        methods.add(getterMethod);

        // Add property setter method...
        if(fluentSetters) {
            JMethod setterMethod = new JMethod(new JType(getSkeletonClass()), "set" + capitalizedPropertyName);
            setterMethod.addParameter(property);
            setterMethod.appendToBody("this." + property.getName() + " = " + property.getName() + ";  return this;");
            methods.add(setterMethod);
        } else {
            JMethod setterMethod = new JMethod("set" + capitalizedPropertyName);
            setterMethod.addParameter(property);
            setterMethod.appendToBody("this." + property.getName() + " = " + property.getName() + ";");
            methods.add(setterMethod);
        }

        return this;
    }

    public List<JNamedType> getProperties() {
        return properties;
    }

    public List<JMethod> getConstructors() {
        return constructors;
    }

    public List<JMethod> getMethods() {
        return methods;
    }

    public JMethod getDefaultConstructor() {
        for(JMethod constructor : constructors) {
            if(constructor.getParameters().isEmpty()) {
                return constructor;
            }
        }

        JMethod constructor = new JMethod(getClassName());
        constructors.add(constructor);

        return constructor;
    }

    public Set<Class<?>> getImports() {
        Set<Class<?>> importSet = new LinkedHashSet<Class<?>>();

        addImports(importSet, implementTypes);
        addImports(importSet, extendTypes);
        addImports(importSet, annotationTypes);
        for(JNamedType property : properties) {
            property.getType().addImports(importSet, new String[] {"java.lang", packageName});
        }
        addMethodImportData(constructors, importSet);
        addMethodImportData(methods, importSet);
        addImports(importSet, rawImports);

        return importSet;
    }

    private void addImports(Set<Class<?>> importSet, Collection<JType> types) {
        for(JType property : types) {
            property.addImports(importSet, new String[] {"java.lang", packageName});
        }
    }

    private void addMethodImportData(List<JMethod> methodList, Set<Class<?>> importSet) {
        for(JMethod method : methodList) {
            method.getReturnType().addImports(importSet, new String[] {"java.lang", packageName});
            for(JNamedType param : method.getParameters()) {
                param.getType().addImports(importSet, new String[] {"java.lang", packageName});
            }
            for(JType exception : method.getExceptions()) {
                exception.addImports(importSet, new String[] {"java.lang", packageName});
            }
        }
    }

    public String getImplementsDecl() {
        return PojoGenUtil.getTypeDecl("implements", implementTypes);
    }

    public String getExtendsDecl() {
        return PojoGenUtil.getTypeDecl("extends", extendTypes);
    }

    public void writeClass(Writer writer) throws IOException {
        Map<String, JClass> contextObj = new HashMap<String, JClass>();

        contextObj.put("class", this);
        writer.write(template.apply(contextObj));

        // Finalize all the methods... allowing them to be GC'd...
        finalizeMethods(constructors);
        finalizeMethods(methods);
        finalized = true;
    }

    public boolean isFinalized() {
        return finalized;
    }

    private void finalizeMethods(List<JMethod> methodList) {
        for(JMethod method : methodList) {
            method.finalizeMethod();
        }
    }

    private void assertPropertyUndefined(JNamedType property) {
        if(hasProperty(property.getName())) {
            throw new IllegalArgumentException("Property '" + property.getName() + "' already defined.");
        }
    }

    public boolean hasProperty(String propertyName) {
        for(JNamedType definedProperty : properties) {
            if(definedProperty.getName().equals(propertyName)) {
                return true;
            }
        }

        return false;
    }
}