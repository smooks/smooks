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
package org.milyn.cdr.xpath.evaluators.equality;

import org.milyn.cdr.xpath.SelectorStep;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitBefore;

import java.io.IOException;

/**
 * Element index counter.
 * <p/>
 * Used for index based XPath predicates.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class ElementIndexCounter implements SAXVisitBefore {

    private SelectorStep selectorStep;

    public ElementIndexCounter(SelectorStep selectorStep) {
        this.selectorStep = selectorStep;
    }

    public SelectorStep getSelectorStep() {
        return selectorStep;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        ElementIndex index = getElementIndex(element);
        if (index != null) {
            index.i++;
        }
    }

    protected int getCount(SAXElement element) {
        ElementIndex index = getElementIndex(element);
        if (index != null) {
            return index.i;
        }
        return 0;
    }

    private ElementIndex getElementIndex(SAXElement element) {
        SAXElement parent = element.getParent();
        ElementIndex index;

        if (parent != null) {
            index = (ElementIndex) parent.getCache(this);
            if (index == null) {
                index = new ElementIndex();
                parent.setCache(this, index);
            }
            return index;
        }

        return null;
    }

    private class ElementIndex {
        private int i = 0;
    }
}
