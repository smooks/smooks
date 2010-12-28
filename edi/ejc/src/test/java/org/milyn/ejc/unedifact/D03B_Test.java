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
package org.milyn.ejc.unedifact;

import junit.framework.TestCase;
import org.milyn.ect.ECTUnEdifactExecutor;
import org.milyn.ejc.EJCExecutor;
import org.milyn.ejc.IllegalNameException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class D03B_Test extends TestCase {

    private static final String MAPPINGS_JAR = "target/D03B_Test/d03b-mappings.jar";
    File mappingModelJar = new File(MAPPINGS_JAR);

    public void test() throws ClassNotFoundException, IOException, SAXException, IllegalNameException {
        compileUnEdifactDef();

        EJCExecutor ejc = new EJCExecutor();

        ejc.setMessages("PAXLST");
        ejc.setDestDir(new File("target/D03B_Test/java"));
        ejc.setEdiMappingModel(MAPPINGS_JAR);
        ejc.setPackageName("org.smooks.test");

        ejc.execute();
    }

    private void compileUnEdifactDef() {
        ECTUnEdifactExecutor executor = new ECTUnEdifactExecutor();

        executor.setUrn("org.milyn.smooks.unedifact:d03b:1.0");
        executor.setUnEdifactZip(new File("src/test/resources/d03b.zip"));
        mappingModelJar.getParentFile().mkdirs();
        mappingModelJar.delete();
        executor.setMappingModelZip(mappingModelJar);

        executor.execute();
    }
}
