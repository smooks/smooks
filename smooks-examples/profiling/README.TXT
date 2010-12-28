About This Example:
===================
    This is a simple example that illustrates how Smooks can be
    used to apply "profile" based transformations on a message.

    Using profiles, you can share common transformation configurations
    across all messages that share the same profiles.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.xml.
        3. smooks-config.xml.  Pay specific attention to the "profile"
           info in the config.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"