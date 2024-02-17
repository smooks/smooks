/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.dom;

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.lifecycle.PostFragmentLifecycle;
import org.smooks.api.resource.visitor.SerializerVisitor;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.engine.delivery.AbstractContentDeliveryConfig;
import org.smooks.engine.delivery.ContentHandlerBindingIndex;
import org.smooks.engine.delivery.ordering.Sorter;

/**
 * DOM specific {@link ContentDeliveryConfig} implementation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class DOMContentDeliveryConfig extends AbstractContentDeliveryConfig {
    private ContentHandlerBindingIndex<DOMVisitBefore> assemblyVisitBeforeIndex = new ContentHandlerBindingIndex<>();
    private ContentHandlerBindingIndex<DOMVisitAfter> assemblyVisitAfterIndex = new ContentHandlerBindingIndex<>();
    private ContentHandlerBindingIndex<DOMVisitBefore> processingVisitBeforeIndex = new ContentHandlerBindingIndex<>();
    private ContentHandlerBindingIndex<DOMVisitAfter> processingVisitAfterIndex = new ContentHandlerBindingIndex<>();
    private ContentHandlerBindingIndex<SerializerVisitor> serializerVisitorIndex = new ContentHandlerBindingIndex<>();
    private ContentHandlerBindingIndex<PostFragmentLifecycle> postFragmentLifecycleIndex = new ContentHandlerBindingIndex<>();
    private FilterBypass filterBypass;

    public ContentHandlerBindingIndex<DOMVisitBefore> getAssemblyVisitBeforeIndex() {
        return assemblyVisitBeforeIndex;
    }

    public void setAssemblyVisitBeforeIndex(ContentHandlerBindingIndex<DOMVisitBefore> assemblyVisitBeforeIndex) {
        this.assemblyVisitBeforeIndex = assemblyVisitBeforeIndex;
    }

    public ContentHandlerBindingIndex<DOMVisitAfter> getAssemblyVisitAfterIndex() {
        return assemblyVisitAfterIndex;
    }

    public void setAssemblyVisitAfterIndex(ContentHandlerBindingIndex<DOMVisitAfter> assemblyVisitAfterIndex) {
        this.assemblyVisitAfterIndex = assemblyVisitAfterIndex;
    }

    public ContentHandlerBindingIndex<DOMVisitBefore> getProcessingVisitBeforeIndex() {
        return processingVisitBeforeIndex;
    }

    public void setProcessingVisitBeforeIndex(ContentHandlerBindingIndex<DOMVisitBefore> processingVisitBeforeIndex) {
        this.processingVisitBeforeIndex = processingVisitBeforeIndex;
    }

    public ContentHandlerBindingIndex<DOMVisitAfter> getProcessingVisitAfterIndex() {
        return processingVisitAfterIndex;
    }

    public void setProcessingVisitAfterIndex(ContentHandlerBindingIndex<DOMVisitAfter> processingVisitAfterIndex) {
        this.processingVisitAfterIndex = processingVisitAfterIndex;
    }

    public ContentHandlerBindingIndex<SerializerVisitor> getSerializerVisitorIndex() {
        return serializerVisitorIndex;
    }

    public void setSerializerVisitorIndex(ContentHandlerBindingIndex<SerializerVisitor> serializerVisitorIndex) {
        this.serializerVisitorIndex = serializerVisitorIndex;
    }

    @SuppressWarnings("WeakerAccess")
    public ContentHandlerBindingIndex<PostFragmentLifecycle> getPostFragmentLifecycleIndex() {
        return postFragmentLifecycleIndex;
    }

    public void setPostFragmentLifecycleIndex(ContentHandlerBindingIndex<PostFragmentLifecycle> postFragmentLifecycleIndex) {
        this.postFragmentLifecycleIndex = postFragmentLifecycleIndex;
    }

    @Override
    public Filter newFilter(ExecutionContext executionContext) {
        return new SmooksDOMFilter(executionContext);
    }

    @Override
    public FilterBypass getFilterBypass() {
    	return filterBypass;
    }

    @Override
    public void sort() throws SmooksConfigException {
        assemblyVisitBeforeIndex.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        assemblyVisitAfterIndex.sort(Sorter.SortOrder.CONSUMERS_FIRST);
        processingVisitBeforeIndex.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        processingVisitAfterIndex.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    @Override
    public void addToExecutionLifecycleSets() throws SmooksConfigException {
        addToExecutionLifecycleSets(assemblyVisitBeforeIndex);
        addToExecutionLifecycleSets(assemblyVisitAfterIndex);
        addToExecutionLifecycleSets(processingVisitBeforeIndex);
        addToExecutionLifecycleSets(processingVisitAfterIndex);
    }

    public void configureFilterBypass() {
		filterBypass = getFilterBypass(assemblyVisitBeforeIndex, assemblyVisitAfterIndex, processingVisitBeforeIndex, processingVisitAfterIndex, serializerVisitorIndex);
	}
}
