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
package org.smooks.reflect;

/**
 * Method call details.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MethodCall {
    
    private int callIndex;
    private Exception callStack;
    private Object[] callArgs;

    /**
     * Public constructor.
     * 
     * @param callIndex See {@link #getCallIndex()}.
     * @param callStack See {@link #getCallStack()}.
     * @param callArgs See {@link #getCallArgs()}.
     */
    public MethodCall(int callIndex, Exception callStack, Object[] callArgs) {
        this.callIndex = callIndex;
        this.callStack = callStack;
        this.callArgs = callArgs;
    }

    /**
     * Get the call index.
     * <p/>
     * The call "index" is just 
     * @return
     */
    public int getCallIndex() {
        return callIndex;
    }

    public Exception getCallStack() {
        return callStack;
    }

    public Object[] getCallArgs() {
        return callArgs;
    }
}
