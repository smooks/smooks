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

import org.smooks.cdr.ParameterAccessor;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.delivery.Filter;
import org.smooks.delivery.VisitorConfigMap;
import org.smooks.event.ExecutionEventListener;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.context.StandaloneBeanContextFactory;
import org.smooks.profile.ProfileSet;
import org.smooks.profile.UnknownProfileMemberException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Hashtable;

/**
 * Standalone Container Request implementation.
 * @author tfennelly
 */
public class StandaloneExecutionContext implements ExecutionContext {

    private final ProfileSet targetProfileSet;
    private final Hashtable<Object, Object> attributes = new Hashtable<Object, Object>();
    private final ContentDeliveryConfig deliveryConfig;
    private URI docSource;
	private String contentEncoding;
    private final ApplicationContext context;
    private ExecutionEventListener executionListener;
    private Throwable terminationError;
    private final boolean isDefaultSerializationOn;
    private BeanContext beanContext;

    /**
	 * Public Constructor.
	 * <p/>
     * The execution context is constructed within the context of a target profile and
     * application context.
	 * @param targetProfile The target base profile for the execution context.
	 * These parameters are not appended to the supplied requestURI.  This arg must be supplied, even if it's empty.
     * @param context The application context.
     * @param extendedVisitorConfigMap Preconfigured/extended Visitor Configuration Map.
     * @throws UnknownProfileMemberException Unknown target profile.
	 */
	public StandaloneExecutionContext(String targetProfile, ApplicationContext context, VisitorConfigMap extendedVisitorConfigMap) throws UnknownProfileMemberException {
		this(targetProfile, context, "UTF-8", extendedVisitorConfigMap);
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
     * @param extendedVisitorConfigMap Preconfigured/extended Visitor Configuration Map.
     * @throws UnknownProfileMemberException Unknown target profile.
	 */
	public StandaloneExecutionContext(String targetProfile, ApplicationContext applicationContext, String contentEncoding, VisitorConfigMap extendedVisitorConfigMap) throws UnknownProfileMemberException {
        if(targetProfile == null) {
            throw new IllegalArgumentException("null 'targetProfile' arg in constructor call.");
        }
        if(applicationContext == null) {
            throw new IllegalArgumentException("null 'context' arg in constructor call.");
        }
		this.context = applicationContext;
		setContentEncoding(contentEncoding);
        targetProfileSet = applicationContext.getProfileStore().getProfileSet(targetProfile);
		deliveryConfig = applicationContext.getContentDeliveryConfigBuilderFactory().create(targetProfileSet).build(extendedVisitorConfigMap);
        isDefaultSerializationOn = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.DEFAULT_SERIALIZATION_ON, String.class, "true", deliveryConfig));
    }

    public void setDocumentSource(URI docSource) {
        this.docSource = docSource;
    }

    public URI getDocumentSource() {
		if(docSource == null) {
			return ExecutionContext.DOCUMENT_URI;
		}
		return docSource;
	}

	public ApplicationContext getApplicationContext() {
		return context;
	}

	public ProfileSet getTargetProfiles() {
		return targetProfileSet;
	}

	public ContentDeliveryConfig getDeliveryConfig() {
		return deliveryConfig;
	}

	/**
	 * Set the content encoding to be used when parsing content on this standalone request instance.
	 * @param contentEncoding Character encoding to be used when parsing content.  Null
	 * defaults to "UTF-8".
	 * @throws IllegalArgumentException Invalid encoding.
	 */
	public void setContentEncoding(String contentEncoding) throws IllegalArgumentException {
		contentEncoding = (contentEncoding == null)?"UTF-8":contentEncoding;
		try {
			// Make sure the encoding is supported....
			"".getBytes(contentEncoding);
		} catch (UnsupportedEncodingException e) {
			IllegalArgumentException argE = new IllegalArgumentException("Invalid 'contentEncoding' arg [" + contentEncoding + "].  This encoding is not supported.", e);
            throw argE;
		}
		this.contentEncoding = contentEncoding;
	}

	/**
	 * Get the content encoding to be used when parsing content on this standalone request instance.
	 * @return Character encoding to be used when parsing content.  Defaults to "UTF-8".
	 */
	public String getContentEncoding() {
		return (contentEncoding == null)?"UTF-8":contentEncoding;
	}

    public void setEventListener(ExecutionEventListener listener) {
        this.executionListener = listener;
    }

    public ExecutionEventListener getEventListener() {
        return executionListener;
    }

    public void setTerminationError(Throwable terminationError) {
        this.terminationError = terminationError;
    }

    public Throwable getTerminationError() {
        return terminationError;
    }

    public String getConfigParameter(String name) {
        return getConfigParameter(name, null);
    }

    public String getConfigParameter(String name, String defaultVal) {
        return ParameterAccessor.getParameterValue(name, String.class, defaultVal, deliveryConfig);
    }

    public boolean isDefaultSerializationOn() {
        return isDefaultSerializationOn;
    }

    /* (non-Javadoc)
      * @see org.smooks.container.BoundAttributeStore#setAttribute(java.lang.Object, java.lang.Object)
      */
	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.smooks.container.BoundAttributeStore#getAttribute(java.lang.Object)
	 */
	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	/* (non-Javadoc)
	 * @see org.smooks.container.BoundAttributeStore#removeAttribute(java.lang.Object)
	 */
	public void removeAttribute(Object key) {
		attributes.remove(key);
	}

    public String toString() {
        return attributes.toString();
    }

    public Hashtable<Object, Object> getAttributes() {
    	return attributes;
    }

    public BeanContext getBeanContext() {
		if(beanContext == null) {
			beanContext = StandaloneBeanContextFactory.create(this);
		}
		return beanContext;
	}

    public void setBeanContext(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
}
