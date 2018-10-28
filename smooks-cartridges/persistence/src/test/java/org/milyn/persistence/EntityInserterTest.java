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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringSource;
import org.milyn.persistence.test.util.BaseTestCase;
import org.milyn.persistence.util.PersistenceUtil;
import org.milyn.scribe.Dao;
import org.milyn.scribe.MappingDao;
import org.milyn.scribe.register.MapDaoRegister;
import org.milyn.scribe.register.SingleDaoRegister;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups="unit")
public class EntityInserterTest extends BaseTestCase {

	private static final boolean ENABLE_REPORTING = false;

	private static final String SIMPLE_XML =  "<root />";

	@Mock
	private Dao<String> dao;

	@Mock
	private MappingDao<String> mappedDao;

	@Test
	public void test_entity_insert() throws Exception {
		String toInsert1 = new String("toInsert1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-inserter-01.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_insert.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toInsert1", toInsert1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).insert(same(toInsert1));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_insert_with_named_dao() throws Exception {
		String toInsert1 = new String("toInsert1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-inserter-02.xml"));

        try {
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("dao1", dao);

            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, MapDaoRegister.newInstance(daoMap));

            enableReporting(executionContext, "report_test_entity_insert_with_named_dao.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toInsert1", toInsert1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).insert(same(toInsert1));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_insert_to_other_beanId() throws Exception {
		String toInsert1 = new String("toInsert1");

		String inserted1 = new String("inserted1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-inserter-03.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext,  new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_insert_to_other_beanId.html");

            when(dao.insert(toInsert1)).thenReturn(inserted1);

            JavaResult result = new JavaResult();
            result.getResultMap().put("toInsert1", toInsert1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            Assert.assertSame(inserted1, result.getBean("inserted1"));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_insert_with_mapped_dao() throws Exception {
		String toInsert1 = new String("toInsert1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-inserter-04.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext,  new SingleDaoRegister<Object>(mappedDao));

            enableReporting(executionContext, "report_test_entity_insert_with_mapped_dao.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toInsert1", toInsert1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(mappedDao).insert(eq("insert1"), same(toInsert1));
        } finally {
            smooks.close();
        }
	}

	@Test
	public void test_entity_insert_with_insertBefore() throws Exception {
		String toInsert1 = new String("toInsert1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-inserter-05.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_insert_with_insertBefore.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toInsert1", toInsert1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).insert(same(toInsert1));
        } finally {
            smooks.close();
        }
	}

	
	@Test
	public void test_entity_insert_producer_consumer() throws Exception {

		Smooks smooks = new Smooks(getResourceAsStream("entity-inserter-06.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_insert_producer_consumer.html");

            JavaResult result = new JavaResult();
            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).insert(same((String)result.getBean("toInsert")));
        } finally {
            smooks.close();
        }
	}

	/**
	 * @param resource
	 * @return
	 */
	private InputStream getResourceAsStream(String resource) {
		return EntityInserterTest.class.getResourceAsStream(resource);
	}

	private void enableReporting(ExecutionContext executionContext, String reportFilePath) throws IOException {
		if(ENABLE_REPORTING) {
			executionContext.setEventListener(new HtmlReportGenerator("target/" + reportFilePath));
		}
	}

}
