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

package org.milyn.routing.jms;

import org.milyn.routing.jms.message.creationstrategies.StrategyFactory;

import javax.jms.Message;

/**
 * Class to hold JMS properties
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class JMSProperties
{
    private String connectionFactoryName = "ConnectionFactory";
    private String destinationName;
    private String deliveryMode = "persistent";
    private int priority = Message.DEFAULT_PRIORITY;
    private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;
    private String securityPrincipal;
    private String securityCredential;
    private boolean transacted = false;
    private String jmsAcknowledgeMode = "AUTO_ACKNOWLEDGE";
    private String messageType = StrategyFactory.TEXT_MESSAGE;

	public String getConnectionFactoryName()
	{
		return connectionFactoryName;
	}
	public void setConnectionFactoryName( String connectionFactoryName )
	{
		this.connectionFactoryName = connectionFactoryName;
	}
	public String getDestinationName()
	{
		return destinationName;
	}
	public void setDestinationName( String destinationName )
	{
		this.destinationName = destinationName;
	}
	public String getDeliveryMode()
	{
		return deliveryMode;
	}
	public void setDeliveryMode( String deliveryMode )
	{
		this.deliveryMode = deliveryMode;
	}
	public int getPriority()
	{
		return priority;
	}
	public void setPriority( int priority )
	{
		this.priority = priority;
	}
	public long getTimeToLive()
	{
		return timeToLive;
	}
	public void setTimeToLive( long timeToLive )
	{
		this.timeToLive = timeToLive;
	}
	public String getSecurityPrincipal()
	{
		return securityPrincipal;
	}
	public void setSecurityPrincipal( String securityPrincipal )
	{
		this.securityPrincipal = securityPrincipal;
	}
	public String getSecurityCredential()
	{
		return securityCredential;
	}
	public void setSecurityCredential( String securityCredential )
	{
		this.securityCredential = securityCredential;
	}
	public boolean isTransacted()
	{
		return transacted;
	}
	public void setTransacted( boolean transacted )
	{
		this.transacted = transacted;
	}
	public String getAcknowledgeMode()
	{
		return jmsAcknowledgeMode;
	}
	public void setAcknowledgeMode( String jmsAcknowledgeMode )
	{
		this.jmsAcknowledgeMode = jmsAcknowledgeMode;
	}
	public String getMessageType()
	{
		return messageType;
	}
	public void setMessageType( String messageType )
	{
		this.messageType = messageType;
	}

}
