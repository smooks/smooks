/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.broker;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import org.drools.core.time.TimerService;
import org.drools.core.time.impl.JDKTimerService;
import org.drools.examples.broker.events.EventFeeder;
import org.drools.examples.broker.events.SmooksEventSource;
import org.drools.examples.broker.model.CompanyRegistry;
import org.drools.examples.broker.ui.BrokerWindow;

import javax.swing.*;
import java.util.Locale;

/**
 * This is the main class for the broker example.
 * 
 * @author etirelli
 */
public class Main {

    /**
     * @param args
     * @throws UnsupportedLookAndFeelException 
     */
    public static void main(String[] args) throws Exception {
        // set up and show main window
        UIManager.setLookAndFeel( new Plastic3DLookAndFeel() );
        Locale.setDefault( Locale.US );
        CompanyRegistry registry = new CompanyRegistry();
        BrokerWindow window = new BrokerWindow( registry.getCompanies() );
        window.show();
        //Thread.sleep( 10000 );
        Broker broker = new Broker( window, registry );
        
        TimerService clock = new JDKTimerService(1);

//        StockTickPersister source = new StockTickPersister();
//        source.openForRead( new InputStreamReader( Main.class.getResourceAsStream( "/stocktickstream.dat" ) ),
//                            System.currentTimeMillis() );

        SmooksEventSource source = new SmooksEventSource();
        source.processFeed(Main.class.getResourceAsStream( "/stocktickstream.dat" ));

        EventFeeder feeder = new EventFeeder(clock, source, broker );
        feeder.feed();

    }
}
