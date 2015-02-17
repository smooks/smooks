package org.milyn.routing.jms.message.creationstrategies;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;

public class ObjectMessageCreationStrategy implements MessageCreationStrategy
{

	public Message createJMSMessage(
			final String beanId,
			final ExecutionContext context,
			final Session jmsSession )
		throws SmooksException
	{
        final Object bean = context.getBeanContext().getBean(beanId);

        if(bean == null) {
        	throw new SmooksException("Bean beandId '" + beanId + "' not available in the bean repository of this execution context.  Check the order in which your resources are being applied (in Smooks configuration).");
        }

        if(bean instanceof Serializable == false) {
			throw new SmooksException("The bean unde beanId '" + beanId + "' with type " + bean.getClass().getName() + "'  can't be send with an JMS ObjectMessage because it isn't serializable.");
		}

        return createObjectMessage( (Serializable) bean, jmsSession );
	}

	private ObjectMessage createObjectMessage(
			final Serializable object,
			final Session session )
		throws SmooksException
	{
		try
		{

			return session.createObjectMessage( object );
		}
		catch (JMSException e)
		{
			final String errorMsg = "JMSException while trying to set JMS Header Fields";
			throw new SmooksConfigurationException( errorMsg, e );
		}
	}

}
