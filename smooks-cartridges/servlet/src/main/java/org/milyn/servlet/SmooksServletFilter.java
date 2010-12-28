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

package org.milyn.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.net.URISyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.useragent.UnknownUseragentException;
import org.milyn.resource.ContainerResourceLocator;
import org.milyn.servlet.container.HttpServletExecutionContext;
import org.milyn.servlet.container.ServletApplicationContext;
import org.milyn.servlet.delivery.ServletResponseWrapper;
import org.milyn.servlet.delivery.ServletResponseWrapperFactory;
import org.milyn.servlet.delivery.XMLServletResponseWrapper;
import org.xml.sax.SAXException;

 /**
 * Smooks Servlet Filter.
 * <p/>
 * This Servlet Filter plugs Smooks into a Servlet Container via
 * the {@link org.milyn.servlet.delivery.XMLServletResponseWrapper}.
 * <p/>
 * This Filter can also be configured to filter other response types in a useragent optimizable
 * fashion e.g. filter using a differnt image filter depending on the requesting browser.
 * See {@link org.milyn.servlet.delivery.ServletResponseWrapperFactory}.
 * <p/>
 * So, this class simply pipes the Servlet response into a {@link org.milyn.servlet.delivery.ServletResponseWrapper}
 * implementation.  The default response wrapper is the {@link org.milyn.servlet.delivery.XMLServletResponseWrapper}. 
 * See {@link org.milyn.servlet.delivery.PassThruServletResponseWrapper}.
 * 
 * <h3>Requirements</h3>
 * <ul>
 * 	<li>JDK 1.5</li>
 * 	<li>Servlet Specification 2.3+ compliant container</li>
 * </ul>
 * 
 * <h3 id="deployment">Deployment</h3>
 * To deploy Smooks:
 * <ol>
 * 	<li>Download the Smooks Core distribution (and its dependencies) from 
 * 		<a href="http://milyn.codehaus.org/downloads">Milyn Downloads</a>.</li>
 * 	<li>Install all jars in your webapps WEB-INF/lib folder</li>
 * </ol>
 * To enable this Filter in your Servlet container simply
 * add the following to the application web.xml file.
 * <pre>
 * &lt;filter&gt;
 *	&lt;filter-name&gt;SmooksFilter&lt;/filter-name&gt;
 *	&lt;filter-class&gt;org.milyn.servlet.SmooksServletFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *	&lt;filter-name&gt;SmooksFilter&lt;/filter-name&gt;
 *	&lt;url-pattern&gt;*.jsp&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;</pre>
 * 
 * @author tfennelly
 */
public class SmooksServletFilter implements Filter {

    /**
     * Smooks config application property name.
     */
	private static final String SMOOKS_CONFIG_PARAM = "SmooksConfig";
    /**
     * Default smooks cdrar list config file.
     */
    private static final String DEFAULT_CONFIG = "/smooks-config.xml";
	/**
	 * Smooks view on the servlet context.
	 */
	private ServletApplicationContext smooksContainerContext;
	/**
	 * FilterConfig adapter.
	 */
	private FilterToServletConfigAdapter servletConfig;
	/**
	 * Logger.
	 */
	private static Log logger = LogFactory.getLog(SmooksServletFilter.class);
	
	/* (non-Javadoc)
	 * @see javax.servlet.Phase#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		try {
			servletConfig = new FilterToServletConfigAdapter(config);
			smooksContainerContext = new ServletApplicationContext(config.getServletContext(), servletConfig);
			loadConfigStore();
            DeviceProfiler.setProfileStore(smooksContainerContext.getProfileStore(), servletConfig.getServletContext());
            logger.info("Smooks Servlet Filter initalised.");
		} catch(Exception e) {
			throw new ServletException("Smooks configuration load failure.", e);
		}		
	}

	/**
	 * Load the CDRStore for this filter instance.
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private void loadConfigStore() throws IllegalArgumentException, IOException, SAXException, URISyntaxException {
		ContainerResourceLocator containerResLocator;
		InputStream smooksConfigStream;
		
		containerResLocator = smooksContainerContext.getResourceLocator();
		smooksConfigStream = containerResLocator.getResource(SMOOKS_CONFIG_PARAM, DEFAULT_CONFIG);
        smooksContainerContext.getStore().registerResources("smooks-config", smooksConfigStream);
		logger.info("Smooks Config Store load complete.");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Phase#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		ServletResponseWrapper responseWrapper = null;

		try {
			long startTime = 0L;
			ExecutionContext executionContext = new HttpServletExecutionContext((HttpServletRequest)request, servletConfig, smooksContainerContext);

			if(logger.isDebugEnabled()) {
				startTime = System.currentTimeMillis();
			}

			// Check for a response wrapper configuration on the request.
			responseWrapper = getResponseWrapper(request.getParameter("smooksrw"), response, executionContext);
			if(responseWrapper == null) {
				// Check for a response wrapper configuration for HTML.  This allows
				// overridding of the default (below).
				responseWrapper = getResponseWrapper("html-smooksrw", response, executionContext);
			}
			if(responseWrapper == null) {
				// Default to the XMLServletResponseWrapper.
				responseWrapper = new XMLServletResponseWrapper(executionContext, (HttpServletResponse)response);
			}

			if(logger.isDebugEnabled()) {
				logger.debug("Applying response wrapper ["+ responseWrapper.getClass() + "] to request [" + ((HttpServletRequest)request).getRequestURI() + "].");
			}
			filterChain.doFilter(request, responseWrapper);
			responseWrapper.deliverResponse();
			if(logger.isDebugEnabled()) {
				logger.debug("[doFilter] " + (System.currentTimeMillis() - startTime) + "ms");
			}
		} catch (UnknownUseragentException e) {
			logger.error("Unknown Device.  Smooks not being used to deliver content.", e);
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(responseWrapper != null) {
				responseWrapper.close();
			}
		}
	}

	/**
	 * Get the response wrapper for the requesting device based on the specified by
	 * response wrapper.
	 * @param selector The smooks-resource selector id for the required ServletResponseWrapper
	 * configuration.
	 * @param response The original servlet response (to be wrapped).
	 * @param executionContext The Smooks ExecutionContext instance.
	 * @return The ServletResponseWrapper instance, or null if no such response wrapper is
	 * configured for the requesting device.
	 */
	private ServletResponseWrapper getResponseWrapper(String selector, ServletResponse response, ExecutionContext executionContext) {
		ServletResponseWrapper responseWrapper = null; 
		
		if(selector != null) {
			List<SmooksResourceConfiguration> resourceConfigList = executionContext.getDeliveryConfig().getSmooksResourceConfigurations(selector);
			if(resourceConfigList != null && !resourceConfigList.isEmpty()) {
				responseWrapper = ServletResponseWrapperFactory.createServletResponseWrapper(resourceConfigList.get(0), executionContext, (HttpServletResponse)response);
			}
		}
		
		return responseWrapper;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Phase#destroy()
	 */
	public void destroy() {
        smooksContainerContext.getStore().close();
        servletConfig.getServletContext().removeAttribute(DeviceProfiler.PROFILE_STORE_CTX_KEY);
    }

	/**
	 * Adaptorfor Phase to Servlet config.
	 * @author tfennelly
	 */
	private class FilterToServletConfigAdapter implements ServletConfig {		
		/**
		 * Phase configuration.
		 */
		private FilterConfig config;
		/**
		 * Constructor.
		 * @param config FilterConfig instance.
		 */
		private FilterToServletConfigAdapter(FilterConfig config) {
			this.config = config;
		}
		/* (non-Javadoc)
		 * @see javax.servlet.ServletConfig#getServletName()
		 */
		public String getServletName() {
			return config.getFilterName();
		}
		/* (non-Javadoc)
		 * @see javax.servlet.ServletConfig#getServletContext()
		 */
		public ServletContext getServletContext() {
			return config.getServletContext();
		}
		/* (non-Javadoc)
		 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
		 */
		public String getInitParameter(String paramName) {
			return config.getInitParameter(paramName);
		}
		/* (non-Javadoc)
		 * @see javax.servlet.ServletConfig#getInitParameterNames()
		 */
		public Enumeration getInitParameterNames() {
			return config.getInitParameterNames();
		}		
	}
}
