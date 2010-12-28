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


import org.junit.Before;
import org.junit.Test;
import org.milyn.container.MockApplicationContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.rules.RuleProviderAccessor;
import org.milyn.rules.regex.RegexProvider;
import org.milyn.payload.FilterResult;
import org.milyn.payload.StringSource;
import org.milyn.Smooks;
import org.milyn.FilterSettings;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

/**
 * Unit test for {@link Validator}
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class ValidatorTest
{
    private MockApplicationContext appContext;
    private RegexProvider regexProvider;

    @Before
    public void setup()
    {
        appContext = new MockApplicationContext();
        regexProvider = new RegexProvider("/smooks-regex.properties");
    }

    @Test
    public void configure()
    {
        final String ruleName = "addressing.email";
        final Validator validator = new Validator(ruleName, OnFail.WARN);

        assertEquals(ruleName, validator.getCompositRuleName());
        assertEquals(OnFail.WARN, validator.getOnFail());
    }

    @Test
    public void validateWarn()
    {
        regexProvider.setName("addressing");
        RuleProviderAccessor.add(appContext, regexProvider);

        final String ruleName = "addressing.email";
        final Validator validator = new Validator(ruleName, OnFail.WARN).setAppContext(appContext);
        final ValidationResult result = new ValidationResult();

        MockExecutionContext executionContext = new MockExecutionContext();
        FilterResult.setResults(executionContext, result);
        validator.validate("xyz", executionContext);
        validator.validate("xyz", executionContext);
        validator.validate("xyz", executionContext);

        assertEquals(0, result.getOKs().size());
        assertEquals(3, result.getWarnings().size());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    public void validateOks()
    {
        regexProvider.setName("addressing");
        RuleProviderAccessor.add(appContext, regexProvider);

        final String ruleName = "addressing.email";
        final Validator validator = new Validator(ruleName, OnFail.OK).setAppContext(appContext);
        final ValidationResult result = new ValidationResult();

        MockExecutionContext executionContext = new MockExecutionContext();
        FilterResult.setResults(executionContext, result);
        validator.validate("xyz", executionContext);
        validator.validate("xyz", executionContext);
        validator.validate("xyz", executionContext);

        assertEquals(3, result.getOKs().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    public void validateErrors()
    {
        regexProvider.setName("addressing");
        RuleProviderAccessor.add(appContext, regexProvider);

        final String ruleName = "addressing.email";
        final Validator validator = new Validator(ruleName, OnFail.ERROR).setAppContext(appContext);
        final ValidationResult result = new ValidationResult();

        MockExecutionContext executionContext = new MockExecutionContext();
        FilterResult.setResults(executionContext, result);
        validator.validate("xyz", executionContext);
        validator.validate("xyz", executionContext);
        validator.validate("xyz", executionContext);

        assertEquals(0, result.getOKs().size());
        assertEquals(0, result.getWarnings().size());
        assertEquals(3, result.getErrors().size());
    }

    @Test
    public void validateFatal()
    {
        regexProvider.setName("addressing");
        RuleProviderAccessor.add(appContext, regexProvider);

        final String ruleName = "addressing.email";
        final String data = "xyz";
        final Validator validator = new Validator(ruleName, OnFail.FATAL).setAppContext(appContext);

        MockExecutionContext executionContext = new MockExecutionContext();
        try
        {
            validator.validate(data, executionContext);
            fail("A ValidationException should have been thrown");
        }
        catch (final Exception e)
        {
            assertTrue( e instanceof ValidationException);

            OnFailResult onFailResult = ((ValidationException) e).getOnFailResult();
            assertNotNull(onFailResult);
            /*
             *  [null] is the failFragmentPath. This test method only exercises the validate method, hence the
             *  frailFramentPath, which is set in visitAfte, is never set.
             */
		    String expected = "[null] RegexRuleEvalResult, matched=false, providerName=addressing, ruleName=email, text=xyz, pattern=\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*([,;]\\s*\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*)*";
            assertEquals(expected, onFailResult.getMessage());
            assertEquals("A FATAL validation failure has occured " + expected, e.getMessage());
        }
    }

    @Test
    public void test_xml_config_01_dom() throws IOException, SAXException {
        test_xml_config_01(FilterSettings.DEFAULT_DOM);
    }

    @Test
    public void test_xml_config_01_sax() throws IOException, SAXException {
        test_xml_config_01(FilterSettings.DEFAULT_SAX);
    }

    private void test_xml_config_01(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        ValidationResult result = new ValidationResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><b x='Xx'>11</b><b x='C'>Aaa</b></a>"), result);

        List<OnFailResult> warnings = result.getWarnings();
        assertEquals(2, warnings.size());
        assertEquals("RegexRuleEvalResult, matched=false, providerName=regex, ruleName=custom, text=11, pattern=[A-Z]([a-z])+", warnings.get(0).getFailRuleResult().toString());
        assertEquals("RegexRuleEvalResult, matched=false, providerName=regex, ruleName=custom, text=C, pattern=[A-Z]([a-z])+", warnings.get(1).getFailRuleResult().toString());
    }

}
