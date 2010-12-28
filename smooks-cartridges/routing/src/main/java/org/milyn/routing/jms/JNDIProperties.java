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
package org.milyn.routing.jms;

import org.milyn.util.JNDIUtil;
import org.milyn.resource.URIResourceLocator;
import org.milyn.cdr.SmooksConfigurationException;

import javax.naming.Context;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;


public class JNDIProperties
{
    private String contextFactory;

    private String providerUrl;

    private String namingFactoryUrlPkgs;

    private Properties defaultProperties = JNDIUtil.getDefaultProperties();
    
    private String propertiesFile;

    private Properties properties;

    public String getContextFactory()
	{
		return contextFactory;
	}

	public void setContextFactory( String contextFactory )
	{
		this.contextFactory = contextFactory;
	}

	public String getProviderUrl()
	{
		return providerUrl;
	}

	public void setProviderUrl( String providerUrl )
	{
		this.providerUrl = providerUrl;
	}

	public String getNamingFactoryUrlPkgs()
	{
		return namingFactoryUrlPkgs;
	}

	public void setNamingFactoryUrlPkgs( String namingFactoryUrl )
	{
		this.namingFactoryUrlPkgs = namingFactoryUrl;
	}

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public Properties toProperties() throws SmooksConfigurationException {
        if(properties == null) {
            properties = new Properties();
        }

        if(propertiesFile != null) {
            try {
                URIResourceLocator locator = new URIResourceLocator();
                properties.load(locator.getResource(propertiesFile));
            } catch (IOException e) {
                throw new SmooksConfigurationException("Failed to read JMS JNDI properties file '" + propertiesFile + "'.", e);
            }
        }

        if(contextFactory != null) {
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        }
        if(providerUrl != null) {
            properties.setProperty(Context.PROVIDER_URL, providerUrl);
        }
        if(namingFactoryUrlPkgs != null) {
            properties.setProperty(Context.URL_PKG_PREFIXES, namingFactoryUrlPkgs);
        }

        // We only use the default properties if none of the JNDI properties have been
        // configured.  Intentionally not merging configured properties with
        // default properties!!!
        if(!properties.isEmpty()) {
            return properties;
        } else {
            return defaultProperties;
        }
    }
}
