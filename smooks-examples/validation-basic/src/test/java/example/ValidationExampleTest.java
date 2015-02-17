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
package example;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;
import org.milyn.validation.ValidationResult;
import org.milyn.validation.OnFailResult;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class ValidationExampleTest {

	@Test
    public void test() throws IOException, SAXException {
        ValidationResult results = Main.runSmooks(Main.readInputMessage());
        List<OnFailResult> errors = results.getErrors();
        List<OnFailResult> warnings = results.getWarnings();

        assertEquals(3, errors.size());
        assertEquals(1, warnings.size());

        assertEquals("Invalid customer number 'user1' at 'Order/header/username'.  Customer number must begin with an uppercase character, followed by 5 digits.", errors.get(0).getMessage());
        assertEquals("Invalid product ID '364b' at 'Order/order-item/productId'.  Product ID must match pattern '[0-9]{3}'.", errors.get(1).getMessage());
        assertEquals("Order A188127 contains an order item for product 299 with a quantity of 2 and a unit price of 29.99. This exceeds the permitted per order item total.", errors.get(2).getMessage());

        assertEquals("Invalid email address 'harry.fletcher@gmail.' at 'Order/header/email'.  Email addresses match pattern '^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$'.", warnings.get(0).getMessage());
    }
}
