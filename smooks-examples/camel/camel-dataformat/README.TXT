About This Example:
===================
 	This is a very simple example that illustrates the usage of SmooksDataFormat. A DataFormat
 	is a class that implements Camel's org.apache.camel.spi.DataFormat.

	The following features are demonstrated:
	   * using SmooksDataFormat in a Camel unmarshall call using Java DSL
	   * using SmooksDataFormat in a Camel unmarshall call using Spring DSL
	   
	See smooks-config.xml for inline comments.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.edi.
        3. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"

Run in Servicemix 4.2
=====================

Install and configure Servicemix 4.2.0
--------------------------------------
1. Install Servicemix 4.2 (http://servicemix.apache.org/servicemix-420.html)
2. Start Servicemix:
	${SERVICEMIX_HOME}/bin/servicemix
3. tail -f ${SERVICEMIX_HOME}/data/log/servicemix.log


Deploy Smooks OSGi Bundle
-------------------------
1. Deploy Smooks OSGi bundle
	a) cd trunk/smooks-all
	b) mvn install
	c) copy target/milyn-smooks-all-<version>.jar ${SERVICEMIX_HOME}/deploy
	d) Download http://repository.springsource.com/maven/bundles/external/org/jaxen/com.springsource.org.jaxen/1.1.1/com.springsource.org.jaxen-1.1.1.jar
	e) copy com.springsource.org.jaxen-1.1.1.jar to ${SERVICEMIX_HOME}/deploy
2. Deploy the example
	cp target/milyn-smooks-example-camel-dataformat-1.0.jar to ${SERVICEMIX_HOME}/deploy

Run the example
---------------
1. cp input-message.xml input-dir
2. check ${SERVICEMIX_HOME}/data/log/servicemix.log for the log messages

