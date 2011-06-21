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
public class EntityUpdaterTest extends BaseTestCase {

	private static final boolean ENABLE_REPORTING = false;

	private static final String SIMPLE_XML =  "<root />";

	@Mock
	private Dao<String> dao;

	@Mock
	private MappingDao<String> mappedDao;

	public void test_entity_update() throws Exception {
		String toUpdate1 = new String("toUpdate1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-updater-01.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_update.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toUpdate1", toUpdate1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).update(same(toUpdate1));
        } finally {
            smooks.close();
        }
	}

	public void test_entity_update_with_named_dao() throws Exception {
		String toUpdate1 = new String("toUpdate1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-updater-02.xml"));

        try {
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("dao1", dao);

            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, MapDaoRegister.newInstance(daoMap));

            enableReporting(executionContext, "report_test_entity_update_with_named_dao.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toUpdate1", toUpdate1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).update(same(toUpdate1));
        } finally {
            smooks.close();
        }
	}

	public void test_entity_update_to_other_beanId() throws Exception {
		String toUpdate1 = new String("toUpdate1");

		String updated1 = new String("updated1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-updater-03.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext,  new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_update_to_other_beanId.html");

            when(dao.update(toUpdate1)).thenReturn(updated1);

            JavaResult result = new JavaResult();
            result.getResultMap().put("toUpdate1", toUpdate1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            Assert.assertSame(updated1, result.getBean("updated1"));
        } finally {
            smooks.close();
        }
	}

	public void test_entity_update_with_mapped_dao() throws Exception {
		String toUpdate1 = new String("toUpdate1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-updater-04.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext,  new SingleDaoRegister<Object>(mappedDao));

            enableReporting(executionContext, "report_test_entity_update_with_mapped_dao.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toUpdate1", toUpdate1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(mappedDao).update(eq("update1"), same(toUpdate1));
        } finally {
            smooks.close();
        }
    }

	public void test_entity_update_with_updateBefore() throws Exception {
		String toUpdate1 = new String("toUpdate1");

		Smooks smooks = new Smooks(getResourceAsStream("entity-updater-05.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_update_with_updateBefore.html");

            JavaResult result = new JavaResult();
            result.getResultMap().put("toUpdate1", toUpdate1);

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).update(same(toUpdate1));
        } finally {
            smooks.close();
        }
	}


	public void test_entity_update_producer_consumer() throws Exception {

		Smooks smooks = new Smooks(getResourceAsStream("entity-updater-06.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_update_producer_consumer.html");

            JavaResult result = new JavaResult();
            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).update(same((String)result.getBean("toUpdate")));
        } finally {
            smooks.close();
        }
	}

	/**
	 * @param resource
	 * @return
	 */
	private InputStream getResourceAsStream(String resource) {
		return EntityUpdaterTest.class.getResourceAsStream(resource);
	}

	private void enableReporting(ExecutionContext executionContext, String reportFilePath) throws IOException {
		if(ENABLE_REPORTING) {
			executionContext.setEventListener(new HtmlReportGenerator("target/" + reportFilePath));
		}
	}

}
