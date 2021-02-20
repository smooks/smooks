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
package org.smooks.engine.delivery.fragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.config.xpath.XPathExpressionEvaluator;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.expression.ExecutionContextExpressionEvaluator;
import org.smooks.support.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class NodeFragment implements Fragment<Node> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFragment.class);

    public static final String RESERVATIONS_USER_DATA_KEY = "reservations";
    public static final String ID_USER_DATA_KEY = "id";

    private final Node node;
    private final boolean isReservationInheritable;

    static class CopyUserDataHandler implements UserDataHandler {
        CopyUserDataHandler() {
            
        }
        
        @Override
        public void handle(final short operation, final String key, final Object data, final Node src, final Node dst) {
            dst.setUserData(key, data, new CopyUserDataHandler());
        }
    }

    static class Reservation {
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

        final CopyUserDataHandler copyUserDataHandler = new CopyUserDataHandler();
        if (node.getUserData(ID_USER_DATA_KEY) == null) {
            node.setUserData(ID_USER_DATA_KEY, String.valueOf(Math.abs(ThreadLocalRandom.current().nextLong())), copyUserDataHandler);
        }

        Map<Long, Reservation> reservations = (Map<Long, Reservation>) node.getUserData(RESERVATIONS_USER_DATA_KEY);
        if (reservations == null) {
            reservations = new HashMap<>();
            node.setUserData(RESERVATIONS_USER_DATA_KEY, reservations, copyUserDataHandler);
        } else {
            reservations = new HashMap<>();
        }

        Node parentNode = node.getParentNode();
        while (parentNode != null) {
            final Map<Long, Reservation> parentNodeReservations = (Map<Long, Reservation>) parentNode.getUserData(RESERVATIONS_USER_DATA_KEY);
            if (parentNodeReservations != null) {
                reservations.putAll(parentNodeReservations.entrySet().stream().filter(e -> e.getValue().isInheritable()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
            parentNode = parentNode.getParentNode();
        }
    }

    @Override
    public String getId() {
        return (String) node.getUserData(ID_USER_DATA_KEY);
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
    public boolean isMatch(SelectorPath selectorPath, ExecutionContext executionContext) {
        if (!assertConditionTrue(executionContext, selectorPath)) {
            return false;
        }

        if (selectorPath.getSelectorNamespaceURI() != null) {
            if (!isTargetedAtNamespace(node.getNamespaceURI(), selectorPath.getSelectorNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(node) + "].  Element not in namespace [" + selectorPath.getSelectorNamespaceURI() + "].");
                }
                return false;
            }
        } else {
            // We don't test the SelectorStep namespace if a namespace is configured on the
            // resource configuration.  This is why we have this code inside the else block.
            if (!selectorPath.getTargetSelectorStep().isTargetedAtNamespace(node.getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(node) + "].  Element not in namespace [" + selectorPath.getTargetSelectorStep().getElement().getNamespaceURI() + "].");
                }
                return false;
            }
        }

        XPathExpressionEvaluator evaluator = selectorPath.getTargetSelectorStep().getPredicatesEvaluator();
        if (evaluator == null) {
            LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
        } else if (!evaluator.evaluate(this, executionContext)) {
            return false;
        }

        if (!isTarget(node, selectorPath, executionContext)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(node) + "].  This resource is only targeted at '" + DomUtils.getName((Element) node) + "' when in the following context '" + selectorPath.getSelector() + "'.");
            }
            return false;
        }

        return true;
    }

    protected boolean isMaybeTarget(Node node, SelectorStep selectorStep) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (selectorStep.isStar() || selectorStep.isStarStar()) {
                return true;
            }

            if (!(DomUtils.getName((Element) node).equals(selectorStep.getElement().getLocalPart()) || (selectorStep.getElement().getLocalPart().equals(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR) && (node.getParentNode() == null || node.getParentNode().equals(node.getOwnerDocument()))))) {
                return false;
            }

            return selectorStep.isTargetedAtNamespace(node.getNamespaceURI());
        } else {
            return false;
        }
    }
    
    /**
     * Is this resource configuration targeted at the specified DOM element
     * in context.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     * <p/>
     * Note this doesn't perform any namespace checking.
     *
     * @param node          The element to check against.
     * @param executionContext The current execution context.
     * @return True if this resource configuration is targeted at the specified
     * element in context, otherwise false.
     */
    protected boolean isTarget(Node node, SelectorPath selectorPath, ExecutionContext executionContext) {
        Node currentNode = node;
        ContextIndex index = new ContextIndex(executionContext);

        index.i = selectorPath.size() - 1;
        
        if (selectorPath.get(index.i).isStarStar()) {
            // The target selector step is "**".  If the parent one is "#document" and we're at
            // the root now, then fail...
            if (selectorPath.size() == 2 && selectorPath.get(0).isRooted() && node.getParentNode() == null) {
                return false;
            }
        }

        if (currentNode == null || currentNode.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }

        // Check the element name(s).
        while (index.i >= 0) {
            Element currentElement = (Element) currentNode;
            Node parentNode;

            parentNode = currentElement.getParentNode();
            if (parentNode == null || parentNode.getNodeType() != Node.ELEMENT_NODE) {
                parentNode = null;
            }

            if (!isTarget(currentElement, (Element) parentNode, index, selectorPath)) {
                return false;
            }

            if (parentNode == null) {
                return true;
            }

            currentNode = parentNode;
        }

        return true;
    }

    protected boolean isTarget(Element element, Element parentElement, ContextIndex index, SelectorPath selectorPath) {
        if (selectorPath.get(index.i).isRooted() && parentElement != null) {
            return false;
        } else if (selectorPath.get(index.i).isStar()) {
            index.i--;
        } else if (selectorPath.get(index.i).isStarStar()) {
            if (index.i == 0) {
                // No more tokens to match and ** matches everything
                return true;
            } else if (index.i == 1) {
                SelectorStep parentStep = selectorPath.get(0);

                if (parentElement == null && parentStep.isRooted()) {
                    // we're at the root of the document and the only selector left is
                    // the document selector.  Pass..
                    return true;
                } else if (parentElement == null) {
                    // we're at the root of the document, yet there are still
                    // unmatched tokens in the selector.  Fail...
                    return false;
                }
            } else if (parentElement == null) {
                // we're at the root of the document, yet there are still
                // unmatched tokens in the selector.  Fail...
                return false;
            }

            SelectorStep parentStep = selectorPath.get(index.i - 1);

            if (isMaybeTarget(parentElement, parentStep)) {
                if (!parentStep.isStarStar()) {
                    XPathExpressionEvaluator evaluator = parentStep.getPredicatesEvaluator();
                    if (evaluator == null) {
                        LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                    } else if (!evaluator.evaluate(new NodeFragment(parentElement), index.executionContext)) {
                        return false;
                    }
                }
                index.i--;
            }
        } else if (!isMaybeTarget(element, selectorPath.get(index.i))) {
            return false;
        } else {
            if (!selectorPath.get(index.i).isStarStar()) {
                XPathExpressionEvaluator evaluator = selectorPath.get(index.i).getPredicatesEvaluator();
                if (evaluator == null) {
                    LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                } else if (!evaluator.evaluate(new NodeFragment(element), index.executionContext)) {
                    return false;
                }
            }
            index.i--;
        }

        if (parentElement == null) {
            if (index.i >= 0 && !selectorPath.get(index.i).isStarStar()) {
                return selectorPath.get(index.i).isRooted();
            }
        }

        return true;
    }

    protected boolean isTargetedAtNamespace(String namespace, String namespaceURI) {
        if (namespaceURI != null) {
            return namespaceURI.equals(namespace);
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean assertConditionTrue(ExecutionContext executionContext, SelectorPath selectorPath) {
        if (selectorPath.getConditionEvaluator() == null) {
            return true;
        }

        return ((ExecutionContextExpressionEvaluator) selectorPath.getConditionEvaluator()).eval(executionContext);
    }

    protected static class ContextIndex {
        private int i;
        private final ExecutionContext executionContext;

        public ContextIndex(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }
    }
    
    @Override
    public String toString() {
        return node.getNodeName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeFragment)) {
            return false;
        }
        return this.getId().equals(((NodeFragment) o).getId());
    }
}