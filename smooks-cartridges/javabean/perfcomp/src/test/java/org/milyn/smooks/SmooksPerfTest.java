/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.smooks;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.milyn.Order;
import org.milyn.Smooks;
import org.milyn.TestConstants;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.context.BeanIdStore;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksPerfTest extends TestCase {

    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));

        for(int i = 0; i < TestConstants.NUM_WARMUPS; i++) {
            JavaResult javaResult = new JavaResult();
            smooks.filterSource(new StreamSource(TestConstants.getMessageReader()), javaResult);
        }

        long start = System.currentTimeMillis();
        JavaResult javaResult = null;
        NoddyObserver nobserver = new NoddyObserver();
        for(int i = 0; i < TestConstants.NUM_ITERATIONS; i++) {
        	ExecutionContext execContext = smooks.createExecutionContext();
        	BeanContext beanCtx = execContext.getBeanContext();
        	
        	for(int ii = 0; ii < 15; ii++) {
        		beanCtx.addObserver(nobserver);
        	}
        	
            javaResult = new JavaResult();
            smooks.filterSource(execContext, new StreamSource(TestConstants.getMessageReader()), javaResult);
        }
        
        System.out.println("Smooks took: " + (System.currentTimeMillis() - start));
        Order order = (Order) javaResult.getBean("order");
        if(order != null) {
        	System.out.println("Num order items: " + order.getOrderItems().size());
        }
    }
    
    class NoddyObserver implements BeanContextLifecycleObserver {

    	private BeanId beanId = new BeanId((BeanIdStore)null, 0, null);
    	
		public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
			if(event.getBeanId() == beanId && event.getLifecycle() == BeanLifecycle.ADD) {
				int s = beanId.getIndex();
				s = s++;
			}
		}    	
    }
}
