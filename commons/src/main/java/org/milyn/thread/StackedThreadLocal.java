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
package org.milyn.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Stacked ThreadLocal.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StackedThreadLocal<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackedThreadLocal.class);

    private String resourceName;
    private ThreadLocal<Stack<T>> stackTL = new ThreadLocal<Stack<T>>();

    public StackedThreadLocal(String resourceName) {
        this.resourceName = resourceName;
    }

    public T get() {
        Stack<T> execContextStack = getExecutionContextStack();
        try {
            return execContextStack.peek();
        } catch (EmptyStackException e) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("No currently stacked '" + resourceName + "' instance on active Thread.", e);
            }
            return null;
        }
    }

    public void set(T value) {
        Stack<T> execContextStack = getExecutionContextStack();
        execContextStack.push(value);
    }

    public void remove() {
        Stack<T> execContextStack = getExecutionContextStack();
        try {
            execContextStack.pop();
        } catch (EmptyStackException e) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("No currently stacked '" + resourceName + "' instance on active Thread.", e);
            }
        }
    }

    private Stack<T> getExecutionContextStack() {
        Stack<T> stack = stackTL.get();
        if(stack == null) {
            synchronized (this) {
                stack = stackTL.get();
                if(stack == null) {
                    stack = new Stack<T>();
                    stackTL.set(stack);
                }
            }
        }
        return stack;
    }
}
