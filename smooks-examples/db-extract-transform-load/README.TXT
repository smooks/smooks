About This Example:
===================
    This example illustrates how Smooks can be used to extract data from an EDI message
    and load this data into a database, all without writing a single line of code
    (except for the "Main" class that executes Smooks).

    This example works by filtering the sample EDI message defined in input-message.edi using
    the EdiSax parser configuration defined in edi-to-sax-order-mapping.xml.  As it filters
    the message, Smooks extracts and binds components from the message into a Virtual Data Model (defined
    in smooks-configs/bindings.xml).  This binding data is then used in a number of SQLExecutor
    resources which are triggered to execute (perform inserts on the database) on passing each
    order and order item in the message.

    An important feature of this example is how it manages its memory footprint.  It processes the message
    using the SAX filter (therefore no in memory DOM), persisting the order and order items without ever
    holding more than the current order's header info, plus the current order-item being processed i.e. it
    never holds a full order in memory.  This means that Smooks can process huge messages (GBs) using
    this processing model.  The same principals can easily applied to splitting, transforming  and routing of
    huge messages.

    See:
        1. The "Main" class in src/main/java/example/Main.java.
        2. The input message in input-message.edi.
        3. The database schema in db-create.script.
        4. smooks-configs/smooks-config.xml (and the other configs in this folder).
        5. The EdiSax parser configuration in edi-to-sax-order-mapping.xml
        6. http://milyn.codehaus.org/EdiSax

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. "mvn clean install"
        2. "mvn exec:java"

