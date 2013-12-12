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
package org.milyn.osgi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.milyn.SmooksFactory;
import static org.mockito.Mockito.mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;

/**
 * Unit test for {@link org.milyn.osgi.SmooksServiceFactory}.
 *
 * @author Daniel Bevenius
 */
public class SmooksServiceFactoryTest {
    @Test
    public void getService() {
        final ServiceFactory serviceFactory = new SmooksServiceFactory();
        final Bundle bundle = mock(Bundle.class);
        final Object service = serviceFactory.getService(bundle, null);
        assertThat(service, is(instanceOf(SmooksFactory.class)));
    }

}
