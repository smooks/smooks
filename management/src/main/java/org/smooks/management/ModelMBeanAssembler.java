/*-
 * ========================LICENSE_START=================================
 * Management
 * %%
 * Copyright (C) 2020 - 2024 Smooks
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
package org.smooks.management;

import org.smooks.management.annotation.ManagedAttribute;
import org.smooks.management.annotation.ManagedNotification;
import org.smooks.management.annotation.ManagedNotifications;
import org.smooks.management.annotation.ManagedOperation;
import org.smooks.management.annotation.ManagedResource;

import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanInfo;
import java.lang.reflect.Method;

/**
 * A Smooks-specific {@link javax.management.MBeanInfo} assembler that reads the details from the
 * {@link ManagedResource}, {@link ManagedAttribute}, {@link ManagedOperation}, {@link ManagedNotification}, and
 * {@link ManagedNotifications} annotations.
 */
public class ModelMBeanAssembler {

    private final ModelMBeanInfoHelper modelMBeanInfoHelper = new ModelMBeanInfoHelper();

    public ManagedResource getManagedResource(Class<?> clazz) {
        return clazz.getAnnotation(ManagedResource.class);
    }

    public ManagedAttribute getManagedAttribute(Method method) {
        return method.getAnnotation(ManagedAttribute.class);
    }

    public ManagedOperation getManagedOperation(Method method) {
        return method.getAnnotation(ManagedOperation.class);
    }

    public ManagedNotification[] getManagedNotifications(Class<?> clazz) {
        ManagedNotifications managedNotifications = clazz.getAnnotation(ManagedNotifications.class);
        return null != managedNotifications ? managedNotifications.value() : new ManagedNotification[0];
    }

    public String getAttributeName(String methodName) {
        if (methodName.indexOf("set") == 0) {
            return methodName.substring(3);
        }
        if (methodName.indexOf("get") == 0) {
            return methodName.substring(3);
        }
        if (methodName.indexOf("is") == 0) {
            return methodName.substring(2);
        }
        return null;
    }

    public static boolean checkMethod(Method[] methods, String methodName) {
        boolean result = false;
        for (Method method : methods) {
            if (method.getName().compareTo(methodName) == 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static String getAttributeType(Method[] methods, String attributeName) {
        String result = null;
        String searchMethod = "get" + attributeName;
        for (Method value : methods) {
            if (value.getName().compareTo(searchMethod) == 0) {
                result = value.getReturnType().getName();
                break;
            }
        }
        // check it is "is " attribute
        if (null == result) {
            searchMethod = "is" + attributeName;
            for (Method method : methods) {
                if (method.getName().compareTo(searchMethod) == 0) {
                    result = method.getReturnType().getName();
                    break;
                }
            }
        }
        return result;
    }

    static class ManagedAttributeInfo {
        String fname;
        String ftype;
        String description;
        boolean read;
        boolean write;
        boolean is;
    };


    public ManagedAttributeInfo getAttributeInfo(Method[] methods,
                                                 String attributName,
                                                 String attributType,
                                                 ManagedAttribute managedAttribute) {
        ManagedAttributeInfo managedAttributeInfo = new ManagedAttributeInfo();
        managedAttributeInfo.fname = attributName;
        managedAttributeInfo.ftype = attributType;
        managedAttributeInfo.description = managedAttribute.description();
        managedAttributeInfo.is = checkMethod(methods, "is" + attributName);
        managedAttributeInfo.write = checkMethod(methods, "set" + attributName);

        if (managedAttributeInfo.is) {
            managedAttributeInfo.read = true;
        } else {
            managedAttributeInfo.read = checkMethod(methods, "get" + attributName);
        }

        return managedAttributeInfo;

    }

    Method findMethodByName(Method[] methods, String methodName) {
        for (Method method : methods) {
            if (method.getName().compareTo(methodName) == 0) {
                return method;
            }
        }
        return null;
    }

    void addAttributeOperation(Method method) {
        Descriptor operationDescriptor = modelMBeanInfoHelper.buildAttributeOperationDescriptor(method.getName());

        Class<?>[] types = method.getParameterTypes();

        String[] paramTypes = new String[types.length];
        String[] paramNames = new String[types.length];
        String[] paramDescs = new String[types.length];

        for (int j = 0; j < types.length; j++) {
            paramTypes[j] = types[j].getName();
            paramDescs[j] = "";
            paramNames[j] = types[j].getName();
        }

        modelMBeanInfoHelper.addModelMBeanMethod(method.getName(),
                paramTypes,
                paramNames,
                paramDescs,
                "",
                method.getReturnType().getName(),
                operationDescriptor);
    }

    public ModelMBeanInfo getModelMbeanInfo(Class<?> clazz) {
        modelMBeanInfoHelper.clear();
        ManagedResource managedResource = getManagedResource(clazz);
        if (managedResource == null) {
            return null;
        }
        String mbeanDescriptor = managedResource.description();

        ManagedNotification[] managedNotifications = getManagedNotifications(clazz);
        for (ManagedNotification managedNotification : managedNotifications) {
            modelMBeanInfoHelper.addModelMBeanNotification(managedNotification.notificationTypes(),
                    managedNotification.name(),
                    managedNotification.description(), null);
        }

        Method[] methods = clazz.getDeclaredMethods();

        for (Method value : methods) {
            ManagedAttribute managedAttribute = getManagedAttribute(value);
            //add Attribute to the ModelMBean
            if (managedAttribute != null) {
                String attributeName = getAttributeName(value.getName());
                if (!modelMBeanInfoHelper.checkAttribute(attributeName)) {
                    String attributeType = getAttributeType(methods, attributeName);
                    ManagedAttributeInfo managedAttributeInfo = getAttributeInfo(methods,
                            attributeName,
                            attributeType,
                            managedAttribute);
                    Descriptor attributeDescriptor = modelMBeanInfoHelper.buildAttributeDescriptor(attributeName,
                            managedAttributeInfo.is, managedAttributeInfo.read, managedAttributeInfo.write);

                    // should setup the description
                    modelMBeanInfoHelper.addModelMBeanAttribute(managedAttributeInfo.fname,
                            managedAttributeInfo.ftype,
                            managedAttributeInfo.read,
                            managedAttributeInfo.write,
                            managedAttributeInfo.is,
                            managedAttributeInfo.description,
                            attributeDescriptor);

                    Method method;
                    // add the attribute methode to operation
                    if (managedAttributeInfo.read) {
                        if (managedAttributeInfo.is) {
                            method = findMethodByName(methods, "is" + attributeName);
                        } else {
                            method = findMethodByName(methods, "get" + attributeName);
                        }
                        addAttributeOperation(method);
                    }
                    if (managedAttributeInfo.write) {
                        method = findMethodByName(methods, "set" + attributeName);
                        addAttributeOperation(method);
                    }
                }

            } else {
                ManagedOperation managedOperation = getManagedOperation(value);

                if (managedOperation != null) {
                    Class<?>[] types = value.getParameterTypes();
                    String[] paramTypes = new String[types.length];
                    String[] paramNames = new String[types.length];
                    String[] paramDescs = new String[types.length];

                    for (int j = 0; j < types.length; j++) {
                        paramTypes[j] = types[j].getName();
                        paramDescs[j] = "";
                        paramNames[j] = types[j].getName();
                    }
                    Descriptor operationDescriptor =
                            modelMBeanInfoHelper.buildOperationDescriptor(managedOperation, value.getName());
                    modelMBeanInfoHelper.addModelMBeanMethod(value.getName(),
                            paramTypes,
                            paramNames,
                            paramDescs,
                            managedOperation.description(),
                            value.getReturnType().getName(),
                            operationDescriptor);
                }
            }

        }
        return modelMBeanInfoHelper.buildModelMBeanInfo(mbeanDescriptor);
    }

}
