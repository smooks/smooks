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

package org.smooks.templating.xslt;

import junit.framework.ComparisonFailure;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.container.standalone.StandaloneExecutionContext;
import org.smooks.io.StreamUtils;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Compare performance of raw XSLT Vs XSLT via Smooks.
 *
 * @author tfennelly
 */
public class PerformanceComparisonTest {

    private Templates xslTemplate;
    private Smooks smooksTransformer_xsltonly;
    private Smooks smooksTransformer_xsltjava;

    private static final double TEST_DURATION_MINS = 1;
    private static final long SLEEP_DURATION = 10;
    private static final int NUM_TRANS_PER_ITERATION = 1;
    private static boolean compareResults = false;
    private static boolean runStandaloneXslt = false;
    private static boolean streamXslt = false;
    private static boolean serializeSmooksRes = false;
    private static boolean runSmooksXslt_nojava = false;
    private static boolean runSmooksXslt_withjava = false;
    public static boolean isSynchronized = false;

    // * xsl_java_01: Is a single XSLT targeted at the whole message + a java single transform for the order-item element.
    // * xsl_java_02: Performs more Java transformation than xsl_java_01.  Only performs a little bit of
    // XSLT (on the Customer).  Thought this might improve performance more, but no!  I guess it's because
    // the message is so flat.  The XSLT is very simple.  It's very hard to optimize it more.
    private static final String SMOOKS_TRANSFORM_XSL_JAVA_CONFIG = "xsl_java_01";

    @Before
    public void setUp() throws Exception {
        // Initialise the transformers...
        System.setProperty(XslContentHandlerFactory.ORG_MILYN_TEMPLATING_XSLT_SYNCHRONIZED, Boolean.toString(isSynchronized));
        initialiseXsltTransformer();
        smooksTransformer_xsltonly = initialiseSmooksTransformer("xsl_only");
        smooksTransformer_xsltjava = initialiseSmooksTransformer(SMOOKS_TRANSFORM_XSL_JAVA_CONFIG);

        compareResults = false;
        runStandaloneXslt = false;
        streamXslt = false;
        serializeSmooksRes = false;
        runSmooksXslt_nojava = false;
        runSmooksXslt_withjava = false;
        isSynchronized = false;
    }

    @Test
    public void test_comparePerformance_multithreaded_XSLT_DOM() throws TransformerException, IOException, InterruptedException, SAXException {
        runStandaloneXslt = true;
        run_comparePerformance_multithreaded("XSLT Standalone (DOM Result)");
    }

    @Test
    public void test_comparePerformance_multithreaded_SmooksXSLT_unserialized() throws TransformerException, IOException, InterruptedException, SAXException {
        runSmooksXslt_nojava = true;
        run_comparePerformance_multithreaded("Smooks XSLT - Result Unserialized (" + SMOOKS_TRANSFORM_XSL_JAVA_CONFIG + ")");
    }

    @Test
    public void test_comparePerformance_multithreaded_XSLT_Streamed() throws TransformerException, IOException, InterruptedException, SAXException {
        runStandaloneXslt = true;
        streamXslt = true;
        run_comparePerformance_multithreaded("XSLT Standalone (Streamed Result)");
    }

    @Test
    public void test_comparePerformance_multithreaded_SmooksXSLT_serialized() throws TransformerException, IOException, InterruptedException, SAXException {
        runSmooksXslt_nojava = true;
        serializeSmooksRes = true;
        run_comparePerformance_multithreaded("Smooks XSLT - Result Serialized (" + SMOOKS_TRANSFORM_XSL_JAVA_CONFIG + ")");
    }

    public void run_comparePerformance_multithreaded(String title) throws TransformerException, IOException, InterruptedException, SAXException {
        List<PerformanceThread> threadList = new ArrayList<PerformanceThread>();

        threadList.add(new PerformanceThread("perf-inputs-01", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-02", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-03", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-04", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-05", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-06", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-07", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-08", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));

        threadList.add(new PerformanceThread("perf-inputs-09", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        threadList.add(new PerformanceThread("perf-inputs-10", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));
        //threadList.add(new PerformanceThread("perf-inputs-11", xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava));

        System.out.println();
        System.out.println("Starting Threads...");
        System.out.println();
        for(PerformanceThread thread : threadList) {
            thread.start();
            Thread.sleep(100);
        }

        boolean finished = false;
        while(!finished) {
            Thread.sleep(1000);
            finished = true;
            for(PerformanceThread thread : threadList) {
                if(!thread.isFinished()) {
                    finished = false;
                    break;
                }
            }
        }

        System.out.println();
        System.out.println("All Threads complete");

        String resultsMessage = "\n=====================================RESULTS: " + title + "===========================================\n";
        resultsMessage += "**** " + new Date() + "\n";
        resultsMessage += "**** " + xslTemplate.getClass().getName() + "\n";
        for(PerformanceThread thread : threadList) {
            resultsMessage += "" + thread.performancePack.processCount + ","
                                  + thread.performancePack.messageBytesIn.length + ","
                                  + thread.performancePack.totalXsltTime + ","
                                  + thread.performancePack.totalSmooksXsltOnlyTime + ","
                                  + thread.performancePack.smooksXsltOnlyOverheadMillis + ","
                                  + thread.performancePack.smooksXsltOnlyOverheadPercentage + ","
                                  + thread.performancePack.totalSmooksXsltJavaTime + ","
                                  + thread.performancePack.smooksXsltJavaOverheadMillis + ","
                                  + thread.performancePack.smooksXsltJavaOverheadPercentage + "\n";
        }
        resultsMessage += "=======================================================================================\n\n";
        System.out.println(resultsMessage);

        logMessage(resultsMessage);
    }

    private void logMessage(String message) throws IOException {
        File file = new File("./perfcomp.log");
        FileWriter writer = new FileWriter(file, true);

        try {
            writer.write(message);
        } finally {
            writer.close();
        }

        System.out.println("Outputting performance comparison messages to " + file.getAbsolutePath());
    }

    private void initialiseXsltTransformer() throws IOException, TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource xslStreamSource;
        byte[] xslt = StreamUtils.readStream(getClass().getResourceAsStream("xsl_only/transform-order.xsl"));

        xslStreamSource = new StreamSource(new InputStreamReader(new ByteArrayInputStream(xslt), "UTF-8"));
        xslTemplate = transformerFactory.newTemplates(xslStreamSource);
        System.out.println("\n\n***** TransformerFactory: " + transformerFactory.getClass().getName());
        System.out.println("***** XSL Templates Impl: " + xslTemplate.getClass().getName());
        if(!transformerFactory.getFeature(DOMSource.FEATURE)) {
            fail("DOM Node XSL processing not supported");
        }
        System.out.println("\n");
    }

    private Smooks initialiseSmooksTransformer(String smooksConfig) throws IOException, SAXException {
        Smooks smooksTransformer = new Smooks();

        smooksTransformer.addConfigurations("order-transforms", getClass().getResourceAsStream(smooksConfig + "/smooks-transforms.xml"));

        return smooksTransformer;
    }

    private static class PerformanceThread extends Thread {

        private boolean finished = false;
        private String packageName;
        private Templates xslTemplate;
        private Smooks smooksTransformer_xsltonly;
        private Smooks smooksTransformer_xsltjava;
        private PerformancePack performancePack;

        public PerformanceThread(String packageName, Templates xslTemplate, Smooks smooksTransformer_xsltonly, Smooks smooksTransformer_xsltjava) {
            this.packageName = packageName;
            this.xslTemplate = xslTemplate;
            this.smooksTransformer_xsltonly = smooksTransformer_xsltonly;
            this.smooksTransformer_xsltjava = smooksTransformer_xsltjava;
        }

        public void run() {
            try {
                performancePack = PerformancePack.runComparisons(packageName, xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava);
            } catch (Throwable t) {
                t.printStackTrace();
                fail(t.getMessage());
            } finally {
                finished = true;
            }
        }

        public boolean isFinished() {
            return finished;
        }
    }

    private static class PerformancePack {

        private Templates xslTemplate;
        private Smooks smooksTransformer_xsltonly;
        private Smooks smooksTransformer_xsltjava;

        private byte[] messageBytesIn;
        private String messageOutExpected;
        private long totalXsltTime;
        private long totalSmooksXsltOnlyTime;
        private long totalSmooksXsltJavaTime;
        private long smooksXsltOnlyOverheadMillis;
        private double smooksXsltOnlyOverheadPercentage;
        private long smooksXsltJavaOverheadMillis;
        private double smooksXsltJavaOverheadPercentage;
        private int processCount;
        private String packageName;

        private static PerformancePack runComparisons(String packageName, Templates xslTemplate, Smooks smooksTransformer_xsltonly, Smooks smooksTransformer_xsltjava) throws IOException, SAXException, TransformerException, InterruptedException {
            PerformancePack pack = new PerformancePack(packageName, xslTemplate, smooksTransformer_xsltonly, smooksTransformer_xsltjava);

            pack.runComparisons();

            return pack;
        }

        private PerformancePack(String packageName, Templates xslTemplate, Smooks smooksTransformer_xsltonly, Smooks smooksTransformer_xsltjava) throws IOException, SAXException, TransformerConfigurationException {
            messageBytesIn = StreamUtils.readStream(getClass().getResourceAsStream("input_messages/" + packageName + "/order-message.xml"));
            messageOutExpected = new String(StreamUtils.readStream(getClass().getResourceAsStream("input_messages/" + packageName + "/order-expected.xml")));

            messageOutExpected = messageOutExpected.trim();

            this.packageName = packageName;
            this.xslTemplate = xslTemplate;
            this.smooksTransformer_xsltonly = smooksTransformer_xsltonly;
            this.smooksTransformer_xsltjava = smooksTransformer_xsltjava;
        }

        public void runComparisons() throws TransformerException, IOException, InterruptedException, SAXException {
            long endTime = (long) (System.currentTimeMillis() + (1000 * 60 * TEST_DURATION_MINS));

            while(System.currentTimeMillis() <= endTime) {
                if(runStandaloneXslt) {
                    totalXsltTime += performXSLTTransforms();
                    Thread.sleep(SLEEP_DURATION);
                }
                if(runSmooksXslt_nojava) {
                    totalSmooksXsltOnlyTime += performSmooksTransforms(smooksTransformer_xsltonly, "Smooks-Xslt-No-Java");
                    Thread.sleep(SLEEP_DURATION);
                }
                if(runSmooksXslt_withjava) {
                    totalSmooksXsltJavaTime += performSmooksTransforms(smooksTransformer_xsltjava, "Smooks-Xslt-With-Java");
                    Thread.sleep(SLEEP_DURATION);
                }
            }

            // So what's the Smooks overhead for XSLT only ??...
            smooksXsltOnlyOverheadMillis = totalSmooksXsltOnlyTime - totalXsltTime;
            smooksXsltOnlyOverheadPercentage = (((double) smooksXsltOnlyOverheadMillis /(double)totalXsltTime) * 100.0);
            // And what's the Smooks overhead for XSLT + some of it done in Java ??...
            smooksXsltJavaOverheadMillis = totalSmooksXsltJavaTime - totalXsltTime;
            smooksXsltJavaOverheadPercentage = (((double) smooksXsltJavaOverheadMillis /(double)totalXsltTime) * 100.0);

        }

        private long performXSLTTransforms() throws TransformerException, IOException, SAXException {
            // Now test how fast it is...
            long start = System.currentTimeMillis();
            for(int i = 0; i < NUM_TRANS_PER_ITERATION; i++) {
                Object result = applyXslt(streamXslt);
                assertMessageOk(result, "XSLT");
            }

            long time = (System.currentTimeMillis() - start);
            System.out.println("XSLT: " + time);

            return time;
        }

        private long performSmooksTransforms(Smooks smooksTransformer, String name) throws IOException, SAXException {
            // Now test how fast it is...
            long start = System.currentTimeMillis();
            for(int i = 0; i < NUM_TRANS_PER_ITERATION; i++) {
                Document message = applySmooks(smooksTransformer);
                assertMessageOk(message, name);
            }

            long time = (System.currentTimeMillis() - start);
            System.out.println(name + ": " + time);

            return time;
        }

        private Object applyXslt(boolean stream) throws SAXException, IOException, TransformerException {
            Document message;
            Object result = null;

            if(stream) {
                CharArrayWriter writer = new CharArrayWriter();
                if(isSynchronized) {
                    synchronized(xslTemplate) {
                        xslTemplate.newTransformer().transform(new StreamSource(new ByteArrayInputStream(messageBytesIn)), new StreamResult(writer));
                    }
                } else {
                    xslTemplate.newTransformer().transform(new StreamSource(new ByteArrayInputStream(messageBytesIn)), new StreamResult(writer));
                }
                result = writer.toString();
            } else {
                message = XmlUtil.parseStream(new ByteArrayInputStream(messageBytesIn), XmlUtil.VALIDATION_TYPE.NONE, false);
                result = message.createElement("result");
                if(isSynchronized) {
                    synchronized(xslTemplate) {
                        xslTemplate.newTransformer().transform(new DOMSource(message.getDocumentElement()), new DOMResult((Element)result));
                    }
                } else {
                    xslTemplate.newTransformer().transform(new DOMSource(message.getDocumentElement()), new DOMResult((Element)result));
                }
            }

            processCount++;

            return result;
        }

        private Document applySmooks(Smooks smooksTransformer) throws SAXException, IOException {
            Document message = null;

            ExecutionContext executionContext = smooksTransformer.createExecutionContext();
            DOMResult result = new DOMResult();

            if(serializeSmooksRes) {
                smooksTransformer.filterSource((StandaloneExecutionContext) executionContext, new StreamSource(new ByteArrayInputStream(messageBytesIn)),
                                         result);
                message = (Document) result.getNode();
            } else {
                smooksTransformer.filterSource((StandaloneExecutionContext) executionContext, new StreamSource(new ByteArrayInputStream(messageBytesIn)),
                                         null);
            }

            processCount++;
            
            return message;
        }

        private void assertMessageOk(Object result, String name) {
            if(compareResults) {
                String actual = null;

                if(result instanceof Node) {
                    actual = XmlUtil.serialize(((Node)result).getChildNodes());
                } else {
                    actual = (String)result;
                }

                try {
                    assertEquals("Message not as expected.", messageOutExpected, actual.trim());
                } catch(ComparisonFailure e) {
                    System.out.println("[" + name + ":" + packageName + "] expected: " + messageOutExpected);
                    System.out.println("[" + name + ":" + packageName + "] actual: " + actual);
                    throw e;
                }
            }
        }
    }
}
