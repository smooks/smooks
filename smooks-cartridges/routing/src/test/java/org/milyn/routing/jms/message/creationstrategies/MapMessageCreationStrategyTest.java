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
package org.milyn.routing.jms.message.creationstrategies;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.parsers.ParserConfigurationException;

import org.milyn.container.MockExecutionContext;
import org.milyn.routing.util.RouterTestHelper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockConnectionFactory;

/**
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test ( groups = "unit" )
public class MapMessageCreationStrategyTest
{
	private final MapMessageCreationStrategy strategy = new MapMessageCreationStrategy();

	private static Session jmsSession;

	@Test
	public void createJMSMessage() throws ParserConfigurationException, JMSException, SAXException, IOException
	{
		final String beanId = "123";
		final Map<String, Object> bean = new HashMap<String, Object>();
		bean.put("string", "Test");
		bean.put("int", 10);
		bean.put("long", 1000l);
		bean.put("double", 1000.01d);
		bean.put("float", 2000.01f);
		bean.put("short", (short)8);
		bean.put("boolean", false);
		bean.put("byte", (byte)1);
		bean.put("char", 'c');
		bean.put("chars", "Test".getBytes());
		bean.put("object", new Object() {
				@Override
				public String toString() {
					return "someObject";
				}
			});

        MockExecutionContext executionContext = RouterTestHelper.createExecutionContext( beanId, bean );

        Message message = strategy.createJMSMessage( beanId, executionContext, jmsSession ) ;

        assertTrue ( message instanceof MapMessage );

        MapMessage mapMessage = (MapMessage) message;

        assertEquals(bean.get("string"), mapMessage.getString("string"));
        assertEquals(bean.get("int"), mapMessage.getInt("int"));
        assertEquals(bean.get("long"), mapMessage.getLong("long"));
        assertEquals(bean.get("double"), mapMessage.getDouble("double"));
        assertEquals(bean.get("float"), mapMessage.getFloat("float"));
        assertEquals(bean.get("short"), mapMessage.getShort("short"));
        assertEquals(bean.get("boolean"), mapMessage.getBoolean("boolean"));
        assertEquals(bean.get("byte"), mapMessage.getByte("byte"));
        assertEquals(bean.get("char"), mapMessage.getChar("char"));
        assertEquals(new String((byte[])bean.get("chars")), new String(mapMessage.getBytes("chars")));
        assertEquals(bean.get("object").toString(), mapMessage.getString("object"));

	}

	@BeforeClass
	public static void setup() throws JMSException
	{
		JMSMockObjectFactory jmsObjectFactory = new JMSMockObjectFactory();
		MockConnectionFactory connectionFactory = jmsObjectFactory.getMockConnectionFactory();
		jmsSession = connectionFactory.createQueueConnection().createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
	}

}
