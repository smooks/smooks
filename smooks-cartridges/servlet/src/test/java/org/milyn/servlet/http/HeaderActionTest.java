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

package org.milyn.servlet.http;

import java.util.Vector;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.Configurator;

import junit.framework.TestCase;

/**
 * 
 * @author tfennelly
 */
public class HeaderActionTest extends TestCase {

	public void testConstructor() {
		SmooksResourceConfiguration unitDef;
		
		unitDef = new SmooksResourceConfiguration("selector", "useragent", "xxx");
		try {
			Configurator.configure(new HeaderAction(), unitDef);
			fail("expected fail - no params set");
		} catch(SmooksConfigurationException s) {
			//OK
		}
		unitDef.setParameter("action", "add");
		try {
            Configurator.configure(new HeaderAction(), unitDef);
			fail("expected fail - only 'action' set");
		} catch(SmooksConfigurationException s) {
			//OK
		}
		unitDef.setParameter("header-name", "namex");
		try {
            Configurator.configure(new HeaderAction(), unitDef);
			fail("expected fail - only 'action' anad 'header-name' set");
		} catch(SmooksConfigurationException s) {
			//OK
		}
		unitDef.setParameter("header-value", "valuex");

		// Should work now
        Configurator.configure(new HeaderAction(), unitDef);
	}
	
	/*
	 * Class under test for boolean equals(Object)
	 */
	public void testEqualsObject() {
		SmooksResourceConfiguration unitDef_01;
		SmooksResourceConfiguration unitDef_02;
		
		unitDef_01 = new SmooksResourceConfiguration("selector", "useragent", "xxx");
		unitDef_01.setParameter("action", "add");
		unitDef_01.setParameter("header-name", "namex");
		unitDef_01.setParameter("header-value", "valuex");
		HeaderAction headerAction1 = Configurator.configure(new HeaderAction(), unitDef_01);

		unitDef_02 = new SmooksResourceConfiguration("selector", "useragent", "xxx");
		unitDef_02.setParameter("action", "add");
		unitDef_02.setParameter("header-name", "namex");
		unitDef_02.setParameter("header-value", "valuex");
		HeaderAction headerAction2 = Configurator.configure(new HeaderAction(), unitDef_02);
		
		assertTrue(headerAction1.equals(headerAction2));
		assertTrue(headerAction1.equals("namex"));
		assertTrue(!headerAction1.equals("namey"));

		unitDef_02 = new SmooksResourceConfiguration("selector", "useragent", "xxx");
		unitDef_02.setParameter("action", "add");
		unitDef_02.setParameter("header-name", "namey");
		unitDef_02.setParameter("header-value", "valuex");
		headerAction2 = Configurator.configure(new HeaderAction(), unitDef_02);
		
		assertTrue(!headerAction1.equals(headerAction2));
	}

	
	/*
	 * Class under test for boolean equals(Object)
	 */
	public void testEqualsObject_InVector() {
		SmooksResourceConfiguration unitDef_01;
		SmooksResourceConfiguration unitDef_02;
		Vector vec = new Vector();
		
		unitDef_01 = new SmooksResourceConfiguration("selector", "useragent", "xxx");
		unitDef_01.setParameter("action", "remove");
		unitDef_01.setParameter("header-name", "namex");
		HeaderAction headerAction1 = Configurator.configure(new HeaderAction(), unitDef_01);

		unitDef_02 = new SmooksResourceConfiguration("selector", "useragent", "xxx");
		unitDef_02.setParameter("action", "remove");
		unitDef_02.setParameter("header-name", "namey");
		HeaderAction headerAction2 = Configurator.configure(new HeaderAction(), unitDef_02);
		
		vec.add(headerAction1);
		assertTrue(vec.contains(headerAction1));
		assertTrue(!vec.contains(headerAction2));
		assertTrue(!vec.contains("namex"));
		assertTrue(!vec.contains("namey"));

		vec.add(headerAction2);
		assertTrue(vec.contains(headerAction1));
		assertTrue(vec.contains(headerAction2));
		assertTrue(!vec.contains("namex"));
		assertTrue(!vec.contains("namey"));
	}
}
