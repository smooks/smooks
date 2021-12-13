/*-
 * ========================LICENSE_START=================================
 * Benchmark
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
package org.smooks.benchmark;

import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.io.Stream;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

public class BibliographyVisitor implements AfterVisitor {
    private final static String TEMPLATE = "<entry><author>%s</author><title>%s</title></entry>";
    private DOMXPath domXPath;
    private DOMXPath titleXPath;

    @PostConstruct
    public void postConstruct() throws JaxenException {
        this.domXPath = new DOMXPath("//author");
        this.titleXPath = new DOMXPath("//title");
    }
    
    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        try {
            List<Element> authors = ((List<Element>)domXPath.evaluate(element));
            List<Element> titles = ((List<Element>)titleXPath.evaluate(element));
            Stream.out(executionContext).write(String.format(TEMPLATE, authors.isEmpty() ? "N/A" : authors.get(0).getTextContent(), "<![CDATA[" + (titles.isEmpty() ? "N/A" : titles.get(0).getTextContent())) + "]]>");
        } catch (IOException | JaxenException e) {
            throw new SmooksException(e);
        }
    }
}
