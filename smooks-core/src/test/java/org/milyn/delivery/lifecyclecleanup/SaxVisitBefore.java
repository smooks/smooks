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
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ExecutionLifecycleCleanable;
import org.milyn.delivery.ExecutionLifecycleInitializable;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.sax.SAXElement;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SaxVisitBefore implements SAXVisitBefore, ExecutionLifecycleInitializable, ExecutionLifecycleCleanable {

    public static boolean initialized;
    public static boolean cleaned;

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(!initialized) {
            TestCase.fail("Resource should be initialized!");
        }
        if(cleaned) {
            TestCase.fail("Resource shouldn't be cleaned yet!");
        }
    }

    public void executeExecutionLifecycleInitialize(ExecutionContext executionContext) {
        initialized = true;
    }

    public void executeExecutionLifecycleCleanup(ExecutionContext executionContext) {
        cleaned = true;
    }
}