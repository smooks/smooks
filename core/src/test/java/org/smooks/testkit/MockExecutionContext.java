/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.testkit;

import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.api.delivery.Filter;
import org.smooks.api.memento.MementoCaretaker;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.profile.Profile;
import org.smooks.api.profile.ProfileSet;
import org.smooks.engine.DefaultExecutionContext;
import org.smooks.engine.delivery.DefaultContentDeliveryRuntime;
import org.smooks.engine.delivery.DefaultReaderPool;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.testkit.delivery.dom.MockContentDeliveryConfig;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.engine.profile.DefaultProfileSet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author tfennelly
 */
@SuppressWarnings("unchecked")
public class MockExecutionContext implements ExecutionContext {
	private final DefaultExecutionContext executionContext;
	public final ProfileSet profileSet = new DefaultProfileSet(Profile.DEFAULT_PROFILE);
	public final ContentDeliveryConfig deliveryConfig = new MockContentDeliveryConfig();
	private ApplicationContext applicationContext = new MockApplicationContext();
	
	public MockExecutionContext() {
		executionContext = new DefaultExecutionContext(Profile.DEFAULT_PROFILE, applicationContext, new ArrayList<>());
	}

	@Override
    public void setDocumentSource(URI docSource) {
		executionContext.setDocumentSource(docSource);
    }
	
    @Override
    public URI getDocumentSource() {
		return executionContext.getDocumentSource();
	}
	
	@Override
	public ApplicationContext getApplicationContext() {
		return executionContext.getApplicationContext();
	}

	/* (non-Javadoc)
	 * @see org.smooks.engine.container.ExecutionContext#getTargetProfiles()
	 */
	@Override
	public ProfileSet getTargetProfiles() {
		return profileSet;
	}

	/* (non-Javadoc)
	 * @see org.smooks.engine.container.ExecutionContext#getDeliveryConfig()
	 */
	@Override
	public ContentDeliveryRuntime getContentDeliveryRuntime() {
		return new DefaultContentDeliveryRuntime(new DefaultReaderPool(Integer.parseInt(ParameterAccessor.getParameterValue(Filter.READER_POOL_SIZE, String.class, "0", deliveryConfig))), deliveryConfig);
	}

    public void setContentEncoding(String contentEncoding) throws IllegalArgumentException {
		executionContext.setContentEncoding(contentEncoding);
    }

	@Override
	public String getContentEncoding() {
		return executionContext.getContentEncoding();
    }

	@Override
	public void setTerminationError(Throwable terminationError) {
		executionContext.setTerminationError(terminationError);
    }

	@Override
	public Throwable getTerminationError() {
        return executionContext.getTerminationError();
    }

	@Override
	public String getConfigParameter(String name) {
		return executionContext.getConfigParameter(name);
    }

	@Override
	public String getConfigParameter(String name, String defaultVal) {
		return executionContext.getConfigParameter(name, defaultVal);
    }

	@SuppressWarnings("unused")
    public MockContentDeliveryConfig getMockDeliveryConfig() {
        return (MockContentDeliveryConfig) this.deliveryConfig;
    }
    
	@Override
	public BeanContext getBeanContext() {
		return executionContext.getBeanContext();
	}

    public void setBeanContext(BeanContext beanContext) {
		executionContext.setBeanContext(beanContext);
    }
    
	@Override
	public MementoCaretaker getMementoCaretaker() {
		return executionContext.getMementoCaretaker();
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public <T> void put(TypedKey<T> key, T value) {
		executionContext.put(key, value);
	}

	@Override
	public <T> T get(TypedKey<T> key) {
		return executionContext.get(key);
	}

	@Override
	public <T> T getOrDefault(TypedKey<T> key, T value) {
		return executionContext.getOrDefault(key, value);
	}

	@Override
	public Map<TypedKey<Object>, Object> getAll() {
		return executionContext.getAll();
	}

	@Override
	public <T> void remove(TypedKey<T> key) {
		executionContext.remove(key);
	}
}
