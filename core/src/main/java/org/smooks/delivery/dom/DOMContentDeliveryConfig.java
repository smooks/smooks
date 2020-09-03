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
package org.smooks.delivery.dom;

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.*;
import org.smooks.delivery.dom.serialize.SerializationUnit;
import org.smooks.delivery.ordering.Sorter;

/**
 * DOM specific {@link org.smooks.delivery.ContentDeliveryConfig} implementation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class DOMContentDeliveryConfig extends AbstractContentDeliveryConfig {
    private ContentHandlerBindings<DOMVisitBefore> assemblyVisitBefores = new ContentHandlerBindings<>();
    private ContentHandlerBindings<DOMVisitAfter> assemblyVisitAfters = new ContentHandlerBindings<>();
    private ContentHandlerBindings<DOMVisitBefore> processingVisitBefores = new ContentHandlerBindings<>();
    private ContentHandlerBindings<DOMVisitAfter> processingVisitAfters = new ContentHandlerBindings<>();
    private ContentHandlerBindings<SerializationUnit> serializationVisitors = new ContentHandlerBindings<>();
    private ContentHandlerBindings<VisitLifecycleCleanable> visitCleanables = new ContentHandlerBindings<>();
    private FilterBypass filterBypass;

    public ContentHandlerBindings<DOMVisitBefore> getAssemblyVisitBefores() {
        return assemblyVisitBefores;
    }

    public void setAssemblyVisitBefores(ContentHandlerBindings<DOMVisitBefore> assemblyVisitBefores) {
        this.assemblyVisitBefores = assemblyVisitBefores;
    }

    public ContentHandlerBindings<DOMVisitAfter> getAssemblyVisitAfters() {
        return assemblyVisitAfters;
    }

    public void setAssemblyVisitAfters(ContentHandlerBindings<DOMVisitAfter> assemblyVisitAfters) {
        this.assemblyVisitAfters = assemblyVisitAfters;
    }

    public ContentHandlerBindings<DOMVisitBefore> getProcessingVisitBefores() {
        return processingVisitBefores;
    }

    public void setProcessingVisitBefores(ContentHandlerBindings<DOMVisitBefore> processingVisitBefores) {
        this.processingVisitBefores = processingVisitBefores;
    }

    public ContentHandlerBindings<DOMVisitAfter> getProcessingVisitAfters() {
        return processingVisitAfters;
    }

    public void setProcessingVisitAfters(ContentHandlerBindings<DOMVisitAfter> processingVisitAfters) {
        this.processingVisitAfters = processingVisitAfters;
    }

    public ContentHandlerBindings<SerializationUnit> getSerializationVisitors() {
        return serializationVisitors;
    }

    public void setSerializationVisitors(ContentHandlerBindings<SerializationUnit> serializationVisitors) {
        this.serializationVisitors = serializationVisitors;
    }

    @SuppressWarnings("WeakerAccess")
    public ContentHandlerBindings<VisitLifecycleCleanable> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(ContentHandlerBindings<VisitLifecycleCleanable> visitCleanables) {
        this.visitCleanables = visitCleanables;
    }

    public Filter newFilter(ExecutionContext executionContext) {
        return new SmooksDOMFilter(executionContext);
    }

    public FilterBypass getFilterBypass() {
    	return filterBypass;
    }

    public void sort() throws SmooksConfigurationException {
        assemblyVisitBefores.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        assemblyVisitAfters.sort(Sorter.SortOrder.CONSUMERS_FIRST);
        processingVisitBefores.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        processingVisitAfters.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    public void addToExecutionLifecycleSets() throws SmooksConfigurationException {
        addToExecutionLifecycleSets(assemblyVisitBefores);
        addToExecutionLifecycleSets(assemblyVisitAfters);
        addToExecutionLifecycleSets(processingVisitBefores);
        addToExecutionLifecycleSets(processingVisitAfters);
    }

    public void configureFilterBypass() {
		filterBypass = getFilterBypass(assemblyVisitBefores, assemblyVisitAfters, processingVisitBefores, processingVisitAfters, serializationVisitors);
	}
}
