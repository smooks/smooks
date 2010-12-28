About This Example:
===================
    This example is an extension of the "edi-to-xml" example.  It extends
    that example to include Javabean Object Graph population from the same EDI
    message.

    Something to note when looking at this example is that Smooks doesn't perform
    this EDI-to-Java transform in a 2 step "pipeline" like process.  It doesn't
    generate and intermediate XML, which it then has to parse and process etc.
    Smooks takes the EDI message and, using the EdiSax parser, goes straight
    to a DOM representation of the EDI message.  This is then used by Smooks,
    through the Javabean Cartridge, to populate the Java Object Graph.

    If you haven't already looked at the "javabean-populator" example, you may
    be interested in looking at that before looking at this example.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.edi.
        3. smooks-config.xml.
        4. The EdiSax parser configuration in
           src/main/java/example/edi-to-xml-order-mapping.xml
        5. http://milyn.codehaus.org/EdiSax
        6. The Javabean classes in src/main/java/example/beans.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"