About This Example:
===================
     This example illustrates how Smooks can be used to split
     a message into smaller messages and route the smaller messages
     to file.

     This example uses Smooks to process an input Order message, splitting
     it into smaller Order Item messages and routing them to file.

     See:
        1.  The Smooks configuration in splitter-router/smooks-config.xml.
            Specifically, take a look at the FileOutputStreamResource resource
            configuration and how it uses the "highWaterMark" param to throttle
            split file generation.
        2.  The "Main" class in splitter-router/src/main/java/example/Main.java.
            This is the class that executes smooks to split Order messages
            into smaller Order Item messages.
        3.  The "Main" class in file-consumer/src/main/java/example/Main.java.
            This is the class that "consumes" the order-item record files as they are
            produces.
        4.  After running (see below), the generated input message can be seen in
            the "target" directory.

How to Run?
===========
     Requirements:
        1.  JDK 1.5
        2.  Maven 2.x (http://maven.apache.org/download.html)

     Running:
        1.  In the "file-router" directory, run "mvn clean install".
        2.  Open a new console window ("Window 1")
        3.  In "Window 1", change directory into the "file-consumer" directory.
        4.  In "Window 1", run "mvn exec:java".
        5.  Open a new console window ("Window 2")
        6.  In "Window 2", change directory into the "splitter-router" directory.
        7.  Arrange "Window 1" and "Window 2" such that the contents of both
            windows is visible at the same time.
        8.  In "Window 2", run "mvn exec:java". When asked how many order-item
            records you'd like in the generated input message, just enter a small
            number to start with (e.g. 20).  Later, try a HUGE message and
            see how Smooks can process it (e.g. 1,000,000 order-items).
        9.  See Smooks throttling against the High Water Mark ("Window 2")
            while the messages are being slowly consumed by the
            file-consumer ("Window 1").

	 Monitoring (optional):
		1.  export MAVEN_OPTS=-Dcom.sun.management.jmxremote
		2.  "jconsole"

