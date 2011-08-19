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
package org.milyn;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

/**
 * Unit test for {@link SmooksOSGIFactory}
 * 
 * @author Daniel Bevenius
 *
 */
public class SmooksOSGIFactoryTest
{
    @Test
    public void createWithoutConfig() throws IOException, SAXException
    {
        final Bundle bundle = mock(Bundle.class);
        final SmooksOSGIFactory factory = new SmooksOSGIFactory(bundle);
        final Smooks smooks = factory.createInstance();
        assertThat(smooks.getClassLoader(), is(instanceOf(BundleClassLoaderDelegator.class)));
    }
}
