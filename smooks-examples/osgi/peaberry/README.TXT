About This Example:
===================
	This examples demonstrates the usage of Peaberry which is an extension library for Google Guice.
	Peaberry does what SpringDM does for the Spring dependency injection framework. Among other things
	Peabery enables you to inject OSGi services using plain Guice annotations.

	This example shows how the Smooks service can be injected into a simple Pojo class using Peaberry. 
	Currently this example uses a bundle activator but this will change and be simplfied in the near 
	future when the peaberry activation feature is available in a maven repository.

    See:
        1. The "PeaberryActivator" class in src/main/java/example/activator.PeaberryActivator.java.
		2. pom.xml, notice that the Bundle-Activator is set to PeaberryActivator
		3. The "Pojo" class in src/main/java/example/Pojo.java".
        4. The input message in input-message.xml.
        5. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)
		3. Smooks-all bundle installed into an OSGi container, see 

    Running:
        1. "mvn clean install"
		3. copy guice-2.0.jar, peaberry-1.1.1.jar to your OSGi container (from target/dependencies)
        2. copy target/milyn-smooks-example-peaberry-1.3.jar to your OSGi container

