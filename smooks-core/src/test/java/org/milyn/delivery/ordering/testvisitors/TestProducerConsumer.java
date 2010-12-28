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

import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.ordering.Consumer;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class TestProducerConsumer implements Producer, Consumer {

    private Set<String> products = new HashSet<String>();
    private List<String> consumes;

    public TestProducerConsumer setProducts(String... products) {
        this.products.addAll(Arrays.asList(products));
        return this;
    }

    public TestProducerConsumer setConsumes(String... consumes) {
        this.consumes = Arrays.asList(consumes);
        return this;
    }

    public Set<? extends Object> getProducts() {
        return products;
    }

    public boolean consumes(Object object) {
        if(consumes == null) {
            return false;
        }
        return consumes.contains(object);
    }
}