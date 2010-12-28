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

package org.milyn.rules;

import java.util.HashMap;
import java.util.Map;

import org.milyn.SmooksException;
import org.milyn.assertion.AssertArgument;
import org.milyn.container.ApplicationContext;

/**
 * RuleProviderAccessor provides convenience methods for adding and getting
 * rule providers in a Smooks Application context.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public final class RuleProviderAccessor
{
    /**
     * Sole private constructor.
     */
    private RuleProviderAccessor() { }

    /**
     * Adds the passed-in provider to the Smooks {@link ApplicationContext}.
     *
     * @param context The Smooks {@link ApplicationContext}.
     * @param provider The {@link RuleProvider} that is to be added.
     */
    public static final void add(final ApplicationContext context, final RuleProvider provider)
    {
        AssertArgument.isNotNull(context, "context");
        AssertArgument.isNotNull(provider, "provider");

        Map<String, RuleProvider> providers = getRuleProviders(context);
        if (providers == null)
        {
            providers = new HashMap<String, RuleProvider>();
            context.setAttribute(RuleProvider.class, providers);
        }

        providers.put(provider.getName(), provider);
    }

    /**
     * Gets a {@link RuleProvider} matching the passed in ruleProviderName.
     *
     * @param context The Smooks {@link ApplicationContext}.
     * @param ruleProviderName The name of the rule provider to lookup.
     * @return {@link RuleProvider} The {@link RuleProvider} matching the passed in ruleProviderName.
     *
     * @throws SmooksException
     *      If no providers have been previously set in the {@link ApplicationContext}, or if
     *      the specified ruleProviderName cannot be found.
     */
    public static final RuleProvider get(final ApplicationContext context, final String ruleProviderName)
    {
        AssertArgument.isNotNull(context, "context");

        Map<String, RuleProvider> providers = getRuleProviders(context);
        if ( providers == null || providers.isEmpty())
        {
            throw new SmooksException("No RuleProviders were found. Have you configured a rules section in your Smooks configuration?");
        }

        final RuleProvider provider = providers.get(ruleProviderName);
        if (provider == null)
        {
            throw new SmooksException("Not provider with name '" + ruleProviderName  + "' was found in the execution context. Have you configured the rules section properly?");
        }

        return provider;
    }

    /**
     * Gets the Map of RuleProviders that exist in the Smooks AppcliationContext.
     *
     * @param context The Smooks {@link ApplicationContext}.
     * @return Map<String, RuleProvider> The Map of rule providers. The String key is the name of the rule provider.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, RuleProvider> getRuleProviders(final ApplicationContext context)
    {
        return (Map<String, RuleProvider>) context.getAttribute(RuleProvider.class);
    }

    /**
     * Parse the rule name from the passed in composite rule name.
     *
     * @param compositeRuleName The composite rule name in the form ruleProvider.ruleName.
     * @return {@code String} The rule name part of the composite rule name.
     */
    public static String parseRuleName(final String compositeRuleName)
    {
        final int lastIndexOfDot = compositeRuleName.lastIndexOf('.');
        if (lastIndexOfDot == -1)
        {
            throwInvalidCompositeRuleName(compositeRuleName);
        }
        return compositeRuleName.substring(lastIndexOfDot + 1);
    }

    /**
     *
     * @param compositeRuleName The composite rule name in the form ruleProvider.ruleName.
     * @return {@code String} The rule provider name part of the composite rule name.
     */
    public static String parseRuleProviderName(final String compositeRuleName)
    {
        final int lastIndexOfDot = compositeRuleName.indexOf('.');
        if (lastIndexOfDot == -1)
        {
            throwInvalidCompositeRuleName(compositeRuleName);
        }
        return compositeRuleName.substring(0, lastIndexOfDot);
    }

    private static void throwInvalidCompositeRuleName(final String compositRuleName)
    {
        throw new SmooksException("A rule must be specified in the format <ruleProviderName>.<ruleName>." +
                " Please check you configuration and make sure that you are referencing the rule in a correct manner.");
    }


}
