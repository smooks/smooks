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
package org.smooks.management.mbean;

import org.smooks.api.SmooksException;
import org.smooks.api.management.InstrumentationResource;
import org.smooks.api.management.MBean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class AbstractMBean implements MBean {

    final InstrumentationResource instrumentationResource;
    protected String hostName = "localhost";

    public AbstractMBean(InstrumentationResource instrumentationResource) {
        this.instrumentationResource = instrumentationResource;
        try {
            hostName = getLocalHostName();
        } catch (UnknownHostException ex) {
            // ignore, use the default "localhost"
        }
    }

    protected String getLocalHostName() throws UnknownHostException {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage();
            if (host != null) {
                return host.contains(":") ? host.substring(0, host.indexOf(":") - 1) : null;
            }
            throw uhe;
        }
    }

    @Override
    public ObjectName getObjectName() {
        StringBuilder objectNameStringBuilder = new StringBuilder();
        objectNameStringBuilder.append(instrumentationResource.getMBeanObjectDomainName()).append(":");
        if (instrumentationResource.getIncludeHostName()) {
            objectNameStringBuilder.append("context=").append(ObjectName.quote(getContext() != null ? hostName + "/" + getContext() : hostName)).append(",");
        } else if (getContext() != null) {
            objectNameStringBuilder.append("context=").append(ObjectName.quote(getContext())).append(",");
        }

        objectNameStringBuilder.append("type=").append(getType()).append(",");
        objectNameStringBuilder.append("name=").append(getName());

        try {
            return new ObjectName(objectNameStringBuilder.toString());
        } catch (MalformedObjectNameException e) {
            throw new SmooksException(e);
        }
    }

    protected abstract String getName();

    protected abstract String getType();

    protected String getContext() {
        return null;
    }
}
