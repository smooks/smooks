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

package org.smooks.javabean.lifecycle;

import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Fragment;
import org.smooks.javabean.repository.BeanId;

/**
 * An event when a lifecycle event has happend to a bean.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class BeanContextLifecycleEvent {

	private final ExecutionContext executionContext;

    private Fragment source;

	private final BeanLifecycle lifecycle;

	private final BeanId beanId;

	private final Object bean;


    /**
     * Public constructor.
	 * @param executionContext
     * @param source Source fragment name.
	 * @param beanId Source bean.
	 * @param lifecycle Lifecycle.
	 * @param bean Bean instance.
	 */
	public BeanContextLifecycleEvent(ExecutionContext executionContext, Fragment source, BeanLifecycle lifecycle, BeanId beanId, Object bean) {

		this.executionContext = executionContext;
        this.source = source;
		this.beanId = beanId;
		this.lifecycle = lifecycle;
		this.bean = bean;
	}

    /**
	 * @return the executionContext
	 */
	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

    /**
     * Get the even source fragment.
     * @return Source fragment.
     */
    public Fragment getSource() {
        return source;
    }

    /**
	 * @return the lifecycle
	 */
	public BeanLifecycle getLifecycle() {
		return lifecycle;
	}

    /**
	 * @return the beanId
	 */
	public BeanId getBeanId() {
		return beanId;
	}

    /**
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}
}
