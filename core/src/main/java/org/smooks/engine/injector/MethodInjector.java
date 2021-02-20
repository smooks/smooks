/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
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
package org.smooks.engine.injector;

import org.smooks.api.SmooksConfigException;
import org.smooks.support.ClassUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.*;

public class MethodInjector extends AbstractInjector<Method> {
    private final Object instance;
    private final Scope scope;

    public MethodInjector(Object instance, Scope scope) {
        this.instance = instance;
        this.scope = scope;
    }

    @Override
    public void inject() {
        Method[] methods = instance.getClass().getMethods();

        for (Method method : methods) {
            Inject injectAnnotation = method.getAnnotation(Inject.class);
            if (injectAnnotation != null) {
                Class[] params = method.getParameterTypes();

                if (params.length == 1) {
                    inject(method.getParameters()[0].getAnnotation(Named.class), method, instance, scope);
                } else {
                    throw new SmooksConfigException("Method '" + ClassUtil.getLongMemberName(method) + "' defines a @Inject, yet it specifies more than a single paramater.");
                }
            }
        }
    }

    @Override
    protected Class<?> getType(Method method) {
        return method.getParameterTypes()[0];
    }

    @Override
    protected Type getActualType(Method method) {
        return ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
    }

    @Override
    protected String getName(Named namedAnnotation, Method method) {
        // Work out the property name, if not specified via the annotation....
        final String name;
        if (namedAnnotation == null) {
            // "name" not defined.  Use the method name...
            name = getPropertyName(method);
            if (name == null) {
                throw new SmooksConfigException("Unable to determine the property name associated with '" +
                        ClassUtil.getLongMemberName(method) + "'. " +
                        "Setter methods that specify the @Inject annotation " +
                        "must either follow the Javabean naming convention ('setX' for property 'x'), or specify the " +
                        "property name via the 'name' parameter on the @Inject annotation.");
            }
        } else {
            name = namedAnnotation.value();
        }

        return name;
    }

    @Override
    protected Object getDefaultParamValue(Object instance, Method member) {
        return null;
    }
    
    @Override
    protected void doSetMember(Member member, Object instance, Object value, String name) throws InvocationTargetException, IllegalAccessException {
        ((Method) member).invoke(instance, value);
    }

    private String getPropertyName(Method method) {
        if (!method.getName().startsWith("set")) {
            return null;
        }

        StringBuffer methodName = new StringBuffer(method.getName());

        if (methodName.length() < 4) {
            return null;
        }

        methodName.delete(0, 3);
        methodName.setCharAt(0, Character.toLowerCase(methodName.charAt(0)));

        return methodName.toString();
    }
}
