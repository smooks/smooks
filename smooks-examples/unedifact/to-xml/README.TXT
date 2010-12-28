About This Example:
===================
    This is an example of how to use Smooks to process a UN/EDIFACT message
    Interchange and from it generate an XML stream that can then be processed
    by standard XML tools (XSLT, XQuery etc) or by other Smooks components.

    What this example illustrates is:
        1.  How to configure the <unedifact:reader> in a Smooks configuration (smooks-config.xml).
        2.  How to configure the UNEdifactReader programmatically (as an alternative to #1 above).

    See:
        1.  The "Main" class in src/main/java/example/Main.java.  You can easily modify the
            Main class (runSmooksTransform method) to configure the Smooks instance using
            the smooks-config.xml, or, programmatically within the Main class itself.
        2.  The input UN/EDIFACT is in the PAXLST.edi file.  It's an Interchange
            containing just a single PAXLST message.
        3.  See the User Guide:
            http://www.smooks.org/mediawiki/index.php?title=V1.4:Smooks_v1.4_User_Guide#UNEdifactReader_Reader_Configuration

How to Run?
===========
    Requirements:
        1.  JDK 1.5+
        2.  Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1.  "mvn clean install"
        2.  "mvn exec:java"
