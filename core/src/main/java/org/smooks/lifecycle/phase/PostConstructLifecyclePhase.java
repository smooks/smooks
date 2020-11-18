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
package org.smooks.lifecycle.phase;

import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.config.Configurable;
import org.smooks.injector.FieldInjector;
import org.smooks.injector.Injector;
import org.smooks.injector.MethodInjector;
import org.smooks.injector.Scope;
import org.smooks.util.ClassUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PostConstructLifecyclePhase extends AbstractLifecyclePhase {

    private final Scope scope;

    public PostConstructLifecyclePhase(final Scope scope) {
        AssertArgument.isNotNull(scope, "scope");
        this.scope = scope;
    }

    public PostConstructLifecyclePhase() {
        scope = null;
    }

    @Override
    public void doApply(final Object o) {
        if (scope != null) {
            final Injector fieldInjector = new FieldInjector(o, scope);
            fieldInjector.inject();

            final Injector methodInjector = new MethodInjector(o, scope);
            methodInjector.inject();
            
            // reflectively call the "setConfiguration" method, if defined...
            setConfiguration(o, scope);
        }
        
        checkPropertiesConfigured(o.getClass(), o);
        invoke(o, PostConstruct.class);
    }

    protected <U> void checkPropertiesConfigured(Class contentHandlerClass, U instance) {
        Field[] fields = contentHandlerClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = contentHandlerClass.getSuperclass();
        if (superClass != null) {
            checkPropertiesConfigured(superClass, instance);
        }

        for (Field field : fields) {
            String fieldName = field.getName();
            Object fieldValue;

            try {
                fieldValue = ClassUtil.getField(field, instance);
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Unable to get property field value for '" + ClassUtil.getLongMemberName(field) + "'.", e);
            }

            if (fieldValue != null) {
                // It's set so no need to check anything....
                continue;
            }

            Inject injectAnnotation = field.getAnnotation(Inject.class);
            if (injectAnnotation == null) {
                // Check is there's a setter method for this property, with the @ConfigParam annotation
                // configured on it...
                String setterName = ClassUtil.toSetterName(fieldName);
                Method setterMethod = ClassUtil.getSetterMethod(setterName, contentHandlerClass, field.getType());

                if (setterMethod != null) {
                    injectAnnotation = setterMethod.getAnnotation(Inject.class);
                }
            }

            if (injectAnnotation != null) {
                throw new SmooksConfigurationException("Property '" + fieldName + "' not configured on class " + instance.getClass().getName() + "'.");
            }
        }
    }

    private <U> void setConfiguration(U instance, Scope scope) {
        if (instance instanceof Configurable) {
            ((Configurable) instance).setConfiguration(((SmooksResourceConfiguration) scope.get(SmooksResourceConfiguration.class)).toProperties());
        } else {
            try {
                Method setConfigurationMethod = instance.getClass().getMethod("setConfiguration", SmooksResourceConfiguration.class);
                setConfigurationMethod.invoke(instance, scope.get(SmooksResourceConfiguration.class));
            } catch (NoSuchMethodException e) {
                // That's fine
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Error invoking 'setConfiguration' method on class '" + instance.getClass().getName() + "'.  This class must be public.  Alternatively, use the @Config annotation on a class field.", e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof SmooksConfigurationException) {
                    throw (SmooksConfigurationException) e.getTargetException();
                } else {
                    Throwable cause = e.getTargetException();
                    throw new SmooksConfigurationException("Error invoking 'setConfiguration' method on class '" + instance.getClass().getName() + "'.", (cause != null ? cause : e));
                }
            }
        }
    }
}
