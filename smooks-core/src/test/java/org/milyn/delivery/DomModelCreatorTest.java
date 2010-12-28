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
package org.milyn.delivery;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.FilterSettings;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;
import org.milyn.xml.XmlUtil;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DomModelCreatorTest extends TestCase {

    protected void setUp() throws Exception {
        ModelCatcher.elements.clear();
    }

    public void test_sax_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("node-model-01.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);

        ExecutionContext executionContext = smooks.createExecutionContext();
        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("order-message.xml")), null);

        DOMModel nodeModel = DOMModel.getModel(executionContext);

        assertTrue(
                StreamUtils.compareCharStreams(
                "<order>\n" +
                "    <header>\n" +
                "        <date>Wed Nov 15 13:45:28 EST 2006</date>\n" +
                "        <customer number=\"123123\">Joe &gt; the man</customer>\n" +
                "    </header>\n" +
                "    <order-items/>\n" +
                "</order>",
                XmlUtil.serialize(nodeModel.getModels().get("order"), true)));

        assertTrue(
                StreamUtils.compareCharStreams(
                "        <order-item>\n" +
                "            <product>222</product>\n" +
                "            <quantity>7</quantity>\n" +
                "            <price>5.20</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(nodeModel.getModels().get("order-item"), true)));

        // Check all the order-item model added...
        assertEquals(2, ModelCatcher.elements.size());
        assertTrue(
                StreamUtils.compareCharStreams(
                "        <order-item>\n" +
                "            <product>111</product>\n" +
                "            <quantity>2</quantity>\n" +
                "            <price>8.90</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(ModelCatcher.elements.get(0), true)));
        assertTrue(
                StreamUtils.compareCharStreams(
                "        <order-item>\n" +
                "            <product>222</product>\n" +
                "            <quantity>7</quantity>\n" +
                "            <price>5.20</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(ModelCatcher.elements.get(1), true)));
    }

    public void test_sax_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("node-model-02.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);

        ExecutionContext executionContext = smooks.createExecutionContext();
        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("order-message.xml")), null);

        DOMModel nodeModel = DOMModel.getModel(executionContext);

        assertTrue(
                StreamUtils.compareCharStreams(
                "<order>\n" +
                "    <header>\n" +
                "        <date>Wed Nov 15 13:45:28 EST 2006</date>\n" +
                "        <customer number=\"123123\">Joe &gt; the man</customer>\n" +
                "    </header>\n" +
                "    <order-items/>\n" +
                "</order>",
                XmlUtil.serialize(nodeModel.getModels().get("order"), true)));

        assertTrue(
                StreamUtils.compareCharStreams(
                "        <order-item>\n" +
                "            <quantity>7</quantity>\n" +
                "            <price>5.20</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(nodeModel.getModels().get("order-item"), true)));

        assertTrue(
                StreamUtils.compareCharStreams(
                "<product>222</product>",
                XmlUtil.serialize(nodeModel.getModels().get("product"), true)));

        // Check all the order-item model added...
        assertEquals(4, ModelCatcher.elements.size());
        assertTrue(
                StreamUtils.compareCharStreams(
                "<product>111</product>",
                XmlUtil.serialize(ModelCatcher.elements.get(0), true)));
        assertTrue(
                StreamUtils.compareCharStreams(
                "<order-item>\n" +
                "            <quantity>2</quantity>\n" +
                "            <price>8.90</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(ModelCatcher.elements.get(1), true)));
        assertTrue(
                StreamUtils.compareCharStreams(
                "<product>222</product>",
                XmlUtil.serialize(ModelCatcher.elements.get(2), true)));
        assertTrue(
                StreamUtils.compareCharStreams(
                "<order-item>\n" +
                "            <quantity>7</quantity>\n" +
                "            <price>5.20</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(ModelCatcher.elements.get(3), true)));
    }

    public void test_sax_03() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("node-model-02.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);

        ExecutionContext executionContext = smooks.createExecutionContext();
        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("order-message-with-ns.xml")), null);

        DOMModel nodeModel = DOMModel.getModel(executionContext);

        assertTrue(
                StreamUtils.compareCharStreams(
                "<order>\n" +
                "    <header>\n" +
                "        <date>Wed Nov 15 13:45:28 EST 2006</date>\n" +
                "        <customer number=\"123123\">Joe &gt; the man</customer>\n" +
                "    </header>\n" +
                "    <order-items/>\n" +
                "</order>",
                XmlUtil.serialize(nodeModel.getModels().get("order"), true)));

        assertTrue(
                StreamUtils.compareCharStreams(
                "        <ordi:order-item xmlns:ordi=\"http://ordi\">\n" +
                "            <ordi:quantity>7</ordi:quantity>\n" +
                "            <ordi:price>5.20</ordi:price>\n" +
                "        </ordi:order-item>",
                XmlUtil.serialize(nodeModel.getModels().get("order-item"), true)));

        assertTrue(
                StreamUtils.compareCharStreams(
                "<ordi:product xmlns:ordi=\"http://ordi\">222</ordi:product>",
                XmlUtil.serialize(nodeModel.getModels().get("product"), true)));
    }

    public void test_dom() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("node-model-01.xml"));
        ExecutionContext executionContext = smooks.createExecutionContext();

        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("order-message.xml")));

        DOMModel nodeModel = DOMModel.getModel(executionContext);

        assertTrue(
                StreamUtils.compareCharStreams(
                "<order>\n" +
                "    <header>\n" +
                "        <date>Wed Nov 15 13:45:28 EST 2006</date>\n" +
                "        <customer number=\"123123\">Joe &gt; the man</customer>\n" +
                "    </header>\n" +
                "    <order-items>\n" +
                "        <order-item>\n" +
                "            <product>111</product>\n" +
                "            <quantity>2</quantity>\n" +
                "            <price>8.90</price>\n" +
                "        </order-item>\n" +
                "        <order-item>\n" +
                "            <product>222</product>\n" +
                "            <quantity>7</quantity>\n" +
                "            <price>5.20</price>\n" +
                "        </order-item>\n" +
                "    </order-items>\n" +
                "</order>\n",
                XmlUtil.serialize(nodeModel.getModels().get("order"), true)));

        assertTrue(
                StreamUtils.compareCharStreams(
                "        <order-item>\n" +
                "            <product>222</product>\n" +
                "            <quantity>7</quantity>\n" +
                "            <price>5.20</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(nodeModel.getModels().get("order-item"), true)));

        // Check all the order-item model added...
        assertEquals(2, ModelCatcher.elements.size());
        assertTrue(
                StreamUtils.compareCharStreams(
                "        <order-item>\n" +
                "            <product>111</product>\n" +
                "            <quantity>2</quantity>\n" +
                "            <price>8.90</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(ModelCatcher.elements.get(0), true)));
        assertTrue(
                StreamUtils.compareCharStreams(
                "        <order-item>\n" +
                "            <product>222</product>\n" +
                "            <quantity>7</quantity>\n" +
                "            <price>5.20</price>\n" +
                "        </order-item>",
                XmlUtil.serialize(ModelCatcher.elements.get(1), true)));
    }

}
