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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.annotation.Uninitialize;
import org.milyn.delivery.annotation.VisitAfterIf;
import org.milyn.delivery.annotation.VisitBeforeIf;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.sax.*;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.routing.SmooksRoutingException;
import org.milyn.routing.jms.message.creationstrategies.MessageCreationStrategy;
import org.milyn.routing.jms.message.creationstrategies.StrategyFactory;
import org.milyn.routing.jms.message.creationstrategies.TextMessageCreationStrategy;
import org.milyn.util.FreeMarkerUtils;
import org.milyn.util.FreeMarkerTemplate;
import org.w3c.dom.Element;

/**
 * <p/>
 * Router is a Visitor for DOM or SAX elements. It sends the content
 * as a JMS Message object to the configured destination.
 * <p/>
 * The type of the JMS Message is determined by the "messageType" config param.
 * <p/>
 * Example configuration:
 * <pre>
 * &lt;resource-config selector="orderItems"&gt;
 *    &lt;resource&gt;org.milyn.routing.jms.JMSRouter&lt;/resource&gt;
 *    &lt;param name="beanId">beanId&lt;/param&gt;
 *    &lt;param name="destinationName"&gt;/queue/smooksRouterQueue&lt;/param&gt;
 * &lt;/resource-config&gt;
 *	....
 * Optional parameters:
 *    &lt;param name="executeBefore"&gt;true&lt;/param&gt;
 *    &lt;param name="jndiContextFactory"&gt;ConnectionFactory&lt;/param&gt;
 *    &lt;param name="jndiProviderUrl"&gt;jnp://localhost:1099&lt;/param&gt;
 *    &lt;param name="jndiNamingFactory"&gt;org.jboss.naming:java.naming.factory.url.pkgs=org.jnp.interfaces&lt;/param&gt;
 *    &lt;param name="connectionFactory"&gt;ConnectionFactory&lt;/param&gt;
 *    &lt;param name="deliveryMode"&gt;persistent&lt;/param&gt;
 *    &lt;param name="priority"&gt;10&lt;/param&gt;
 *    &lt;param name="timeToLive"&gt;100000&lt;/param&gt;
 *    &lt;param name="securityPrincipal"&gt;username&lt;/param&gt;
 *    &lt;param name="securityCredential"&gt;password&lt;/param&gt;
 *    &lt;param name="acknowledgeMode"&gt;AUTO_ACKNOWLEDGE&lt;/param&gt;
 *    &lt;param name="transacted"&gt;false&lt;/param&gt;
 *    &lt;param name="correlationIdPattern"&gt;orderitem-${order.orderId}-${order.orderItem.itemId}&lt;/param&gt;
 *    &lt;param name="messageType"&gt;ObjectMessage&lt;/param&gt;
 *    &lt;param name="highWaterMark"&gt;50&lt;/param&gt;
 *    &lt;param name="highWaterMarkTimeout"&gt;5000&lt;/param&gt;
 *    &lt;param name="highWaterMarkPollFrequency"&gt;500&lt;/param&gt;
 * </pre>
 * Description of configuration properties:
 * <ul>
 * <li><i>jndiContextFactory</i>: the JNDI ContextFactory to use.
 * <li><i>jndiProviderUrl</i>:  the JNDI Provider URL to use.
 * <li><i>jndiNamingFactory</i>: the JNDI NamingFactory to use.
 * <li><i>connectionFactory</i>: the ConnectionFactory to look up.
 * <li><i>deliveryMode</i>: the JMS DeliveryMode. 'persistent'(default) or 'non-persistent'.
 * <li><i>priority</i>: the JMS Priority to be used.
 * <li><i>timeToLive</i>: the JMS Time-To-Live to be used.
 * <li><i>securityPrincipal</i>: security principal use when creating the JMS connection.
 * <li><i>securityCredential</i>: the security credentials to use when creating the JMS connection.
 * <li><i>acknowledgeMode</i>: the acknowledge mode to use. One of 'AUTO_ACKNOWLEDGE'(default), 'CLIENT_ACKNOWLEDGE', 'DUPS_OK_ACKNOWLEDGE'.
 * <li><i>transacted</i>: determines if the session should be transacted. Defaults to 'false'.
 * <li><i>correlationIdPattern</i>: JMS Correlation pattern that will be used for the outgoing message. Supports templating.
 * <li><i>messageType</i>: type of JMS Message that should be sent. 'TextMessage'(default), 'ObjectMessage' or 'MapMessage'.
 * <li><i>highWaterMark</i>: max number of messages that can be sitting in the JMS Destination at any any time. Default is 200.
 * <li><i>highWaterMarkTimeout</i>: number of ms to wait for the system to process JMS Messages from the JMS destination
 * 		so that the number of JMS Messages drops below the highWaterMark. Default is 60000 ms.
 * <li><i>highWaterMarkPollFrequency</i>: number of ms to wait between checks on the High Water Mark, while
 *      waiting for it to drop. Default is 1000 ms.
 * </ul>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 * @since 1.0
 *
 */
@VisitBeforeIf(	condition = "parameters.containsKey('executeBefore') && parameters.executeBefore.value == 'true'")
@VisitAfterIf(	condition = "!parameters.containsKey('executeBefore') || parameters.executeBefore.value != 'true'")
public class JMSRouter implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, Consumer
{
	/*
	 *	Log instance
	 */
	private final Log logger = LogFactory.getLog( JMSRouter.class );

	/*
	 *	JNDI Properties holder
	 */
    private final JNDIProperties jndiProperties = new JNDIProperties();

    /*
     *	JMS Properties holder
     */
    private final JMSProperties jmsProperties = new JMSProperties();

    /*
	 * 	BeanId is a key that is used to look up a bean
	 * 	in the execution context
     */
    @ConfigParam( use = ConfigParam.Use.REQUIRED )
    private String beanId;

    @ConfigParam( use = ConfigParam.Use.OPTIONAL )
    private String correlationIdPattern;
    private FreeMarkerTemplate correlationIdTemplate;

    @ConfigParam(defaultVal = "200")
    private int highWaterMark = 200;
    @ConfigParam(defaultVal = "60000")
    private long highWaterMarkTimeout = 60000;

    @ConfigParam(defaultVal = "1000")
    private long highWaterMarkPollFrequency = 1000;

    /*
     * 	Strategy for JMS Message object creation
     */
    private MessageCreationStrategy msgCreationStrategy = new TextMessageCreationStrategy();

    /*
     * 	JMS Destination
     */
    private Destination destination;

    /*
     * 	JMS Connection
     */
    private Connection connection;

    /*
     * 	JMS Message producer
     */
    private MessageProducer msgProducer;
    /*
     * 	JMS Session
     */
    private Session session;

    @Initialize
    public void initialize() throws SmooksConfigurationException, JMSException {
        Context context = null;
        boolean initialized = false;

        if(beanId == null) {
            throw new SmooksConfigurationException("Mandatory 'beanId' property not defined.");
        }
        if(jmsProperties.getDestinationName() == null) {
            throw new SmooksConfigurationException("Mandatory 'destinationName' property not defined.");
        }

        try
        {
            if(correlationIdPattern != null) {
                correlationIdTemplate = new FreeMarkerTemplate(correlationIdPattern);
            }

            Properties jndiContextProperties = jndiProperties.toProperties();

            if(jndiContextProperties.isEmpty()) {
                context = new InitialContext();
            } else {
                context = new InitialContext(jndiContextProperties);
            }
            destination = (Destination) context.lookup( jmsProperties.getDestinationName() );
            msgProducer = createMessageProducer( destination, context );
            setMessageProducerProperties( );

            initialized = true;
        }
        catch (NamingException e)
        {
            final String errorMsg = "NamingException while trying to lookup [" + jmsProperties.getDestinationName() + "]";
            logger.error( errorMsg, e );
            throw new SmooksConfigurationException( errorMsg, e );
        } finally {
            if ( context != null )
            {
                try { context.close(); } catch (NamingException e) { logger.debug( "NamingException while trying to close initial Context"); }
            }

            if(!initialized) {
                releaseJMSResources();
            }
        }
    }

    @Uninitialize
    public void uninitialize() throws JMSException {
        releaseJMSResources();
    }

    public boolean consumes(Object object) {
        if(object.toString().startsWith(beanId)) {
            // We use startsWith (Vs equals) so as to catch bean populations e.g. "address.street".
            return true;
        }

        return false;
    }

    public void setBeanId(String beanId) {
        AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
        this.beanId = beanId;
    }

    public void setCorrelationIdPattern(String correlationIdPattern) {
        this.correlationIdPattern = correlationIdPattern;
    }

    public void setHighWaterMark(int highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    public void setHighWaterMarkTimeout(long highWaterMarkTimeout) {
        this.highWaterMarkTimeout = highWaterMarkTimeout;
    }

    public void setHighWaterMarkPollFrequency(long highWaterMarkPollFrequency) {
        this.highWaterMarkPollFrequency = highWaterMarkPollFrequency;
    }

    @ConfigParam ( use = Use.OPTIONAL )
    public void setJndiContextFactory( final String contextFactory )
    {
        jndiProperties.setContextFactory( contextFactory );
    }

    @ConfigParam ( use = Use.OPTIONAL )
    public void setJndiProperties(final String propertiesFile )
    {
        jndiProperties.setPropertiesFile( propertiesFile );
    }

    public void setJndiProperties(final Properties properties )
    {
        jndiProperties.setProperties(properties);
    }

    @ConfigParam ( use = Use.OPTIONAL )
    public void setJndiProviderUrl(final String providerUrl )
    {
        jndiProperties.setProviderUrl( providerUrl );
    }


    @ConfigParam ( use = Use.OPTIONAL )
    public void setJndiNamingFactoryUrl(final String pkgUrl )
    {
        jndiProperties.setNamingFactoryUrlPkgs( pkgUrl );
    }

    @ConfigParam ( use = Use.REQUIRED )
    public void setDestinationName( final String destinationName )
    {
        AssertArgument.isNotNullAndNotEmpty(destinationName, "destinationName");
        jmsProperties.setDestinationName( destinationName );
    }

    @ConfigParam ( choice = { "persistent", "non-persistent" }, defaultVal = "persistent", use = Use.OPTIONAL )
    public void setDeliveryMode( final String deliveryMode )
    {
        jmsProperties.setDeliveryMode( deliveryMode );
    }

    @ConfigParam ( use = Use.OPTIONAL )
    public void setTimeToLive( final long timeToLive )
    {
        jmsProperties.setTimeToLive( timeToLive );
    }

    @ConfigParam ( use = Use.OPTIONAL )
    public void setSecurityPrincipal( final String securityPrincipal )
    {
        jmsProperties.setSecurityPrincipal( securityPrincipal );
    }

    @ConfigParam ( use = Use.OPTIONAL )
    public void setSecurityCredential( final String securityCredential )
    {
        jmsProperties.setSecurityCredential( securityCredential );
    }

    @ConfigParam ( use = Use.OPTIONAL, defaultVal = "false" )
    public void setTransacted( final boolean transacted )
    {
        jmsProperties.setTransacted( transacted );
    }

    @ConfigParam( defaultVal = "ConnectionFactory" , use = Use.OPTIONAL )
    public void setConnectionFactoryName( final String connectionFactoryName )
    {
        jmsProperties.setConnectionFactoryName( connectionFactoryName );
    }

    @ConfigParam ( use = Use.OPTIONAL )
    public void setPriority( final int priority )
    {
        jmsProperties.setPriority( priority );
    }

    @ConfigParam (defaultVal = "AUTO_ACKNOWLEDGE",
            choice = {"AUTO_ACKNOWLEDGE", "CLIENT_ACKNOWLEDGE", "DUPS_OK_ACKNOWLEDGE" } )
    public void setAcknowledgeMode( final String jmsAcknowledgeMode )
    {
        jmsProperties.setAcknowledgeMode( jmsAcknowledgeMode );
    }

    @ConfigParam (
            defaultVal = StrategyFactory.TEXT_MESSAGE,
            choice = { StrategyFactory.TEXT_MESSAGE ,  StrategyFactory.OBJECT_MESSAGE }  )
    public void setMessageType( final String messageType )
    {
        msgCreationStrategy = StrategyFactory.getInstance().createStrategy( messageType );
        jmsProperties.setMessageType( messageType );
    }

    //	Vistor methods

    public void visitAfter( final Element element, final ExecutionContext execContext ) throws SmooksException
	{
		visit( execContext );
	}

    public void visitBefore( final Element element, final ExecutionContext execContext ) throws SmooksException
	{
		visit( execContext );
	}

    public void visitAfter( final SAXElement element, final ExecutionContext execContext ) throws SmooksException, IOException
	{
		visit( execContext );
	}

    public void visitBefore( final SAXElement element, final ExecutionContext execContext ) throws SmooksException, IOException
	{
		visit( execContext );
	}

    private void visit( final ExecutionContext execContext ) throws SmooksException	{
        Message message = msgCreationStrategy.createJMSMessage(beanId, execContext, session);

        if(correlationIdTemplate != null) {
            setCorrelationID(execContext, message);
        }

        sendMessage(message);
	}

    //	Lifecycle

    protected MessageProducer createMessageProducer( final Destination destination, final Context context ) throws JMSException {
		try
		{
		    final ConnectionFactory connFactory = (ConnectionFactory) context.lookup( jmsProperties.getConnectionFactoryName() );

			connection = (jmsProperties.getSecurityPrincipal() == null && jmsProperties.getSecurityCredential() == null ) ?
					connFactory.createConnection():
					connFactory.createConnection( jmsProperties.getSecurityPrincipal(), jmsProperties.getSecurityCredential() );

			session = connection.createSession( jmsProperties.isTransacted(),
					AcknowledgeModeEnum.getAckMode( jmsProperties.getAcknowledgeMode().toUpperCase() ).getAcknowledgeModeInt() );

			msgProducer = session.createProducer( destination );
			connection.start();
			logger.info ("JMS Connection started");
		}
		catch( JMSException e)
		{
			final String errorMsg = "JMSException while trying to create MessageProducer for Queue [" + jmsProperties.getDestinationName() + "]";
            releaseJMSResources();
            throw new SmooksConfigurationException( errorMsg, e );
		}
		catch (NamingException e)
		{
			final String errorMsg = "NamingException while trying to lookup ConnectionFactory [" + jmsProperties.getConnectionFactoryName() + "]";
            releaseJMSResources();
			throw new SmooksConfigurationException( errorMsg, e );
		}

		return msgProducer;
	}

    /**
     * Sets the following MessageProducer properties:
     * <lu>
     * 	<li>TimeToLive
     * 	<li>Priority
     * 	<li>DeliveryMode
     * </lu>
     * <p>
     * Subclasses may override this behaviour.
     */
	protected void setMessageProducerProperties() throws SmooksConfigurationException
	{
		try
		{
			msgProducer.setTimeToLive( jmsProperties.getTimeToLive() );
			msgProducer.setPriority( jmsProperties.getPriority() );

			final int deliveryModeInt = "non-persistent".equals( jmsProperties.getDeliveryMode() ) ?
					DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT;
			msgProducer.setDeliveryMode( deliveryModeInt );
		}
		catch (JMSException e)
		{
			final String errorMsg = "JMSException while trying to set JMS Header Fields";
			throw new SmooksConfigurationException( errorMsg, e );
		}
	}

    protected void sendMessage( final Message message ) throws SmooksRoutingException
	{
        try {
            waitWhileAboveHighWaterMark();
        } catch (JMSException e) {
            throw new SmooksRoutingException("Exception while attempting to check JMS Queue High Water Mark.", e );
        }

        try
		{
            msgProducer.send( message );
		}
		catch (JMSException e)
		{
			final String errorMsg = "JMSException while sending Message.";
			throw new SmooksRoutingException( errorMsg, e );
		}
	}

    private void waitWhileAboveHighWaterMark() throws JMSException, SmooksRoutingException {
        if(highWaterMark == -1) {
            return;
        }

        if(session instanceof QueueSession) {
            QueueSession queueSession = (QueueSession) session;
            QueueBrowser queueBrowser = queueSession.createBrowser((Queue) destination);

            try {
                int length = getQueueLength(queueBrowser);
                long start = System.currentTimeMillis();

                if(logger.isDebugEnabled() && length >= highWaterMark) {
                    logger.debug("Length of JMS destination Queue '" + jmsProperties.getDestinationName() + "' has reached " + length +  ".  High Water Mark is " + highWaterMark +  ".  Waiting for Queue length to drop.");
                }

                while(length >= highWaterMark && (System.currentTimeMillis() < start + highWaterMarkTimeout)) {
                    try {
                        Thread.sleep(highWaterMarkPollFrequency);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted", e);
                        return;
                    }
                    length = getQueueLength(queueBrowser);
                }

                // Check did the queue length drop below the HWM...
                if(length >= highWaterMark) {
                    throw new SmooksRoutingException("Failed to route JMS message to Queue destination '" + ((Queue) destination).getQueueName() + "'. Timed out (" + highWaterMarkTimeout + " ms) waiting for queue length to drop below High Water Mark (" + highWaterMark + ").  Consider increasing 'highWaterMark' and/or 'highWaterMarkTimeout' param values.");
                }
            } finally {
                queueBrowser.close();
            }
        }
    }

    private int getQueueLength(QueueBrowser queueBrowser) throws JMSException {
        int length = 0;
        Enumeration queueEnum = queueBrowser.getEnumeration();
        while(queueEnum.hasMoreElements()) {
            length++;
            queueEnum.nextElement();
        }
        return length;
    }

    protected void close( final Connection connection )
	{
		if ( connection != null )
		{
			try
			{
				connection.close();
			}
			catch (JMSException e)
			{
				final String errorMsg = "JMSException while trying to close connection";
				logger.debug( errorMsg, e );
			}
		}
	}

    protected void close( final Session session )
	{
		if ( session != null )
		{
			try
			{
				session.close();
			}
			catch (JMSException e)
			{
				final String errorMsg = "JMSException while trying to close session";
				logger.debug( errorMsg, e );
			}
		}
	}

    public Destination getDestination()
	{
		return destination;
	}

    public String getJndiContextFactory()
	{
		return jndiProperties.getContextFactory();
	}

    public String getJndiProviderUrl()
	{
		return jndiProperties.getProviderUrl();
	}

    public String getJndiNamingFactoryUrl()
	{
		return jndiProperties.getNamingFactoryUrlPkgs();
	}

    public String getDestinationName()
	{
		return jmsProperties.getDestinationName();
	}

    private void setCorrelationID(ExecutionContext execContext, Message message) {
        Map<String, Object> beanMap = FreeMarkerUtils.getMergedModel(execContext);
        String correlationId = correlationIdTemplate.apply(beanMap);

        try {
            message.setJMSCorrelationID(correlationId);
        } catch (JMSException e) {
            throw new SmooksException("Failed to set CorrelationID '" + correlationId + "' on message.", e);
        }
    }

    public String getDeliveryMode()
	{
		return jmsProperties.getDeliveryMode();
	}

    public long getTimeToLive()
	{
		return jmsProperties.getTimeToLive();
	}

    public String getSecurityPrincipal()
	{
		return jmsProperties.getSecurityPrincipal();
	}

    public String getSecurityCredential()
	{
		return jmsProperties.getSecurityCredential();
	}

    public boolean isTransacted()
	{
		return jmsProperties.isTransacted();
	}

    public String getConnectionFactoryName()
	{
		return jmsProperties.getConnectionFactoryName();
	}

    public int getPriority()
	{
		return jmsProperties.getPriority();
	}

    public String getAcknowledgeMode()
	{
		return jmsProperties.getAcknowledgeMode();
	}

    public void setMsgCreationStrategy( final MessageCreationStrategy msgCreationStrategy )
	{
		this.msgCreationStrategy = msgCreationStrategy;
	}

    private void releaseJMSResources() throws JMSException {
        if (connection != null) {
            try {
                try {
                    connection.stop();
                } finally {
                    try {
                        closeProducer();
                    } finally {
                        closeSession();
                    }
                }
            } catch (JMSException e) {
                logger.debug("JMSException while trying to stop JMS Connection.", e);
            } finally {
                connection.close();
                connection = null;
            }
        }
    }

    private void closeProducer() {
        if (msgProducer != null) {
            try {
                msgProducer.close();
            } catch (JMSException e) {
                logger.debug("JMSException while trying to close JMS Message Producer.", e);
            } finally {
                msgProducer = null;
            }
        }
    }

    private void closeSession() {
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                logger.debug("JMSException while trying to close JMS Session.", e);
            } finally {
                session = null;
            }
        }
    }
}
