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
package org.smooks.container.standalone;

import static org.junit.Assert.*;

import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.SmooksUtil;
import org.smooks.profile.DefaultProfileSet;

/**
 * Unit test for {@link StandaloneExecutionContext}
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class StandaloneExecutionContextTest
{
    private StandaloneExecutionContext context;
	@Test
	public void getAttributes()
	{
        final String key = "testKey";
        final String value = "testValue";
        context.setAttribute( key, value );
        
        Hashtable attributes = context.getAttributes();
        
        assertTrue( attributes.containsKey( key ) );
        assertTrue( attributes.contains( value ) );
	}
	
	@Before
	public void setup()
	{
        Smooks smooks = new Smooks();
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("device1", new String[] {"profile1"}), smooks);
        context = new StandaloneExecutionContext("device1", smooks.getApplicationContext(), null);
	}
	

}
