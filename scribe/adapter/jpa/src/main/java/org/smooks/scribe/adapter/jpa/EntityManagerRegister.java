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
package org.smooks.scribe.adapter.jpa;

import java.util.Map;

import javax.persistence.EntityManager;

import org.smooks.scribe.register.AbstractDaoAdapterRegister;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class EntityManagerRegister extends AbstractDaoAdapterRegister<EntityManagerDaoAdapter, EntityManager> {

	public EntityManagerRegister(EntityManager defaultAdaptable) {
		super(defaultAdaptable);
	}

	public EntityManagerRegister(EntityManager defaultAdaptable,
			Map<String, ? extends EntityManager> adaptableMap) {
		super(defaultAdaptable, adaptableMap);
	}

	public EntityManagerRegister(
			Map<String, ? extends EntityManager> adaptableMap) {
		super(adaptableMap);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.register.AbstractDaoAdapterRegister#createAdapter(java.lang.Object)
	 */
	@Override
	protected EntityManagerDaoAdapter createAdapter(EntityManager adaptable) {
		return new EntityManagerDaoAdapter(adaptable);
	}



}
