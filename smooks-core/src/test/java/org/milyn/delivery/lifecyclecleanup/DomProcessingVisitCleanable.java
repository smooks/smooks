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
package org.milyn.delivery.lifecyclecleanup;

import junit.framework.TestCase;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.dom.Phase;
import org.milyn.delivery.dom.VisitPhase;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Phase(value = VisitPhase.PROCESSING)
public class DomProcessingVisitCleanable implements DOMVisitBefore, DOMVisitAfter, VisitLifecycleCleanable {

    public static boolean cleaned;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if (cleaned) {
            TestCase.fail("Resource shouldn't be cleaned yet!");
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if (cleaned) {
            TestCase.fail("Resource shouldn't be cleaned yet!");
        }
    }

    public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        cleaned = true;
    }
}