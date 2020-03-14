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
package org.smooks.javabean.observers;

import org.smooks.container.ExecutionContext;
import org.smooks.javabean.BeanInstancePopulator;
import org.smooks.javabean.lifecycle.BeanContextLifecycleEvent;
import org.smooks.javabean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.javabean.lifecycle.BeanLifecycle;
import org.smooks.javabean.repository.BeanId;

/**
 * List to array change event listener.
 * <p/>
 * Arrays start out their lives as Lists.  When the list is populated with all
 * wired in object entries, the List is converted to an Array.  This observer listens
 * for that event and triggers the wiring of the array into the target bean.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ListToArrayChangeObserver implements BeanContextLifecycleObserver {
	
    private String property;
    private BeanInstancePopulator populator;
	private BeanId watchedBean;
	
	public ListToArrayChangeObserver(BeanId watchedBean, String property, BeanInstancePopulator populator) {
		this.watchedBean = watchedBean;
		this.property = property;
		this.populator = populator;
	}

	/* (non-Javadoc)
	 * @see org.smooks.javabean.lifecycle.BeanContextLifecycleObserver#onBeanLifecycleEvent(org.smooks.javabean.lifecycle.BeanContextLifecycleEvent)
	 */
	public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
		if(event.getBeanId() == watchedBean && event.getLifecycle() == BeanLifecycle.CHANGE) {
			ExecutionContext executionContext = event.getExecutionContext();

			// Set the array on the object, via the populator...
			populator.setPropertyValue(property, event.getBean(), executionContext, event.getSource());
			// Remove this observer...
			executionContext.getBeanContext().removeObserver(this);
		}
	}
}
