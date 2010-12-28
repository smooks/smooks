About This Usecase::
===================
    This use case transformation has been donated by the Swedish Railway (SJ) where
	it is used to retrieve information about train compositions (rollingstock).
	The data that is the source of the transformation "lives" in a mainframe computer
	and is exported as a batch job to a file. The source data can be found in the file
	input-message.xml

    In this example, we simply configure in the EdiSax parser to process the EDI
    stream into Java Object model.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.edi.
        3. smooks-config.xml.
        4. http://milyn.codehaus.org/EdiSax

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"

Note
===========
We update this usecase to the latest Smooks version.