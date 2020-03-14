/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.rules;

import org.smooks.SmooksException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.cdr.annotation.ConfigParam.Use;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.annotation.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RulesProviderFactory is responsible for creating {@link RuleProvider}s
 * and installing those providers in the Smooks {@link ApplicationContext}.
 * <p/>
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public final class RulesProviderFactory implements ContentHandler<RuleProvider>
{
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesProviderFactory.class);

    /**
     * The Smooks {@link ApplicationContext}.
     */
    @AppContext
    private ApplicationContext applicationContext;

    /**
     * The rule name to be used.
     */
    @ConfigParam(use = Use.REQUIRED )
    private String name;

    /**
     * The {@link RuleProvider} implementation to be used.
     */
    @ConfigParam(name = "provider", use = Use.REQUIRED)
    private Class<RuleProvider> provider;

    /**
     * The source of the rule. Is implementation dependent, may be a file for example.
     */
    @ConfigParam(use = Use.OPTIONAL)
    private String src;

    /**
     * Creates and installs the configured rule provider.
     *
     * @throws SmooksConfigurationException
     */
    @Initialize
    public void installRuleProvider() throws SmooksConfigurationException
    {
        LOGGER.debug(this.toString());
        if(RuleProvider.class.isAssignableFrom(provider))
        {
            final RuleProvider providerImpl = createProvider(provider);
            providerImpl.setName(name);
            providerImpl.setSrc(src);

            RuleProviderAccessor.add(applicationContext, providerImpl);
        }
        else
        {
            throw new SmooksConfigurationException("Invalid rule provider configuration :'" + this + "'");
        }
    }

    RuleProvider createProvider(final Class<? extends RuleProvider> providerClass) throws SmooksException
    {
        try
        {
            return providerClass.newInstance();
        }
        catch (final InstantiationException e)
        {
            throw new SmooksException(e.getMessage(), e);
        }
        catch (final IllegalAccessException e)
        {
            throw new SmooksException(e.getMessage(), e);
        }
    }

    @Override
    public String toString()
    {
        return String.format("%s [name=%s, src=%s, provider=%s]", getClass().getSimpleName(), name, src, provider);
    }

}
