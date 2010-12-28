About This Example:
===================
    This example illustrates how to use the Smooks CSS Cartridge to
    analyse the CSS components of a HTML document.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. input.html.  Contains inlined and linked CSS.
        3. style.css.  Linked into input.html.
        4. smooks-config.xml.

    After running, you should see the following output to the console:

        ================== CSS Properties ==================

        /html:            'padding':   50
        /html/body/p:     'margin':    10
        /html/body/p:     'font-size': 4em
        /html/body/h1/p:  'font-size': 2em
        
        ====================================================

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"