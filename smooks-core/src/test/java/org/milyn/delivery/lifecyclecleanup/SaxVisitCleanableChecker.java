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

import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ExecutionLifecycleCleanable;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SaxVisitCleanableChecker implements SAXVisitBefore {

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(!SaxVisitCleanable.cleaned) {
            fail("Resource should have been cleaned!");
        }
    }
}