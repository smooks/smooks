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
package org.smooks.db;

import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.ordering.Consumer;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.SmooksException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DSConnectionUser implements SAXVisitAfter, Consumer {

	@ConfigParam(defaultVal = MockDatasource.MOCK_DS_NAME)
	private String datasource;

	public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        AbstractDataSource.getConnection(datasource, executionContext);
    }

    public boolean consumes(Object object) {
        return object.equals(datasource);
    }
}
