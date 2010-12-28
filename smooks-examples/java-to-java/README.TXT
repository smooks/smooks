About This Example:
===================
    This example illustrates how Smooks can be used to transform one Java
    Object Graph to another Java Object Graph.

    In this particular example, Smooks uses the SAX processing model, which
    means no intermediate object model is constructed for populating the
    target Java Object Graph.  Instead, we go straight from the source Java Object
    Graph, to a stream of SAX events, which are used to populate the target Java
    Object Graph.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The source Java model in the src/main/java/example/srcmodel package.
        3. The target Java model in the src/main/java/example/trgmodel package.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"
