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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class FieldInjector extends AbstractInjector<Field> {

    private final Object instance;
    private final Scope scope;

    public FieldInjector(Object instance, Scope scope) {
        this.instance = instance;
        this.scope = scope;
    }
    
    @Override
    public void inject() {
        inject(instance.getClass(), instance, scope);
    }

    private void inject(Class<?> instanceClass, Object instance, Scope scope) {
        Field[] fields = instanceClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = instanceClass.getSuperclass();
        if (superClass != null) {
            inject(superClass, instance, scope);
        }

        for (Field field : fields) {
            Inject injectAnnotation;

            injectAnnotation = field.getAnnotation(Inject.class);
            if (injectAnnotation != null) {
                inject(field.getAnnotation(Named.class), field, instance, scope);
            }
        }
    }

    @Override
    protected Class<?> getType(Field field) {
        return field.getType();
    }

    @Override
    protected Type getActualType(Field field) {
        return ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    @Override
    protected String getName(Named namedAnnotation, Field field) {
        // Work out the property name, if not specified via the annotation....
        if (namedAnnotation == null) {
            // "name" not defined.  Use the field/method name...
            return field.getName();
        } else {
            return namedAnnotation.value();
        }
    }

    @Override
    protected Object getDefaultParamValue(Object instance, Field field) {
        try {
            return ClassUtil.getField(field, instance);
        } catch (IllegalAccessException e) {
            throw new SmooksConfigException(e);
        }
    }

    @Override
    protected void doSetMember(final Member member, final Object instance, final Object value, final String name) throws IllegalAccessException {
        ClassUtil.setField((Field) member, instance, value);
    }
}
