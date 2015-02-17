package org.milyn.delivery.nested;

import org.junit.Test;
import static org.junit.Assert.*;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NestedExecutionVisitorTest {

	@Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        StringResult result = new StringResult();
        JavaResult beans = new JavaResult();
        final List<String> orderItems = new ArrayList<String>();

        smooks.getApplicationContext().addBeanContextLifecycleObserver(new BeanContextLifecycleObserver() {
            public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
                if(event.getLifecycle() == BeanLifecycle.REMOVE && event.getBeanId().getName().equals("orderItem")) {
                    orderItems.add((String) event.getBean());
                }
            }
        });

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order-message.xml")), result, beans);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("order-message.xml")), new StringReader(result.toString()));

        assertEquals("header", beans.getBean("header"));
        assertEquals("trailer", beans.getBean("trailer"));
        assertEquals(2, orderItems.size());
    }
}
