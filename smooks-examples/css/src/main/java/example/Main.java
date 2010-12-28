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
package example;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.container.ExecutionContext;
import org.milyn.css.CSSAccessor;
import org.milyn.io.StreamUtils;
import org.milyn.magger.CSSProperty;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    protected static byte[] htmlIn = readInputMessage();

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        ExecutionContext executionContext = Main.runSmooksFilter();
        CSSAccessor cssAccessor = CSSAccessor.getInstance(executionContext);
        Document htmlDoc = XmlUtil.parseStream(new ByteArrayInputStream(Main.htmlIn), XmlUtil.VALIDATION_TYPE.NONE, false);
        CSSProperty property;

        System.out.println("\n\n================== CSS Properties ==================\n\n");

        Element htmlElement = (Element) XmlUtil.getNode(htmlDoc, "/html");
        property = cssAccessor.getProperty(htmlElement, "padding");
        System.out.println("/html:            'padding':   " + property.getValue().getIntegerValue());

        Element pElement = (Element) XmlUtil.getNode(htmlDoc, "/html/body/p");
        property = cssAccessor.getProperty(pElement, "margin");
        System.out.println("/html/body/p:     'margin':    " + property.getValue().getIntegerValue());
        property = cssAccessor.getProperty(pElement, "font-size");
        System.out.println("/html/body/p:     'font-size': " + property.getValue());

        Element h1pElement = (Element) XmlUtil.getNode(htmlDoc, "/html/body/h1/p");
        property = cssAccessor.getProperty(h1pElement, "font-size");
        System.out.println("/html/body/h1/p:  'font-size': " + property.getValue());

        System.out.println("\n\n====================================================\n\n");
    }

    protected static ExecutionContext runSmooksFilter() throws IOException, SAXException, SmooksException {

        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks("smooks-config.xml");

        try {
            ExecutionContext executionContext;

            // Set the Resource locator for picking up the <link/> ref'd .css files
            // from the local File sys... Only need to do this because of a bug in
            // the URIResourceLocator (which has been fixed: http://jira.codehaus.org/browse/MILYN-56).
            smooks.getApplicationContext().setResourceLocator(new LocalFilesysLocator());

             // Create an exec context - no profiles....
            executionContext = smooks.createExecutionContext();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the input html through Smooks... we're only analysing...
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(htmlIn)), new DOMResult());

            return executionContext;
        } finally {
            smooks.close();
        }
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(new FileInputStream("input.html"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }
}
