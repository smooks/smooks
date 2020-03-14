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
package org.smooks.delivery.ordering;

import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Visitor;

import java.util.Set;

/**
 * Object Producer interface.
 * <p/>
 * A producer is a {@link org.smooks.delivery.Visitor} that "produces" a named object that is added to the
 * {@link ExecutionContext} for use by a {@link Consumer} of some sort.  Note that a {@link org.smooks.delivery.Visitor}
 * does not qualify as a producer just because it "produces" something.  It is only a producer
 * if it produces something that is added to the {@link ExecutionContext} for use by a
 * {@link Consumer}.
 * <p/>
 * The {@link Producer}/{@link Consumer} interfaces allows us to order the execution of multiple
 * {@link org.smooks.delivery.Visitor} instances, targetted at the same element selector, based on what the {@link org.smooks.delivery.Visitor}
 * produces and/or consumes.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 * @see Consumer
 * @since 1.2
 */
public interface Producer extends Visitor {

    /**
     * Get the set of products produced by this producer instance.
     * @return The set the set of products produced by this producer instance.
     */
    Set<? extends Object> getProducts();
}
