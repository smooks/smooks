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

package org.smooks.cdr;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tfennelly
 */
public class SmooksResourceConfigurationListTest {

	@Test
    public void testConstructor() {
        testBadArgs(null);
        testBadArgs(" ");

        SmooksResourceConfigurationList list = new SmooksResourceConfigurationList("list-name");
        assertEquals("list-name", list.getName());
    }

    private void testBadArgs(String name) {
        try {
            new SmooksResourceConfigurationList(name);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // OK
        }
    }
    
    @Test
    public void testAdd() {
        SmooksResourceConfigurationList list = new SmooksResourceConfigurationList("list-name");
        
        assertTrue(list.isEmpty());
        list.add(new SmooksResourceConfiguration("*", "a/b.zap"));
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals("a/b.zap", list.get(0).getResource());
        
        
        list.add(new SmooksResourceConfiguration("*", "c/d.zap"));
        assertEquals(2, list.size());        
        assertEquals("c/d.zap", list.get(1).getResource());
    }
}
