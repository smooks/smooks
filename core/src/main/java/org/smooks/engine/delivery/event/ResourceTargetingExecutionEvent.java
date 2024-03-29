/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.event;

import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.event.ResourceAwareEvent;

import java.util.Arrays;

/**
 * Resource targeting event.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ResourceTargetingExecutionEvent<T> extends FragmentExecutionEvent<T> implements ResourceAwareEvent {

    private final ResourceConfig resourceConfig;
    private Object[] metadata;
    private VisitSequence sequence;

    /**
     * Event constructor.
     *
     * @param fragment       The element ({@link org.smooks.engine.delivery.sax.SAXElement}/{@link org.w3c.dom.Element})
     *                       being targeted by the resource.
     * @param resourceConfig The resource configuration.
     * @param metadata       Optional event metadata.
     */
    public ResourceTargetingExecutionEvent(Fragment<T> fragment, ResourceConfig resourceConfig, Object... metadata) {
        super(fragment);
        this.resourceConfig = resourceConfig;
        this.metadata = metadata;
    }

    /**
     * Event constructor.
     *
     * @param fragment       The element ({@link org.smooks.engine.delivery.sax.SAXElement}/{@link org.w3c.dom.Element})
     *                       being targeted by the resource.
     * @param resourceConfig The resource configuration.
     * @param metadata       Optional event metadata.
     */
    public ResourceTargetingExecutionEvent(Fragment<T> fragment, ResourceConfig resourceConfig, VisitSequence sequence, Object... metadata) {
        this(fragment, resourceConfig, metadata);
        this.sequence = sequence;
    }

    /**
     * Get the tagreted resource configuration.
     *
     * @return The targeted resource configuration.
     */
    @Override
    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    /**
     * Set event metadata.
     *
     * @param metadata Event metadata.
     */
    public void setMetadata(Object... metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the optional event metadata.
     *
     * @return Event metadata.
     */
    public Object[] getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Target: ").append(getFragment().unwrap()).append(". ");
        builder.append("Resource: ").append(resourceConfig).append(". ");
        if (metadata != null) {
            builder.append("Event Metadata: ").append(Arrays.asList(metadata)).append(".");
        }

        return builder.toString();
    }

    public VisitSequence getSequence() {
        return sequence;
    }
}
