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
package org.milyn.javabean;

import org.milyn.assertion.AssertArgument;
import org.milyn.delivery.VisitorAppender;

/**
 * Abstract Binding Appender.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class BindingAppender implements VisitorAppender {

	private String beanId;

	/**
	 * Public constructor.
	 * @param beanId Bean Id.
	 */
	public BindingAppender(String beanId) {
        AssertArgument.isNotNull(beanId, "beanId");
		this.beanId = beanId;
	}

	/**
	 * Get the beanId of this Bean configuration.
	 *
	 * @return The beanId of this Bean configuration.
	 */
	public String getBeanId() {
	    return beanId;
	}
}