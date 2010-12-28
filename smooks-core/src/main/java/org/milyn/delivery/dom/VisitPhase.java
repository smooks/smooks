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

/**
 * DOM Phase phase enumerations.
 * <p/>
 * Used in conjunction with the {@link org.milyn.delivery.dom.Phase}
 * annotation to specify the Visit Phase during which a {@link org.milyn.delivery.dom.DOMElementVisitor}
 * is to be applied.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public enum VisitPhase {

    /**
     * Apply the {@link org.milyn.delivery.dom.DOMElementVisitor} during the <b>Assembly</b> visit phase.
     */
    ASSEMBLY,

    /**
     * Apply the {@link org.milyn.delivery.dom.DOMElementVisitor} during the <b>Processing</b> visit phase.
     */
    PROCESSING,
}
