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
package org.milyn.delivery.ordering.testvisitors;

import org.milyn.delivery.ordering.Consumer;

import java.util.List;
import java.util.Arrays;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class TestConsumer implements Consumer {

    private List<String> consumes;

    public TestConsumer(String... consumes) {
        this.consumes = Arrays.asList(consumes);
    }

    public boolean consumes(Object object) {
        return consumes.contains(object);
    }
}
