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

package org.milyn.cartridge.javabean;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.milyn.Smooks;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockApplicationContext;
import org.milyn.commons.io.StreamUtils;
import org.milyn.payload.JavaResult;
import org.milyn.commons.util.ClassUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author tfennelly
 */
public class BeanPopulatorTest extends TestCase {

	private Locale defaultLocale;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		defaultLocale = Locale.getDefault();
		Locale.setDefault(new Locale("en", "IE"));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		Locale.setDefault(defaultLocale);
	}

    public void testConstructorConfigValidation() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration("x", BeanPopulator.class.getName());

        testConstructorConfigValidation(config, "Invalid Smooks bean configuration. 'beanClass' <param> not specified.");

        config.setParameter("beanId", "x");
        config.setParameter("beanClass", " ");

        testConstructorConfigValidation(config, "Invalid Smooks bean configuration. 'beanClass' <param> not specified.");
    }

    private void testConstructorConfigValidation(SmooksResourceConfiguration config, String expected) {
        try {
            Configurator.configure(new BeanPopulator(), config, new MockApplicationContext());
            fail("Expected SmooksConfigurationException - " + expected);
        } catch(SmooksConfigurationException e) {
            Throwable t = e.getCause();
            if(t.getMessage().indexOf(expected) == -1) {
                e.printStackTrace();
                fail("Expected message to contain [" + expected + "]. Actual [" + t.getMessage() + "]");
            }
        }
    }

    public void test_TypePopCheckBean_dom() throws ParseException, IOException, SAXException {
        test_TypePopCheckBean("type-pop-check-bean-smooks-config.xml");
    }

    public void test_TypePopCheckBean_sax() throws ParseException, IOException, SAXException {
        test_TypePopCheckBean("type-pop-check-bean-smooks-config-sax.xml");
    }

    public void test_TypePopCheckBean(String configName) throws ParseException, IOException, SAXException {
        String packagePath = ClassUtil.toFilePath(getClass().getPackage());
        Smooks smooks = new Smooks(packagePath + "/" + configName);
        ExecutionContext executionContext = smooks.createExecutionContext();
        JavaResult result = new JavaResult(true);

        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("type-pop-check-bean-data.xml")), result);

        TypePopCheckBean bean = (TypePopCheckBean) result.getBean("data");
        assertEquals("1, 2, true, 3.0, 4.0, a, 5, 1163616328000, 6, 7, 8, [9, 10, 11], [12, 13, 14], {integerVal1=15, integerVal2=16, integerVal3=17, integerVal4=18, integerVal5=19, integerVal6=20, integerVal=21, mixedMap={intVal=1, longVal=2, boolVal=true, floatVal=3.0, doubleVal=4.0, charVal=a, integerVal=5, mixedIntValArray=[6, 7, 8]}},", bean.toString().trim());
    }

    /*
    public void test_visit_validateSetterName() throws SAXException, IOException {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration("x", BeanPopulator.class.getName());
        MockExecutionContext request = new MockExecutionContext();
        Document doc = XmlUtil.parseStream(getClass().getResourceAsStream("testxml.txt"), XmlUtil.VALIDATION_TYPE.NONE, true);

        config.setParameter("beanId", "userBean");
        config.setParameter("beanClass", MyGoodBean.class.getName());
        config.setParameter("attributeName", "phoneNumber");
        config.setParameter("setterName", "setX");
        BeanPopulator pppu = new BeanPopulator();
        Configurator.configure(pppu, config);
        try {
            pppu.visitBefore(doc.getDocumentElement(), request);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Bean [userBean] configuration invalid.  Bean setter method [setX(java.lang.String)] not found on type [org.milyn.javabean.MyGoodBean].  You may need to set a 'decoder' on the binding config.", e.getMessage());
        }
    }
    */

    public void test_visit_2() throws SAXException, IOException {
        test_visit_userBean("compressed-config.xml");
    }

    public void test_visit_userBean(String configName) throws SAXException, IOException {

        Smooks smooks = new Smooks(getClass().getResourceAsStream(configName));
        ExecutionContext executionContext = smooks.createExecutionContext();

        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("testxml.txt")), new DOMResult());

        MyGoodBean bean = (MyGoodBean)executionContext.getBeanContext().getBean("userBean");
        assertNotNull("Null bean", bean);
        assertEquals("Myself", bean.getName());
        assertEquals("0861070070", bean.getPhoneNumber());
        assertEquals("Skeagh Bridge...", bean.getAddress());
    }

    public void test_populate_Order() throws SAXException, IOException, InterruptedException {
        test_populate_Order("order-01-smooks-config.xml");
        test_populate_Order("order-01-smooks-config-sax.xml");
        test_populate_Order("order-01-smooks-config-arrays.xml");
    }

    private void test_populate_Order(String configName) throws SAXException, IOException, InterruptedException {

        String packagePath = ClassUtil.toFilePath(getClass().getPackage());
        Smooks smooks = new Smooks(packagePath + "/" + configName);
        ExecutionContext executionContext = smooks.createExecutionContext();

        String resource = StreamUtils.readStream(new InputStreamReader(getClass().getResourceAsStream("order-01.xml")));
        JavaResult result = new JavaResult();

        smooks.filterSource(executionContext, new StreamSource(new StringReader(resource)), result);

        Order order = (Order) result.getBean("order");


        assertOrder(order);
    }

    public void test_update_Order() throws SAXException, IOException, InterruptedException {
    	test_update_Order("order-01-smooks-config-update.xml");
    }

    private void test_update_Order(String configName) throws SAXException, IOException, InterruptedException {

        String packagePath = ClassUtil.toFilePath(getClass().getPackage());

        Order inOrder = new Order();
        Header inHeader = new Header();
        List<OrderItem> inOrderItems = new ArrayList<OrderItem>();

        JavaResult result = new JavaResult();
        result.getResultMap().put("order", inOrder);
   	 	result.getResultMap().put("orderItemList", inOrderItems);
   	 	result.getResultMap().put("header", inHeader);


        Smooks smooks = new Smooks(packagePath + "/" + configName);

        ExecutionContext executionContext = smooks.createExecutionContext();

        String resource = StreamUtils.readStream(new InputStreamReader(getClass().getResourceAsStream("order-01.xml")));

        smooks.filterSource(executionContext, new StreamSource(new StringReader(resource)), result);

        Order order = (Order) result.getBean("order");

        assertSame(inOrder, order);
        assertSame(inOrderItems, order.getOrderItems());
        assertSame(inHeader, order.getHeader());

        assertOrder(order);

    }

    private void assertOrder(Order order) {

    	assertNotNull(order);
        assertNotNull(order.getHeader());

        assertEquals(1163616328000L, order.getHeader().getDate().getTime());
        assertEquals("Joe", order.getHeader().getCustomerName());
        assertEquals(new Long(123123), order.getHeader().getCustomerNumber());

        assertNotNull(order.getHeader().getOrder());

        assertTrue("PrivatePerson was not set to true", order.getHeader().getPrivatePerson());

        List<OrderItem> orderItems = order.getOrderItems();
        OrderItem[] orderItemsArray = order.getOrderItemsArray();

        assertTrue(orderItems != null || orderItemsArray != null);

        if(orderItemsArray != null) {
            orderItems = Arrays.asList(orderItemsArray);
        }

        assertEquals(2, orderItems.size());

        OrderItem orderItem = orderItems.get(0);
        assertEquals(8.90d, orderItem.getPrice());
        assertEquals(111, orderItem.getProductId());
        assertEquals(new Integer(2), orderItem.getQuantity());

        if(orderItemsArray == null) {
        	assertNotNull(orderItem.getOrder());
        }

        orderItem = orderItems.get(1);
        assertEquals(5.20d, orderItem.getPrice());
        assertEquals(222, orderItem.getProductId());
        assertEquals(new Integer(7), orderItem.getQuantity());

        if(orderItemsArray == null) {
        	assertNotNull(orderItem.getOrder());
        }

    }

    public void test_populate_Employee() throws SAXException, IOException, InterruptedException {
        test_populate_Employee("employee-01-smooks-config.xml");
    }

	/**
	 * Test with inherited model
	 */
	private void test_populate_Employee(String configName) throws IOException, SAXException {

		String packagePath = ClassUtil.toFilePath(getClass().getPackage());

        Smooks smooks = new Smooks(packagePath + "/" + configName);
        ExecutionContext executionContext = smooks.createExecutionContext();

        JavaResult result = new JavaResult();

        String resource = StreamUtils.readStream(new InputStreamReader(getClass().getResourceAsStream("employee-01.xml")));
        smooks.filterSource(executionContext, new StreamSource(new StringReader(resource)), result);

        Employee employee = (Employee) result.getBean("employee");

        assertNotNull(employee);

        assertEquals("111", employee.getFirstName());
        assertEquals("222", employee.getLastName());
        assertEquals("333", employee.getEmployeeId());

	}


	public void test_factory() throws Exception {
		String packagePath = ClassUtil.toFilePath(getClass().getPackage());
        Smooks smooks = new Smooks(packagePath + "/factory-01-config.xml");
        ExecutionContext executionContext = smooks.createExecutionContext();
        JavaResult result = new JavaResult(true);

        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("factory-01.xml")), result);

        assertNotNull(result.getBean("arrayList"));
        assertNotNull(result.getBean("arrayList") instanceof ArrayList<?>);

        assertNotNull(result.getBean("linkedList"));
        assertNotNull(result.getBean("linkedList") instanceof LinkedList<?>);
	}

}
