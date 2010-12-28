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
package org.milyn.persistence;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.javabean.repository.BeanRepository;
import org.milyn.persistence.test.dao.FullInterfaceDao;
import org.milyn.persistence.test.util.BaseTestCase;
import org.milyn.persistence.util.PersistenceUtil;
import org.milyn.scribe.register.MapDaoRegister;
import org.milyn.scribe.register.SingleDaoRegister;
import org.mockito.Mock;
import org.testng.annotations.Test;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups="unit")
public class EntityLocatorTest extends BaseTestCase {
	private static final boolean ENABLE_REPORTING = false;

	@Mock
	private FullInterfaceDao<Object> dao;

	@SuppressWarnings("unchecked")
	public void test_entity_locate() throws Exception {
		Object result = new Object();

		HashMap<String, Object> expectedArg3 = new HashMap<String, Object>();
		expectedArg3.put("d", new Integer(2));
		expectedArg3.put("e", new Integer(3));

		HashMap<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put("arg1", new Integer(1));
		expectedMap.put("arg2", new Integer(5));
		expectedMap.put("arg3", expectedArg3);
		expectedMap.put("arg4", "value");
		expectedMap.put("arg5", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2009-02-11 23:15:11"));

		when(dao.lookup(anyString(), anyMap())).thenReturn(result);

		Smooks smooks = new Smooks(getResourceAsStream("entity-locator-01.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "test_entity_locate.html");

            Source source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml" ) );
            smooks.filterSource(executionContext, source);

            assertSame(result, executionContext.getBeanContext().getBean("entity"));
        } finally {
            smooks.close();
        }

        verify(dao).lookup("something", expectedMap);
	}

	@SuppressWarnings("unchecked")
	public void test_entity_locate_query_no_result() throws Exception {
		Collection<?> result = Collections.emptyList();

		when(dao.lookupByQuery(anyString(), anyMap())).thenReturn(result);

		Smooks smooks = new Smooks(getResourceAsStream("entity-locator-02.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            //We put an object on the 'entity' location to check if the locater removes it because it found
            //no result
            executionContext.getBeanContext().addBean("entity", new Object(), null);

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "test_entity_locate_query_no_result.html");

            Source source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml" ) );
            smooks.filterSource(executionContext, source);

            assertNull(executionContext.getBeanContext().getBean("entity"));
        } finally {
            smooks.close();
        }

        verify(dao).lookupByQuery(eq("from SomeThing"), anyMap());
	}

	@SuppressWarnings("unchecked")
	public void test_entity_locate_no_result_but_expected() throws Exception {
		Collection<?> result = Collections.emptyList();

		when(dao.lookupByQuery(anyString(), anyMap())).thenReturn(result);

		Smooks smooks = new Smooks(getResourceAsStream("entity-locator-03.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            Source source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml" ) );

            smooks.filterSource(executionContext, source);

        }catch (SmooksException e) {
			assertSame(ExceptionUtils.getCause(e).getClass(), NoLookupResultException.class);

			return;
		}
        finally {
            smooks.close();
        }

        fail("NoLookupResultException was not thrown.");
	}

	public void test_entity_locate_query_positional_parameter() throws Exception {
		Collection<?> result = Collections.emptyList();

		when(dao.lookupByQuery(anyString(), anyString(), anyString())).thenReturn(result);

		Smooks smooks = new Smooks(getResourceAsStream("entity-locator-04.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            Source source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml" ) );

            enableReporting(executionContext, "test_entity_locate_query_positional_parameter.html");

            smooks.filterSource(executionContext, source);

        }finally {
            smooks.close();
        }

        verify(dao).lookupByQuery(eq("from SomeThing where arg1=:1 and arg2=:2"), eq("value-1"), eq("value-2"));
	}

	public void test_entity_locate_positional_parameter() throws Exception {
		Collection<?> result = Collections.emptyList();

		when(dao.lookup(anyString(), anyString(), anyString())).thenReturn(result);

		Smooks smooks = new Smooks(getResourceAsStream("entity-locator-05.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, MapDaoRegister.builder().put("some", dao).build());

            Source source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml" ) );

            enableReporting(executionContext, "test_entity_locate_positional_parameter.html");

            smooks.filterSource(executionContext, source);

        }finally {
            smooks.close();
        }

        verify(dao).lookup(eq("test"), eq("value-1"), eq("value-2"));
	}

	/**
	 * @param resource
	 * @return
	 */
	private InputStream getResourceAsStream(String resource) {
		return EntityLocatorTest.class.getResourceAsStream(resource);
	}

	private void enableReporting(ExecutionContext executionContext, String reportFilePath) throws IOException {
		if(ENABLE_REPORTING) {
			executionContext.setEventListener(new HtmlReportGenerator("target/" + reportFilePath));
		}
	}
}
