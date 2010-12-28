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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.milyn.container.MockExecutionContext;

/**
 * Unit test for RegexProviderTest.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class RegexProviderTest
{
    private RegexProvider provider;

    @Before
    public void createProvider()
    {
        provider = new RegexProvider("/smooks-regex.properties");
        provider.setName(RegexProvider.class.getSimpleName());
    }

    @Test (expected = IllegalArgumentException.class )
    public void showThrowIfRuleNameIsNull()
    {
        provider.evaluate(null, "some text", new MockExecutionContext());
    }

    @Test (expected = IllegalArgumentException.class )
    public void showThrowIfSelectedDataIsNull()
    {
        provider.evaluate("ruleName", null , new MockExecutionContext());
    }

    @Test
    public void getProviderName()
    {
        assertEquals("RegexProvider", provider.getName());
    }

    @Test
    public void setSrc()
    {
        final String regexFile = "/regex.properties";
        provider.setSrc(regexFile);
        assertEquals(regexFile, provider.getSrc());
    }

    @Test
    public void evalutatePhoneNumberSE()
    {
        final String ruleName = "phoneNumberSE";
        assertTrue(provider.evaluate(ruleName, "08-7549922", null).matched());
        assertFalse(provider.evaluate(ruleName, "7549922", null).matched());
    }

    @Test
    public void phoneNumberUnitedStates()
    {
        final String ruleName = "phoneNumberUS";
        assertTrue(provider.evaluate(ruleName, "2405525009", null).matched());
        assertTrue(provider.evaluate(ruleName, "1(240) 652-5009", null).matched());
        assertTrue(provider.evaluate(ruleName, "240/752-5009 ext.55", null).matched());

        assertFalse(provider.evaluate(ruleName, "(2405525009", null).matched());
        assertFalse(provider.evaluate(ruleName, "2 (240) 652-5009", null).matched());
    }

    @Test
    public void phoneNumberIndia()
    {
        final String ruleName = "phoneNumberIN";
        assertTrue(provider.evaluate(ruleName, "0493 - 3227341", null).matched());
        assertTrue(provider.evaluate(ruleName, "0493 3227341", null).matched());
        assertTrue(provider.evaluate(ruleName, "493 3227341", null).matched());

        assertFalse(provider.evaluate(ruleName, "93 02273419", null).matched());
        assertFalse(provider.evaluate(ruleName, "493 322734111", null).matched());
        assertFalse(provider.evaluate(ruleName, "493 -- 3227341", null).matched());
    }

    @Test
    public void phoneNumberAustralia()
    {
        final String ruleName = "phoneNumberAU";
        assertTrue(provider.evaluate(ruleName, "0732105432", null).matched());
        assertTrue(provider.evaluate(ruleName, "1300333444", null).matched());
        assertTrue(provider.evaluate(ruleName, "131313", null).matched());

        assertFalse(provider.evaluate(ruleName, "32105432", null).matched());
        assertFalse(provider.evaluate(ruleName, "13000456", null).matched());
    }

    @Test
    public void phoneNumberUnitedKingdom()
    {
        final String ruleName = "phoneNumberGB";
        assertTrue(provider.evaluate(ruleName, "+447222555555", null).matched());
        assertTrue(provider.evaluate(ruleName, "+44 7222 555 555", null).matched());
        assertTrue(provider.evaluate(ruleName, "(0722) 5555555 #2222", null).matched());

        assertFalse(provider.evaluate(ruleName, "(+447222)555555", null).matched());
        assertFalse(provider.evaluate(ruleName, "+44(7222)555555", null).matched());
        assertFalse(provider.evaluate(ruleName, "(0722) 5555555 #22", null).matched());
    }

    @Test
    public void phoneNumberItaly()
    {
        final String ruleName = "phoneNumberIT";
        assertTrue(provider.evaluate(ruleName, "02-343536", null).matched());
        assertTrue(provider.evaluate(ruleName, "02/343536", null).matched());
        assertTrue(provider.evaluate(ruleName, "02 343536", null).matched());

        assertFalse(provider.evaluate(ruleName, "02a343536", null).matched());
        assertFalse(provider.evaluate(ruleName, "02+343536", null).matched());
    }

    @Test
    public void phoneNumberNetherlands()
    {
        final String ruleName = "phoneNumberNL";
        assertTrue(provider.evaluate(ruleName, "06 12345678", null).matched());
        assertTrue(provider.evaluate(ruleName, "010-1234560", null).matched());
        assertTrue(provider.evaluate(ruleName, "0111-101234", null).matched());

        assertFalse(provider.evaluate(ruleName, "05-43021212", null).matched());
        assertFalse(provider.evaluate(ruleName, "123-4567890", null).matched());
        assertFalse(provider.evaluate(ruleName, "1234567890", null).matched());
    }

    @Test
    public void email()
    {
        final String ruleName = "email";
        assertTrue(provider.evaluate(ruleName, "daniel.bevenius@gmail.com", null).matched());
        assertTrue(provider.evaluate(ruleName, "daniel@gmail.se", null).matched());
        assertTrue(provider.evaluate(ruleName, "daniel.bevenius@gmail.uk.com", null).matched());
        assertTrue(provider.evaluate(ruleName, "d.b@gmail.uk.com", null).matched());
        assertTrue(provider.evaluate(ruleName, "db@gl.s", null).matched());

        assertFalse(provider.evaluate(ruleName, "@gmail.com", null).matched());
    }

    @Test
    public void dateMMddyyyyTime()
    {
        final String ruleName = "dateMMddyyyy";
        assertTrue(provider.evaluate(ruleName, "12/30/2002", null).matched());
        assertTrue(provider.evaluate(ruleName, "12/30/2002 9:35 pm", null).matched());
        assertTrue(provider.evaluate(ruleName, "12/30/2002 19:35:02", null).matched());

        assertFalse(provider.evaluate(ruleName, "18/22/2003", null).matched());
        assertFalse(provider.evaluate(ruleName, "8/12/99", null).matched());
        assertFalse(provider.evaluate(ruleName, "8/22/2003 25:00", null).matched());
    }

    @Test
    public void dateyyyyMMdd()
    {
        final String ruleName = "dateyyyyMMdd";
        assertTrue(provider.evaluate(ruleName, "2002-01-31", null).matched());
        assertTrue(provider.evaluate(ruleName, "1997-04-30", null).matched());
        assertTrue(provider.evaluate(ruleName, "2004-01-01", null).matched());

        assertFalse(provider.evaluate(ruleName, "2002-01-32", null).matched());
        assertFalse(provider.evaluate(ruleName, "2003-02-29", null).matched());
        assertFalse(provider.evaluate(ruleName, "04-01-01", null).matched());
    }

    @Test
    public void time()
    {
        final String ruleName = "time";
        assertTrue(provider.evaluate(ruleName, "1 AM", null).matched());
        assertTrue(provider.evaluate(ruleName, "1 PM", null).matched());
        assertTrue(provider.evaluate(ruleName, "23:00:00", null).matched());
        assertTrue(provider.evaluate(ruleName, "23:00", null).matched());
        assertTrue(provider.evaluate(ruleName, "5:29:59 PM", null).matched());

        assertFalse(provider.evaluate(ruleName, "13 PM", null).matched());
        assertFalse(provider.evaluate(ruleName, "13:60:00", null).matched());
        assertFalse(provider.evaluate(ruleName, "00:00:00 AM", null).matched());
    }

    @Test
    public void test_Multiple_Providers() {
        RegexProvider ordersProvider = new RegexProvider();
        RegexProvider productsProvider = new RegexProvider();

        productsProvider.setSrc("/org/milyn/rules/regex/rules-products.properties");
        ordersProvider.setSrc("/org/milyn/rules/regex/rules-orders.properties");

        assertTrue(ordersProvider.evaluate("id", "Z0123456789", null).matched());
        assertFalse(ordersProvider.evaluate("id", "YZ012345678901234", null).matched());

        assertFalse(productsProvider.evaluate("id", "Z0123456789", null).matched());
        assertTrue(productsProvider.evaluate("id", "YZ012345678901234", null).matched());
    }
}
