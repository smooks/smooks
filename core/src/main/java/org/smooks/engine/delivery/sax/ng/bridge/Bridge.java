/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.engine.delivery.sax.ng.bridge;

import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.engine.xml.Namespace;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Represents a <i>bridge</i> node and provides convenience methods to retrieve attribute values from the node.
 * <br/><br/>
 * A <i>bridge</i> node holds the state of an execution. Formally, a <i>bridge</i> is a pair of attributes:
 * <li>
 * <il>source: key to an execution context value holding the event</il>
 * <il>visit: name of the visit method that the event is targeting</il>
 * </li>
 * <p>
 * Bridge nodes are meant for nested Smooks executions (i.e., a Smooks execution within another Smooks execution).
 * It allows the outer execution to carry over its visit state to the nested execution with the help of
 * {@link BridgeInterceptor}. Without a <i>bridge</i>, the nested Smooks instance
 * cannot join the inner execution to the outer one.
 */
public class Bridge {

    private final Node node;

    public Bridge(Node node) {
        if (!isBridge(node)) {
            throw new SmooksException("Node is not a bridge element");
        }
        this.node = node;
    }

    /**
     * Checks whether a node is a <i>bridge</i> element.
     *
     * @param node the node to be tested
     * @return <code>true</code> if the node is a bridge otherwise <code>false</code>
     */
    public static boolean isBridge(Node node) {
        return node instanceof Element && node.getNamespaceURI() != null &&
                node.getNamespaceURI().equals(Namespace.SMOOKS_URI) &&
                node.getLocalName().equals("bridge");
    }

    /**
     * Gets the execution context key which maps to the node representing the event.
     *
     * @return the key of the execution context entry that holds the event node
     */
    public TypedKey<Node> getSourceKey() {
        return TypedKey.of(node.getAttributes().getNamedItem("source").getNodeValue());
    }

    /**
     * Gets the name of the visit this <code>Bridge</code> is targeting.
     *
     * @return the name of the visit
     */
    public String getVisit() {
        return node.getAttributes().getNamedItem("visit").getNodeValue();
    }

    /**
     * Provides a convenience method to retrieve the source node.
     *
     * @param executionContext the execution context holding the source
     * @return the source node or <code>null</code> if not found
     */
    public Node getSourceValue(ExecutionContext executionContext) {
        return executionContext.get(getSourceKey());
    }
}