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
package example;

import org.apache.activemq.broker.BrokerService;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class Main implements MessageListener
{

    private static final String LISTEN_QUEUE = "smooks.exampleQueue";
    private static int messageCounter;
    private Connection connection;
    private static Main consumerMain;

    public static void main(String[] args) throws Exception {
        consumerMain = new Main();
        consumerMain.setupMessageListener();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                consumerMain.closeConnection();
            }
        });

        Object sleep = new Object();
        synchronized (sleep) {
            sleep.wait();
        }
    }

    private void setupMessageListener() throws NamingException, JMSException {
        InitialContext context = null;
    	try
		{
			context = new InitialContext();
			ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");
	        connection = connectionFactory.createConnection();
	        Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
	        Destination destination = (Destination)context.lookup(LISTEN_QUEUE);
	        MessageConsumer consumer = session.createConsumer( destination );
	        consumer.setMessageListener( this );
	        connection.start();
	        System.out.println("JMS Listener started");
        }
        catch (NamingException e) {
            e.printStackTrace();
            throw e;
        } catch (JMSException e) {
            e.printStackTrace();
            throw e;
        } finally
    	{
    		try { context.close(); } catch (NamingException e) { e.printStackTrace(); }
    	}
    }

    public void onMessage( Message message )
	{
		messageCounter++;
        System.out.println("\n[ Received Message[" + messageCounter + "]");
        try
		{
			System.out.println("\t[JMSMessageID : " +  message.getJMSMessageID() + "]" );
	        System.out.println("\t[JMSCorrelelationID : " +  message.getJMSCorrelationID() + "]" );
	        if ( message instanceof ObjectMessage )
	        {
	            System.out.println("\t[MessageType : ObjectMessage]");
	            System.out.println( "\t[Object : " +  ((ObjectMessage)message).getObject() + "]" );
	        }
	        else if ( message instanceof TextMessage )
	        {
	            System.out.println("\t[MessageType : TextMessage]");
	            System.out.println( "\t[Text : \n" +  ((TextMessage)message).getText() + "]" );
	        }
		} catch (JMSException e)
		{
			e.printStackTrace();
		}
        System.out.println("]");

        // Slow the processing of the messages so as to force the High Water Mark...
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection()
    {
        System.out.println("Closing JMS Listener...");
        try
        {
            if ( connection != null )
                connection.close();
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
    }
}