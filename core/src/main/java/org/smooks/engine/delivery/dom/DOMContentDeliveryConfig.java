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

import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.resource.visitor.SerializerVisitor;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.delivery.*;
import org.smooks.engine.delivery.ordering.Sorter;
import org.smooks.api.lifecycle.VisitLifecycleCleanable;

/**
 * DOM specific {@link ContentDeliveryConfig} implementation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class DOMContentDeliveryConfig extends AbstractContentDeliveryConfig {
    private SelectorTable<DOMVisitBefore> assemblyVisitBeforeSelectorTable = new SelectorTable<>();
    private SelectorTable<DOMVisitAfter> assemblyVisitAfterSelectorTable = new SelectorTable<>();
    private SelectorTable<DOMVisitBefore> processingVisitBeforeSelectorTable = new SelectorTable<>();
    private SelectorTable<DOMVisitAfter> processingVisitAfterSelectorTable = new SelectorTable<>();
    private SelectorTable<SerializerVisitor> serializerVisitorSelectorTable = new SelectorTable<>();
    private SelectorTable<VisitLifecycleCleanable> visitLifecycleCleanableSelectorTable = new SelectorTable<>();
    private FilterBypass filterBypass;

    public SelectorTable<DOMVisitBefore> getAssemblyVisitBeforeSelectorTable() {
        return assemblyVisitBeforeSelectorTable;
    }

    public void setAssemblyVisitBeforeSelectorTable(SelectorTable<DOMVisitBefore> assemblyVisitBeforeSelectorTable) {
        this.assemblyVisitBeforeSelectorTable = assemblyVisitBeforeSelectorTable;
    }

    public SelectorTable<DOMVisitAfter> getAssemblyVisitAfterSelectorTable() {
        return assemblyVisitAfterSelectorTable;
    }

    public void setAssemblyVisitAfterSelectorTable(SelectorTable<DOMVisitAfter> assemblyVisitAfterSelectorTable) {
        this.assemblyVisitAfterSelectorTable = assemblyVisitAfterSelectorTable;
    }

    public SelectorTable<DOMVisitBefore> getProcessingVisitBeforeSelectorTable() {
        return processingVisitBeforeSelectorTable;
    }

    public void setProcessingVisitBeforeSelectorTable(SelectorTable<DOMVisitBefore> processingVisitBeforeSelectorTable) {
        this.processingVisitBeforeSelectorTable = processingVisitBeforeSelectorTable;
    }

    public SelectorTable<DOMVisitAfter> getProcessingVisitAfterSelectorTable() {
        return processingVisitAfterSelectorTable;
    }

    public void setProcessingVisitAfterSelectorTable(SelectorTable<DOMVisitAfter> processingVisitAfterSelectorTable) {
        this.processingVisitAfterSelectorTable = processingVisitAfterSelectorTable;
    }

    public SelectorTable<SerializerVisitor> getSerializerVisitorSelectorTable() {
        return serializerVisitorSelectorTable;
    }

    public void setSerializerVisitorSelectorTable(SelectorTable<SerializerVisitor> serializerVisitorSelectorTable) {
        this.serializerVisitorSelectorTable = serializerVisitorSelectorTable;
    }

    @SuppressWarnings("WeakerAccess")
    public SelectorTable<VisitLifecycleCleanable> getVisitLifecycleCleanableSelectorTable() {
        return visitLifecycleCleanableSelectorTable;
    }

    public void setVisitLifecycleCleanableSelectorTable(SelectorTable<VisitLifecycleCleanable> visitLifecycleCleanableSelectorTable) {
        this.visitLifecycleCleanableSelectorTable = visitLifecycleCleanableSelectorTable;
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
        assemblyVisitBeforeSelectorTable.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        assemblyVisitAfterSelectorTable.sort(Sorter.SortOrder.CONSUMERS_FIRST);
        processingVisitBeforeSelectorTable.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        processingVisitAfterSelectorTable.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    @Override
    public void addToExecutionLifecycleSets() throws SmooksConfigException {
        addToExecutionLifecycleSets(assemblyVisitBeforeSelectorTable);
        addToExecutionLifecycleSets(assemblyVisitAfterSelectorTable);
        addToExecutionLifecycleSets(processingVisitBeforeSelectorTable);
        addToExecutionLifecycleSets(processingVisitAfterSelectorTable);
    }

    public void configureFilterBypass() {
		filterBypass = getFilterBypass(assemblyVisitBeforeSelectorTable, assemblyVisitAfterSelectorTable, processingVisitBeforeSelectorTable, processingVisitAfterSelectorTable, serializerVisitorSelectorTable);
	}
}
