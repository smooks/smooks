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

package org.milyn.rules.regex;

import org.milyn.SmooksException;
import org.milyn.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;
import org.milyn.resource.URIResourceLocator;
import org.milyn.rules.RuleEvalResult;
import org.milyn.rules.RuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Regex Rule Provider.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class RegexProvider implements RuleProvider
{
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RegexProvider.class);

    /**
     * Option string identifying a file that contains regex mappings.
     */
    private String src;

    /**
     * The name of this rule provider.
     */
    private String providerName;

    /**
     * The rules.
     */
    private Map<String, Pattern> rules = new HashMap<String, Pattern>();

    /**
     * No-args constructor required by Smooks.
     */
    public RegexProvider()
    {
    }

    /**
     * Constructor which accepts a source regex file.
     * @param src The name/path of the properties file containing the reqular expressions.
     */
    public RegexProvider(final String src)
    {
        setSrc(src);
    }

    /**
     *
     */
    public RuleEvalResult evaluate(final String ruleName, final CharSequence selectedData, final ExecutionContext context) throws SmooksException
    {
        AssertArgument.isNotNullAndNotEmpty(ruleName, "ruleName");
        AssertArgument.isNotNull(selectedData, "selectedData");

        final Pattern pattern = rules.get(ruleName);

        if (pattern == null) {
            throw new SmooksException("Unknown rule name '" + ruleName + "' on Regex RuleProvider '" + providerName + "'.");
        }

        final boolean matched = pattern.matcher(selectedData).matches();

        return new RegexRuleEvalResult(matched, ruleName, providerName, pattern, selectedData.toString());
    }

    public String getName()
    {
        return providerName;
    }

    public void setName(final String name)
    {
        this.providerName = name;
    }

    public String getSrc()
    {
        return src;
    }

    public void setSrc(String src)
    {
        this.src = src;
        loadRules(src);
    }

    /**
     * Load the regex rule from the specified rule file.
     *
     * @param ruleFile The rule file path.
     */
    protected void loadRules(final String ruleFile)
    {
        if (ruleFile == null) {
            throw new SmooksException("ruleFile not specified.");
        }

        InputStream ruleStream;

        // Get the input stream...
        try
        {
            ruleStream = new URIResourceLocator().getResource(ruleFile);
        }
        catch (final IOException e)
        {
            throw new SmooksException("Failed to open rule file '" + ruleFile + "'.", e);
        }

        Properties rawRuleTable = new Properties();

        // Load the rawRuleTable into a Properties instance...
        try
        {
            rawRuleTable.load(ruleStream);
        }
        catch (final IOException e)
        {
            throw new SmooksException("Error reading InputStream to rule file '" + ruleFile + "'.", e);
        }
        finally
        {
            try
            {
                ruleStream.close();
            }
            catch (final IOException e)
            {
                LOGGER.debug("Error closing InputStream to Regex Rule file '" + ruleFile + "'.", e);
            }
        }

        // Generate rules Map (Map<String, Pattern>) from the raw rule table...
        Set<Map.Entry<Object, Object>> ruleEntrySet = rawRuleTable.entrySet();
        for(Map.Entry<Object, Object> rule : ruleEntrySet)
        {
            String ruleName = (String) rule.getKey();
            String rulePattern = (String) rule.getValue();

            rules.put(ruleName, Pattern.compile(rulePattern));
        }
    }

}
