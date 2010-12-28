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

package org.milyn.container;

import org.milyn.cdr.ParameterAccessor;
import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.delivery.dom.MockContentDeliveryConfig;
import org.milyn.event.ExecutionEventListener;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.context.StandaloneBeanContextFactory;
import org.milyn.profile.DefaultProfileSet;
import org.milyn.profile.Profile;
import org.milyn.profile.ProfileSet;
import org.milyn.util.IteratorEnumeration;

import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author tfennelly
 */
public class MockExecutionContext implements ExecutionContext {

	public String contextPath;
	public URI docSource;
	public ProfileSet profileSet = new DefaultProfileSet(Profile.DEFAULT_PROFILE);
	public ContentDeliveryConfig deliveryConfig = new MockContentDeliveryConfig();
	public MockApplicationContext context = new MockApplicationContext();
	private Hashtable attributes = new Hashtable();
	public LinkedHashMap parameters = new LinkedHashMap();
	public Hashtable headers = new Hashtable();
	public Hashtable elementListTable = new Hashtable();
    private String contentEncoding;
    private ExecutionEventListener executionListener;
    private Throwable terminationError;
    private BeanContext beanContext;

    public void setDocumentSource(URI docSource) {
        this.docSource = docSource;
    }

    public URI getDocumentSource() {
		return docSource;
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return (new IteratorEnumeration(parameters.keySet().iterator()));
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		return (String[])parameters.get(name);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getApplicationContext()
	 */
	public ApplicationContext getContext() {
		if(context == null) {
			throw new IllegalStateException("Call to getApplicationContext before context member has been initialised.  Set the 'context' member variable.");
		}
		return context;
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getTargetProfiles()
	 */
	public ProfileSet getTargetProfiles() {
		if(profileSet == null) {
			throw new IllegalStateException("Call to getTargetProfiles before profileSet member has been initialised.  Set the 'profileSet' member variable.");
		}
		return profileSet;
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getDeliveryConfig()
	 */
	public ContentDeliveryConfig getDeliveryConfig() {
		if(deliveryConfig == null) {
			throw new IllegalStateException("Call to getDeliveryConfig before deliveryConfig member has been initialised.  Set the 'deliveryConfig' member variable.");
		}
		return deliveryConfig;
	}

    public void setContentEncoding(String contentEncoding) throws IllegalArgumentException {
        this.contentEncoding = contentEncoding;
    }

    public String getContentEncoding() {
        return contentEncoding;
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
        return ParameterAccessor.getStringParameter(name, defaultVal, deliveryConfig);
    }

    public boolean isDefaultSerializationOn() {
        return true;
    }

    /* (non-Javadoc)
      * @see org.milyn.container.BoundAttributeStore#setAttribute(java.lang.String, java.lang.Object)
      */
	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#getAttribute(java.lang.String)
	 */
	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(Object key) {
		attributes.remove(key);
	}

    public MockContentDeliveryConfig getMockDeliveryConfig() {
        return (MockContentDeliveryConfig) this.deliveryConfig;
    }

    public Map getAttributes()
    {
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
