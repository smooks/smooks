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

import javax.jms.Message;
import javax.jms.Session;

import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;

/**
 * A Strategy for creating different kinds of JMS Message objects from
 * either DOM Element structures of SAX Element structures.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public interface MessageCreationStrategy
{
	/**
	 * Create a JMS Message object with the content of Java Object
	 * identified by beanId.
	 *
	 * @param beanId		- the beanId of the Java Object to populate the JMS Message body with
	 * @param context
	 * @param jmsSession
	 * @return
	 * @throws SmooksException
	 */
	Message createJMSMessage( String beanId, ExecutionContext context, Session jmsSession ) throws SmooksException;


}
