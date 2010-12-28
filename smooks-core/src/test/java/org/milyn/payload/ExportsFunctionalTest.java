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
package org.milyn.payload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import javax.xml.transform.Result;

import org.junit.Test;
import org.milyn.Smooks;

/**
 * Functional test for {@link Exports}.
 * 
 * @author Daniel Bevenius
 * 
 */
public class ExportsFunctionalTest
{
    @Test
    public void multipleExportTypes() throws Exception
    {
        Smooks smooks = new Smooks("/org/milyn/payload/exports-01.xml");
        smooks.createExecutionContext();

        Exports exports = Exports.getExports(smooks.getApplicationContext());

        Set<Class<?>> resultTypes = exports.getResultTypes();
        assertTrue(resultTypes.contains(StringResult.class));
        assertTrue(resultTypes.contains(JavaResult.class));
    }
    
    @Test
    public void multipleNamedExportTypes() throws Exception
    {
        Smooks smooks = new Smooks("/org/milyn/payload/exports-named.xml");
        smooks.createExecutionContext();

        Exports exports = Exports.getExports(smooks.getApplicationContext());
        Collection<Export> exportTypes = exports.getExports();
        assertEquals(2, exportTypes.size());
    }

    @Test
    public void programmaticConfiguration()
    {
        Smooks smooks = new Smooks();
        smooks.createExecutionContext();
        smooks.setExports(new Exports(StringResult.class));

        Exports exports = Exports.getExports(smooks.getApplicationContext());

        assertTrue(exports.getResultTypes().contains(StringResult.class));
        assertEquals(1, exports.getResultTypes().size());
    }

    @Test
    public void getResultsFromApplicationContext()
    {
        Smooks smooks = new Smooks();
        smooks.createExecutionContext();
        Exports exports =  new Exports(StringResult.class);
        smooks.setExports(exports);
        Result[] results = exports.createResults();
        assertEquals(1, results.length);
        assertEquals(StringResult.class, results[0].getClass());
    }

}
