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

import jakarta.annotation.PostConstruct;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.delivery.ReaderPool;
import org.smooks.api.lifecycle.ContentDeliveryConfigLifecycle;
import org.smooks.api.lifecycle.FilterLifecycle;
import org.smooks.api.management.InstrumentationAgent;
import org.smooks.api.management.InstrumentationAgentFactory;
import org.smooks.api.management.InstrumentationResource;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.engine.lookup.ResourceConfigSeqsLookup;
import org.smooks.management.mbean.ManagedReaderPool;
import org.smooks.management.mbean.ManagedResourceConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class DefaultInstrumentationResource implements FilterLifecycle, ContentDeliveryConfigLifecycle, InstrumentationResource {

    private final List<ReaderPool> managedReaderPools = new ArrayList<>(1);

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private Boolean usePlatformMBeanServer;

    @Inject
    private String mBeanServerDefaultDomain;

    @Inject
    private String mBeanObjectDomainName;

    @Inject
    private Boolean includeHostName;

    private InstrumentationAgent instrumentationAgent;

    @PostConstruct
    public void postConstruct() {
        Iterator<InstrumentationAgentFactory> instrumentationAgentFactoryIterator = ServiceLoader.load(InstrumentationAgentFactory.class, applicationContext.getClassLoader()).iterator();
        if (instrumentationAgentFactoryIterator.hasNext()) {
            InstrumentationAgentFactory instrumentationAgentFactory = instrumentationAgentFactoryIterator.next();
            instrumentationAgent = instrumentationAgentFactory.create(usePlatformMBeanServer, mBeanServerDefaultDomain);
            applicationContext.getRegistry().registerObject(INSTRUMENTATION_RESOURCE_TYPED_KEY, this);
        } else {
            throw new SmooksConfigException(String.format("%s service not found. Hint: ensure the Smooks application context has the correct class loader set", InstrumentationAgentFactory.class.getName()));
        }
    }

    @Override
    public void onPreFilter(ExecutionContext executionContext) {
        ReaderPool readerPool = executionContext.getContentDeliveryRuntime().getReaderPool();
        if (!managedReaderPools.contains(readerPool)) {
            synchronized (this) {
                if (!managedReaderPools.contains(executionContext.getContentDeliveryRuntime().getReaderPool())) {
                    managedReaderPools.add(readerPool);
                    ManagedReaderPool managedReaderPool = new ManagedReaderPool(executionContext.getContentDeliveryRuntime().getReaderPool(), this);
                    instrumentationAgent.register(managedReaderPool, managedReaderPool.getObjectName());
                }
            }
        }
    }

    @Override
    public void onPostFilter(ExecutionContext executionContext) {

    }

    @Override
    public void onContentHandlersCreated() {
        List<ResourceConfigSeq> resourceConfigSeqs = applicationContext.getRegistry().lookup(new ResourceConfigSeqsLookup());
        for (ResourceConfigSeq resourceConfigSeq : resourceConfigSeqs) {
            for (ResourceConfig resourceConfig : resourceConfigSeq) {
                ManagedResourceConfig managedResourceConfig = new ManagedResourceConfig(resourceConfigSeq, resourceConfig, this);
                instrumentationAgent.register(managedResourceConfig, managedResourceConfig.getObjectName());
            }
        }
    }

    @Override
    public void onContentDeliveryBuilderCreated() {

    }

    @Override
    public void onContentDeliveryConfigCreated() {

    }

    @Override
    public String getMBeanObjectDomainName() {
        return mBeanObjectDomainName;
    }

    @Override
    public Boolean getIncludeHostName() {
        return includeHostName;
    }

    @Override
    public InstrumentationAgent getInstrumentationAgent() {
        return instrumentationAgent;
    }
}
