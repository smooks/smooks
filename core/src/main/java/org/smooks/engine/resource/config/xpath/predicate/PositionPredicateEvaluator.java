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
package org.smooks.engine.resource.config.xpath.predicate;

import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.XPathFunctionContext;
import org.jaxen.dom.DocumentNavigator;
import org.jaxen.expr.Expr;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.resource.config.xpath.PredicateEvaluator;
import org.smooks.engine.resource.config.xpath.ElementPositionCounter;
import org.smooks.support.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import java.util.Arrays;

public class PositionPredicateEvaluator implements PredicateEvaluator {
    private final Expr expr;
    private final String elementName;
    private final String elementNS;
    private ElementPositionCounter counter;

    public PositionPredicateEvaluator(Expr expr, String elementName, String elementNS) {
        this.expr = expr;
        this.elementName = elementName;
        this.elementNS = elementNS;
    }

    public void setCounter(ElementPositionCounter positionCounter) {
        this.counter = positionCounter;
    }

    @Override
    public boolean evaluate(Fragment<?> fragment, ExecutionContext executionContext) {
        Element element = (Element) fragment.unwrap();
        int count = counter == null ? 1 : counter.getCount((Element) fragment.unwrap(), executionContext);

        Node parent = element.getParentNode();

        NodeList childNodes = parent.getChildNodes();
        int childNodeCount = childNodes.getLength();

        for (int i = 0; i < childNodeCount; i++) {
            Node childNode = childNodes.item(i);

            if (childNode == element) {
                break;
            }

            if (childNode.getNodeType() == Node.ELEMENT_NODE && DomUtils.getName((Element) childNode).equalsIgnoreCase(elementName)) {
                if (elementNS.equals(XMLConstants.NULL_NS_URI) || elementNS.equals(childNode.getNamespaceURI())) {
                    count++;
                }
            }
        }

        Context context = new Context(new ContextSupport(new SimpleNamespaceContext(), XPathFunctionContext.getInstance(), new SimpleVariableContext(), DocumentNavigator.getInstance()));
        Node unwrap = (Node) fragment.unwrap();
        context.setNodeSet(Arrays.asList(unwrap));

        try {
            return ((double) expr.evaluate(context)) == count;
        } catch (JaxenException e) {
            throw new SmooksException(e);
        }
    }
}