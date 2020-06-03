/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.reflect;

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
 * if not careful. Ideally, it should only log external Object invocations.</i>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unused")
public class MethodInvocationProxyHandler<T> implements InvocationHandler {

    private T object;
    private List<MethodCall> callList = new ArrayList<MethodCall>();
    private Map<String, List<MethodCall>> callMap = new LinkedHashMap<String, List<MethodCall>>();

    @SuppressWarnings("unused")
    public MethodInvocationProxyHandler(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    @SuppressWarnings({ "unchecked", "unused" })
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
    @SuppressWarnings({ "WeakerAccess", "unused" })
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
    @SuppressWarnings("WeakerAccess")
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
    @SuppressWarnings("unused")
    public Map<String, List<MethodCall>> getCallMap() {
        return callMap;
    }
}
