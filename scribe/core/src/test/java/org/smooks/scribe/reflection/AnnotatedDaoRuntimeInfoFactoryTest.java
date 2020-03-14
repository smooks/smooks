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
package org.smooks.scribe.reflection;

import static org.junit.Assert.*;

import org.smooks.scribe.test.dao.FullAnnotatedDao;
import org.smooks.scribe.test.dao.MinimumAnnotatedDao;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test( groups = "unit" )
public class AnnotatedDaoRuntimeInfoFactoryTest {


	public void test_create() {

		AnnotatedDaoRuntimeInfoFactory factory = new AnnotatedDaoRuntimeInfoFactory();
		AnnotatedDaoRuntimeInfo runtimeInfo = factory.create(FullAnnotatedDao.class);

		assertNotNull(runtimeInfo);

		AnnotatedDaoRuntimeInfo runtimeInfo2 = factory.create(FullAnnotatedDao.class);

		assertSame(runtimeInfo, runtimeInfo2);

		AnnotatedDaoRuntimeInfo runtimeInfo3 = factory.create(MinimumAnnotatedDao.class);

		assertNotSame(runtimeInfo, runtimeInfo3);
	}

}
