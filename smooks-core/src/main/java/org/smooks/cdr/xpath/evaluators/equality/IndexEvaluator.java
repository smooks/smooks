/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.cdr.xpath.evaluators.equality;

import org.smooks.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.container.ExecutionContext;
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