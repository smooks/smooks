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

import org.smooks.container.ExecutionContext;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MockDatasource extends AbstractDataSource {

    public static boolean committed;
    public static boolean rolledBack;
    public static int cleanupCallCount = 0;
    public static final String MOCK_DS_NAME = "mockDS";

    public String getName() {
        return MOCK_DS_NAME;
    }

    public Connection getConnection() throws SQLException {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getName().equals("commit")) {
                    committed = true;
                    return null;
                } else if(method.getName().equals("rollback")) {
                    rolledBack = true;
                    return null;
                } else if(method.getName().equals("setAutoCommit")) {
                    return null;
                } else if(method.getName().equals("close")) {
                    return null;
                }  else if(method.getName().equals("getAutoCommit")) {
                    return true;
                }

                throw new RuntimeException("Unexpected call to method: " + method);
            }
        };

        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                handler);
    }

    public boolean isAutoCommit() {
        return false;
    }

    protected void unbind(ExecutionContext executionContext) {
        cleanupCallCount++;
        super.unbind(executionContext);
    }
}
