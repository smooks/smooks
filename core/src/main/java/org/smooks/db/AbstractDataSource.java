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
package org.smooks.db;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Fragment;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.delivery.ordering.Producer;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.delivery.sax.ng.BeforeVisitor;
import org.smooks.lifecycle.ExecutionLifecycleCleanable;
import org.smooks.lifecycle.VisitLifecycleCleanable;
import org.smooks.util.CollectionsUtil;
import org.w3c.dom.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

/**
 * DataSource management resource.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class AbstractDataSource implements SAXVisitBefore, DOMVisitBefore, BeforeVisitor, Producer, VisitLifecycleCleanable, ExecutionLifecycleCleanable {

    private static final String DS_CONTEXT_KEY_PREFIX = AbstractDataSource.class.getName() + "#datasource:";
    private static final String CONNECTION_CONTEXT_KEY_PREFIX = AbstractDataSource.class.getName() + "#connection:";
    private static final String TRANSACTION_MANAGER_CONTEXT_KEY_PREFIX = AbstractDataSource.class.getName() + "#transactionManager:";

    @Override
    public final void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        bind(executionContext);
    }

    @Override
    public final void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        bind(executionContext);
    }

    @Override
    public final void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        unbind(executionContext);
    }

    @Override
    public final void executeExecutionLifecycleCleanup(ExecutionContext executionContext) {
        // This guarantees Datasource resource cleanup (at the end of an ExecutionContext lifecycle) in
        // situations where the Smooks filter operation has terminated prematurely i.e. where the
        // executeVisitLifecycleCleanup event method was not called...
        unbind(executionContext);
    }

    protected void bind(ExecutionContext executionContext) {
        executionContext.setAttribute(DS_CONTEXT_KEY_PREFIX + getName(), this);
    }

    protected void unbind(ExecutionContext executionContext) {
        try {
            Connection connection = executionContext.getAttribute(CONNECTION_CONTEXT_KEY_PREFIX + getName());

            if(connection != null) {
            	TransactionManager transactionManager = executionContext.getAttribute(TRANSACTION_MANAGER_CONTEXT_KEY_PREFIX  + getName());
            	if(transactionManager == null) {
            		throw new SmooksException("No TransactionManager is set for the datasource '" + getName() + "'");
            	}
                try {
                    if(!isAutoCommit()) {
                        // If there's no termination error on the context, commit, otherwise rollback...
                        if(executionContext.getTerminationError() == null) {
                        	transactionManager.commit();
                        } else {
                        	transactionManager.rollback();
                        }
                    }
                } finally {
                    executionContext.removeAttribute(CONNECTION_CONTEXT_KEY_PREFIX + getName());
                    connection.close();
                }
            }
        } catch (SQLException e) {
            throw new SmooksException("Unable to unbind DataSource '" + getName() + "'.", e);
        } finally {
            executionContext.removeAttribute(DS_CONTEXT_KEY_PREFIX + getName());
            executionContext.removeAttribute(TRANSACTION_MANAGER_CONTEXT_KEY_PREFIX + getName());
        }
    }

    public static Connection getConnection(String dataSourceName, ExecutionContext executionContext) throws SmooksException {
        Connection connection = (Connection) executionContext.getAttribute(CONNECTION_CONTEXT_KEY_PREFIX + dataSourceName);

        if(connection == null) {
            AbstractDataSource datasource = (AbstractDataSource) executionContext.getAttribute(DS_CONTEXT_KEY_PREFIX + dataSourceName);

            if(datasource == null) {
                throw new SmooksException("DataSource '" + dataSourceName + "' not bound to context.  Configure an '" + AbstractDataSource.class.getName() +  "' implementation and target it at '#document'.");
            }
            try {
                connection = datasource.getConnection();

                TransactionManager transactionManager = datasource.createTransactionManager(connection);
                transactionManager.begin();

                executionContext.setAttribute(CONNECTION_CONTEXT_KEY_PREFIX + dataSourceName, connection);
                executionContext.setAttribute(TRANSACTION_MANAGER_CONTEXT_KEY_PREFIX + dataSourceName, transactionManager);
            } catch (SQLException e) {
                throw new SmooksException("Unable to open connection to dataSource '" + dataSourceName + "'.", e);
            }

        }

        return connection;
    }

    @Override
    public Set<String> getProducts() {
        return CollectionsUtil.toSet(getName());
    }

    public abstract String getName();

    public abstract Connection getConnection() throws SQLException;

    public abstract boolean isAutoCommit();

    public TransactionManager createTransactionManager(Connection connection) {
    	return new JdbcTransactionManager(connection, isAutoCommit());
    }

}
