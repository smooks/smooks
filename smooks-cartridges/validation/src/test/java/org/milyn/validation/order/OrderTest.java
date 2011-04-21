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
package org.milyn.validation.order;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.validation.ValidationResult;
import org.milyn.validation.OnFailResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class OrderTest extends TestCase {

    public void test_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
        ValidationResult result = new ValidationResult();

        try {
            smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order-message-01.xml")), result);

            assertEquals(4, result.getNumFailures());

            List<OnFailResult> errors = result.getErrors();
            List<OnFailResult> warnings = result.getWarnings();

            assertEquals(3, errors.size());
            assertEquals(1, warnings.size());

            assertEquals("Invalid customer number '123123' at 'order/header/customer/@number'.  Customer number must match pattern '[A-Z]-[0-9]{5}'.", errors.get(0).getMessage());
            assertEquals("Invalid product ID '222' at 'order/order-items/order-item/product'.  Product ID must match pattern '[0-9]{6}'.", errors.get(1).getMessage());
            assertEquals("Order 12129 (Customer 123123) contains an order item for product 222 which contains an invalid quantity of 7. This quantity exceeds the maximum permited quantity for this product (5).", errors.get(2).getMessage());
            assertEquals("Invalid customer name 'Joe' at 'order/header/customer'.  Customer name must match pattern '[A-Z][a-z]*, [A-Z][a-z]*'.", warnings.get(0).getMessage());
        } finally {
            smooks.close();
        }
    }

    public void test_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
        ValidationResult result = new ValidationResult();

        try {
            smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order-message-02.xml")), result);
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("The maximum number of allowed validation failures (5) has been exceeded.", e.getCause().getMessage());
            assertEquals(6, result.getNumFailures());
        } finally {
            smooks.close();
        }
    }

    public void test_03() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
        ValidationResult result = new ValidationResult();

        try {
            smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order-message-03.xml")), result);
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("A FATAL validation failure has occured [order/order-items/order-item/fail] RegexRuleEvalResult, matched=false, providerName=product, ruleName=failProduct, text=true, pattern=false", e.getCause().getMessage());
            assertEquals(5, result.getNumFailures());
            assertEquals("[order/order-items/order-item/fail] RegexRuleEvalResult, matched=false, providerName=product, ruleName=failProduct, text=true, pattern=false", result.getFatal().toString());
        } finally {
            smooks.close();
        }
    }
}
