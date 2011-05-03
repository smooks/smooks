About This Example:
===================
    This example illustrates how to configure Smooks to
    process variable record CSV messages, where the first CSV field name
    denotes the name for that record.

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