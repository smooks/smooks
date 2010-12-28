About This Example:
===================
    This example illustrates how to perform a model driven transform using the
    Smooks Javabean and Templating Cartridges.  FreeMarker is used to perform
    the templating.

    The example takes a Shipping History message, generates a "model" of that message
    using the <jb:bindings> configs of the Javabean Cartridge.  The populated bean
    context is then used to generate the required output using the FreeMarker templating
    engine.

    One of the more interesting parts of the transformation is where the <trackingNumbers>
    need to be broken out into seperate <trackingNumber> records.  These records are all supplied in
    a single Text node.  The records are separated by newlines (see "input-message.xml").

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.xml.
        3. The model code in the "example.model" package in the src/main/java folder.
        4. The FreeMarker template in the "example.templates" package in the src/main/java folder.
        5. The Smooks configuration in smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"