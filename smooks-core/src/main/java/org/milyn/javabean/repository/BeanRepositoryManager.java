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

import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.context.BeanContext;

/**
 * The Bean Repository Manager
 * <p/>
 * Manages the {@link BeanRepository} of the current {@link ExecutionContext} and the {@link BeanIdRegister}
 * of the current {@link ApplicationContext}. It ensures that both objects are correctly instantiated.
 *
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @deprecated Use the {@link BeanContext} via the {@link ExecutionContext#getBeanContext()} method
 */
@Deprecated
public class BeanRepositoryManager {

	public static final String CONTEXT_KEY = BeanRepositoryManager.class.getName() + "#CONTEXT_KEY";

	public static final String BEAN_REPOSITORY_CONTEXT_KEY = BeanRepository.class.getName() + "#CONTEXT_KEY";

	private final BeanIdRegister beanIdRegister;

	/**
	 * Returns the instance of the {@link BeanRepositoryManager}, which is bound to the
	 * given {@link ApplicationContext}. If the {@link BeanRepositoryManager} doesn't
	 * exist yet, then one is created.
	 *
	 * @param applicationContext The {@link ApplicationContext} to which the instance is bound
	 * @return The {@link BeanRepositoryManager} instance of the given {@link ApplicationContext}
	 */
	public static BeanRepositoryManager getInstance(ApplicationContext applicationContext) {
		BeanRepositoryManager beanRepositoryManager = (BeanRepositoryManager) applicationContext.getAttribute(CONTEXT_KEY);

		if(beanRepositoryManager == null) {

			beanRepositoryManager = new BeanRepositoryManager(new BeanIdRegister(applicationContext.getBeanIdStore()));

			applicationContext.setAttribute(CONTEXT_KEY, beanRepositoryManager);

		}

		return beanRepositoryManager;

	}

	/**
	 * The object can only be instantiated with the {@link #getInstance(ApplicationContext)} method.
	 */
	private BeanRepositoryManager(BeanIdRegister beanIdRegister) {
		this.beanIdRegister = beanIdRegister;
	}


	/**
	 * @return the {@link BeanIdRegister} of the bound {@link ApplicationContext}
	 */
	public BeanIdRegister getBeanIdRegister() {
		return beanIdRegister;
	}

	/**
	 * @return the {@link BeanRepository} of the given {@link ExecutionContext}. If the {@link BeanRepository} does not
	 * 			exist then one is created. The {@link BeanIdRegister} which is bound to the {@link ApplicationContext}
	 * 			of the given {@link ExecutionContext} is bound to the created {@link BeanRepository}.
	 */
	public static BeanRepository getBeanRepository(ExecutionContext executionContext) {
		BeanRepository beanRepository = (BeanRepository) executionContext.getAttribute(BEAN_REPOSITORY_CONTEXT_KEY);

		if(beanRepository == null) {

			beanRepository = new BeanRepository(executionContext.getBeanContext());

			executionContext.setAttribute(BEAN_REPOSITORY_CONTEXT_KEY, beanRepository);
		}

		return beanRepository;
	}

}
