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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.rules.regex.RegexRuleEvalResult;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

/**
 * Unit test for RuleProviderFactory.
 * <p/>
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class RulesProviderFactoryTest
{
    @Test
    public void extendedConfig() throws IOException, SAXException
    {
        final Smooks smooks = new Smooks("/smooks-configs/extended/1.0/smooks-rules-config.xml");
        final StringSource source = new StringSource("<order></order>");
        final StringResult result = new StringResult();

        smooks.filterSource(source, result);

        final Map<String, RuleProvider> ruleProviders = RuleProviderAccessor.getRuleProviders(smooks.getApplicationContext());

        assertNotNull("Not rules providers were created!", ruleProviders);
        assertEquals(1, ruleProviders.size());
        assertNotNull(RuleProviderAccessor.get(smooks.getApplicationContext(), "custom"));
    }

    @Test
    @Ignore
    public void createProvider()
    {
        final RuleProvider provider = new RulesProviderFactory().createProvider(MockProvider.class);
        assertNotNull(provider);
        assertTrue(provider instanceof MockProvider);
        assertEquals("MockProvider", provider.getName());
    }

    public static class MockProvider implements RuleProvider
    {
        public String getName()
        {
            return getClass().getSimpleName();
        }

        public String getSrc()
        {
            return null;
        }

        public void setSrc(String src)
        {
        }

        public RuleEvalResult evaluate(String ruleName, CharSequence selectedData, ExecutionContext context) throws SmooksException
        {
            return new RegexRuleEvalResult(true, ruleName, "MockProvider", null, selectedData.toString());
        }

        public void setName(String name)
        {
        }
    }

}
