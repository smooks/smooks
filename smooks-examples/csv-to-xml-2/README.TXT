About This Example:
===================
    This example extends the more basic "csv-to-xml" example by
    demonstrating how to perform a transformation on the individual records
    in the CSV set.
	This example also demonstrates how to define string functions in the
	field declaration.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.csv.
        3. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5+
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"