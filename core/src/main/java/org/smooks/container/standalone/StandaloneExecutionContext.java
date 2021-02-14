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
package org.smooks.container.standalone;

import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.ParameterAccessor;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.container.MementoCaretaker;
import org.smooks.container.TypedKey;
import org.smooks.delivery.ContentDeliveryRuntime;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Visitor;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.context.StandaloneBeanContextFactory;
import org.smooks.profile.ProfileSet;
import org.smooks.profile.UnknownProfileMemberException;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Standalone Container Request implementation.
 * @author tfennelly
 */
public class StandaloneExecutionContext implements ExecutionContext {

    private final ProfileSet targetProfileSet;
	private final Map<TypedKey<Object>, Object> attributes = new Hashtable<>();
    private final ContentDeliveryRuntime contentDeliveryRuntime;
	private final MementoCaretaker mementoCaretaker;
	private final ApplicationContext applicationContext;

	private URI docSource;
	private String contentEncoding;
    private Throwable terminationError;
    private BeanContext beanContext;

	/**
	 * Public Constructor.
	 * <p/>
     * The execution context is constructed within the context of a target profile and
     * application context.
	 * @param targetProfile The target base profile for the execution context.
	 * These parameters are not appended to the supplied requestURI.  This arg must be supplied, even if it's empty.
     * @param applicationContext The application context.
     * @param extendedContentHandlerBindings Preconfigured/extended Visitor Configuration Map.
     * @throws UnknownProfileMemberException Unknown target profile.
	 */
	public StandaloneExecutionContext(String targetProfile, ApplicationContext applicationContext, List<ContentHandlerBinding<Visitor>> extendedContentHandlerBindings) throws UnknownProfileMemberException {
		this(targetProfile, applicationContext, "UTF-8", extendedContentHandlerBindings);
	}

	/**
	 * Public Constructor.
	 * <p/>
     * The execution context is constructed within the context of a target profile and
     * application context.
	 * @param targetProfile The target profile (base profile) for this context.
	 * These parameters are not appended to the supplied requestURI.  This arg must be supplied, even if it's empty.
     * @param applicationContext The application context.
	 * @param contentEncoding Character encoding to be used when parsing content.  Null
	 * defaults to "UTF-8".
     * @param extendedContentHandlerBindings Preconfigured/extended Visitor Configuration Map.
     * @throws UnknownProfileMemberException Unknown target profile.
	 */
	public StandaloneExecutionContext(String targetProfile, ApplicationContext applicationContext, String contentEncoding, List<ContentHandlerBinding<Visitor>> extendedContentHandlerBindings) throws UnknownProfileMemberException {
		AssertArgument.isNotNull(targetProfile, "targetProfile");
		AssertArgument.isNotNull(applicationContext, "applicationContext");

		this.applicationContext = applicationContext;
		setContentEncoding(contentEncoding);
        targetProfileSet = applicationContext.getProfileStore().getProfileSet(targetProfile);
		contentDeliveryRuntime = applicationContext.getContentDeliveryConfigBuilderFactory().create(targetProfileSet, extendedContentHandlerBindings);
		
		mementoCaretaker = new DefaultMementoCaretaker(this);
    }

	@Override
	public void setDocumentSource(URI docSource) {
        this.docSource = docSource;
    }

	@Override
	public URI getDocumentSource() {
		if(docSource == null) {
			return ExecutionContext.DOCUMENT_URI;
		}
		return docSource;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public ProfileSet getTargetProfiles() {
		return targetProfileSet;
	}

	@Override
	public ContentDeliveryRuntime getContentDeliveryRuntime() {
		return contentDeliveryRuntime;
	}

	/**
	 * Set the content encoding to be used when parsing content on this standalone request instance.
	 * @param contentEncoding Character encoding to be used when parsing content.  Null
	 * defaults to "UTF-8".
	 * @throws IllegalArgumentException Invalid encoding.
	 */
	@Override
	public void setContentEncoding(String contentEncoding) throws IllegalArgumentException {
		contentEncoding = (contentEncoding == null) ? "UTF-8" : contentEncoding;
		try {
			// Make sure the encoding is supported....
			Charset.forName(contentEncoding);
		} catch (UnsupportedCharsetException e) {
			throw new IllegalArgumentException("Invalid 'contentEncoding' arg [" + contentEncoding + "].  This encoding is not supported.", e);
		}
		this.contentEncoding = contentEncoding;
	}

	/**
	 * Get the content encoding to be used when parsing content on this standalone request instance.
	 * @return Character encoding to be used when parsing content.  Defaults to "UTF-8".
	 */
	@Override
	public String getContentEncoding() {
		return (contentEncoding == null) ? "UTF-8" : contentEncoding;
	}

	@Override
	public void setTerminationError(Throwable terminationError) {
        this.terminationError = terminationError;
    }

	@Override
	public Throwable getTerminationError() {
        return terminationError;
    }

	@Override
	public String getConfigParameter(String name) {
        return getConfigParameter(name, null);
    }

	@Override
	public String getConfigParameter(String name, String defaultVal) {
        return ParameterAccessor.getParameterValue(name, String.class, defaultVal, contentDeliveryRuntime.getContentDeliveryConfig());
    }
    
	@Override
	public <T> void put(TypedKey<T> key, T value) {
		attributes.put((TypedKey<Object>) key, value);
	}
	
	@Override
	public <T> T get(TypedKey<T> key) {
		return (T) attributes.get(key);
	}

	@Override
	public <T> T getOrDefault(TypedKey<T> key, T value) {
		return (T) attributes.getOrDefault(key, value);
	}
	
	@Override
	public <T> void remove(TypedKey<T> key) {
		attributes.remove(key);
	}

	@Override
	public String toString() {
        return attributes.toString();
    }

	@Override
	public Map<TypedKey<Object>, Object> getAll() {
    	return Collections.unmodifiableMap(attributes);
    }

	@Override
	public BeanContext getBeanContext() {
		if (beanContext == null) {
			beanContext = StandaloneBeanContextFactory.create(this);
		}
		return beanContext;
	}

	@Override
	public void setBeanContext(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
    
	@Override
	public MementoCaretaker getMementoCaretaker() {
		return mementoCaretaker;
	}
}
