About This Example
==================

This is a very simple example that illustrates the usage of SmooksComponent

See:

* The input message in `input-message.xml`
* smooks configuration in `smooks-config.xml`

How to Run?
===========

Requirements:

* JDK 1.5
* Maven 2.x (http://maven.apache.org/download.html)

Running:

    > mvn clean install
    > mvn camel:run

Run in Karaf 2.2.x or ServiceMix 4.x
====================================

Install and configure Karaf 2.2.x or ServiceMix 4.x
---------------------------------------------------

1. Install [Karaf 2.2.x](http://karaf.apache.org/index/community/download.html)
2. Start Karaf:

    > <KARAF_HOME>/bin/karaf
    > tail -f <KARAF_HOME>/data/log/karaf.log

Deploy the example (Karaf Admin Shell)
--------------------------------------

    karaf@root> features:addUrl mvn:org.milyn/milyn-smooks-example-camel-unedifact-to-xml/1.6-SNAPSHOT/xml/features
    karaf@root> features:install smooks-example-camel-unedifact-to-xml

Run the example (Command Prompt)
--------------------------------

    > cp input-dir/input-message.txt <KARAF_HOME>/input-dir
