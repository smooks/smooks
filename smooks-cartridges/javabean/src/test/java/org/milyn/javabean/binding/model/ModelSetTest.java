package org.milyn.javabean.binding.model;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ModelSetTest extends TestCase {

    public void test_01() throws IOException, SAXException {
        test("config-01.xml", true);
    }

    public void test_02() throws IOException, SAXException {
        test("config-02.xml", false);
    }

    public void test(String config, boolean isBinding) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));

        smooks.createExecutionContext();
        ModelSet beanModel = ModelSet.get(smooks.getApplicationContext());

        assertEquals("isBinding test failed", isBinding, beanModel.isBindingOnlyConfig());
    }
}
