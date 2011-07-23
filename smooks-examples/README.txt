Building and Running The Examples:
==================================
In order to build any of the examples, you need to install Maven (http://maven.apache.org/).
We use Maven v2.0.8, so if you're using a different version and have any issues, please try
v2.0.8 before raising a JIRA (http://jira.codehaus.org/browse/MILYN).

All of the examples are built and run in the same way:

To Build: "mvn -s [path to settings.xml file in examples root] clean install"
To Run:   "mvn exec:java"

Online Documentation:
=====================
Smooks User Guide:     http://milyn.codehaus.org/Smooks+User+Guide
Example Documentation: http://milyn.codehaus.org/Tutorials

Smooks Execution Report:
========================
Most of the examples generate a Smooks Execution Report in the "target/report" folder
of the example.  This can be a useful tool for comprehending the processing performed
by Smooks.