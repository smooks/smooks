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
package org.milyn.delivery.ordering;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Visitor;

/**
 * Object Consumer interface.
 * <p/>
 * A consumer is a {@link org.milyn.delivery.Visitor} that "consumes" a named object that has been added to the
 * {@link ExecutionContext} by a {@link Producer} of some sort.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 * @see Producer
 * @since 1.2
 */
public interface Consumer extends Visitor {

    /**
     * Does this consumer consume the specified named object.
     * <p/>
     * The named object would be a product of a {@link Producer} that is executing
     * on the same element.  The consumer should only return <code>false</code> if it knows for
     * certain that it doesn't consumer the specified named object.  If uncertain, it should
     * error on the side of saying that it does consume the object.
     *
     * @param object The product representation
     * @return True if the consumer consumes the specified product, otherwise false.
     */
    boolean consumes(Object object);
}
