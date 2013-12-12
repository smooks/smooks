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
package org.milyn.cartridge.javabean.observers;

import org.milyn.cartridge.javabean.BeanInstancePopulator;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;

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
	 * @see org.milyn.javabean.lifecycle.BeanContextLifecycleObserver#onBeanLifecycleEvent(org.milyn.javabean.lifecycle.BeanContextLifecycleEvent)
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
