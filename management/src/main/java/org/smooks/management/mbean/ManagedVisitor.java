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

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.management.InstrumentationAgent;
import org.smooks.api.management.InstrumentationResource;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.management.ModelMBeanAssembler;
import org.smooks.management.annotation.ManagedAttribute;
import org.smooks.management.annotation.ManagedNotification;
import org.smooks.management.annotation.ManagedResource;
import org.w3c.dom.Node;

import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

@ManagedResource
@ManagedNotification(name = "org.smooks.api.resource.visitor", notificationTypes = {"javax.management.Notification"})
public class ManagedVisitor extends AbstractMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedVisitor.class);
    private static final char PATH_SEPARATOR = '/';

    private final AtomicLong seqNo = new AtomicLong();
    private final AtomicLong visitBeforeCounter = new AtomicLong();
    private final AtomicLong visitChildElementCounter = new AtomicLong();
    private final AtomicLong visitChildTextCounter = new AtomicLong();
    private final AtomicLong visitAfterCounter = new AtomicLong();
    private final AtomicLong failedVisitCounter = new AtomicLong();
    private final AtomicLong totalProcessingTime = new AtomicLong();

    private final LongUnaryOperator incrementOrReset = operand -> operand == Long.MAX_VALUE ? 0 : operand + 1;
    private final ResourceConfig visitorResourceConfig;
    private final Visitor visitor;
    private final RequiredModelMBean requiredModelMBean;

    public ManagedVisitor(InstrumentationResource instrumentationResource, ResourceConfig visitorResourceConfig, Visitor visitor) {
        super(instrumentationResource);
        this.visitorResourceConfig = visitorResourceConfig;
        this.visitor = visitor;

        ModelMBeanAssembler modelMBeanAssembler = new ModelMBeanAssembler();
        ModelMBeanInfo modelMBeanInfo = modelMBeanAssembler.getModelMbeanInfo(this.getClass());
        InstrumentationAgent instrumentationAgent = instrumentationResource.getInstrumentationAgent();
        this.requiredModelMBean = instrumentationAgent.register(this, this.getObjectName(), modelMBeanInfo, false);
    }

    @ManagedAttribute(description = "Selector")
    public String getSelector() {
        return visitorResourceConfig.getSelectorPath().getSelector();
    }

    @ManagedAttribute(description = "Number of visited start events")
    public long getVisitBeforeCount() {
        return visitBeforeCounter.get();
    }

    @ManagedAttribute(description = "Number of visited child events")
    public long getVisitChildElementCount() {
        return visitChildElementCounter.get();
    }

    @ManagedAttribute(description = "Number of visited text events")
    public long getVisitChildTextCount() {
        return visitChildTextCounter.get();
    }

    @ManagedAttribute(description = "Number of visited end events")
    public long getVisitAfterCount() {
        return visitAfterCounter.get();
    }

    @ManagedAttribute(description = "Total visit processing time (in milliseconds)")
    public long getTotalProcessingTime() {
        return totalProcessingTime.get();
    }

    @ManagedAttribute(description = "Number of failed visits")
    public long getFailedVisitCount() {
        return failedVisitCounter.get();
    }

    public void incrementVisitBeforeCounter() {
        visitBeforeCounter.updateAndGet(incrementOrReset);
    }

    public void incrementVisitAfterCounter() {
        visitAfterCounter.updateAndGet(incrementOrReset);
    }

    public void incrementVisitChildElementCounter() {
        visitChildElementCounter.updateAndGet(incrementOrReset);
    }

    public void incrementVisitChildTextCounter() {
        visitChildTextCounter.updateAndGet(incrementOrReset);
    }

    public void incrementFailedVisitCounter() {
        failedVisitCounter.updateAndGet(incrementOrReset);
    }

    public void addTotalProcessingTime(long visitProcessingTime) {
        totalProcessingTime.addAndGet(visitProcessingTime);
    }

    public void sendNotification(Node node, long visitProcessingTime) {
        Notification notification = new Notification("org.smooks.api.resource.visitor", requiredModelMBean, seqNo.updateAndGet(incrementOrReset), new Date().getTime(), "visitBefore");
        Map<String, Object> userData = new HashMap<>();
        userData.put("path", toPath(node));
        userData.put("processingTimeMs", visitProcessingTime);
        notification.setUserData(userData);
        try {
            requiredModelMBean.sendNotification(notification);
        } catch (MBeanException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    protected String toPath(Node node) {
        StringBuilder path = new StringBuilder(node.getNodeName());
        node = node.getParentNode();
        while (node != null) {
            path.insert(0, node.getNodeName() + PATH_SEPARATOR);
            node = node.getParentNode();
        }

        return path.toString();
    }

    @Override
    protected String getName() {
        if (visitor.getClass().getAnnotation(Resource.class) != null) {
            return visitor.getClass().getAnnotation(Resource.class).name() + "@" + Integer.toHexString(visitor.hashCode());
        } else {
            return visitor.toString();
        }
    }

    @Override
    protected String getType() {
        return "visitor";
    }

    @Override
    protected String getContext() {
        return visitorResourceConfig.getSelectorPath().getSelector();
    }

}
