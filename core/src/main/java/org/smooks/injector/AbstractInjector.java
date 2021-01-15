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
package org.smooks.injector;

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.converter.TypeConverter;
import org.smooks.converter.TypeConverterException;
import org.smooks.converter.factory.TypeConverterFactory;
import org.smooks.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.registry.Registry;
import org.smooks.registry.lookup.LifecycleManagerLookup;
import org.smooks.registry.lookup.converter.SourceTargetTypeConverterFactoryLookup;
import org.smooks.util.ClassUtil;

import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractInjector<M extends Member> implements Injector {

    protected void setMember(final M member, final Object instance, final Object value, final String name) {
        try {
            doSetMember(member, instance, value, name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SmooksConfigurationException("Failed to set parameter configuration value on '" + ClassUtil.getLongMemberName(member) + "'.", e);
        }
    }

    protected TypeConverter<?, ?> createTypeConverter(final Class<?> sourceType, final M member, final Registry registry) {
        final TypeConverterFactory<?, ?> typeConverterFactory;
        final Class<?> targetType = getType(member);
        if (targetType.equals(Optional.class)) {
            final Type actualType = getActualType(member);
            if (actualType instanceof Class<?>) {
                typeConverterFactory = registry.lookup(new SourceTargetTypeConverterFactoryLookup<>(sourceType, (Class<?>) actualType));
            } else {
                typeConverterFactory = registry.lookup(new SourceTargetTypeConverterFactoryLookup<>(sourceType, (Class<?>) ((ParameterizedType) actualType).getRawType()));
            }
        } else {
            typeConverterFactory = registry.lookup(new SourceTargetTypeConverterFactoryLookup<>(sourceType, targetType));
        }
        
        final TypeConverter<?, ?> typeConverter;
        if (typeConverterFactory != null) {
            typeConverter = typeConverterFactory.createTypeConverter();
        } else {
            typeConverter = registry.lookup(new SourceTargetTypeConverterFactoryLookup<>(Object.class, Object.class)).createTypeConverter();
        }
        
        return typeConverter;
    }
    
    protected boolean isEnum(final Type type) {
        return type instanceof Class && ((Class<?>) type).isEnum();
    }
    
    protected void inject(final Named namedAnnotation, final M member, final Object instance, final Scope scope) throws SmooksConfigurationException {
        final String name = getName(namedAnnotation, member);
        Object valueInject = scope.get(name);
        final Type realType = getRealType(member);

        if (valueInject == null) {
            valueInject = scope.get(realType);
        }
        
        if (valueInject != null) {
            try {
                final TypeConverter<?, ?> typeConverter = createTypeConverter(valueInject.getClass(), member, scope.getRegistry());
                scope.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(typeConverter, new PostConstructLifecyclePhase(scope));
                Object convertedValueInject = ((TypeConverter) typeConverter).convert(valueInject);

                if (isEnum(realType)) {
                    try {
                        convertedValueInject = Enum.valueOf((Class) realType, convertedValueInject.toString());
                    } catch (IllegalArgumentException e) {
                        final Object[] enumConstants = ((Class<?>) realType).getEnumConstants();
                        throw new SmooksConfigurationException("Value '" + convertedValueInject.toString() + "' for parameter '" + name + "' is invalid.  Valid choices for this parameter are: " + Arrays.stream(enumConstants).map(c -> ((Enum) c).name()).collect(Collectors.toList()));
                    }
                }

                if (getType(member).equals(Optional.class) && !(valueInject instanceof Optional)) {
                    setMember(member, instance, Optional.of(convertedValueInject), name);
                } else {
                    setMember(member, instance, convertedValueInject, name);
                }
            } catch (TypeConverterException e) {
                throw new SmooksConfigurationException("Failed to set parameter configuration value on '" + ClassUtil.getLongMemberName(member) + "'.", e);
            }
        } else {
            if (getDefaultParamValue(instance, member) == null) {
                if (getType(member).equals(Optional.class)) {
                    setMember(member, instance, Optional.empty(), name);
                } else {
                    throw new SmooksConfigurationException("<param> '" + name + "' not specified on resource configuration:\n" + scope);
                }
            }
        }
    }

    protected abstract Class<?> getType(M member);
    
    protected abstract Type getActualType(M member);
    
    protected abstract String getName(Named namedAnnotation, M member);

    protected abstract Object getDefaultParamValue(Object instance, M member);
    
    protected abstract void doSetMember(Member member, Object instance, Object value, String name) throws InvocationTargetException, IllegalAccessException;
    
    protected Type getRealType(final M member) {
        final Class<?> type = getType(member);
        if (getType(member).equals(Optional.class)) {
            return getActualType(member);
        } else {
            return type;
        }
    }
}