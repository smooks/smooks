About This Example:
===================
    This example illustrates how to hook the EdiSax (http://milyn.codehaus.org/EdiSax)
    EDI parser into a Smooks based transform (filter operation).  It's another example
    of how non-XML streams can be processed by Smooks (see the "csv-to-xml" example).

    In this example, we simply configure in the EdiSax parser to process the EDI
    stream into XML.  We don't perform any other transforms on the underlying data. For
    an example of how other transform operations can be built on top of this edi-to-xml
    transform, see the "edi-to-java" example.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.edi.
        3. smooks-config.xml.
        4. The EdiSax parser configuration in
           src/main/java/example/edi-to-xml-order-mapping.xml
        5. http://milyn.codehaus.org/EdiSax

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"

