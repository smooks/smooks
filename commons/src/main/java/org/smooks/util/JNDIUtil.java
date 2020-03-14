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
package org.smooks.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * JNDI utilities.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JNDIUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JNDIUtil.class);

    /**
     * Private default constructor.
     */
    private JNDIUtil() {
    }

    /**
     * Get the JNDI Context.
     * <p/>
     * Don't forget to close it when done!
     *
     * @param jndiProperties JNDI properties.
     * @return The context.
     * @throws javax.naming.NamingException Error getting context.
     */
    public static Context getNamingContext(final Properties jndiProperties) throws NamingException {
        Context context;
        try {
            context = jndiProperties.isEmpty() ? new InitialContext() : new InitialContext(jndiProperties);
        }
        catch (NamingException e) {
            LOGGER.error("NamingException while try to create initialContext. jndiProperties are " + jndiProperties, e);
            throw ((NamingException) new NamingException("Failed to load InitialContext: " + jndiProperties).initCause(e));
        }
        if (context == null) {
            throw new NamingException("Failed to create JNDI context.  Check that '" + Context.PROVIDER_URL + "', '" + Context.INITIAL_CONTEXT_FACTORY + "', '" + Context.URL_PKG_PREFIXES + "' are correctly configured in the supplied JNDI properties.");
        }

        return context;
    }

    /**
     * Lookup an object through the JNDI context.
     *
     * @param objectName     The name of the object to be looked up.
     * @param jndiProperties JNDI properties.
     * @return The object.
     * @throws NamingException Error getting object.
     */
    public static Object lookup(final String objectName, final Properties jndiProperties) throws NamingException {
        Object object = null;
        Context context;

        context = JNDIUtil.getNamingContext(jndiProperties);
        try {
            object = context.lookup(objectName);
        }
        finally {
            try {
                context.close();
            }
            catch (NamingException ne) {
                LOGGER.debug("Failed to close Naming Context.", ne);
            }
        }

        return object;
    }

    /**
     * Lookup an object through the JNDI context.
     *
     * @param objectName     The name of the object to be looked up.
     * @param jndiProperties JNDI properties.
     * @param classLoaders   The {@link ClassLoader ClassLoaders) to be used during the lookup.
     * @return The object.
     * @throws NamingException Error getting object.
     */
    public static Object lookup(final String objectName, final Properties jndiProperties, final ClassLoader[] classLoaders) throws NamingException {
        ClassLoader tcClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            for (ClassLoader classLoader : classLoaders) {
                Thread.currentThread().setContextClassLoader(classLoader);
                try {
                    return JNDIUtil.lookup(objectName, jndiProperties);
                }
                catch (NamingException e) {
                    LOGGER.debug("NamingException while trying to lookup '" + objectName + "' using JNDI Properties '" + jndiProperties + "', classloader used '" + classLoader + "'", e);
                    // Try the other ClassLoaders...
                }
            }
        }
        finally {
            Thread.currentThread().setContextClassLoader(tcClassLoader);
        }

        throw new NamingException("JNDI lookup of Object [" + objectName + "] failed.");
    }

    public static Properties getDefaultProperties() {
        Properties defaultProperties = new Properties();
        try {
            InitialContext context = new InitialContext();
            defaultProperties.putAll(context.getEnvironment());
        } catch (Exception e) {
            LOGGER.debug("Unexpected exception when trying to retrieve default naming context.", e);
        }
        return defaultProperties;
    }
}
