About This Example
==================

This is a very simple example that illustrates the usage of `SmooksDataFormat`. A `DataFormat`
is a class that implements Camel's `org.apache.camel.spi.DataFormat`.

This example demonstrates marshalling and unmarshalling within Camel using the
Smooks Camel component in both Camel's Java and Spring XML DSL (Domain Specific Language).

See:

* The input message in `input-message.edi`
* smooks configuration in `smooks-config.xml`

How to Run?
===========

Requirements:

* JDK 1.5
* [Maven 2.x](http://maven.apache.org/download.html)

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

    karaf@root> features:addUrl mvn:org.milyn/milyn-smooks-example-camel-dataformat/1.6-SNAPSHOT/xml/features
    karaf@root> features:install smooks-example-camel-dataformat

Run the example (Command Prompt)
--------------------------------

    > cp input-dir/input-message.edi <KARAF_HOME>/input-dir