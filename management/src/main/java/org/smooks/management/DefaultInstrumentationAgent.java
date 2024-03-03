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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.api.management.InstrumentationAgent;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class DefaultInstrumentationAgent implements InstrumentationAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInstrumentationAgent.class);

    private final MBeanServer mBeanServer;

    public DefaultInstrumentationAgent(boolean usePlatformMBeanServer, String mBeanServerDefaultDomain) {
        mBeanServer = createOrFindMBeanServer(usePlatformMBeanServer, mBeanServerDefaultDomain);
    }

    @Override
    public void register(Object object, ObjectName objectName) {
        try {
            registerMBeanWithServer(object, objectName, false);
        } catch (NotCompliantMBeanException e) {
            ModelMBeanAssembler modelMBeanAssembler = new ModelMBeanAssembler();
            ModelMBeanInfo modelMBeanInfo = modelMBeanAssembler.getModelMbeanInfo(object.getClass());
            register(object, objectName, modelMBeanInfo, false);
        } catch (JMException e) {
            throw new SmooksException(e);
        }
    }

    @Override
    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    @Override
    public RequiredModelMBean register(Object object, ObjectName objectName, ModelMBeanInfo modelMBeanInfo, boolean forceRegistration) {
        final RequiredModelMBean requiredModelMBean;
        try {
            requiredModelMBean = (RequiredModelMBean) mBeanServer.instantiate("javax.management.modelmbean.RequiredModelMBean");
            requiredModelMBean.setModelMBeanInfo(modelMBeanInfo);
            requiredModelMBean.setManagedResource(object, "ObjectReference");
            registerMBeanWithServer(requiredModelMBean, objectName, forceRegistration);
        } catch (InvalidTargetObjectTypeException | JMException e) {
            throw new SmooksException(e);
        }

        return requiredModelMBean;
    }

    private void registerMBeanWithServer(Object object, ObjectName objectName, boolean forceRegistration) throws JMException {
        try {
            LOGGER.info("Registering MBean {}: {}", objectName, object);
            mBeanServer.registerMBean(object, objectName);
        } catch (InstanceAlreadyExistsException e) {
            if (forceRegistration) {
                mBeanServer.unregisterMBean(objectName);
                mBeanServer.registerMBean(object, objectName);
            } else {
                throw e;
            }
        }
    }

    protected MBeanServer createOrFindMBeanServer(boolean usePlatformMBeanServer, String mBeanServerDefaultDomain) {
        if (usePlatformMBeanServer) {
            return ManagementFactory.getPlatformMBeanServer();
        }

        List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

        for (MBeanServer mBeanServer : servers) {
            LOGGER.debug("Found MBeanServer with default domain {}", mBeanServer.getDefaultDomain());

            if (mBeanServerDefaultDomain.equals(mBeanServer.getDefaultDomain())) {
                return mBeanServer;
            }
        }

        return MBeanServerFactory.createMBeanServer(mBeanServerDefaultDomain);
    }
}
