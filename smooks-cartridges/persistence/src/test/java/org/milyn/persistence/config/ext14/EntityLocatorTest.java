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

package org.milyn.persistence.config.ext14;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringSource;
import org.milyn.persistence.test.dao.FullInterfaceDao;
import org.milyn.persistence.test.util.BaseTestCase;
import org.milyn.persistence.util.PersistenceUtil;
import org.milyn.scribe.register.MapDaoRegister;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups="unit")
public class EntityLocatorTest extends BaseTestCase {
	private static final boolean ENABLE_REPORTING = false;

    private static final String SIMPLE_XML =  "<root />";
    
	@Mock
	private FullInterfaceDao<Object> dao;

	@Test
	public void test_entity_locate_no_selector() throws Exception {
		String searchResult = "Test";
        List<String> searchResultList = new ArrayList<String>();
        searchResultList.add(searchResult);

		when(dao.lookup(anyString(), anyString())).thenReturn(searchResultList);

		Smooks smooks = new Smooks(getResourceAsStream("entity-locator-no-selector.xml"));


        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            PersistenceUtil.setDAORegister(executionContext, MapDaoRegister.builder().put("some", dao).build());

            enableReporting(executionContext, "test_entity_locate_no_selector.html");

            JavaResult result = new JavaResult();

            smooks.filterSource(executionContext, new StringSource(SIMPLE_XML), result);

            verify(dao).lookup(eq("test"), eq("value-1"));

            List<String> resultList = (List<String>) result.getBean("theList");

            Assert.assertNotNull(resultList);
            Assert.assertSame(searchResult, resultList.get(0));

        }finally {
            smooks.close();
        }


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
