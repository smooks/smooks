About This Example:
===================
    This example illustrates how Smooks can be used to combine Groovy scripting
    with XSLT to perform an Order message transform.

    In this example, Smooks applies a piece of Groovy script to a message "date"
    fragment.  This script parses the date, re-adding it to the message as a
    set of nodes containing the date's consituent parts.  The main transformation
    is then carried out on the whole document ("#document") using XSLT.

    What this illustrates is how easy it is to combine the powers of different
    technologies under a single Smooks transform.  We use Groovy to pre-process
    a message fragment that is very difficult to process using XSLT
    (i.e. a date string).  We then use XSLT to do what it's good at i.e. templating.

    Of course you can get similar functionality to this using XSLT Extensions.
    However, the difference is that this approach allows you to maintain portability
    across XSLT Processors.  Note below how you can switch XSLT Processors.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.xml.
        3. The code in the src/main/java folder tree (.java, .groovy, .xsl).
        4. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"
        3. Modify pom.xml to switch XSLT Processors.
        4. Re-run "mvn exec:java".