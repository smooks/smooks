About This Example:
===================
    This example is exactly the same as "xml-to-java" accept that it
    uses a "Virtual Model".  This basically means that it uses Maps instead
    of a real object model.

    A Virtual Model is probably of most use in a templating type solution. See
    the "model-driven-basic-virtual" example.

    For more on "Virtual Models" etc, see the Smooks User Guide at
    http://milyn.codehaus.org/Smooks.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.xml.
        3. The code in the src/main/java folder tree (.java).
        4. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"
