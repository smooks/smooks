About This Example:
===================
    This is an example of how to use Smooks to process a UN/EDIFACT message
    Interchange using EJC generated Java Object models.

    See:
        1.  The "Main" class in src/main/java/example/Main.java.
        2.  The input UN/EDIFACT is in the INVOIC.edi file.  It's an Interchange
            containing just a single INVOIC message.
        3.  The pom.xml file for how to include the binding dependency for the
            interchange version being processed.
        4.  See the User Guide:
            http://www.smooks.org/mediawiki/index.php?title=V1.4:Smooks_v1.4_User_Guide#Reading_and_Writing_UN.2FEDIFACT_with_Java

How to Run?
===========
    Requirements:
        1.  JDK 1.5+
        2.  Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1.  "mvn clean install"
        2.  "mvn exec:java"
