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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.milyn.commons.SmooksException;
import org.milyn.rules.regex.RegexRuleEvalResult;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockApplicationContext;

/**
 * Unit test for {@link RuleProviderAccessor}.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class RuleProviderAccessorTest
{

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowIfAddingNullProvider()
    {
        RuleProviderAccessor.add(new MockApplicationContext(), null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowIfAddingNullApplicationContext()
    {
        RuleProviderAccessor.add(null, new MockProvider());
    }

    @Test
    public void add()
    {
        final MockApplicationContext context = new MockApplicationContext();
        final MockProvider provider = new MockProvider();

        RuleProviderAccessor.add(context, provider);

        assertNotNull(RuleProviderAccessor.getRuleProviders(context));
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowIfApplicationContextIsNull()
    {
        RuleProviderAccessor.get(null, "dummyName");
    }

    @Test (expected = SmooksException.class)
    public void shouldThrowIfTheProviderCannotBeFound()
    {
        RuleProviderAccessor.get(new MockApplicationContext(), "dummyName");
    }

    @Test (expected = SmooksException.class)
    public void shouldThrowIfGettingNonExistingProvider()
    {
        final MockProvider provider = new MockProvider();
        final MockApplicationContext context = new MockApplicationContext();
        RuleProviderAccessor.add(context, provider);

        RuleProviderAccessor.get(context, "nonExistingProviderName");
    }

    @Test
    public void parseRuleProviderName()
    {
        final String rule = "addressing.email";
        String ruleName = RuleProviderAccessor.parseRuleProviderName(rule);
        assertEquals("addressing", ruleName);
    }

    @Test (expected = SmooksException.class)
    public void shouldThrowIfRuleProviderNameIsInvalid()
    {
        RuleProviderAccessor.parseRuleProviderName("addressing");
    }

    @Test
    public void parseRuleName()
    {
        final String rule = "addressing.email";
        String ruleName = RuleProviderAccessor.parseRuleName(rule);
        assertEquals("email", ruleName);
    }

    @Test (expected = SmooksException.class)
    public void shouldThrowIfRuleNameIsInvalid()
    {
        RuleProviderAccessor.parseRuleProviderName("email");
    }


    public static class MockProvider implements RuleProvider
    {
        private String src;
        private String name;

        public String getName()
        {
            return name;
        }

        public String getSrc()
        {
            return src;
        }

        public void setSrc(String src)
        {
            this.src = src;
        }

        public RuleEvalResult evaluate(String ruleName, CharSequence selectedData, ExecutionContext context) throws SmooksException
        {
            return new RegexRuleEvalResult(true, ruleName, "MockProvider", null, selectedData.toString());
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

}
