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

package org.smooks.routing.io;

import static org.testng.AssertJUnit.*;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.Configurator;
import org.smooks.container.MockApplicationContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Unit test for {@link OutputStreamRouter}
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
@Test ( groups = "unit" )
public class OutputStreamRouterTest
{
	private String resourceName = "testResource";
	private String beanId = "testBeanId";
	private OutputStreamRouter router = new OutputStreamRouter();
	private SmooksResourceConfiguration config;
	
	@Test
	public void configure()
	{
        Configurator.configure( router, config, new MockApplicationContext() );
        
        assertEquals( resourceName, router.getResourceName() );
	}
	
	@BeforeTest
	public void setup()
	{
		config = createConfig( resourceName, beanId );
	}
	
	//	private
	
	private SmooksResourceConfiguration createConfig( 
			final String resourceName,
			final String beanId)
	{
    	SmooksResourceConfiguration config = new SmooksResourceConfiguration( "x", OutputStreamRouter.class.getName() );
		config.setParameter( "resourceName", resourceName );
		config.setParameter( "beanId", beanId );
		return config;
	}

}
