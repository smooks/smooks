/**
 * Copyright 2011 AT&T Intellectual Property. All Rights Reserved.
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
