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

package org.smooks.javabean.factory;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.container.MockApplicationContext;

/**
 * 
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class FactoryDefinitionParserFactoryTest {

    @Test
    public void test_get_instance_default() {
        FactoryDefinitionParser parser = FactoryDefinitionParser.FactoryDefinitionParserFactory.getInstance(new MockApplicationContext());

        assertNotNull(parser);
        assertEquals(BasicFactoryDefinitionParser.class, parser.getClass());
    }

    @Test
    public void test_get_instance_default_alias() {
        FactoryDefinitionParser parser = FactoryDefinitionParser.FactoryDefinitionParserFactory.getInstance(FactoryDefinitionParser.FactoryDefinitionParserFactory.DEFAULT_ALIAS, new MockApplicationContext());

        assertNotNull(parser);
        assertEquals(BasicFactoryDefinitionParser.class, parser.getClass());
    }

    @Test
    public void test_get_instance_mvel_alias() {
        FactoryDefinitionParser parser = FactoryDefinitionParser.FactoryDefinitionParserFactory.getInstance("mvel", new MockApplicationContext());

        assertNotNull(parser);
        assertEquals(MVELFactoryDefinitionParser.class, parser.getClass());
    }

    @Test
    public void test_get_instance_mvel_by_classname() {
        FactoryDefinitionParser parser = FactoryDefinitionParser.FactoryDefinitionParserFactory.getInstance(MVELFactoryDefinitionParser.class.getName(), new MockApplicationContext());

        assertNotNull(parser);
        assertEquals(MVELFactoryDefinitionParser.class, parser.getClass());
    }
}
