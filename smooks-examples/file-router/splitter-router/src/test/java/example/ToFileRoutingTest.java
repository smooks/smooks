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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.milyn.Smooks;
import org.milyn.StreamFilterType;
import org.milyn.FilterSettings;
import org.milyn.container.ExecutionContext;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ToFileRoutingTest {

    private File targetDir = new File("target/orders");
    private File file1 = new File(targetDir, "order-231-1.xml");
    private File file2 = new File(targetDir, "order-231-2.xml");
    private File file3 = new File(targetDir, "order-231-3.xml");
    private File file4 = new File(targetDir, "order-231-4.xml");
    private File file5 = new File(targetDir, "order-231-5.xml");
    private File file6 = new File(targetDir, "order-231-6.xml");
    private File file7 = new File(targetDir, "order-231-7.xml");
    private File listFile = new File(targetDir, "order-231.lst");

    @Before
    public void setUp() throws Exception {
        deleteFiles();
    }

    @After
    public void tearDown() throws Exception {
        deleteFiles();
    }

    private void deleteFiles() {
        file1.delete();
        file2.delete();
        file3.delete();
        file4.delete();
        file5.delete();
        file6.delete();
        file7.delete();
        listFile.delete();
    }

    public void test_dom() throws IOException, SAXException {
        test(StreamFilterType.DOM);
    }

    public void test_sax() throws IOException, SAXException {
        test(StreamFilterType.SAX);
    }

    public void test(StreamFilterType filterType) throws IOException, SAXException {
        startSmooksThread(filterType);

        // The highWaterMark is set to 3 in the smooks config...
        waitForFile(file1, 5000);
        waitForFile(file2, 5000);
        waitForFile(file3, 5000);

        sleep(500);
        // file4 shouldn't be there...
        assertTrue("file4 exists!", !file4.exists());

        // delete file1 and file4 should appear then...
        file1.delete();
        waitForFile(file4, 5000);
        // file4 should be there...
        assertTrue("file4 doesn't exists!", file4.exists());

        sleep(1000);
        // file5 shouldn't be there...
        assertTrue("file5 exists!", !file5.exists());

        // delete file2, file3, file4 and file5, file6 and file7 should appear then...
        file2.delete();
        file3.delete();
        file4.delete();
        sleep(2000);
        waitForFile(file7, 5000);
        assertTrue(file5.exists());
        assertTrue(file6.exists());
        assertTrue(file7.exists());
        assertTrue(listFile.exists());
    }

    private void startSmooksThread(StreamFilterType filterType) {
        SmooksThread thread = new SmooksThread(filterType);

        thread.start();
        while(!thread.running) {
            sleep(100);
        }
    }

    private void waitForFile(File file, int maxWait) {
        long start = Math.max(500, System.currentTimeMillis());

        while(!file.exists() && (System.currentTimeMillis() < start + maxWait)) {
            sleep(100);
        }
    }

    private void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private class SmooksThread extends Thread {
        boolean running = false;
        private StreamFilterType filterType;

        public SmooksThread(StreamFilterType filterType) {
            this.filterType = filterType;
        }

        public void run() {
            Smooks smooks = null;

            try {
                try {
                    smooks = new Smooks(new FileInputStream("smooks-config.xml"));
                } catch (IOException e) {
                    fail(e.getMessage());
                } catch (SAXException e) {
                    fail(e.getMessage());
                }

                ExecutionContext execCtx = smooks.createExecutionContext();
                //execCtx.setEventListener(new HtmlReportGenerator("/zap/x.html"));
                smooks.setFilterSettings(new FilterSettings(filterType));
                running = true;
                smooks.filterSource(execCtx, new StreamSource(getClass().getResourceAsStream("order-message.xml")), null);
            } finally {
                smooks.close();
            }
        }
    }
}
