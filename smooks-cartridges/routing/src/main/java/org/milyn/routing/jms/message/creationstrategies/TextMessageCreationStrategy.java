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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.repository.BeanRepositoryManager;

/**
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class TextMessageCreationStrategy implements MessageCreationStrategy
{

	public Message createJMSMessage(
			final String beanId,
			final ExecutionContext context,
			final Session jmsSession ) throws SmooksException
	{
        final Object bean = BeanRepositoryManager.getBeanRepository(context).getBean( beanId );
        if(bean == null) {
            throw new SmooksException("Bean beandId '" + beanId + "' not available in the bean repository of this execution context.  Check the order in which your resources are being applied (in Smooks configuration).");
        }
        return createTextMessage( bean.toString(), jmsSession );
	}

	private TextMessage createTextMessage( final String text, final Session jmsSession ) throws SmooksException
	{
		try
		{
			return jmsSession.createTextMessage( text );
		}
		catch (JMSException e)
		{
			final String errorMsg = "JMSException while trying to create TextMessae";
			throw new SmooksConfigurationException( errorMsg, e );
		}
	}

}
