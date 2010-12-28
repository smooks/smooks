About This Example:
===================
    This is a very simple example that illustrates how to
    configure Smooks to process fixed length records into Java objects.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The Customer and Gender Types.
        3. The input message in input-message.txt.
        4. smooks-config.xml.
        5. See how to solve the same use case using a programmatic (non-XML)
           Smooks configuration in the "fixed-length-to-java-programmatic" example.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"