/*-
 * ========================LICENSE_START=================================
 * Core
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

import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.api.resource.visitor.sax.SAXVisitBefore;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.lifecycle.ExecutionLifecycleCleanable;
import org.smooks.api.lifecycle.VisitLifecycleCleanable;
import org.smooks.support.CollectionsUtil;
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
        executionContext.put(new TypedKey<>(DS_CONTEXT_KEY_PREFIX + getName()), this);
    }

    protected void unbind(ExecutionContext executionContext) {
        try {
            Connection connection = executionContext.get(new TypedKey<>(CONNECTION_CONTEXT_KEY_PREFIX + getName()));

            if(connection != null) {
            	TransactionManager transactionManager = executionContext.get(new TypedKey<>(TRANSACTION_MANAGER_CONTEXT_KEY_PREFIX  + getName()));
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
                    executionContext.remove(new TypedKey<>(CONNECTION_CONTEXT_KEY_PREFIX + getName()));
                    connection.close();
                }
            }
        } catch (SQLException e) {
            throw new SmooksException("Unable to unbind DataSource '" + getName() + "'.", e);
        } finally {
            executionContext.remove(new TypedKey<>(DS_CONTEXT_KEY_PREFIX + getName()));
            executionContext.remove(new TypedKey<>(TRANSACTION_MANAGER_CONTEXT_KEY_PREFIX + getName()));
        }
    }

    public static Connection getConnection(String dataSourceName, ExecutionContext executionContext) throws SmooksException {
        Connection connection = executionContext.get(new TypedKey<>(CONNECTION_CONTEXT_KEY_PREFIX + dataSourceName));

        if(connection == null) {
            AbstractDataSource datasource = executionContext.get(new TypedKey<>(DS_CONTEXT_KEY_PREFIX + dataSourceName));

            if(datasource == null) {
                throw new SmooksException("DataSource '" + dataSourceName + "' not bound to context.  Configure an '" + AbstractDataSource.class.getName() +  "' implementation and target it at '#document'.");
            }
            try {
                connection = datasource.getConnection();

                TransactionManager transactionManager = datasource.createTransactionManager(connection);
                transactionManager.begin();

                executionContext.put(new TypedKey<>(CONNECTION_CONTEXT_KEY_PREFIX + dataSourceName), connection);
                executionContext.put(new TypedKey<>(TRANSACTION_MANAGER_CONTEXT_KEY_PREFIX + dataSourceName), transactionManager);
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
