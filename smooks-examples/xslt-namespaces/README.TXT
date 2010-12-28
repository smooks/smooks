About This Example:
===================
    This is a very basic example that illustrates how Smooks can be
    used to apply an XSLT based transform on a message fragments that
	use namespaces. 

    What this example illustrates is:
        1.  A transformation of an xml representing an order format from
            an external system, to a different xml format:
        2.  How to inline an XSLT insdide the Smooks configuration:
                Order.xml -> CanonicalOrderFormat.xml
        3.  How to select values with namespaces.
        4.  It also shows how to exclude the orders namespace from the
            target namespace using the "exclude-result-prefixes" attribute.

    See:
        1.  The "Main" class in src/main/java/example/Main.java.
        2.  The input message in input-message.xml.
        3.  The code in the src/main/java folder tree (.java).
        4.  smooks-config.xml.

How to Run?
===========
    Requirements:
        1.  JDK 1.5
        2.  Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1.  "mvn clean install"
        2.  "mvn exec:java"
