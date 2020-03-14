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
package org.smooks.persistence;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.event.report.HtmlReportGenerator;
import org.smooks.payload.JavaResult;
import org.smooks.payload.StringSource;
import org.smooks.persistence.test.util.BaseTestCase;
import org.smooks.persistence.util.PersistenceUtil;
import org.smooks.scribe.Dao;
import org.smooks.scribe.MappingDao;
import org.smooks.scribe.register.MapDaoRegister;
import org.smooks.scribe.register.SingleDaoRegister;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups="unit")
public class EntityDeleterTest extends BaseTestCase {

	private static final boolean ENABLE_REPORTING = false;

	private static final String SIMPLE_XML =  "<root />";

	@Mock
	private Dao<String> dao;

	@Mock
	private MappingDao<String> mappedDao;

	@Test
	public void test_entity_delete() throws Exception {
		String toDelete1 = new String("toDelete1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-deleter-01.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_delete.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toDelete1", toDelete1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).delete(same(toDelete1));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_delete_with_named_dao() throws Exception {
		String toDelete1 = new String("toDelete1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-deleter-02.xml"));

        try {
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("dao1", dao);

            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, MapDaoRegister.newInstance(daoMap));

            enableReporting(executionContext, "report_test_entity_delete_with_named_dao.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toDelete1", toDelete1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).delete(same(toDelete1));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_delete_to_other_beanId() throws Exception {
		String toDelete1 = new String("toDelete1");

		String deleted1 = new String("deleted1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-deleter-03.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext,  new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_delete_to_other_beanId.html");

            when(dao.delete(toDelete1)).thenReturn(deleted1);

            JavaResult result = new JavaResult();
            result.getResultMap().put("toDelete1", toDelete1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            Assert.assertSame(deleted1, result.getBean("deleted1"));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_delete_with_mapped_dao() throws Exception {
		String toDelete1 = new String("toDelete1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-deleter-04.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext,  new SingleDaoRegister<Object>(mappedDao));

            enableReporting(executionContext, "report_test_entity_delete_with_mapped_dao.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toDelete1", toDelete1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(mappedDao).delete(eq("delete1"), same(toDelete1));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_delete_with_deleteBefore() throws Exception {
		String toDelete1 = new String("toDelete1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-deleter-05.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_delete_with_deleteBefore.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toDelete1", toDelete1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).delete(same(toDelete1));
        } finally {
            smooks.close();
        }

	}

	@Test
	public void test_entity_delete_producer_consumer() throws Exception {

		Smooks smooks = new Smooks(getResourceAsStream("entity-deleter-06.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_delete_producer_consumer.html");

            JavaResult result = new JavaResult();
            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).delete(same((String)result.getBean("toDelete")));
        } finally {
            smooks.close();
        }

	}

	/**
	 * @param resource
	 * @return
	 */
	private InputStream getResourceAsStream(String resource) {
		return EntityDeleterTest.class.getResourceAsStream(resource);
	}

	private void enableReporting(ExecutionContext executionContext, String reportFilePath) throws IOException {
		if(ENABLE_REPORTING) {
			executionContext.setEventListener(new HtmlReportGenerator("target/" + reportFilePath));
		}
	}

}
