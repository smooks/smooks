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

package org.milyn.servlet.container;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.event.ExecutionEventListener;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.context.StandaloneBeanContext;
import org.milyn.javabean.context.StandaloneBeanContextFactory;
import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.delivery.ContentDeliveryConfigBuilder;
import org.milyn.profile.ProfileSet;
import org.milyn.servlet.ServletUAContext;
import org.milyn.useragent.UAContext;
import org.milyn.useragent.UnknownUseragentException;
import org.milyn.useragent.request.HttpRequest;
import org.milyn.cdr.ParameterAccessor;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;

/**
 * Smooks ExecutionContext implementation for the HttpServlet container.
 * @author tfennelly
 */
public class HttpServletExecutionContext implements ExecutionContext, HttpRequest {

    private static Log logger = LogFactory.getLog(HttpServletExecutionContext.class);

    /**
	 * HttpServletRequest instance.
	 */
	private HttpServletRequest servletRequest;
	/**
	 * Requesting device UAContext.
	 */
	private UAContext uaContext;
	/**
	 * Requesting device ContentDeliveryConfigBuilder.
	 */
	private ContentDeliveryConfig deliveryConfig;
	/**
	 * Associated ApplicationContext for the servlet environment.
	 */
	private ApplicationContext applicationContext;
	/**
	 * Request URI.
	 */
	private URI requestURI;
    /**
     * Execution Listener.
     */
    private ExecutionEventListener executionListener;
    private Throwable terminationError;

    private BeanContext beanContext;

    /**
	 * Public Constructor.
	 * @param servletRequest HttpServletRequest instance.
	 * @param servletConfig ServletConfig instance.
	 * @throws UnknownUseragentException Unable to match device.
	 */
	public HttpServletExecutionContext(HttpServletRequest servletRequest, ServletConfig servletConfig, ServletApplicationContext containerContext) throws UnknownUseragentException {
		if(servletRequest == null) {
			throw new IllegalArgumentException("null 'servletRequest' arg in constructor call.");
		}
		if(containerContext == null) {
			throw new IllegalArgumentException("null 'applicationContext' arg in constructor call.");
		}
		this.servletRequest = servletRequest;
		this.applicationContext = containerContext;
		uaContext = ServletUAContext.getInstance(servletRequest, servletConfig);
		deliveryConfig = ContentDeliveryConfigBuilder.getConfig(uaContext.getProfileSet(), containerContext, null);
	}

    public void setDocumentSource(URI docSource) {
        logger.error("Cannot set the document source on this context implementation.");
    }

    /* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getDocumentSource()
	 */
	public URI getDocumentSource() {
		if(requestURI == null) {
			String queryString = servletRequest.getQueryString();
			if(queryString != null) {
				requestURI = URI.create(servletRequest.getRequestURL().toString() + "?" + queryString);
			} else {
				requestURI = URI.create(servletRequest.getRequestURL().toString());
			}
		}
		return requestURI;
	}

	/* (non-Javadoc)
	 * @see org.milyn.useragent.request.HttpRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {
		return servletRequest.getHeader(name);
	}

	/* (non-Javadoc)
	 * @see org.milyn.useragent.request.HttpRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		return servletRequest.getParameter(name);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return servletRequest.getParameterNames();
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		return servletRequest.getParameterValues(name);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getApplicationContext()
	 */
	public ApplicationContext getContext() {
		return applicationContext;
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getTargetProfiles()
	 */
	public ProfileSet getTargetProfiles() {
		return uaContext.getProfileSet();
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ExecutionContext#getDeliveryConfig()
	 */
	public ContentDeliveryConfig getDeliveryConfig() {
		return deliveryConfig;
	}

    public void setContentEncoding(String contentEncoding) throws IllegalArgumentException {
        logger.error("Cannot set the contentEncoding on this context implementation.");
    }

    public String getContentEncoding() {
        return servletRequest.getCharacterEncoding();
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
      * @see org.milyn.container.BoundAttributeStore#setAttribute(java.lang.Object, java.lang.Object)
      */
	public void setAttribute(Object key, Object value) {
		servletRequest.setAttribute(key.toString(), value);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#getAttribute(java.lang.Object)
	 */
	public Object getAttribute(Object key) {
		return servletRequest.getAttribute(key.toString());
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#removeAttribute(java.lang.Object)
	 */
	public void removeAttribute(Object key) {
		servletRequest.removeAttribute(key.toString());
	}

	/**
	 * Get the HttpServletRequest instance associated with this ExecutionContext
	 * implementation.
	 * @return HttpServletRequest instance.
	 */
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public Map getAttributes()
	{
		throw new UnsupportedOperationException( "Method getAttributes is not implemented" );
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
