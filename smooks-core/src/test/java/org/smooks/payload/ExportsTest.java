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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Unit test for {@link Exports}.
 * 
 * @author Daniel Bevenius
 * 
 */
public class ExportsTest
{
    @Test
    public void createSingleExports()
    {
        Exports exports = new Exports(StringResult.class);
        Collection<Export> exportTypes = exports.getExports();
        assertFalse(exportTypes.isEmpty());
        assertEquals(exportTypes.iterator().next().getType(), StringResult.class);
    }

    @Test
    public void createMultipleExports()
    {
        Set<Export> results = new HashSet<Export>();
        results.add(new Export(StringResult.class));
        results.add(new Export(JavaResult.class));
        Exports exports = new Exports(results);

        Collection<Export> exportTypes = exports.getExports();
        assertEquals(2, exportTypes.size());
    }

}
