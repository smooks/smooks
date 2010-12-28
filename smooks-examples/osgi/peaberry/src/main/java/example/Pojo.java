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
package example;

import org.milyn.Smooks;

import com.google.inject.Inject;

import example.model.Order;

/**
 * Simple example of using Google-Guice injection
 * 
 * @author Daniel Bevenius
 *
 */
public class Pojo
{
    private Smooks smooks;
    
    @Inject
    public Pojo(final Smooks smooks)
    {
        this.smooks = smooks;
    }
    
    public Order filter(final String input)
    {
        Order order = ExampleUtil.performFiltering(input, smooks);
        return order;
    }
    
    public void stop()
    {
        System.out.println("Pojo stop");
        smooks.close();
    }
    
}
