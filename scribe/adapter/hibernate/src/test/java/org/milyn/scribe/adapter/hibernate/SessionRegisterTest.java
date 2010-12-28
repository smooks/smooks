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
package org.milyn.scribe.adapter.hibernate;

import static junit.framework.Assert.*;

import org.hibernate.Session;
import org.milyn.scribe.adapter.hibernate.SessionDaoAdapter;
import org.milyn.scribe.adapter.hibernate.SessionRegister;
import org.milyn.scribe.adapter.hibernate.test.util.BaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class SessionRegisterTest extends BaseTestCase {

	@Mock
	Session session;

	@Test( groups = "unit" )
	public void test_getDao() {

		SessionRegister register = new SessionRegister(session);

		SessionDaoAdapter sessionDaoAdapter = register.getDefaultDao();

		assertNotNull(sessionDaoAdapter);

		assertSame(session, sessionDaoAdapter.getSession());

	}


}
