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
package org.milyn.delivery.dom;

import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.*;
import org.milyn.delivery.dom.serialize.SerializationUnit;
import org.milyn.delivery.ordering.Sorter;

/**
 * DOM specific {@link org.milyn.delivery.ContentDeliveryConfig} implementation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class DOMContentDeliveryConfig extends AbstractContentDeliveryConfig {
    private ContentHandlerConfigMapTable<DOMVisitBefore> assemblyVisitBefores;
    private ContentHandlerConfigMapTable<DOMVisitAfter> assemblyVisitAfters;
    private ContentHandlerConfigMapTable<DOMVisitBefore> processingVisitBefores;
    private ContentHandlerConfigMapTable<DOMVisitAfter> processingVisitAfters;
    private ContentHandlerConfigMapTable<SerializationUnit> serailizationVisitors;
    private ContentHandlerConfigMapTable<VisitLifecycleCleanable> visitCleanables;
    private FilterBypass filterBypass;

    public ContentHandlerConfigMapTable<DOMVisitBefore> getAssemblyVisitBefores() {
        return assemblyVisitBefores;
    }

    public void setAssemblyVisitBefores(ContentHandlerConfigMapTable<DOMVisitBefore> assemblyVisitBefores) {
        this.assemblyVisitBefores = assemblyVisitBefores;
    }

    public ContentHandlerConfigMapTable<DOMVisitAfter> getAssemblyVisitAfters() {
        return assemblyVisitAfters;
    }

    public void setAssemblyVisitAfters(ContentHandlerConfigMapTable<DOMVisitAfter> assemblyVisitAfters) {
        this.assemblyVisitAfters = assemblyVisitAfters;
    }

    public ContentHandlerConfigMapTable<DOMVisitBefore> getProcessingVisitBefores() {
        return processingVisitBefores;
    }

    public void setProcessingVisitBefores(ContentHandlerConfigMapTable<DOMVisitBefore> processingVisitBefores) {
        this.processingVisitBefores = processingVisitBefores;
    }

    public ContentHandlerConfigMapTable<DOMVisitAfter> getProcessingVisitAfters() {
        return processingVisitAfters;
    }

    public void setProcessingVisitAfters(ContentHandlerConfigMapTable<DOMVisitAfter> processingVisitAfters) {
        this.processingVisitAfters = processingVisitAfters;
    }

    public ContentHandlerConfigMapTable<SerializationUnit> getSerailizationVisitors() {
        return serailizationVisitors;
    }

    public void setSerailizationVisitors(ContentHandlerConfigMapTable<SerializationUnit> serailizationVisitors) {
        this.serailizationVisitors = serailizationVisitors;
    }

    @SuppressWarnings("WeakerAccess")
    public ContentHandlerConfigMapTable<VisitLifecycleCleanable> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(ContentHandlerConfigMapTable<VisitLifecycleCleanable> visitCleanables) {
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
		filterBypass = getFilterBypass(assemblyVisitBefores, assemblyVisitAfters, processingVisitBefores, processingVisitAfters, serailizationVisitors);
	}
}
