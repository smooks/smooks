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

import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.repository.BeanRepositoryManager;

/**
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MapMessageCreationStrategy  implements MessageCreationStrategy {

	public Message createJMSMessage(String beanId, ExecutionContext context, Session jmsSession) throws SmooksException {
		final Object bean = BeanRepositoryManager.getBeanRepository(context).getBean( beanId );
        if(bean == null) {
            throw new SmooksException("Bean beandId '" + beanId + "' not available in the bean repository of this execution context.  Check the order in which your resources are being applied (in Smooks configuration).");
        }
        if(bean instanceof Map == false) {
        	throw new SmooksException("The bean unde beanId '" + beanId + "' with type " + bean.getClass().getName() + "'  can't be send with an JMS MapMessage because it doesn't implement a Map interface.");
        }

        return createMapMessage( (Map<?, ?>) bean, jmsSession );
	}

	private MapMessage createMapMessage( final Map<?, ?> map, final Session jmsSession ) throws SmooksException
	{
		try
		{
			MapMessage mapMessage = jmsSession.createMapMessage();

			mapToMapMessage(map, mapMessage);

			return mapMessage;
		}
		catch (JMSException e)
		{
			final String errorMsg = "JMSException while trying to create TextMessae";
			throw new SmooksConfigurationException( errorMsg, e );
		}
	}

	private void mapToMapMessage(final Map<?, ?> map, MapMessage mapMessage) throws JMSException {

		for(Entry<?, ?> entry : map.entrySet()) {

			String key = entry.getKey().toString();
			Object value = entry.getValue();

			if(value instanceof String) {

				mapMessage.setString(key, (String)value);

			} else if(value instanceof Integer) {

				mapMessage.setInt(key, (Integer)value);

			} else if(value instanceof Long) {

				mapMessage.setLong(key, (Long)value);

			} else if(value instanceof Double) {

				mapMessage.setDouble(key, (Double)value);

			} else if(value instanceof Float) {

				mapMessage.setFloat(key, (Float)value);

			} else if(value instanceof Boolean) {

				mapMessage.setBoolean(key, (Boolean)value);

			} else if(value instanceof Short) {

				mapMessage.setShort(key, (Short)value);

			} else if(value instanceof Byte) {

				mapMessage.setByte(key, (Byte)value);

			} else if(value instanceof Character) {

				mapMessage.setChar(key, (Character)value);

			} else if(value instanceof byte[]) {

				mapMessage.setBytes(key, (byte[])value);

			} else {

				mapMessage.setString(key, value.toString());

			}
		}

	}

}
