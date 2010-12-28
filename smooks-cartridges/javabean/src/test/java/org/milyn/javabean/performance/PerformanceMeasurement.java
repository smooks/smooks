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

package org.milyn.javabean.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaResult;
import org.milyn.util.ClassUtil;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class PerformanceMeasurement {

	private static final Log logger = LogFactory.getLog(PerformanceMeasurement.class);

	/**
	 * @param args
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, SAXException {
		try {
			logger.info("Start");

			boolean simple = false;

			String file = simple ? "/smooks-config-simple.xml" : "/smooks-config-orders.xml";

			String name = simple ? "simple" : "orders";

			test(file, name + "-1.xml", name + "-1.xml");
			test(file, name + "-1.xml", name + "-50.xml");
			test(file, name + "-1.xml", name + "-500.xml");
			test(file, name + "-1.xml", name + "-5000.xml");
			test(file, name + "-1.xml", name + "-50000.xml");
			test(file, name + "-1.xml", name + "-500000.xml");



		} catch (Exception e) {
			logger.error("Exception", e);
		}
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 */
	private static void test(String configFile, String warmupFilename, String inFilename) throws IOException, SAXException {
		Long beginTime = System.currentTimeMillis();

		String packagePath = ClassUtil.toFilePath(PerformanceMeasurement.class.getPackage());
		Smooks smooks = new Smooks(packagePath + configFile);

		ExecutionContext executionContext = smooks.createExecutionContext();

		JavaResult result = new JavaResult();

		File warmupFile = new File(warmupFilename);
		File inFile  = new File(inFilename);
		Long endTime = System.currentTimeMillis();

		logger.info(inFile + " initialize: " + (endTime - beginTime) + "ms");

		beginTime = System.currentTimeMillis();

		smooks.filterSource(executionContext, new StreamSource(new InputStreamReader(new FileInputStream(warmupFile))), result);

		endTime = System.currentTimeMillis();

		logger.info(inFile + " filter warmup: " + (endTime - beginTime) + "ms");

		beginTime = System.currentTimeMillis();

		smooks.filterSource(executionContext, new StreamSource(new InputStreamReader(new FileInputStream(inFile))), result);

		endTime = System.currentTimeMillis();

		logger.info(inFile + " filter run: " + (endTime - beginTime) + "ms");

		System.gc();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}
}
