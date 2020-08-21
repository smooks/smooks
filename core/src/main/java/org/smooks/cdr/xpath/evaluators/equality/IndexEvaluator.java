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
package org.smooks.cdr.xpath.evaluators.equality;

import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;

/**
 * Simple element index predicate evaluator.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class IndexEvaluator extends XPathExpressionEvaluator {

    private int index;
    private ElementIndexCounter counter;
    private String elementName;
    private String elementNS;

    public IndexEvaluator(int index, SelectorStep selectorStep) {
        this.index = index;
        elementName = selectorStep.getTargetElement().getLocalPart();
        elementNS = selectorStep.getTargetElement().getNamespaceURI();
        if(elementNS == XMLConstants.NULL_NS_URI) {
            elementNS = null;
        }
    }

    public ElementIndexCounter getCounter() {
        return counter;
    }

    public void setCounter(ElementIndexCounter indexCounter) {
        this.counter = indexCounter;
    }

    public boolean evaluate(SAXElement element, ExecutionContext executionContext) {
        return counter.getCount(element) == index;
    }

    public boolean evaluate(Element element, ExecutionContext executionContext) {
        Node parent = element.getParentNode();

        if(parent == null) {
            return (index == 0);
        }

        NodeList siblings = parent.getChildNodes();
        int count = 0;
        int siblingCount = siblings.getLength();

        for(int i = 0; i < siblingCount; i++) {
            Node sibling = siblings.item(i);

            if(sibling.getNodeType() == Node.ELEMENT_NODE && DomUtils.getName((Element) sibling).equalsIgnoreCase(elementName)) {
                if(elementNS == null || elementNS.equals(sibling.getNamespaceURI())) {
                    count++;
                }
            }

            if(sibling == element) {
                break;
            }
        }

        return (index == count);
    }

    public String toString() {
        return "[" + index + "]";
    }
}
