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
package org.milyn.persistence.observers;

import org.milyn.container.ExecutionContext;
import org.milyn.javabean.BeanRuntimeInfo;
import org.milyn.javabean.BeanRuntimeInfo.Classification;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;
import org.milyn.persistence.EntityLocatorParameterVisitor;

/**
 * Bean creation lifecycle observer.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanCreateLifecycleObserver implements BeanContextLifecycleObserver {

    private EntityLocatorParameterVisitor populator;
	private BeanId watchedBean;
	private BeanRuntimeInfo wiredBeanRI;
	private ArrayToListChangeObserver arrayToListChangeObserver;
	
	/**
	 * Public constructor.
	 * @param beanContext The associ
	 * @param populator
	 * @param watchedBean
	 * @param wiredBeanRI
	 */
	public BeanCreateLifecycleObserver(BeanId watchedBean, EntityLocatorParameterVisitor populator, BeanRuntimeInfo wiredBeanRI) {
		this.watchedBean = watchedBean;
		this.populator = populator;
		this.wiredBeanRI = wiredBeanRI;
	}

	/* (non-Javadoc)
	 * @see org.milyn.javabean.lifecycle.BeanContextLifecycleObserver#onBeanLifecycleEvent(org.milyn.javabean.lifecycle.BeanContextLifecycleEvent)
	 */
	public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
		if(event.getBean() == watchedBean) {
			switch(event.getLifecycle()) {
			case ADD:
				if (wiredBeanRI != null && wiredBeanRI.getClassification() == Classification.ARRAY_COLLECTION) {
					// Register an observer which looks for the change that the mutable
					// list of the selected bean gets converted to an array. We
					// can then set this array
					arrayToListChangeObserver = new ArrayToListChangeObserver();
					event.getExecutionContext().getBeanContext().addObserver(arrayToListChangeObserver);
				} else {
					populator.populateAndSetPropertyValue(event.getBean(), event.getExecutionContext());
				}
			case REMOVE:
				try{
					if(arrayToListChangeObserver != null) {
						event.getExecutionContext().getBeanContext().removeObserver(arrayToListChangeObserver);
					}
				} finally {
					event.getExecutionContext().getBeanContext().removeObserver(this);
				}
			}
		}
	}

	class ArrayToListChangeObserver implements BeanContextLifecycleObserver {
				
		/* (non-Javadoc)
		 * @see org.milyn.javabean.lifecycle.BeanContextLifecycleObserver#onBeanLifecycleEvent(org.milyn.javabean.lifecycle.BeanContextLifecycleEvent)
		 */
		public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
			if(event.getBeanId() == watchedBean && event.getLifecycle() == BeanLifecycle.CHANGE) {
				ExecutionContext executionContext = event.getExecutionContext();

				// Set the list on the object, via the populator...
				populator.populateAndSetPropertyValue(event.getBean(), executionContext);
				// Remove this observer...
				executionContext.getBeanContext().removeObserver(this);
				arrayToListChangeObserver = null;
			}
		}
	}
}
