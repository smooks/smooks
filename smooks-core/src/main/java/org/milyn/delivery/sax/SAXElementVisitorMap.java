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
package org.milyn.delivery.sax;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.xpath.SelectorStep;
import org.milyn.commons.util.ClassUtil;
import org.milyn.delivery.ContentHandlerConfigMap;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.sax.annotation.StreamResultWriter;
import org.milyn.delivery.sax.annotation.TextConsumer;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * SAXElement visitor Map.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXElementVisitorMap {

    private List<ContentHandlerConfigMap<SAXVisitBefore>> visitBefores;
    private List<ContentHandlerConfigMap<SAXVisitChildren>> childVisitors;
    private List<ContentHandlerConfigMap<SAXVisitAfter>> visitAfters;
    private List<ContentHandlerConfigMap<VisitLifecycleCleanable>> visitCleanables;
    private boolean accumulateText = false;
    private SAXVisitor acquireWriterFor = null;

    public List<ContentHandlerConfigMap<SAXVisitBefore>> getVisitBefores() {
        return visitBefores;
    }

    public void setVisitBefores(List<ContentHandlerConfigMap<SAXVisitBefore>> visitBefores) {
        this.visitBefores = visitBefores;
    }

    public List<ContentHandlerConfigMap<SAXVisitChildren>> getChildVisitors() {
        return childVisitors;
    }

    public void setChildVisitors(List<ContentHandlerConfigMap<SAXVisitChildren>> childVisitors) {
        this.childVisitors = childVisitors;
    }

    public List<ContentHandlerConfigMap<SAXVisitAfter>> getVisitAfters() {
        return visitAfters;
    }

    public void setVisitAfters(List<ContentHandlerConfigMap<SAXVisitAfter>> visitAfters) {
        this.visitAfters = visitAfters;
    }

    public List<ContentHandlerConfigMap<VisitLifecycleCleanable>> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(List<ContentHandlerConfigMap<VisitLifecycleCleanable>> visitCleanables) {
        this.visitCleanables = visitCleanables;
    }

    public boolean accumulateText() {
        return accumulateText;
    }

    public SAXVisitor acquireWriterFor() {
        return acquireWriterFor;
    }

    public void initAccumulateText() {
        // If any of the before/after handlers are marked as text consumers...
        if (getAnnotatedHandler(visitBefores, TextConsumer.class, false) != null) {
            accumulateText = true;
            return;
        }
        if (getAnnotatedHandler(visitAfters, TextConsumer.class, false) != null) {
            accumulateText = true;
            return;
        }

        // If any of the selector steps need access to the fragment text...
        if (visitAfters == null) {
            return;
        }
        for (ContentHandlerConfigMap<? extends SAXVisitor> contentHandlerMap : visitAfters) {
            SmooksResourceConfiguration resourceConfig = contentHandlerMap.getResourceConfig();
            SelectorStep selectorStep = resourceConfig.getSelectorStep();

            if (selectorStep.accessesText()) {
                accumulateText = true;
                break;
            }
        }
    }

    public void initAccumulateText(SAXElementVisitorMap srcMap) {
        this.accumulateText = (this.accumulateText || srcMap.accumulateText);
    }

    public void initAcquireWriterFor(SAXElementVisitorMap srcMap) {
        if (this.acquireWriterFor == null) {
            this.acquireWriterFor = srcMap.acquireWriterFor;
        }
    }

    public void initAcquireWriterFor() {
        acquireWriterFor = getAnnotatedHandler(visitBefores, StreamResultWriter.class, true);
        if (acquireWriterFor == null) {
            acquireWriterFor = getAnnotatedHandler(visitAfters, StreamResultWriter.class, true);
        }
    }

    public SAXElementVisitorMap merge(SAXElementVisitorMap map) {
        if (map == null) {
            // No need to merge...
            return this;
        }

        SAXElementVisitorMap merge = new SAXElementVisitorMap();

        merge.visitBefores = new ArrayList<ContentHandlerConfigMap<SAXVisitBefore>>();
        merge.childVisitors = new ArrayList<ContentHandlerConfigMap<SAXVisitChildren>>();
        merge.visitAfters = new ArrayList<ContentHandlerConfigMap<SAXVisitAfter>>();
        merge.visitCleanables = new ArrayList<ContentHandlerConfigMap<VisitLifecycleCleanable>>();

        merge.visitBefores.addAll(visitBefores);
        merge.visitBefores.addAll(map.visitBefores);
        merge.childVisitors.addAll(childVisitors);
        merge.childVisitors.addAll(map.childVisitors);
        merge.visitAfters.addAll(visitAfters);
        merge.visitAfters.addAll(map.visitAfters);
        merge.visitCleanables.addAll(visitCleanables);
        merge.visitCleanables.addAll(map.visitCleanables);

        merge.accumulateText = (accumulateText || merge.accumulateText);

        return merge;
    }

    private <T extends SAXVisitor> T getAnnotatedHandler(List<ContentHandlerConfigMap<T>> handlerMaps, Class<? extends Annotation> annotationClass, boolean checkFields) {
        if (handlerMaps == null) {
            return null;
        }

        for (ContentHandlerConfigMap<T> handlerMap : handlerMaps) {
            T contentHandler = handlerMap.getContentHandler();
            if (contentHandler.getClass().isAnnotationPresent(annotationClass)) {
                return contentHandler;
            } else if (checkFields && !ClassUtil.getAnnotatedFields(contentHandler.getClass(), annotationClass).isEmpty()) {
                return contentHandler;
            }
        }

        return null;
    }
}
