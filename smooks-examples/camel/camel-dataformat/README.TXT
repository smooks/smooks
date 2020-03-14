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

Deploy Smooks OSGi Bundle
-------------------------
1. Deploy Smooks OSGi bundle
    
2. Deploy the example
    karaf@root> osgi:install -s mvn:org.smooks/milyn-smooks-example-camel-dataformat/1.0

Run the example
---------------
1. cp input-message.xml input-dir
2. check ${SERVICEMIX_HOME}/data/log/servicemix.log for the log messages

