About This Example:
===================
    This example is exactly the same as the "model-driven-basic" example except that it
    uses a "Virtual Model".  This basically means that it uses Maps instead
    of a real object model.  You'll see that everything is the same
    as with the "model-driven-basic" example except that it doesn't have
    the model classes defined in the "example.model" package and the
    smooks-config.xml uses java.util.HashMap in place of the model classes
    that are in the "example.model" package in the "model-driven-basic" example.

    For more on "Virtual Models" etc, see the Smooks User Guide at
    http://milyn.codehaus.org/Smooks.    

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.xml.
        3. The FreeMarker template in the "example.templates" package in the src/main/java folder.
        4. The Smooks configuration in smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"