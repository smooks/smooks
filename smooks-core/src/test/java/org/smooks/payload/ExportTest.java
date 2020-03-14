/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.payload;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for {@link Export}.
 * 
 * @author Daniel Bevenius
 *
 */
public class ExportTest
{
    @Test
    public void equalsIsRelexive()
    {
        Export export = new Export(String.class);
        assertEquals(export, export);
    }
    
    @Test
    public void equalsIsSymetric()
    {
        Export x = new Export(String.class);
        Export y = new Export(String.class);
        assertEquals(x, y);
        assertEquals(y, x);
        assertEquals(x.hashCode(), y.hashCode());
    }
    
    @Test
    public void equalsIsTransitive()
    {
        Export x = new Export(String.class);
        Export y = new Export(String.class);
        Export z = new Export(String.class);
        assertEquals(x, y);
        assertEquals(y, z);
        assertEquals(x, z);
        
        assertEquals(x.hashCode(), y.hashCode());
        assertEquals(y.hashCode(), z.hashCode());
        assertEquals(x.hashCode(), z.hashCode());
    }
    
    @Test
    public void equalsIsConsistent()
    {
        assertFalse(new Export(String.class, "exportNameX").equals(new Export(String.class)));
        assertFalse(new Export(String.class, "exportNameX").equals(new Export(String.class, "exportNameY")));
        assertTrue(new Export(String.class, "exportNameX").equals(new Export(String.class, "exportNameX")));
        
        assertFalse(new Export(String.class, "exportNameX", "ext1").equals(new Export(String.class, "exportNameX")));
        assertTrue(new Export(String.class, "exportNameX", "ext1").equals(new Export(String.class, "exportNameX","ext1")));
    }
    
    @Test
    public void equalsNull()
    {
        Export export = new Export(String.class);
        assertFalse(export.equals(null));
    }

}
