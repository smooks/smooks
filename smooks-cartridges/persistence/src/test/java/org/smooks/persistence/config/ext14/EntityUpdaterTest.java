/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.persistence.config.ext14;

import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.event.report.HtmlReportGenerator;
import org.smooks.payload.JavaResult;
import org.smooks.payload.StringSource;
import org.smooks.persistence.test.util.BaseTestCase;
import org.smooks.persistence.util.PersistenceUtil;
import org.smooks.scribe.Dao;
import org.smooks.scribe.register.SingleDaoRegister;
import org.mockito.Mock;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;

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
    
	@Test
	public void test_entity_update_no_selector() throws Exception {

		Smooks smooks = new Smooks(getResourceAsStream("entity-updater-no-selector.xml"));

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, new SingleDaoRegister<Object>(dao));

            enableReporting(executionContext, "report_test_entity_update_no_selector.html");

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
