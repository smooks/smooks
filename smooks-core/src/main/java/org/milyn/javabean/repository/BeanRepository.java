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

package org.milyn.javabean.repository;

import org.milyn.container.ExecutionContext;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.context.StandaloneBeanContext;

import java.util.Map;

/**
 * Bean Repository
 * <p/>
 * This class represents a repository of bean's and the means to get and
 * set there instances.
 * <p/>
 * This class uses a {@link BeanIdRegister} to optimize the access performance. If
 * all the {@link BeanId} objects are registered with the BeanIdList before this object
 * is created then you get direct access performance. If you regularly register new
 * {@link BeanId} objects with the {@link BeanIdRegister}, after this object is created
 * then the BeanRepository needs to sync up with the {@link BeanIdRegister}. That
 * sync process takes some time, so it is adviced to register all the BeanId's up front.
 * <p/>
 * Only {@link BeanId} objects from the {@link BeanIdRegister}, which is set on
 * this BeanRepository, can be used with almost all of the methods.
 * <p/>
 * For ease of use it is also possible to get the bean by it's beanId name. This has however
 * not the direct access performance because a Map lookup is done. It is advised to use
 * the {@link BeanId} to get the bean from the repository.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Deprecated
public class BeanRepository {

	/**
     * Get the {@link BeanRepository} of the given {@link ExecutionContext}.
     *
     * @return the {@link BeanRepository} of the given {@link ExecutionContext}.
     * @see {@link BeanRepositoryManager#getBeanRepository(org.milyn.container.ExecutionContext)}.
     */
    public static BeanRepository getInstance(ExecutionContext executionContext) {
        return BeanRepositoryManager.getBeanRepository(executionContext);
    }

	private final BeanContext beanContext;

	public BeanRepository(BeanContext beanContext) {
		this.beanContext = beanContext;
	}

	public BeanRepository(ExecutionContext executionContext, BeanIdRegister beanIdRegister, Map<String, Object> beanMap) {
		beanContext = new StandaloneBeanContext(executionContext, beanIdRegister.getBeanIdStore(), beanMap);
	}

	public void addBean(BeanId beanId, Object bean) {
		beanContext.addBean(beanId, bean, null);
	}

	public void addBean(String beanId, Object bean) {
		beanContext.addBean(beanId, bean, null);
	}

	public void changeBean(BeanId beanId, Object bean) {
		beanContext.changeBean(beanId, bean, null);
	}

	public void clear() {
		beanContext.clear();
	}

	public boolean containsBean(BeanId beanId) {
		return beanContext.containsBean(beanId);
	}

	public Object getBean(BeanId beanId) {
		return beanContext.getBean(beanId);
	}

	public Object getBean(String beanId) {
		return beanContext.getBean(beanId);
	}

	public BeanId getBeanId(String beanId) {
		return beanContext.getBeanId(beanId);
	}

	public Map<String, Object> getBeanMap() {
		return beanContext.getBeanMap();
	}

	public Object removeBean(BeanId beanId) {
		return beanContext.removeBean(beanId, null);
	}

	public Object removeBean(String beanId) {
		return beanContext.removeBean(beanId, null);
	}

	public void setBeanInContext(BeanId beanId, boolean inContext) {
		beanContext.setBeanInContext(beanId, inContext);
	}

	public String toString() {
		return beanContext.toString();
	}

}
