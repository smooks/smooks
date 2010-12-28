About This Example:
===================
    This is a very simple example that illustrates how Smooks can
    be used to perform fragment based transforms using Groovy.

    In this example, we perform a DOM based manipulation of a Shopping
    list message.  SAX based scripting is also supported.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.xml.
        3. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"