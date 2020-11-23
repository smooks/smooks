/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.delivery.sax;

import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.sax.annotation.StreamResultWriter;
import org.smooks.delivery.sax.annotation.TextConsumer;
import org.smooks.lifecycle.VisitLifecycleCleanable;
import org.smooks.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * SAXElement visitor Map.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXElementVisitorMap {

    private List<ContentHandlerBinding<SAXVisitBefore>> visitBefores;
    private List<ContentHandlerBinding<SAXVisitChildren>> childVisitors;
    private List<ContentHandlerBinding<SAXVisitAfter>> visitAfters;
    private List<ContentHandlerBinding<VisitLifecycleCleanable>> visitCleanables;
    private boolean accumulateText = false;
    private SAXVisitor acquireWriterFor = null;

    public List<ContentHandlerBinding<SAXVisitBefore>> getVisitBefores() {
        return visitBefores;
    }

    public void setVisitBefores(List<ContentHandlerBinding<SAXVisitBefore>> visitBefores) {
        this.visitBefores = visitBefores;
    }

    public List<ContentHandlerBinding<SAXVisitChildren>> getChildVisitors() {
        return childVisitors;
    }

    public void setChildVisitors(List<ContentHandlerBinding<SAXVisitChildren>> childVisitors) {
        this.childVisitors = childVisitors;
    }

    public List<ContentHandlerBinding<SAXVisitAfter>> getVisitAfters() {
        return visitAfters;
    }

    public void setVisitAfters(List<ContentHandlerBinding<SAXVisitAfter>> visitAfters) {
        this.visitAfters = visitAfters;
    }

    public List<ContentHandlerBinding<VisitLifecycleCleanable>> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(List<ContentHandlerBinding<VisitLifecycleCleanable>> visitCleanables) {
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
        if(getAnnotatedHandler(visitBefores, TextConsumer.class, false) != null) {
            accumulateText = true;
        	return;
        }
        if(getAnnotatedHandler(visitAfters, TextConsumer.class, false) != null) {
            accumulateText = true;
        	return;
        }
    	
    	// If any of the selector steps need access to the fragment text...
        if(visitAfters == null) {
            return;
        }
        for(ContentHandlerBinding<? extends SAXVisitor> contentHandlerMap : visitAfters) {
            ResourceConfig resourceConfig = contentHandlerMap.getResourceConfig();
            SelectorStep selectorStep = resourceConfig.getSelectorPath().getTargetSelectorStep();

            if(selectorStep.accessesText()) {
                accumulateText = true;
                break;
            }
        }
    }

    public void initAccumulateText(SAXElementVisitorMap srcMap) {
    	this.accumulateText = (this.accumulateText || srcMap.accumulateText);
    }
    
    public void initAcquireWriterFor(SAXElementVisitorMap srcMap) {
    	if(this.acquireWriterFor == null) {
    		this.acquireWriterFor = srcMap.acquireWriterFor;
    	}
    }
    
    public void initAcquireWriterFor() {
    	acquireWriterFor = getAnnotatedHandler(visitBefores, StreamResultWriter.class, true);
    	if(acquireWriterFor == null) {
        	acquireWriterFor = getAnnotatedHandler(visitAfters, StreamResultWriter.class, true);
    	}
    }

    public SAXElementVisitorMap merge(SAXElementVisitorMap map) {
    	if(map == null) {
    		// No need to merge...
    		return this;
    	}    	
    	
    	SAXElementVisitorMap merge = new SAXElementVisitorMap();
    	
        merge.visitBefores = new ArrayList<>();
        merge.childVisitors = new ArrayList<>();
        merge.visitAfters = new ArrayList<>();
        merge.visitCleanables = new ArrayList<>();
        
        merge.visitBefores.addAll(visitBefores);
        merge.visitBefores.addAll(map.visitBefores);
        merge.childVisitors.addAll(childVisitors);
        merge.childVisitors.addAll(map.childVisitors);
        merge.visitAfters.addAll(visitAfters);
        merge.visitAfters.addAll(map.visitAfters);
        if (visitCleanables != null) {
            merge.visitCleanables.addAll(visitCleanables);
        }
        if (map.visitCleanables != null) {
            merge.visitCleanables.addAll(map.visitCleanables);
        }
        
        merge.accumulateText = (accumulateText || merge.accumulateText);

        return merge;
    }

	private <T extends SAXVisitor> T getAnnotatedHandler(List<ContentHandlerBinding<T>> handlerMaps, Class<? extends Annotation> annotationClass, boolean checkFields) {
		if(handlerMaps == null) {
			return null;
		}
		
		for(ContentHandlerBinding<T> handlerMap : handlerMaps) {
        	T contentHandler = handlerMap.getContentHandler();
			if(contentHandler.getClass().isAnnotationPresent(annotationClass)) {
        		return contentHandler;
        	} else if(checkFields && !ClassUtil.getAnnotatedFields(contentHandler.getClass(), annotationClass).isEmpty()) {
        		return contentHandler;
        	}
        }
		
		return null;
	}
}
