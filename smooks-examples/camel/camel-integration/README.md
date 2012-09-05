About This Example:
===================
 	This is a very simple example that illustrates the usage of the Camel SmooksComponent.
 	A SmooksComponent can be used in a Camel route like this:
      <to uri="smooks://file:./smooks-config.xml"/>

	The following features are demonstrated:
	* Routing using Smooks Routing feature. This is an example of a split and route using Smooks.

	See smooks-config.xml for inline comments.
	See src/main/resources/META-INF/spring/camel-context.xml for inline comments.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.xml.
        3. smooks-config.xml.
        4. src/main/resources/META-INF/spring/camel-context.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"
