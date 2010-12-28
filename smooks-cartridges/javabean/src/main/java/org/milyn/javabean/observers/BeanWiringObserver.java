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
package org.milyn.javabean.observers;

import java.lang.annotation.Annotation;

import org.milyn.container.ExecutionContext;
import org.milyn.javabean.BeanInstancePopulator;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;

/**
 * {@link BeanContext} Observer performing bean wiring.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanWiringObserver implements BeanContextLifecycleObserver {

	private BeanId watchedBeanId;
	private Class<?> watchedBeanType;
	private Class<? extends Annotation> watchedBeanAnnotation;
	private BeanId watchingBeanId;
	private BeanInstancePopulator populator;

	public BeanWiringObserver(BeanId watchingBean, BeanInstancePopulator populator) {
		this.watchingBeanId = watchingBean;
		this.populator = populator;		
	}
	
	public BeanWiringObserver watchedBeanId(BeanId watchedBeanId) {
		this.watchedBeanId = watchedBeanId;
		return this;
	}

	public BeanWiringObserver watchedBeanType(Class watchedBeanType) {
		this.watchedBeanType = watchedBeanType;
		return this;
	}

	public BeanWiringObserver watchedBeanAnnotation(Class<? extends Annotation> watchedBeanAnnotation) {
		this.watchedBeanAnnotation = watchedBeanAnnotation;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.milyn.javabean.lifecycle.BeanContextLifecycleObserver#onBeanLifecycleEvent(org.milyn.javabean.lifecycle.BeanContextLifecycleEvent)
	 */
	public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
		BeanId beanId = event.getBeanId();
		BeanLifecycle lifecycle = event.getLifecycle();		
		
		if(lifecycle == BeanLifecycle.ADD) {
			if(watchedBeanId != null && beanId != watchedBeanId) {
				return;
			}

			Object bean = event.getBean();
			if(!isMatchingBean(bean, watchedBeanType, watchedBeanAnnotation)) {
				return;
			}
			
			ExecutionContext executionContext = event.getExecutionContext();
			populator.populateAndSetPropertyValue(bean, executionContext.getBeanContext(), watchingBeanId, executionContext, event.getSource());
		} else if(beanId == watchingBeanId && lifecycle == BeanLifecycle.REMOVE) {
			BeanContext beanContext = event.getExecutionContext().getBeanContext();
			
			beanContext.removeObserver(this);
			// Need to remove the watched bean from the bean context too because it's lifecycle is associated 
			// with the lifecycle of the watching bean, which has been removed...
			if(watchedBeanId != null) {
				beanContext.removeBean(watchedBeanId, event.getSource());
			}
		}
	}
	
	public static boolean isMatchingBean(Object bean, Class<?> type, Class<? extends Annotation> annotation) {
		Class<? extends Object> beanClass = bean.getClass();

		if(type != null && !type.isAssignableFrom(beanClass)) {
			return false;
		}
		if(annotation != null && !beanClass.isAnnotationPresent(annotation)) {
			return false;
		}
		
		return true;
	}
}
