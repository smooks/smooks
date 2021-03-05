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
import org.smooks.assertion.AssertArgument;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.resource.visitor.VisitAfterReport;
import org.smooks.api.resource.visitor.VisitBeforeReport;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Jndi based DataSource.
 * <p />
 * Configure the JNDI datasource, transaction manager, transaction jndi (optional), etc.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@VisitBeforeReport(summary = "Bind JndiDataSource <b>${resource.parameters.datasource}</b> to ExecutionContext.", detailTemplate = "reporting/JndiDataSource_before.html")
@VisitAfterReport(summary = "Cleaning up JndiDataSource <b>${resource.parameters.datasource}</b>. Includes performing commit/rollback etc.", detailTemplate = "reporting/JndiDataSource_after.html")
public class JndiDataSource extends AbstractDataSource {

    @Inject
    @Named("datasource")
    private String name;

    @Inject
    private Optional<String> datasourceJndi;

    @Inject
    private Optional<String> transactionJndi;

    @Inject
    private Boolean autoCommit = false;

    @Inject
    private Boolean setAutoCommitAllowed = true;

    @Inject
    @Named("transactionManager")
    private TransactionManagerType transactionManagerType = TransactionManagerType.JDBC;

    private DataSource datasource;

    public JndiDataSource() {
    }

    public JndiDataSource(String name, boolean autoCommit) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        this.name = name;
        this.autoCommit = autoCommit;
    }

    @Override
    public String getName() {
        return name;
    }

    @PostConstruct
    public void postConstruct() {
        if (!datasourceJndi.isPresent()) {
            datasourceJndi = Optional.of(name);
        }

        datasource = (DataSource) lookup(datasourceJndi.orElse(null));

        if (transactionManagerType == TransactionManagerType.JTA) {
            if (!transactionJndi.isPresent() || transactionJndi.get().length() == 0) {
                throw new SmooksConfigException("The transactionJndi attribute must be set when the JTA transaction manager is set.");
            }

            //On JTA transaction manager then the autoCommit is always false
            autoCommit = false;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    private Object lookup(String jndi) {
        Context context = null;
        try {
            context = new InitialContext();
            return context.lookup(jndi);
        } catch (NamingException e) {
            throw new SmooksConfigException("JNDI Context lookup failed for '" + jndi + "'.", e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    throw new SmooksConfigException("Error closing Naming Context after looking up DataSource JNDI '" + datasourceJndi + "'.", e);
                }
            }
        }
    }

    @Override
    public boolean isAutoCommit() {
        return autoCommit;
    }

    @Override
    public TransactionManager createTransactionManager(Connection connection) {
        switch (transactionManagerType) {
            case JDBC:
                return new JdbcTransactionManager(connection, isAutoCommit());
            case JTA:
                return new JtaTransactionManager(connection, (UserTransaction) lookup(transactionJndi.orElse(null)), setAutoCommitAllowed);
            case EXTERNAL:
                return new ExternalTransactionManager(connection, isAutoCommit(), setAutoCommitAllowed);
            default:
                throw new SmooksException("The TransactionManager type '" + transactionManagerType + "' is unknown. This is probably a bug!");
        }
    }
}
