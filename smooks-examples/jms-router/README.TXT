About This Example:
===================
     This example illustrates how Smooks can be used to split
     a message into smaller messages and route the smaller messages
     to a JMS Destination.

     This example uses Smooks to process an input Order message, splitting
     it into smaller Order Item messages and routing them to a JMS Destination.

     See:
        1.  The Smooks configuration in splitter-router/smooks-config.xml.
            Specifically, take a look at the JMSRouter resource
            configuration and how it uses the "highWaterMark" param to throttle
            split file generation to the JMS Queue.
        2.  The "Main" class in splitter-router/src/main/java/example/Main.java.
            This is the class that executes smooks to split Order messages
            into smaller Order Item messages.
        3.  The "Main" class in jms-consumer/src/main/java/example/Main.java.
            This is the class that "consumes" the order-item messages as they are
            delivered to the JMS Queue.
        4.  The input-message.xml file in the root folder of the example.

How to Run?
===========
     Requirements:
        1.  JDK 1.5
        2.  Maven 2.x (http://maven.apache.org/download.html)

     Running:
        1.  In the "jms-router" directory, run "mvn clean install".
        2.  Open a   new console window ("Window 1")
        3.  In "Window 1", change directory into the "jms-provider" directory.
        4.  In "Window 1", run "mvn activemq:run".
        5.  Open a new console window ("Window 2")
        6.  In "Window 2", change directory into the "jms-consumer" directory.
        7.  In "Window 2", run "mvn exec:java".
        8.  Open a new console window ("Window 3")
        9.  Arrange "Window 1", "Window 2" and "Window 3" such that the contents
            of all windows is visible at the same time.
        10. In "Window 3", change directory into the "splitter-router" directory.
        11. In "Window 3", run "mvn exec:java".  This executes a Smooks instance to
            process the input-message.xml. Follow the on-screen instructions
            and you will see Smooks throttling against the High Water Mark ("Window 3")
            while the messages are being slowly consumed by the jms-consumer ("Window 2").

	 Monitoring (optional):
		1.  export MAVEN_OPTS=-Dcom.sun.management.jmxremote
		2.  "jconsole"
