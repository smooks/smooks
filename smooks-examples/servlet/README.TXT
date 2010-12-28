About This Example:
===================
    This example illustrates how Smooks can be deloyed inside a Servlet Container
    (as a Servlet filter).  It also illustrates how Smooks can use the Tinak (http://milyn.codehaus.org/Tinak)
    module for performing useragent/browser recognition, making it possible to use Smooks
    profiling to perform useragent specific transforms on content being returned by
    the Servlet Container.

    This example is very trivial.  It's a simple webapp with an index.html (see src/webapp). Requesting
    the index.html page through Firefox or Internet Explorer yields slightly different results.

    The Smooks configuration (war/src/main/webapp/WEB-INF/smooks-config.xml) specifies
    some simple browser specific transforms for Firefox and Internet Explorer.  This config
    also specifies the browser profiles.

    Also Note:
    1.  The useragent/browser recognition configurations are specified in
        war/src/main/webapp/WEB-INF/device-ident.xml.  See http://milyn.codehaus.org/Tinak.
    2.  The SmooksServletFilter is configured into the webapp in
        war/src/main/webapp/WEB-INF/web.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. Change directory to the war directory
        2. "mvn clean install jetty:run"
        3. Open Firefox and Internet Explorer browsers and browse to 
           http://localhost:19191/example-servlet/.  The content should
           appear differently in both.

