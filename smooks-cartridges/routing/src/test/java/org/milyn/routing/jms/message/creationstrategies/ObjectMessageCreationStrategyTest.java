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

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.xml.parsers.ParserConfigurationException;

import org.milyn.container.MockExecutionContext;
import org.milyn.routing.jms.TestBean;
import org.milyn.routing.util.RouterTestHelper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockConnectionFactory;

/**
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
@Test ( groups = "unit")
public class ObjectMessageCreationStrategyTest
{
	private final ObjectMessageCreationStrategy strategy = new ObjectMessageCreationStrategy();

	private static Session jmsSession;

	@Test
	public void createJMSMessage() throws ParserConfigurationException, JMSException, SAXException, IOException
	{
		final String beanId = "123";
		final TestBean bean = RouterTestHelper.createBean();
        MockExecutionContext executionContext = RouterTestHelper.createExecutionContext( beanId, bean );

        Message message = strategy.createJMSMessage( beanId, executionContext, jmsSession ) ;

        assertTrue ( message instanceof ObjectMessage );

        ObjectMessage objectMessage = (ObjectMessage) message;

        assertSame(bean, objectMessage.getObject());
	}

	@BeforeClass
	public static void setup() throws JMSException
	{
		JMSMockObjectFactory jmsObjectFactory = new JMSMockObjectFactory();
		MockConnectionFactory connectionFactory = jmsObjectFactory.getMockConnectionFactory();
		jmsSession = connectionFactory.createQueueConnection().createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
	}

}
