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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockejb.jndi.MockContextFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.report.HtmlReportGenerator;
import org.smooks.io.payload.StringSource;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.xml.transform.Source;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link JndiDataSource}.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class JndiDataSourceTest {

    public Source source;

    @Mock
    private DataSource dataSource;

    @Mock
    private UserTransaction transaction;

    @Mock
    private Connection connection;

    private static final boolean REPORT = false;


    @Test
    public void test_jndi_autoCommit() throws Exception {

        when(connection.getAutoCommit()).thenReturn(false);

        executeSmooks("jndi_autocommit", "test_jndi_autoCommit", REPORT);

        verify(dataSource).getConnection();
        verify(connection).setAutoCommit(true);
        verify(connection).close();

        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    public void test_jndi() throws Exception {

        when(connection.getAutoCommit()).thenReturn(true);

        executeSmooks("jndi", "test_jndi", REPORT);

        verify(dataSource).getConnection();
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).close();

        verify(connection, never()).rollback();
    }


    @Test
    public void test_jndi_exception() throws Exception {

        when(connection.getAutoCommit()).thenReturn(false);

        executeSmooksWithException("jndi_exception", "test_jndi_exception");

        verify(dataSource).getConnection();
        verify(connection).rollback();
        verify(connection).close();

        verify(connection, never()).setAutoCommit(false);
        verify(connection, never()).commit();
    }

    @Test
    public void test_jta_new_transaction() throws Exception {

        when(connection.getAutoCommit()).thenReturn(true);
        when(transaction.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

        executeSmooks("jta", "test_jta_new_transaction", REPORT);

        verify(dataSource, atLeastOnce()).getConnection();
        verify(transaction).begin();
        verify(connection).setAutoCommit(false);
        verify(transaction).commit();
        verify(connection).close();

        verify(transaction, never()).rollback();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();

    }

    @Test
    public void test_jta_existing_transaction() throws Exception {

        when(connection.getAutoCommit()).thenReturn(false);
        when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);

        executeSmooks("jta", "test_jta_existing_transaction", REPORT);

        verify(dataSource, atLeastOnce()).getConnection();
        verify(connection).close();

        verify(transaction, never()).begin();
        verify(transaction, never()).commit();
        verify(transaction, never()).rollback();
        verify(connection, never()).setAutoCommit(false);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();

    }

    @Test
    public void test_jta_exception() throws Exception {

        when(connection.getAutoCommit()).thenReturn(false);
        when(transaction.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

        executeSmooksWithException("jta_exception", "test_jta_exception");

        verify(dataSource, atLeastOnce()).getConnection();
        verify(transaction).begin();
        verify(transaction).rollback();
        verify(connection).close();

        verify(transaction, never()).commit();
        verify(transaction, never()).setRollbackOnly();
        verify(connection, never()).setAutoCommit(false);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();

    }

    @Test
    public void test_jta_existing_transaction_exception() throws Exception {

        when(connection.getAutoCommit()).thenReturn(false);
        when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);

        executeSmooksWithException("jta_exception", "test_jta_existing_transaction_exception");

        verify(dataSource, atLeastOnce()).getConnection();
        verify(transaction).setRollbackOnly();
        verify(connection).close();

        verify(transaction, never()).begin();
        verify(transaction, never()).commit();
        verify(transaction, never()).rollback();
        verify(connection, never()).setAutoCommit(false);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();

    }

    @Test
    public void test_jta_set_autocommit_not_allowed() throws Exception {
        when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);

        executeSmooks("jta_set_autocommit_not_allowed", "test_jta_set_autocommit_not_allowed", REPORT);

        verify(dataSource, atLeastOnce()).getConnection();
        verify(connection).close();

        verify(connection, never()).setAutoCommit(false);
        verify(connection, never()).getAutoCommit();
    }

    @Test
    public void test_jta_missing_transaction() {

        try {
            executeSmooks("jta_missing_transaction", "test_jta_missing_transaction", REPORT);
        } catch (Exception e) {
            assertEquals("The transactionJndi attribute must be set when the JTA transaction manager is set.", ExceptionUtils.getCause(e).getMessage());

            return;
        }
        fail("Exception was not thrown to indicate that the transactionJndi wasn't set.");
    }

    @Test
    public void test_external() throws Exception {

        when(connection.getAutoCommit()).thenReturn(true);

        executeSmooks("external", "external", true);

        verify(dataSource).getConnection();
        verify(connection).setAutoCommit(false);
        verify(connection).close();

        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    public void test_external_autocommit() throws Exception {

        when(connection.getAutoCommit()).thenReturn(false);

        executeSmooks("external_autocommit", "test_external_autocommit", REPORT);

        verify(dataSource).getConnection();
        verify(connection).setAutoCommit(true);
        verify(connection).close();

        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    public void test_external_set_autocommit_not_allowed() throws Exception {

        executeSmooks("external_set_autocommit_not_allowed", "test_external_set_autocommit_not_allowed", true);

        verify(dataSource).getConnection();
        verify(connection).close();

        verify(connection, never()).getAutoCommit();
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    public void test_external_exception() throws Exception {

        when(connection.getAutoCommit()).thenReturn(false);

        executeSmooksWithException("external_exception", "test_external_exception");

        verify(dataSource).getConnection();
        verify(connection).close();

        verify(connection, never()).setAutoCommit(false);
        verify(connection, never()).rollback();
        verify(connection, never()).commit();
    }

    private void executeSmooks(String profile, String testName, boolean report) throws Exception {

        Smooks smooks = new Smooks(getClass().getResourceAsStream("jndi-ds-lifecycle.xml"));

        ExecutionContext ec = smooks.createExecutionContext(profile);

        if (report) {
            ec.getContentDeliveryRuntime().addExecutionEventListener(new HtmlReportGenerator("target/report/" + testName + ".html"));
        }

        smooks.filterSource(ec, source);
    }

    private void executeSmooksWithException(String profile, String testName) throws Exception {
        try {
            executeSmooks(profile, testName, JndiDataSourceTest.REPORT);
        } catch (Exception e) {
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockContextFactory.setAsInitial();

        source = new StringSource("<root><a /><b /></root>");

        InitialContext context = new InitialContext();
        context.bind("java:/mockDS", dataSource);
        context.bind("java:/mockTransaction", transaction);

        when(dataSource.getConnection()).thenReturn(connection);

    }

    @After
    public void tearDown() throws Exception {
        MockContextFactory.revertSetAsInitial();
    }
}
