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

import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.event.ExecutionEventListener;
import org.milyn.javabean.context.BeanContext;
import org.milyn.profile.ProfileSet;

import java.net.URI;

/**
 * Smooks execution context interface definition.
 *
 * @author tfennelly
 */
public interface ExecutionContext extends BoundAttributeStore {

    /**
     * Sometimes the document being transformed/analysed has a URI associated with it.
     * This can be bound to the execution context under this key.
     */
    public static final URI DOCUMENT_URI = URI.create("org:milyn:smooks:unknowndoc");

    /**
     * Set the document source URI.
     *
     * @param docSource The document URI.
     */
    public void setDocumentSource(URI docSource);

    /**
     * Get the document source URI.
     * <p/>
     * If the document source URI is not set for the context, implementations should
     * return the {@link #DOCUMENT_URI} constant.
     *
     * @return The document URI.
     */
    public URI getDocumentSource();

    /**
     * Get the application context within which this execution context "lives".
     *
     * @return The ApplicationContext instance.
     */
    public abstract ApplicationContext getContext();

    /**
     * Get the set of profiles at which this execution context is targeted.
     * <p/>
     * Basically, the set of profiles for which this execution context is to perform
     * transformation/analysis.
     *
     * @return The target {@link org.milyn.profile.ProfileSet}.
     */
    public abstract ProfileSet getTargetProfiles();

    /**
     * Get the content delivery configuration for the profile set at which this
     * context is targeted.
     *
     * @return ContentDeliveryConfig instance.
     */
	public abstract ContentDeliveryConfig getDeliveryConfig();


    /**
     * Set the content encoding to be used when parsing content on this context.
     * @param contentEncoding Character encoding to be used when parsing content.  Null
     * defaults to "UTF-8".
     * @throws IllegalArgumentException Invalid encoding.
     */
    public abstract void setContentEncoding(String contentEncoding) throws IllegalArgumentException;

    /**
     * Get the content encoding to be used when parsing content on this context.
     * @return Character encoding to be used when parsing content.  Defaults to "UTF-8".
     */
    public abstract String getContentEncoding();

    /**
     * Set the ExecutionEventListener for the {@link ExecutionContext}.
     * <p/>
     * Allows calling code to listen to (and capture data on) specific
     * context execution events e.g. the targeting of resources.
     * <p/>
     * Note, this is not a logging facility and should be used with care.
     * It's overuse should be avoided as it can have a serious negative effect
     * on performance.  By default, no listenrs are applied and so no overhead
     * is incured.
     *
     * @param listener The listener instance.
     * @see org.milyn.event.BasicExecutionEventListener
     */
    public abstract void setEventListener(ExecutionEventListener listener);

    /**
     * Get the ExecutionEventListener for the {@link ExecutionContext}.
     * @return The listener instance.
     * @see #setEventListener(ExecutionEventListener)
     */
    public abstract ExecutionEventListener getEventListener();

    /**
     * Set the error/exception that caused the filter operation associated with
     * this ExecutionContext to terminate.
     *
     * @param terminationError The termination Error/Exception.
     */
    public abstract void  setTerminationError(Throwable terminationError);


    /**
     * Set the error/exception that caused the filter operation associated with
     * this ExecutionContext to terminate.
     *
     * @return The Error/Exception that caused the associated filter operation to
     * terminate (if it did terminate), otherwise null.
     */
    public abstract Throwable getTerminationError();

    /**
     * Get a global configuration parameter associated with this execution context.
     * <p/>
     * For more fine grained control, see the {@link org.milyn.cdr.ParameterAccessor} class.
     * @param name The name of the parameter.
     * @return The parameter value, or null if the parameter is not configured.
     */
    public abstract String getConfigParameter(String name);

    /**
     * Get a global configuration parameter associated with this execution context.
     * <p/>
     * For more fine grained control, see the {@link org.milyn.cdr.ParameterAccessor} class.
     * @param name The name of the parameter.
     * @param defaultVal The default value to be returned if the configuration parameter is not set.
     * @return The parameter value, or "defaultVal" if the parameter is not configured.
     */
    public abstract String getConfigParameter(String name, String defaultVal);

    /**
     * Is default serialization on for this execution context.
     * <p/>
     * This is controlled by the {@link org.milyn.delivery.Filter#DEFAULT_SERIALIZATION_ON}
     * global param.  Default Serialization is on by default.
     * <p/>
     * <b>Example Configuration:</b>
     * <pre>
     * &lt;params&gt;
     *     &lt;param name="default.serialization.on"&gt;false&lt;/param&gt;
     * &lt;/params&gt;
     * </pre>
     *
     * @return True if default serialization is on, otherwise false.
     */
    public boolean isDefaultSerializationOn();

    /**
     * Get the BeanContext in use on this context instance.
     * @return The BeanContext.
     */
    public BeanContext getBeanContext();

    /**
     * Set the BeanContext to be use on this context instance.
     * @param beanContext The BeanContext.
     */
    public void setBeanContext(BeanContext beanContext);
}
