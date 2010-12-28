About This Example:
===================
    This example illustrates the use of the Edifact Java Compiler (EJC).

    EJC generates:
        1. A Java Object model for a given EDI Mapping Model.
        2. A Smooks Java Binding config to populate the Java Object model from an
           instance of the EDI message described by the EDI Mapping Model (see #1 above).
        3. A Factory class that makes it very east to use EJC to bind EDI data
           to Java Object Model.

    The example is split into 2 modules.
        1. The "ejc-gen" module demonstrates how to execute EJC on an EDI Mapping
           Model (for asn Order message) using the EJC Maven Plugin, which generates
           the artifacts listed above and then jars them up and installs in the
           local Maven Repository.
        2. The "ejc-use" module demonstrates how to use the EJC Generated artifacts
           via the OrderFactory class.

    See:
        1. The Maven POM in "ejc-gen".  This demos how to use the EJC Maven Plugin.
        2. The Main class in "ejc-use".  This demos how to use the EJC generated artifacts.
        3. User Guide: http://www.smooks.org/mediawiki/index.php?title=V1.2:Smooks_v1.2_User_Guide#Processing_EDI

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. In "ejc-gen", run "mvn clean install"
        2. In "ejc-use", run "mvn clean install"
        3. In "ejc-use", run "mvn exec:java"