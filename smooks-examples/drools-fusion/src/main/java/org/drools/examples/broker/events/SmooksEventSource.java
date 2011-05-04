/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.drools.examples.broker.events;

import org.drools.examples.broker.model.StockTick;
import org.milyn.Smooks;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksEventSource implements EventSource {

    private Smooks smooks;
    private BlockingQueue<StockTick> inQueue = new SynchronousQueue<StockTick>();

    public SmooksEventSource() throws IOException, SAXException {
        smooks = new Smooks("./smooks-config.xml");
        smooks.getApplicationContext().addBeanContextLifecycleObserver(new BeanContextObserver());
    }

    public void processFeed(final InputStream tickerFeed) {
        new Thread() {
            @Override
            public void run() {
                smooks.filterSource(new StreamSource(tickerFeed));
            }
        }.start();
    }

    public boolean hasNext() {
        // Returning true because otherwise it will exit immediately...
        return true;
    }

    public Event<?> getNext() {
        try {
            StockTick stockTick = inQueue.take();
            return new EventImpl<StockTick>(stockTick.getTimestamp(), stockTick);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Listen for StockTicker beans being created in Smooks BeanContexts and add them to the
     * StockTick inQueue...
     */
    private class BeanContextObserver implements BeanContextLifecycleObserver {

        public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
            if(event.getLifecycle() == BeanLifecycle.END_FRAGMENT) {
                if(event.getBeanId().getName().equals("stockTick")) {
                    try {
                        inQueue.put((StockTick) event.getBean());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
