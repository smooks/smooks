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

import org.smooks.api.SmooksException;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.visitor.sax.SAXVisitBefore;

import java.io.IOException;

/**
 * Element index counter.
 * <p/>
 * Used for index based XPath predicates.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class ElementIndexCounter implements SAXVisitBefore {

    private final SelectorStep selectorStep;

    public ElementIndexCounter(SelectorStep selectorStep) {
        this.selectorStep = selectorStep;
    }

    public SelectorStep getSelectorStep() {
        return selectorStep;
    }

    @Override
    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        ElementIndex index = getElementIndex(element);
        if(index != null) {
            index.i++;
        }
    }

    protected int getCount(SAXElement element) {
        ElementIndex index = getElementIndex(element);
        if(index != null) {
            return index.i;
        }
        return 0;
    }

    private ElementIndex getElementIndex(SAXElement element) {
        SAXElement parent = element.getParent();
        ElementIndex index;

        if(parent != null) {
            index = (ElementIndex) parent.getCache(this);
            if(index == null) {
                index = new ElementIndex();
                parent.setCache(this, index);
            }
            return index;
        }

        return null;
    }

    private static class ElementIndex {
        private int i = 0;
    }
}
