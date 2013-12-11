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
package org.milyn.container.plugin;

import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.payload.FilterResult;
import org.milyn.payload.JavaResult;

import javax.xml.transform.Result;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaObjectSetter implements SAXVisitBefore {

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        Result result = FilterResult.getResult(executionContext, JavaResult.class);
        if (result != null) {
            Map beans = new HashMap();

            ((JavaResult) result).setResultMap(beans);
            beans.put("theBean", "Hi there!");
        }
    }
}
