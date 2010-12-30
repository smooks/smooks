Deploying Smooks as an OSGi Bundle
==================================

Requirements
------------
Smooks has a number of dependencies that must be deployed into an OSGI environment. 
The complete list of bundles required for this version of Smooks can be found in target/test-classes/features.xml.

Deploying Smooks
================
1. mvn install
2. copy all the dependencies listed in the file target/test-classes/features.xml to your OSGI Containers deploy directory.
3. copy target/milyn-smooks-all-<version>.jar to your OSGi containers deploy directory.

Apache Karaf/Apache ServiceMix 4.x
==================================
You can install the target/test-classes/feature.xml file directly into Karaf by using:
karaf@root> features:addUrl  file:///path/to/smooks/smooks-all/target/test-classes/features.xml
karaf@root> features:install smooks


