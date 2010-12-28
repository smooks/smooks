About This Example:
===================
    This is a very basic example that illustrates how Smooks can be
    used to validate xml message fragments. 

    What this example illustrates is:
        1.  How to create a set of Regex and MVEL "ruleBases" (in the "rules" folder).
        2.  How to define localized messages for the rules defined in your ruleBases.
        3.  How to config your ruleBases in your Smooks configuration.
        4.  How to target validation rules at specfic message fragments.

    See:
        1.  The "Main" class in src/main/java/example/Main.java.
        2.  The input message in input-message.xml.
        3.  The code in the src/main/java folder tree (.java).
        4.  smooks-config.xml.
        5.  The rules in rule message bundles in the "rules/i18n" folder.

How to Run?
===========
    Requirements:
        1.  JDK 1.5
        2.  Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1.  "mvn clean install"
        2.  "mvn exec:java"
