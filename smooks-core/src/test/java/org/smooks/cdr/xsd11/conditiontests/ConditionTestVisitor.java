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
package org.smooks.cdr.xsd11.conditiontests;

import org.smooks.SmooksException;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitBefore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConditionTestVisitor implements SAXVisitBefore {

    @ConfigParam
    private String message;

    public static List<String> messagesUsed = new ArrayList<String>();

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        messagesUsed.add(message);
    }
}
