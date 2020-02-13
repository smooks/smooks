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
package org.milyn.routing.jms.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.jndi.ActiveMQInitialContextFactory;
import org.milyn.util.JNDIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.lang.IllegalStateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class ActiveMQProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQProvider.class);

    public static final String DEFAULT_PROVIDER_URL = "tcp://localhost:61717";

    private Properties jndiProperties;
    private String providerUrl = DEFAULT_PROVIDER_URL;
    private BrokerService brokerService;

    private Connection queueConnection = null;
    private QueueSession queueSession = null;
    private Connection topicConnection = null;
    private TopicSession topicSession = null;

    private List<MessageConsumer> consumers = new ArrayList<MessageConsumer>();

    public ActiveMQProvider() {
        jndiProperties = new Properties();
        jndiProperties.setProperty(Context.INITIAL_CONTEXT_FACTORY, ActiveMQInitialContextFactory.class.getName());
        jndiProperties.setProperty(Context.PROVIDER_URL, DEFAULT_PROVIDER_URL);
    }

    public ActiveMQProvider(final String providerUrl) {
        this();
        jndiProperties.setProperty(Context.PROVIDER_URL, providerUrl);
        this.providerUrl = providerUrl;
    }

    public final Properties getJndiProperties() {
        return (Properties) jndiProperties.clone();
    }

    public final void start() throws Exception {
        assertNotStarted();

        brokerService = new BrokerService();

        // configure the brokerService
        brokerService.setDataDirectoryFile(new File("./target/activeMQData"));
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.addConnector(providerUrl);

        brokerService.start();
    }

    public final void stop() throws Exception {
        assertStarted();

        if (brokerService != null) {
            for(MessageConsumer consumer : consumers) {
                try {
                    consumer.close();
                } catch (Exception e) {
                    LOGGER.debug("Failed to close consumer.", e);
                }
            }

            close(queueConnection, queueSession);
            close(topicConnection, topicSession);

            brokerService.stop();
            brokerService = null;
        }
    }

    public void addQueue(String queueName) {
        assertNotStarted();
        jndiProperties.setProperty("queue." + queueName, queueName);
    }

    public void addTopic(String topicName) {
        assertNotStarted();
        jndiProperties.setProperty("topic." + topicName, topicName);
    }

    private void assertNotStarted() {
        if(brokerService != null) {
            throw new IllegalStateException("Invalid method call after provider has been started.");
        }
    }

    private void assertStarted() {
        if(brokerService == null) {
            throw new IllegalStateException("Invalid method call before provider has been started.");
        }
    }

    public void addQueueListener(String queueName, MessageListener listener) throws JMSException {
        assertStarted();
        if (queueSession == null) {
            createQueueSession();
        }
        addListener(queueName, listener, queueSession);
    }

    public void addTopicListener(String topicName, MessageListener listener) throws JMSException {
        assertStarted();
        if (topicSession == null) {
            createTopicSession();
        }
        addListener(topicName, listener, topicSession);
    }

    private void addListener(String destinationName, MessageListener listener, Session session) throws JMSException {
        Destination destination = lookupDestination(destinationName);
        MessageConsumer consumer = session.createConsumer(destination);

        consumer.setMessageListener(listener);
        consumers.add(consumer);
    }

    private void createQueueSession() throws JMSException {
        try {
            ConnectionFactory connectionFactory = getConnectionFactory();

            queueConnection = ((QueueConnectionFactory) connectionFactory).createQueueConnection();
            queueSession = ((QueueConnection) queueConnection).createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // Start the connection...
            queueConnection.start();
        }
        catch (JMSException e) {
            close(queueConnection, queueSession);
            throw e;
        }
        catch (Throwable t) {
            close(queueConnection, queueSession);
            throw (JMSException) (new JMSException("Unexpected exception while creating JMS Session.").initCause(t));
        }
    }

    private void createTopicSession() throws JMSException {
        try {
            ConnectionFactory connectionFactory = getConnectionFactory();

            topicConnection = ((TopicConnectionFactory) connectionFactory).createTopicConnection();
            topicSession = ((TopicConnection) topicConnection).createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);

            // Start the connection...
            topicConnection.start();
        }
        catch (JMSException e) {
            close(topicConnection, topicSession);
            throw e;
        }
        catch (Throwable t) {
            close(topicConnection, topicSession);
            throw (JMSException) (new JMSException("Unexpected exception while creating JMS Session.").initCause(t));
        }
    }

    private ConnectionFactory getConnectionFactory() throws JMSException {
        String connectionFactoryRuntime = jndiProperties.getProperty(ConnectionFactory.class.getName(), "ConnectionFactory");
        try {
            return (ConnectionFactory) JNDIUtil.lookup(connectionFactoryRuntime, jndiProperties);
        }
        catch (NamingException e) {
            throw (JMSException) (new JMSException("JNDI lookup of JMS Connection Factory [" + connectionFactoryRuntime + "] failed.").initCause(e));
        }
        catch (ClassCastException e) {
            throw (JMSException) (new JMSException("JNDI lookup of JMS Connection Factory failed.  Class [" + connectionFactoryRuntime + "] is not an instance of [" + ConnectionFactory.class.getName() + "].").initCause(e));
        }
    }

    private final Destination lookupDestination(String destinationName) throws JMSException
    {
        try
        {
            return (Destination) JNDIUtil.lookup(destinationName, jndiProperties);
        }
        catch (NamingException e)
        {
            throw (JMSException) (new JMSException("JMS Destination lookup failed.  Destination name '" + destinationName + "'.").initCause(e));
        }
        catch (ClassCastException e)
        {
            throw (JMSException) (new JMSException("JMS Destination lookup failed.  Class [" + destinationName + "] is not an instance of [" + Destination.class.getName() + "].").initCause(e));
        }
    }

    private final void close(Connection conn, Session session) {
        try {
            if (conn != null) {
                conn.stop();
                LOGGER.debug("Stopping JMS Connection for connected session.");
            }
        }
        catch (Throwable e) {
            LOGGER.debug("Failed to stop JMS connection.", e);
            conn = null;
        }
        try {
            if (session != null) {
                session.close();
                LOGGER.debug("Closing JMS Session.");
            }
        }
        catch (Throwable e) {
            LOGGER.debug("Failed to close JMS session.", e);
        }
        finally {
            session = null;
        }
        try {
            if (conn != null) {
                conn.close();
                LOGGER.debug("Closing JMS Connection for connected session.");
            }
        }
        catch (Throwable e) {
            LOGGER.debug("Failed to close JMS connection.", e);
        }
        finally {
            conn = null;
        }
    }
}
