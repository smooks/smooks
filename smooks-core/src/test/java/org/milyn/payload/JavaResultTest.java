/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.payload;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link JavaResult}.
 * </p>
 *
 * @author Daniel Bevenius
 */
@SuppressWarnings("unchecked")
public class JavaResultTest
{
    private HashMap<String, Object> beans;

    @Before
    public void createBeanMap()
    {
        beans = new HashMap<String, Object>();
        beans.put("first", "bean1");
        beans.put("second", "bean2");
        beans.put("third", "bean3");
    }

    @Test
    public void extractSpecificBean()
    {
        JavaResult javaResult = new JavaResult(beans);
        Object result = javaResult.extractFromResult(javaResult, new Export(JavaResult.class, null, "second"));
        assertEquals("bean2", result);
    }

    @Test
    public void extractSpecificBeans()
    {
        JavaResult javaResult = new JavaResult(beans);
        Map<String, Object> result = (Map<String, Object>) javaResult.extractFromResult(javaResult, new Export(JavaResult.class, null, "second,first"));
        Assert.assertTrue(result.containsKey("first"));
        Assert.assertTrue(result.containsKey("second"));
        Assert.assertFalse(result.containsKey("third"));
    }
}
