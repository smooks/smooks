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
package org.milyn.javabean.decoders;

import org.milyn.javabean.DataDecodeException;

import junit.framework.TestCase;

/**
 * BooleanDecoderTest
 * 
 * @author <a href="mailto:mgoodwin1989@gmail.com">Matt Goodwin</a>
 */
public class BooleanDecoderTest extends TestCase {

    public void testBoolean() {
        String data = "Y";
        BooleanDecoder decoder = new BooleanDecoder();
        
        Object result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean)result);
        
        data = "y";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean)result);
        
        data = "Yes";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean)result);
        
        data = "yes";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean)result);
        
        data = "true";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean)result);
        
        data = "1";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean)result);
        
        data = "No";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean)result);
        
        data = "no";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean)result);
        
        data = "N";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean)result);
        
        data = "n";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean)result);
        
        data = "false";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean)result);
        
        data = "0";
        result = decoder.decode(data);
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean)result);
        
        try {
            data = "eeee";
            result = decoder.decode(data);
            fail("Should have thrown exception");
        } catch (DataDecodeException e) {
        }
    }
}
