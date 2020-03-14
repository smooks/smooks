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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Result;

import org.smooks.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.util.ClassUtil;

/**
 * An Exports instance holds a Map of {@link Export}s that Smooks
 * produces/exports.
 * </p>
 * The map uses the type of of result as its key and the {@link Export}
 * as its value.
 * 
 * @author Daniel Bevenius
 * @since 1.4
 * 
 */
public class Exports implements ContentHandler<Exports>
{
    private static final Exports NULL_EXPORTS = new Exports();
    
    private Map<Class<?>, Export> exportsMap = new HashMap<Class<?>, Export>();
    
    @AppContext
    private ApplicationContext applicationContext;
    
    public Exports()
    {
    }
    
    public Exports(final Export export)
    {
        addExport(export);
    }
    
    public Exports(final Set<Export> exportTypes)
    {
        for (Export export : exportTypes)
        {
            addExport(export);
        }
    }
    
    public Exports(final Class<?> resultType)
    {
        AssertArgument.isNotNull(resultType, "resultType");
        addExport(new Export(resultType));
    }
    
    public Exports(final String resultType)
    {
        AssertArgument.isNotNull(resultType, "resultType");
        addExport(new Export(getClassForType(resultType)));
    }
    
    private Class<?> getClassForType(final String type)
    {
        try
        {
            return ClassUtil.forName(type, Exports.class);
        } 
        catch (ClassNotFoundException e)
        {
            throw new SmooksException("Could not load class for type [" + type + "].");
        }
    }

    @Initialize
    public void setExportsInAppContext()
    {
        Exports.setExportsInApplicationContext(applicationContext, this);
    }
    
    public void addExport(Export export)
    {
        exportsMap.put(export.getType(), export);
    }
    
    public Collection<Export> getExports()
    {
        return Collections.unmodifiableCollection(exportsMap.values());
    }
    
    public Set<Class<?>> getResultTypes()
    {
        return Collections.unmodifiableSet(exportsMap.keySet());
    }

    public boolean hasExports()
    {
        return exportsMap.isEmpty() == false;
    }
    
    public Export getExport(Class<?> type)
    {
        return exportsMap.get(type);
    }
    
    public Result[] createResults()
    {
        Set<Result> results = new HashSet<Result>();
        for (Class<?> resultTypeClass : exportsMap.keySet())
        {
            results.add(createResultInstance(resultTypeClass));
        }
        return results.toArray(new Result[] {});
    }
    
    public Collection<Export> getProducts()
    {
        return getExports();
    }
    
    public static void setExportsInApplicationContext(final ApplicationContext appContext, final Exports exports)
    {
        appContext.setAttribute(Exports.class, exports);
    }
    
    public static Exports getExports(final ApplicationContext appContext)
    {
        Exports exports = (Exports) appContext.getAttribute(Exports.class);
        if (exports == null)
        {
            return NULL_EXPORTS;
        }
        return exports;
    }
    
    public static void addExport(ApplicationContext appContext, Export export)
    {
        Exports exports = getExports(appContext);
        exports.addExport(export);
    }
    
    /**
     * Will return the Objects contained in the results array. If the corresponding
     * {@link Export} for that result type was configured with an extract property 
     * only that portion of the result will be returned.
     * 
     * @param results The results produced by a Smooks filtering operation.
     * @param exports The exports.
     * @return List<Object> Either the results unchanged if no 'extract' was configured
     * or if an 'extract' was configured in the corresponding Export then only the
     * object identified will be returned in the list of objects.
     */
    public static List<Object> extractResults(final Result[] results, final Exports exports)
    {
        final List<Object> objects = new ArrayList<Object>();
        for (Result result : results)
        {
            if (result instanceof ResultExtractor)
            {
                @SuppressWarnings("unchecked")
                final ResultExtractor<Result> e = (ResultExtractor<Result>) result;
                objects.add(e.extractFromResult(result, exports.getExport(result.getClass())));
            }
            else
            {
                objects.add(result);
            }
        }
        
        return objects;
    }
    
    private static Result createResultInstance(final Class<?> resultTypeClass)
    {
        try
        {
            return (Result) resultTypeClass.newInstance();
        } 
        catch (InstantiationException e)
        {
            throw new SmooksException("Could not instantiate instance for result type [" 
                        + resultTypeClass.getName() + "]", e);
        } 
        catch (IllegalAccessException e)
        {
            throw new SmooksException("Could not create instance for result type [" 
                        + resultTypeClass.getName() + "]", e);
        }
    }

}
