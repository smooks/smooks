/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.engine.db;

import org.smooks.api.ExecutionContext;

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
