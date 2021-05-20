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
package org.smooks.engine.resource.config.xpath.evaluators.equality;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.config.xpath.XPathExpressionEvaluator;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.support.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;

/**
 * Simple element position predicate evaluator.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class PositionEvaluator implements XPathExpressionEvaluator {

    private final int position;
    private ElementPositionCounter counter;
    private final String elementName;
    private String elementNS;

    public PositionEvaluator(int position, SelectorStep selectorStep) {
        this.position = position;
        elementName = selectorStep.getElement().getLocalPart();
        elementNS = selectorStep.getElement().getNamespaceURI();
        if (elementNS.equals(XMLConstants.NULL_NS_URI)) {
            elementNS = null;
        }
    }

    public ElementPositionCounter getCounter() {
        return counter;
    }

    public void setCounter(ElementPositionCounter positionCounter) {
        this.counter = positionCounter;
    }

    protected boolean evaluate(Element element, ExecutionContext executionContext) {
        int count;
        if (counter == null) {
            count = 0;
        } else {
            count = counter.getCount(element, executionContext) - 1;
        }
        Node parent = element.getParentNode();

        if (parent == null) {
            return position == 0;
        }

        NodeList childNodes = parent.getChildNodes();
        int childNodeCount = childNodes.getLength();

        for (int i = 0; i < childNodeCount; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE && DomUtils.getName((Element) childNode).equalsIgnoreCase(elementName)) {
                if (elementNS == null || elementNS.equals(childNode.getNamespaceURI())) {
                    count++;
                }
            }

            if (childNode == element) {
                break;
            }
        }

        return position == count;
    }

    @Override
    public String toString() {
        return "[" + position + "]";
    }

    @Override
    public boolean evaluate(Fragment<?> fragment, ExecutionContext executionContext) {
        if (fragment instanceof NodeFragment) {
            return evaluate((Element) ((NodeFragment) fragment).unwrap(), executionContext);
        }
        
        throw new UnsupportedOperationException();
    }
}
