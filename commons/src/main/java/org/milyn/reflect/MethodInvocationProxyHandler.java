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
package org.milyn.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for capturing method invocation statistics.
 * <p/>
 * Useful for testing.
 * <p/>
 * <b>Note</b>: <i>Be careful how you use this class.  It doesn't filter out
 * recursive/internal method calls i.e. you could eat memory with this class
 * if not carefull. Ideally, it should only log external Object invocations.</i>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MethodInvocationProxyHandler<T> implements InvocationHandler {

    private T object;
    private List<MethodCall> callList = new ArrayList<MethodCall>();
    private Map<String, List<MethodCall>> callMap = new LinkedHashMap<String, List<MethodCall>>();

    public MethodInvocationProxyHandler(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public T newProxy(Class[] interfaces) {
        return (T) Proxy.newProxyInstance(MethodInvocationProxyHandler.class.getClassLoader(), interfaces, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logCall(method, args);

        return method.invoke(object);
    }

    private synchronized void logCall(Method method, Object[] args) {
        Exception thisCallStack = new Exception();
        MethodCall methodCall = new MethodCall(callList.size(), thisCallStack, args);

        callList.add(methodCall);
        getCallList(method.getName()).add(methodCall);
    }

    /**
     * Get the complete method call list.
     * <p/>
     * This is the list of method calls, ordered by the order in which the
     * calls were made.
     *
     * @return Call list.
     */
    public List<MethodCall> getCallList() {
        return callList;
    }

    /**
     * Get the call list for a specific method.
     * <p/>
     * Overloaded method calls are bundled together.
     *
     * @param methodName The method name.
     * @return Call list.
     */
    public synchronized List<MethodCall> getCallList(String methodName) {
        List<MethodCall> methodCallList = callMap.get(methodName);

        if (methodCallList == null) {
            methodCallList = new ArrayList<MethodCall>();
            callMap.put(methodName, methodCallList);
        }
        
        return methodCallList;
    }


    /**
     * Get the call map.
     * <p/>
     * This is the {@link #getCallList() call list} sorted by method name.  Overloaded method
     * calls are bundled together.
     *
     * @return The call map.
     */
    public Map<String, List<MethodCall>> getCallMap() {
        return callMap;
    }
}
