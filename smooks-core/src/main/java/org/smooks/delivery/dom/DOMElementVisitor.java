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

package org.smooks.delivery.dom;

/**
 * Element <b>Visitor</b> (GoF) interface for DOM.
 * <p/>
 * {@link SmooksDOMFilter} filters (analyses/transforms) XML/XHTML/HTML content
 * by "visting" the DOM {@link org.w3c.dom.Element} nodes through a series of iterations over
 * the source XML DOM.
 * <p/>
 * This interface defines the methods for a "visiting" filter.
 * Implementations of this interface provide a means of hooking analysis
 * and transformation logic into the {@link SmooksDOMFilter} filtering process.
 * <p/>
 * Implementations should be annotated with the {@link org.smooks.delivery.dom.Phase}
 * annotation, indicating in which of the {@link SmooksDOMFilter Visit Phases} the visitor should be applied. If not
 * annotated, the visitor is applied during the Processing phase.  The phase may also be specified via the
 * "VisitPhase" property on the {@link org.smooks.cdr.SmooksResourceConfiguration resource configuration}.  Valid values
 * in this case are "ASSEMBLY" and "PROCESSING".
 * <p/>
 * Implementations must be stateless.  If state storage is required, attach the state to the
 * supplied {@link org.smooks.container.ExecutionContext}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface DOMElementVisitor extends DOMVisitBefore, DOMVisitAfter {
}