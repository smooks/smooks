Deploying Smooks as an OSGi Bundle
==================================

Requirements
------------
Smooks requires jaxen 1.1.1 which is available as a bundle from the SpringSource repository and needs to be installed into your OSGi container
1. Download http://repository.springsource.com/maven/bundles/external/org/jaxen/com.springsource.org.jaxen/1.1.1/com.springsource.org.jaxen-1.1.1.jar
2. Copy this to your containers deploy directory

Deploying Smooks
================
1. mvn install
2. copy target/milyn-smooks-all-<version>.jar to your OSGi containers deploy directory.

