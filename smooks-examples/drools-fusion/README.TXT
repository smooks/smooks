About This Example:
===================
    This is an example of how one can use Smooks to "feed" Drools Fusion with CEP
    events.  It's a full blown copy of the Drools Fusion Stock Ticker example from
    the Drools project.

    The only real changes were the addition of the SmooksEventSource class, which
    basically acts as a replacement to for the StockTickPersister class.  There
    are also changes to the Main class, removing StockTickPersister and plugging in
    the new SmooksEventSource as the Event Source to the Event Feeder.

    SmooksEventSource creates a Smooks instance that is configured with the smooks-config.xml
    file located in the root of the example.  Smooks is configured to parse the same CSV
    stock ticker feed as is used by the original example copied from Drools and produce
    StockTick instances from the CSV records.

    The main point of integration is how the SmooksEventSource listens to the Smooks
    runtime BeanContext for creation of StockTick object instances.  It listens using the
    BeanContextObserver class (inner class of SmooksEventSource).  The SmooksEventSource
    can process multiple feeds concurrently, listening to them all using the same
    BeanContextObserver instance.

    See:
        1. The "Main" class in src/main/java/org/drools/examples/broker/Main.java.
        2. The "SmooksEventSource" class in
           src/main/java/org/drools/examples/broker/events/SmooksEventSource.java.
        2. The "BeanContextObserver" class in
           src/main/java/org/drools/examples/broker/events/SmooksEventSource.java.
        4. smooks-config.xml.

How to Run?
===========
    Requirements:
        1. JDK 1.5
        2. Maven 2.x (http://maven.apache.org/download.html)

    Running:
        1. Run the Main class in your IDE, OR.... however it is run in the Drools project :)