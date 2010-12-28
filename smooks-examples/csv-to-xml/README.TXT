About This Example:
===================
    This is a very simple example that illustrates how Smooks to
    configure in a non-XML stream reader (CSV) into Smooks.

    This capability enables Smooks to be used for processing any
    hierarchical data, and not just XML.

    The EDI-to-XML and EDI-to-Java examples also illustrate this
    capability, but for an EDI message stream.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.csv.
        3. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"