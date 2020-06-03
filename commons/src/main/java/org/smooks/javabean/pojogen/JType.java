/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.javabean.pojogen;

import org.smooks.assertion.AssertArgument;
import org.apache.commons.lang.ArrayUtils;

import java.util.Set;

/**
 * Java type model.
 * <p/>
 * Includes generic typing.
 *
 * @author bardl
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JType {

    private Class<?> type;
    private Class<?> genericType;

    public JType(Class<?> type) {
        AssertArgument.isNotNull(type, "type");
        this.type = type;
    }

    public JType(Class<?> type, Class<?> genericType) {
        AssertArgument.isNotNull(type, "type");
        AssertArgument.isNotNull(genericType, "genericType");
        this.type = type;
        this.genericType = genericType;
    }

    public Class<?> getType() {
        return type;
    }

    public Class<?> getGenericType() {
        return genericType;
    }

    public void addImports(Set<Class<?>> importSet, String[] excludePackages) {
        AssertArgument.isNotNull(importSet, "importSet");
        if(!type.isPrimitive()) {
            if(!ArrayUtils.contains(excludePackages, getPackageName(type))) {
                importSet.add(type);
            }
        }
        if(genericType != null && !genericType.isPrimitive()) {
            if(!ArrayUtils.contains(excludePackages, getPackageName(genericType))) {
                importSet.add(genericType);
            }
        }
    }

    private String getPackageName(Class clazz) {
        // We can't use Class.getPackage for some reason because Javassist is not returning anything??

        String name = clazz.getName();
        int lastDot = name.lastIndexOf('.');

        return name.substring(0, lastDot);
    }

    @Override
    public String toString() {
        if(genericType != null) {
            return type.getSimpleName() + "<" + genericType.getSimpleName() + ">";
        }

        return type.getSimpleName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JType) {
            JType typeObj = (JType) obj;

            if(typeObj.getType().getName().equals(type.getName())) {
                if(genericType != null && typeObj.genericType != null) {
                    if(!typeObj.genericType.getName().equals(genericType.getName())) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        if(genericType != null) {
            return (type.getName().hashCode() + genericType.getName().hashCode());
        } else {
            return type.getName().hashCode();
        }
    }
}
