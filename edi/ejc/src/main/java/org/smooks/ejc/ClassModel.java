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
package org.smooks.ejc;

import org.smooks.edisax.model.internal.Edimap;
import org.smooks.edisax.model.internal.MappingNode;
import org.smooks.javabean.pojogen.JClass;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ClassModel contains a Map of {@link org.smooks.javabean.pojogen.JClass} for easy lookup when
 * {@link org.smooks.ejc.BeanWriter} and {@link org.smooks.ejc.BindingWriter} needs to access the
 * classes.
 *
 * Holds information about the xmltag and typeParameters associated with a given {@link org.smooks.javabean.pojogen.JClass}
 * or {@link org.smooks.javabean.pojogen.JNamedType}. These values are held in the {@link org.smooks.ejc.ValueNodeInfo}.
 *
 * @see org.smooks.ejc.BeanWriter
 * @see org.smooks.ejc.BindingWriter
 * @see org.smooks.ejc.ValueNodeInfo
 * @author bardl
 */
public class ClassModel {

    private static final Logger LOGGER = EJCLoggerFactory.getLogger(ClassModel.class);

    private BindingConfig rootBeanConfig;
    private Edimap edimap;
    private List<JClass> createdClasses;
    private Collection<JClass> referencedClasses;
    private Map<MappingNode, JClass> classesByNode;
    private String bindingFilePath;

    public void setEdimap(Edimap edimap) {
        this.edimap = edimap;
    }

    public Edimap getEdimap() {
        return edimap;
    }

    public BindingConfig getRootBeanConfig() {
        return rootBeanConfig;
    }

    public void setRootBeanConfig(BindingConfig rootBeanConfig) {
        this.rootBeanConfig = rootBeanConfig;
    }

    /**
     * Returns a List of all generated {@link org.smooks.javabean.pojogen.JClass}.
     * @return A {@link java.util.List} of {@link org.smooks.javabean.pojogen.JClass}.
     */
    public List<JClass> getCreatedClasses() {
        if ( createdClasses == null ) {
            this.createdClasses = new ArrayList<JClass>();
        }
        return createdClasses;
    }

    /**
     * Adds a {@link org.smooks.javabean.pojogen.JClass} to the ClassModel.
     * @param jclass the {@link org.smooks.javabean.pojogen.JClass} to add.
     */
    public void addCreatedClass(JClass jclass) {
        getCreatedClasses().add(jclass);
        LOGGER.info("Added class " + jclass.getPackageName() + "." + jclass.getClassName() + " to model.");
    }

    public void setClassesByNode(Map<MappingNode, JClass> classesBySegref) {
        this.classesByNode = classesBySegref;
    }

    public Map<MappingNode, JClass> getClassesByNode() {
        return classesByNode;
    }

    public boolean isClassCreator(JClass jClass) {
        return createdClasses.contains(jClass);
    }

    public void setReferencedClasses(Collection<JClass> referencedClasses) {
        this.referencedClasses = referencedClasses;
    }

    public String getBindingFilePath() {
        return bindingFilePath;
    }

    public void setBindingFilePath(String bindingFilePath) {
        this.bindingFilePath = bindingFilePath;
    }
}
