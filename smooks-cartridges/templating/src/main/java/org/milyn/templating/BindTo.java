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
package org.milyn.templating;

import org.milyn.commons.assertion.AssertArgument;

/**
 * BindTo template usage.
 * <p/>
 * Bind the templating result to the specified beanId.  The templating result is
 * then available for other puroposes e.g. routing.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class BindTo implements Usage {

    private String beanId;

    public BindTo(String beanId) {
        AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
        this.beanId = beanId;
    }

    protected String getBeanId() {
        return beanId;
    }

    public static BindTo beanId(String beanId) {
        return new BindTo(beanId);
    }
}
