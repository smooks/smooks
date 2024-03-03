/*-
 * ========================LICENSE_START=================================
 * management
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
import org.smooks.management.annotation.ManagedOperation;
import org.smooks.management.annotation.ManagedResource;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

public class ModelMBeanInfoHelper {
    protected Map<String, ModelMBeanAttributeInfo> attributes = new HashMap<>();
    protected Map<Constructor<?>, ModelMBeanConstructorInfo> constructors = new HashMap<>();
    protected Map<String, ModelMBeanOperationInfo> operations = new HashMap<>();
    protected Map<String, ModelMBeanNotificationInfo> notifications = new HashMap<>();

    public void clear() {
        attributes.clear();
        notifications.clear();
        constructors.clear();
        operations.clear();
    }

    public void addModelMBeanMethod(String name, String[] paramTypes, String[] paramNames, String[] paramDescs,
                                    String description, String rtype, Descriptor desc) {
        MBeanParameterInfo[] params = null;
        if (paramTypes != null) {
            params = new MBeanParameterInfo[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = new MBeanParameterInfo(paramNames[i],
                        paramTypes[i], paramDescs[i]);
            }
        }

        operations.put(name,
                new ModelMBeanOperationInfo(name,
                        description,
                        params,
                        rtype,
                        MBeanOperationInfo.ACTION,
                        desc));
    }

    public boolean checkAttribute(String attributeName) {
        return attributes.containsKey(attributeName);
    }

    public void addModelMBeanAttribute(String fname,
                                       String ftype,
                                       boolean read,
                                       boolean write,
                                       boolean is,
                                       String description,
                                       Descriptor desc) {
        attributes.put(fname, new ModelMBeanAttributeInfo(fname,
                ftype,
                description,
                read,
                write,
                is,
                desc));
    }

    public void addModelMBeanNotification(String[] type,
                                          String className,
                                          String description,
                                          Descriptor desc) {
        notifications.put(className, new ModelMBeanNotificationInfo(type, className, description, desc));
    }

    public void addModelMBeanConstructor(Constructor<?> c,
                                         String description,
                                         Descriptor desc) {
        this.constructors.put(c,
                new ModelMBeanConstructorInfo(description,
                        c,
                        desc));
    }

    public ModelMBeanInfo buildModelMBeanInfo(String description) {

        ModelMBeanOperationInfo[] modelMBeanOperationInfos = operations.values().toArray(new ModelMBeanOperationInfo[operations.values().size()]);
        ModelMBeanAttributeInfo[] modelMBeanAttributeInfos = attributes.values().toArray(new ModelMBeanAttributeInfo[attributes.values().size()]);
        ModelMBeanConstructorInfo[] modelMBeanConstructorInfos = constructors.values().toArray(new ModelMBeanConstructorInfo[constructors.values().size()]);
        ModelMBeanNotificationInfo[] modelMBeanNotificationInfos = notifications.values().toArray(new ModelMBeanNotificationInfo[notifications.values().size()]);

        return new ModelMBeanInfoSupport("javax.management.modelmbean.ModelMBeanInfo",
                description,
                modelMBeanAttributeInfos,
                modelMBeanConstructorInfos,
                modelMBeanOperationInfos,
                modelMBeanNotificationInfos);
    }


    public Descriptor buildAttributeDescriptor(String attributeName, boolean is, boolean read, boolean write) {
        Descriptor descriptor = new DescriptorSupport();
        descriptor.setField("name", attributeName);
        descriptor.setField("descriptorType", "attribute");

        if (read) {
            if (is) {
                descriptor.setField("getMethod", "is" + attributeName);
            } else {
                descriptor.setField("getMethod", "get" + attributeName);
            }
        }

        if (write) {
            descriptor.setField("setMethod", "set" + attributeName);
        }

        return descriptor;
    }

    public Descriptor buildOperationDescriptor(ManagedOperation managedOperation, String operationName) {
        Descriptor descriptor = new DescriptorSupport();
        descriptor.setField("name", operationName);
        descriptor.setField("descriptorType", "operation");
        descriptor.setField("role", "operation");

        if (managedOperation.description() != null) {
            descriptor.setField("displayName", managedOperation.description());
        }

        return descriptor;
    }

    public Descriptor buildAttributeOperationDescriptor(String operationName) {
        Descriptor descriptor = new DescriptorSupport();
        descriptor.setField("name", operationName);

        descriptor.setField("descriptorType", "operation");

        if (operationName.indexOf("set") == 0) {
            descriptor.setField("role", "setter");
        } else {
            descriptor.setField("role", "getter");
        }

        return descriptor;
    }
}
