/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.db;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.Connection;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.xml.transform.Source;

import junit.framework.TestCase;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.StringSource;
import org.mockejb.jndi.MockContextFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

/**
 * Unit test for {@link JndiDataSource}.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class JndiDataSourceTest extends TestCase {

	public Source source;

	@Mock
	private DataSource dataSource;

	@Mock
	private UserTransaction transaction;

	@Mock
	private Connection connection;

	private static boolean REPORT = false;


	public void test_jndi_autoCommit() throws Exception {

		when(connection.getAutoCommit()).thenReturn(false);

		executeSmooks("jndi_autocommit", "test_jndi_autoCommit", REPORT);

		verify(dataSource).getConnection();
		verify(connection).setAutoCommit(true);
		verify(connection).close();

		verify(connection, never()).commit();
		verify(connection, never()).rollback();
	}

	public void test_jndi() throws Exception{

		when(connection.getAutoCommit()).thenReturn(true);

		executeSmooks("jndi", "test_jndi", REPORT);

		verify(dataSource).getConnection();
		verify(connection).setAutoCommit(false);
		verify(connection).commit();
		verify(connection).close();

		verify(connection, never()).rollback();
	}


	public void test_jndi_exception() throws Exception{

		when(connection.getAutoCommit()).thenReturn(false);

		executeSmooksWithException("jndi_exception", "test_jndi_exception", REPORT);

		verify(dataSource).getConnection();
		verify(connection).rollback();
		verify(connection).close();

		verify(connection, never()).setAutoCommit(false);
		verify(connection, never()).commit();
	}

	public void test_jta_new_transaction() throws Exception{

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

	public void test_jta_existing_transaction() throws Exception{

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

	public void test_jta_exception() throws Exception{

		when(connection.getAutoCommit()).thenReturn(false);
		when(transaction.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

		executeSmooksWithException("jta_exception", "test_jta_exception", REPORT);

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

	public void test_jta_existing_transaction_exception() throws Exception{

		when(connection.getAutoCommit()).thenReturn(false);
		when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);

		executeSmooksWithException("jta_exception", "test_jta_existing_transaction_exception", REPORT);

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

	public void test_jta_set_autocommit_not_allowed() throws Exception{
		when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);

		executeSmooks("jta_set_autocommit_not_allowed", "test_jta_set_autocommit_not_allowed", REPORT);

		verify(dataSource, atLeastOnce()).getConnection();
		verify(connection).close();

		verify(connection, never()).setAutoCommit(false);
		verify(connection, never()).getAutoCommit();
	}

	public void test_jta_missing_transaction() {

		try {
			executeSmooks("jta_missing_transaction", "test_jta_missing_transaction", REPORT);
		} catch (Exception e) {
			assertEquals("The transactionJndi attribute must be set when the JTA transaction manager is set.", ExceptionUtils.getCause(e).getMessage());

			return;
		}
		fail("Exception was not thrown to indicate that the transactionJndi wasn't set.");
	}

	public void test_external() throws Exception{

		when(connection.getAutoCommit()).thenReturn(true);

		executeSmooks("external", "external", true);

		verify(dataSource).getConnection();
		verify(connection).setAutoCommit(false);
		verify(connection).close();

		verify(connection, never()).commit();
		verify(connection, never()).rollback();
	}

	public void test_external_autocommit() throws Exception{

		when(connection.getAutoCommit()).thenReturn(false);

		executeSmooks("external_autocommit", "test_external_autocommit", REPORT);

		verify(dataSource).getConnection();
		verify(connection).setAutoCommit(true);
		verify(connection).close();

		verify(connection, never()).commit();
		verify(connection, never()).rollback();
	}

	public void test_external_set_autocommit_not_allowed() throws Exception{

		executeSmooks("external_set_autocommit_not_allowed", "test_external_set_autocommit_not_allowed", true);

		verify(dataSource).getConnection();
		verify(connection).close();

		verify(connection, never()).getAutoCommit();
		verify(connection, never()).setAutoCommit(true);
		verify(connection, never()).commit();
		verify(connection, never()).rollback();
	}

	public void test_external_exception() throws Exception{

		when(connection.getAutoCommit()).thenReturn(false);

		executeSmooksWithException("external_exception", "test_external_exception", REPORT);

		verify(dataSource).getConnection();
		verify(connection).close();

		verify(connection, never()).setAutoCommit(false);
		verify(connection, never()).rollback();
		verify(connection, never()).commit();
	}

	private void executeSmooks(String profile, String testName, boolean report) throws Exception {

		Smooks smooks = new Smooks(getClass().getResourceAsStream("jndi-ds-lifecycle.xml"));

		ExecutionContext ec = smooks.createExecutionContext(profile);

		if(report) {
			ec.setEventListener(new HtmlReportGenerator("target/report/"+ testName +".html"));
		}

		smooks.filterSource(ec, source);
	}

	private void executeSmooksWithException(String profile, String testName, boolean report) throws Exception {
		try {
			executeSmooks(profile, testName, report);
		} catch (Exception e) {
		}
		return;
	}

	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		MockContextFactory.setAsInitial();

		source = new StringSource("<root><a /><b /></root>");

		InitialContext context = new InitialContext();
		context.bind("java:/mockDS", dataSource);
		context.bind("java:/mockTransaction", transaction);

		when(dataSource.getConnection()).thenReturn(connection);

	}

	@Override
	protected void tearDown() throws Exception {
		MockContextFactory.revertSetAsInitial();
	}
}