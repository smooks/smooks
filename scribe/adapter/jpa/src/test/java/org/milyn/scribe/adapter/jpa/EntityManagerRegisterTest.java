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
package org.milyn.scribe.adapter.jpa;

import static junit.framework.Assert.*;

import javax.persistence.EntityManager;

import org.milyn.scribe.adapter.jpa.test.util.BaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.Test;
/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class EntityManagerRegisterTest extends BaseTestCase {

	@Mock
	EntityManager entityManager;

	@Test( groups = "unit" )
	public void test_getDao() {

		EntityManagerRegister register = new EntityManagerRegister(entityManager);

		EntityManagerDaoAdapter entityManagerDaoAdapter = register.getDefaultDao();

		assertNotNull(entityManagerDaoAdapter);

		assertSame(entityManager, entityManagerDaoAdapter.getEntityManager());

	}


}
