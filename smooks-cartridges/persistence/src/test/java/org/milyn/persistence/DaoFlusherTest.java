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

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.StringSource;
import org.milyn.persistence.test.dao.FullInterfaceDao;
import org.milyn.persistence.test.dao.FullInterfaceMappedDao;
import org.milyn.persistence.test.util.BaseTestCase;
import org.milyn.persistence.util.PersistenceUtil;
import org.milyn.scribe.register.MapDaoRegister;
import org.milyn.scribe.register.SingleDaoRegister;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@Test(groups = "unit")
public class DaoFlusherTest extends BaseTestCase {

    private static final boolean ENABLE_REPORTING = false;

    private static final String SIMPLE_XML = "<root />";

    @Mock
    private FullInterfaceDao<Object> dao;

    @Mock
    private FullInterfaceMappedDao<Object> mappedDao;

    @Test
    public void test_dao_flush() throws Exception {
        Smooks smooks = new Smooks(getResourceAsStream("doa-flusher-01.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_dao_flush.html");

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), null);

            verify(dao).flush();
        } finally {
            smooks.close();
        }
    }

    @Test
    public void test_dao_flush_with_named_dao() throws Exception {

        Smooks smooks = new Smooks(getResourceAsStream("doa-flusher-02.xml"));

        try {
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("dao1", dao);

            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, MapDaoRegister.newInstance(daoMap));

            enableReporting(executionContext, "report_test_dao_flush_with_named_dao.html");

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), null);

            verify(dao).flush();
        } finally {
            smooks.close();
        }
    }


    @Test
    public void test_dao_flush_with_flushBefore() throws Exception {
        Smooks smooks = new Smooks(getResourceAsStream("doa-flusher-03.xml"));

        try {
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("mappedDao", mappedDao);
            daoMap.put("dao", dao);

            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, MapDaoRegister.newInstance(daoMap));

            enableReporting(executionContext, "report_test_dao_flush_with_flushBefore.html");

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), null);

            verify(dao).flush();
        } finally {
            smooks.close();
        }
    }


    /**
     * @param resource
     * @return
     */
    private InputStream getResourceAsStream(String resource) {
        return DaoFlusherTest.class.getResourceAsStream(resource);
    }

    private void enableReporting(ExecutionContext executionContext, String reportFilePath) throws IOException {
        if (ENABLE_REPORTING) {
            executionContext.setEventListener(new HtmlReportGenerator("target/" + reportFilePath));
        }
    }
}
