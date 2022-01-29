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
package org.smooks.engine.delivery.fragment;

import org.jaxen.*;
import org.jaxen.dom.DocumentNavigator;
import org.jaxen.saxpath.SAXPathException;
import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.expression.ExecutionContextExpressionEvaluator;
import org.smooks.engine.resource.config.xpath.IndexedSelectorPath;
import org.smooks.engine.resource.config.xpath.JaxenPatternSelectorPath;
import org.smooks.engine.resource.config.xpath.step.AttributeSelectorStep;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

import javax.xml.XMLConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class NodeFragment implements Fragment<Node> {

    protected static final UserDataHandler COPY_USER_DATA_HANDLER = new UserDataHandler() {
        @Override
        public void handle(final short operation, final String key, final Object data, final Node src, final Node dst) {
            dst.setUserData(key, data, this);
        }
    };
    
    public static final String RESERVATIONS_USER_DATA_KEY = "reservations";
    public static final String ID_USER_DATA_KEY = "id";

    private final Node node;
    private final boolean isReservationInheritable;
    private String id;
    private int hash;

    protected static class Reservation {
        private final Object token;
        private final boolean inheritable;

        Reservation(final Object token, final boolean inheritable) {
            this.token = token;
            this.inheritable = inheritable;
        }

        public Object getToken() {
            return token;
        }

        public boolean isInheritable() {
            return inheritable;
        }
    }
    
    public NodeFragment(final Node node) {
        this(node, false);
    }

    public NodeFragment(final Node node, final boolean isReservationInheritable) {
        this.node = node;
        this.isReservationInheritable = isReservationInheritable;
        
        Map<Long, Reservation> reservations = (Map<Long, Reservation>) node.getUserData(RESERVATIONS_USER_DATA_KEY);
        if (reservations == null) {
            reservations = new HashMap<>();
            node.setUserData(RESERVATIONS_USER_DATA_KEY, reservations, COPY_USER_DATA_HANDLER);
        } 
        
        Node parentNode = node.getParentNode();
        while (parentNode != null) {
            final Map<Long, Reservation> parentNodeReservations = (Map<Long, Reservation>) parentNode.getUserData(RESERVATIONS_USER_DATA_KEY);
            if (parentNodeReservations != null && !parentNodeReservations.isEmpty()) {
                reservations.putAll(parentNodeReservations.entrySet().stream().filter(e -> e.getValue().isInheritable()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
            parentNode = parentNode.getParentNode();
        }
    }

    @Override
    public String getId() {
        if (id == null) {
            id = (String) node.getUserData(ID_USER_DATA_KEY);
            if (id == null) {
                id = String.valueOf(Math.abs(ThreadLocalRandom.current().nextLong()));
                node.setUserData(ID_USER_DATA_KEY, id, COPY_USER_DATA_HANDLER);
            }
        }
        return id;
    }

    @Override
    public Node unwrap() {
        return node;
    }

    @Override
    public boolean reserve(final long id, final Object token) {
        return ((Map<Long, Reservation>) node.getUserData(RESERVATIONS_USER_DATA_KEY)).
                computeIfAbsent(id, key -> new Reservation(token, isReservationInheritable)).
                getToken().equals(token);
    }

    @Override
    public boolean release(final long id, final Object token) {
        final Map<Long, Reservation> reservedTokens = (Map<Long, Reservation>) node.getUserData(RESERVATIONS_USER_DATA_KEY);
        final Object reservedToken = reservedTokens.getOrDefault(id, new Reservation(token, isReservationInheritable)).getToken();
        if (reservedToken.equals(token)) {
            reservedTokens.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isMatch(final SelectorPath selectorPath, final ExecutionContext executionContext) {
        if (!assertConditionTrue(executionContext, selectorPath)) {
            return false;
        }

        try {
            return isPatternMatch(node, selectorPath) && evaluate(node, selectorPath, executionContext);
        } catch (SAXPathException e) {
            throw new SmooksException(e);
        }
    }

    protected boolean evaluate(final Node node, final SelectorPath selectorPath, final ExecutionContext executionContext) throws JaxenException {
        Node nodeUnderTest = null;
        if (selectorPath instanceof IndexedSelectorPath &&
                ((IndexedSelectorPath) selectorPath).getTargetSelectorStep() instanceof AttributeSelectorStep && node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.hasAttributes()) {
                final AttributeSelectorStep attributeSelectorStep = (AttributeSelectorStep) ((IndexedSelectorPath) selectorPath).getTargetSelectorStep();
                final String namespaceURI = attributeSelectorStep.getQName().getNamespaceURI();
                nodeUnderTest = node.getAttributes().getNamedItemNS(namespaceURI.equals(XMLConstants.NULL_NS_URI) ? null : namespaceURI, attributeSelectorStep.getQName().getLocalPart());
            }
        } else {
            nodeUnderTest = node;
        }
        if (nodeUnderTest == null) {
            return false;
        }
        for (int i = selectorPath.size() - 1; i >= 0; i--) {
            if (!selectorPath.get(i).evaluate(new NodeFragment(nodeUnderTest), executionContext)) {
                return false;
            }
            if (nodeUnderTest.getNodeType() == Node.ATTRIBUTE_NODE) {
                nodeUnderTest = ((Attr) nodeUnderTest).getOwnerElement();
            } else {
                nodeUnderTest = nodeUnderTest.getParentNode();
            }
        }

        return true;
    }

    protected boolean isPatternMatch(final Node node, final SelectorPath selectorPath) throws JaxenException {
        final SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext();
        final Properties namespaces = selectorPath.getNamespaces();
        for (String namespacePrefix : namespaces.stringPropertyNames()) {
            simpleNamespaceContext.addNamespace(namespacePrefix, namespaces.getProperty(namespacePrefix));
        }
        final Context context = new Context(new ContextSupport(simpleNamespaceContext, XPathFunctionContext.getInstance(), new SimpleVariableContext(), DocumentNavigator.getInstance()));
        boolean isMatch = true;
        if (selectorPath instanceof IndexedSelectorPath &&
                ((IndexedSelectorPath) selectorPath).getTargetSelectorStep() instanceof AttributeSelectorStep && node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.hasAttributes()) {
                for (int i = 0; i < node.getAttributes().getLength(); i++) {
                    isMatch = ((JaxenPatternSelectorPath) selectorPath).getPattern().matches(node.getAttributes().item(i), context);
                    if (isMatch) {
                        break;
                    }
                }
            } else {
                isMatch = false;
            }
        } else {
            isMatch = ((JaxenPatternSelectorPath) selectorPath).getPattern().matches(node, context);
        }

        return isMatch;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean assertConditionTrue(final ExecutionContext executionContext, final SelectorPath selectorPath) {
        if (selectorPath.getConditionEvaluator() == null) {
            return true;
        }

        return ((ExecutionContextExpressionEvaluator) selectorPath.getConditionEvaluator()).eval(executionContext);
    }

    @Override
    public String toString() {
        return node.getNodeName();
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(getId());
        }
        return hash;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeFragment)) {
            return false;
        }
        return this.getId().equals(((NodeFragment) o).getId());
    }
}