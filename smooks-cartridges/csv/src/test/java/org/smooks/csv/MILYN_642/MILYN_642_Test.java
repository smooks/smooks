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
package org.smooks.csv.MILYN_642;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.SmooksUtil;
import org.smooks.container.ExecutionContext;
import org.smooks.csv.CSVRecordParserConfigurator;
import org.xml.sax.SAXException;

/**
 * @author Ken Hill
 */
public class MILYN_642_Test {
	// Default escape char
    @Test
    public void test_01() throws SmooksException, IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-01-config.xml"));

        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("test-01-data.csv"), smooks);

        assertEquals(
            "<csv-set><csv-record number=\"1\"><name>Erika Mustermann</name><email>e.m@ex.org</email></csv-record><csv-record number=\"2\"><name>Max \"The Man\" Mustermann</name><email>m.m@ex.org</email></csv-record></csv-set>",
            result);
    }

	// Custom escape char, xml config
    @Test
    public void test_02() throws SmooksException, IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test-02-config.xml"));

        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("test-02-data.csv"), smooks);

        assertEquals(
            "<csv-set><csv-record number=\"1\"><name>Erika Mustermann</name><email>e.m@ex.org</email></csv-record><csv-record number=\"2\"><name>Max \"The Man\" Mustermann</name><email>m.m@ex.org</email></csv-record></csv-set>",
            result);
    }
	
	// Custom escape, programmatic config
    @Test
    public void test_03() throws SmooksException, IOException, SAXException {
        Smooks smooks = new Smooks();
		
        smooks.setReaderConfig(
            new CSVRecordParserConfigurator("name,email")
                .setEscapeChar('~'));

        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("test-02-data.csv"), smooks);
		
        assertEquals(
            "<csv-set><csv-record number=\"1\"><name>Erika Mustermann</name><email>e.m@ex.org</email></csv-record><csv-record number=\"2\"><name>Max \"The Man\" Mustermann</name><email>m.m@ex.org</email></csv-record></csv-set>",
            result);
    }
}
