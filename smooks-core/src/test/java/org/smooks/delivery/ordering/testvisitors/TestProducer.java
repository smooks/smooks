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
package org.smooks.delivery.ordering.testvisitors;

import org.smooks.delivery.ordering.Producer;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class TestProducer implements Producer {

    private Set<String> products = new HashSet<String>();

    public TestProducer(String... products) {
        this.products.addAll(Arrays.asList(products));
    }

    public Set<? extends Object> getProducts() {
        return products;
    }
}
