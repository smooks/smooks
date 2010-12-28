About This Example:
===================
    This example illustrates how Smooks can be used to perform an XML to Java Transform.
    It also shows how the populated Object graph is then accessible outside Smooks via the
    JavaResult instance.

    In reality, the Javabeans feature can be used as is (ala JAXB, XStream etc), or can be
    used to feed data into other fragment transforms e.g. Templating, Scripting or Java
    Transforms.

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
