About This Example:
===================
    This is an example of how to use Smooks and Apache Camel to process a UN/EDIFACT message
    Interchange, splitting the individual interchange messages out by
    binding them to their EJC generated object models and routing the
    objects to separate POJO services (over JMS) for processing.

    What this example illustrates is:
        1.  How to configure the <unedifact:reader> in a Smooks configuration (smooks-config.xml).
        2.  How to configure (by import) binding configurations for the EJC generated object models
            for the UN/EDIFACT directory being processed.

    See:
        1.  The Camel configuration files for the 3 Camel Services that are run separately.  These
            are located in src/main/resources/META-INF.
        2.  The smooks configuration file (used by the Camel "unedifact-server" service) is located in the
            examples root folder, just like all other examples.
        3.  The POJOs that act as the Services for processing UN/EDIFACT Order and Invoice messages.
            These are located in src/main/java.
        4   The sample UN/EDIFACT interchange messages in the "sample-data" folder.  These will be
            copied into the "edi-in" folder when running the example.  See below.
        5.  See the User Guide:
            http://www.smooks.org/mediawiki/index.php?title=V1.4:Smooks_v1.4_User_Guide#UNEdifactReader_Reader_Configuration
        6.  Screencast: http://www.screencast.com/t/Pm3eAIR8DSd

How to Run?
===========
    Requirements:
        1.  JDK 1.5+
        2.  Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1.  Open one terminal window (Window 1)....
            Start the UN/EDIFACT Server.  This Camel Service polls the "edi-in"
            folder for UN/EDIFACT message files.  The files are routed into the
            Smooks Camel Component, which binds the individual UN/EDIFACT ORDERS
            and INVOIC messages in the Interchange into their appropriate EJC
            generated "Orders" and "Invoic" Object models. These Java Objects are
            then routed different JMS Queues for processing.  The OrdersService
            and InvoiceService Services are attached to these JMS endpoints, processing
            the Orders and Invoic object instances.

                 "mvn exec:java -PUnedifactServer"

        2.  Open another terminal window (Window 2)....
            Start the "OrdersService" Service.  This Service will process UN/EDIFACT ORDERS
            messages that have been split out and bound into Orders Object model instances.
            The OrderService returns an Ordrsp (UN/EDIFACT ORDRSP - Purchase Order Response)
            Object, which gets serialized to a UN/EDIFACT stream (by the Camel Type Conversions
            mechanism) and routed to the target/edi-out folder.

                 "mvn exec:java -POrdersService"

        3.  Open another terminal window (Window 3)....
            Start the "InvoiceService" Service.  This Service will process UN/EDIFACT INVOIC
            messages that have been split out and bound into Invoic Object model instances.

                 "mvn exec:java -PInvoiceService"

        4.  Open another terminal window (Window 4)....
            Start the "DispatchAdviceService" Service.  This Service will process UN/EDIFACT DESADV
            (Dispatch Advice) messages that have been split out of the UN/EDIFACT interchange messages
            and routed to file, from where this Service picks up and processes the files.

                 "mvn exec:java -PDispatchAdviceService"

        5.  Copy files from the "sample-data" folder into the "edi-in" folder.  Then watch
            the Orders (Java), Invoic (Java) and DESADV (XML) messages being processed by
            the OrdersService (Window 2), the InvoiceService (Window 3) and the
            DispatchAdviceService (Windows 4).  Note that these messages are split out and
            routed to the Services as the UN/EDIFACT message is being filtered/streamed
            i.e. not as a batch operation at the end.  This allows huge messages to be
            processed, if needed.