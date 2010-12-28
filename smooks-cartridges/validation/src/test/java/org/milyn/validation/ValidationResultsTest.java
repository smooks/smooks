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

package org.milyn.validation;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.milyn.container.MockExecutionContext;
import org.milyn.rules.RuleEvalResult;

/**
 * Test for {@link ValidationResult}.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class ValidationResultsTest
{
    private MockExecutionContext context;
    private MockResult result;

    @Before
    public void setup()
    {
        context = new MockExecutionContext();
        result = new MockResult("ruleName", "provider", true);
    }

    @Test
    public void addWarn()
    {
        ValidationResult validationResult = new ValidationResult();

        validationResult.addResult(new MockOnFailResult(result), OnFail.WARN);
        List<OnFailResult> warnings = validationResult.getWarnings();
        assertFalse(warnings.isEmpty());
        assertEquals(1, warnings.size());

        validationResult.addResult(new MockOnFailResult(result), OnFail.WARN);
        warnings = validationResult.getWarnings();
        assertEquals(2, warnings.size());
    }

    private class MockResult implements RuleEvalResult
    {
        private String ruleName;
        private String name;
        private boolean matched;

        public MockResult(final String ruleName, final String name, final boolean matched)
        {
            this.ruleName = ruleName;
            this.name = name;
            this.matched = matched;
        }

        public void setRuleName(final String ruleName)
        {
            this.ruleName = ruleName;
        }

        public String getRuleName()
        {
            return ruleName;
        }

        public void setRuleProviderName(final String name)
        {
            this.name = name;
        }
        public String getRuleProviderName()
        {
            return name;
        }

        public Throwable getEvalException() {
            return null;
        }

        public void setMatched(final boolean matched)
        {
            this.matched = matched;
        }

        public boolean matched()
        {
            return matched;
        }

        @Override
        public String toString()
        {
            return "MockResult";
        }
    }

    private class MockOnFailResult implements OnFailResult {
        private RuleEvalResult ruleResult;

        public MockOnFailResult(RuleEvalResult ruleResult) {
            this.ruleResult = ruleResult;
        }

        public String getFailFragmentPath() {
            return "x";
        }

        public RuleEvalResult getFailRuleResult() {
            return ruleResult;
        }

        public String getMessage() {
            return "x";
        }

        public String getMessage(Locale locale) {
            return "x";
        }
    }
}
