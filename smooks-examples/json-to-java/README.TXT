About This Example:
===================
 	This is a very simple example that illustrates the use
 	of the JSON reader within Smooks

	For example purposes we created a JSON structure that can't
	be parsed directly into a SAX stream. Several map keys contain
	whitespaces and there is a key that contains an ampersand.
	Because map key names get converted into XML element names those
	characters are not allowed. This example demonstrates how
	to solve this issue, by configuring the JSON Reader to replace
	those characters before handing them over to the SAX Stream.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.jsn.
        3. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"