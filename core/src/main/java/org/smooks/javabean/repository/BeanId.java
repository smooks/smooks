/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.javabean.repository;

import org.smooks.cdr.ResourceConfig;
import org.smooks.javabean.context.BeanIdStore;



/**
 * Bean Id
 * <p />
 * The BeanId represents the id of a bean. It is used to set  and
 * retrieve instance of the bean from the BeanRepository.
 * <p />
 * The String representation of the BeanId is registered with a {@link BeanIdStore}
 * and there by coupled to that {@link BeanIdStore}. The BeanId holds the original
 * {@link String} beanId,also called beanIdName. It also holds the index of
 * the place it has within the {@link BeanIdStore}.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class BeanId {

	private final int index;

	private final String name;

	private final BeanIdStore beanIdStore;

	private ResourceConfig createResourceConfig;

	/**
	 * @param index
	 * @param name
	 */
	public BeanId(BeanIdStore beanIdStore, int index, String beanId) {
		this.beanIdStore = beanIdStore;
		this.index = index;
		this.name = beanId;
	}

	/**
	 * Set the {@link ResourceConfig} associated with the
	 * {@link BeanInstanceCreator} that is creating the bean with which this
	 * {@link BeanId} instance is associated.
	 * @param createResourceConfig The {@link ResourceConfig} of the creator.
	 * @return This object instance.
	 */
	public BeanId setCreateResourceConfiguration(ResourceConfig createResourceConfig) {
		this.createResourceConfig = createResourceConfig;
		return this;
	}

	/**
	 * Get the {@link ResourceConfig} associated with the
	 * {@link BeanInstanceCreator} that is creating the bean with which this
	 * {@link BeanId} instance is associated.
	 * @return The {@link ResourceConfig} of the creator.
	 */
	public ResourceConfig getCreateResourceConfiguration() {
		return createResourceConfig;
	}

	/**
	 * Returns the index of the place
	 * it holds in the {@link BeanIdStore}.
	 *
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the BeanId name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 54 + index;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}

		if(!(obj instanceof BeanId)) {
			return false;
		}
		BeanId rhs = (BeanId) obj;
		if(this.beanIdStore != rhs.beanIdStore) {
			return false;
		}
		return this.name == rhs.name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
}
