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

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.milyn.scribe.adapter.hibernate.test.util.BaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class SessionDaoAdapterTest extends BaseTestCase {

    @Mock
    private Session session;

    @Mock
    private Query query;

    private SessionDaoAdapter adapter;

    @Test(groups = "unit")
    public void test_persist() {

        // EXECUTE

        Object toPersist = new Object();

        // VERIFY

        adapter.insert(toPersist);

        verify(session).save(same(toPersist));

    }

    @Test(groups = "unit")
    public void test_merge() {

        // EXECUTE

        Object toMerge = new Object();

        Object merged = adapter.update(toMerge);

        // VERIFY

        verify(session).update(same(toMerge));

        assertSame(toMerge, merged);

    }

    @Test(groups = "unit")
    public void test_flush() {

        // EXECUTE

        adapter.flush();

        // VERIFY

        verify(session).flush();

    }

    @Test(groups = "unit")
    public void test_lookupByQuery_map_parameters() {

        // STUB

        List<?> listResult = Collections.emptyList();

        when(session.createQuery(anyString())).thenReturn(query);
        when(query.list()).thenReturn(listResult);

        // EXECUTE

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        Object result = adapter.lookupByQuery("query", params);

        // VERIFY

        assertSame(listResult, result);

        verify(session).createQuery(eq("query"));

        verify(query).setParameter(eq("key1"), eq("value1"));
        verify(query).setParameter(eq("key2"), eq("value2"));
        verify(query).list();

    }

    @Test(groups = "unit")
    public void test_lookupByQuery_array_parameters() {

        // STUB

        List<?> listResult = Collections.emptyList();

        when(session.createQuery(anyString())).thenReturn(query);
        when(query.list()).thenReturn(listResult);

        // EXECUTE

        Object[] params = new Object[2];
        params[0] = "value1";
        params[1] = "value2";

        Object result = adapter.lookupByQuery("query", params);

        // VERIFY

        assertSame(listResult, result);

        verify(session).createQuery(eq("query"));

        verify(query).setParameter(eq(1), eq("value1"));
        verify(query).setParameter(eq(2), eq("value2"));
        verify(query).list();

    }

    @Test(groups = "unit")
    public void test_lookup_map_parameters() {

        // STUB

        List<?> listResult = Collections.emptyList();

        when(session.getNamedQuery(anyString())).thenReturn(query);
        when(query.list()).thenReturn(listResult);

        // EXECUTE

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        Object result = adapter.lookup("name", params);

        // VERIFY

        assertSame(listResult, result);

        verify(session).getNamedQuery(eq("name"));

        verify(query).setParameter(eq("key1"), eq("value1"));
        verify(query).setParameter(eq("key2"), eq("value2"));
        verify(query).list();

    }

    @Test(groups = "unit")
    public void test_lookup_array_parameters() {

        // STUB

        List<?> listResult = Collections.emptyList();

        when(session.getNamedQuery(anyString())).thenReturn(query);
        when(query.list()).thenReturn(listResult);

        // EXECUTE

        Object[] params = new Object[2];
        params[0] = "value1";
        params[1] = "value2";

        Object result = adapter.lookup("name", params);

        // VERIFY

        assertSame(listResult, result);

        verify(session).getNamedQuery(eq("name"));

        verify(query).setParameter(eq(1), eq("value1"));
        verify(query).setParameter(eq(2), eq("value2"));
        verify(query).list();

    }


    /* (non-Javadoc)
     * @see org.milyn.scribe.test.util.BaseTestCase#beforeMethod()
     */
    @BeforeMethod(alwaysRun = true)
    @Override
    public void beforeMethod() {
        super.beforeMethod();

        adapter = new SessionDaoAdapter(session);
    }


}
