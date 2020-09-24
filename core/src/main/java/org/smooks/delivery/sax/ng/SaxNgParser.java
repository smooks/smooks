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
package org.smooks.delivery.sax.ng;

import org.smooks.container.ExecutionContext;
import org.smooks.delivery.AbstractParser;
import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.delivery.XMLReaderHierarchyChangeListener;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.smooks.xml.NamespaceManager;
import org.smooks.xml.hierarchy.HierarchyChangeReader;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.Source;
import java.io.IOException;
import java.nio.charset.Charset;

public class SaxNgParser extends AbstractParser {

    private SaxNgHandler saxHandler;

    public SaxNgParser(ExecutionContext execContext) {
        super(execContext);
    }

    protected void parse(Source source, ExecutionContext executionContext) throws SAXException, IOException {
        ContentDeliveryConfig deliveryConfig = executionContext.getDeliveryConfig();
        XMLReader saxReader = getXMLReader(executionContext);

        saxHandler = new SaxNgHandler(getExecutionContext());

        try {
            if(saxReader == null) {
                saxReader = deliveryConfig.getXMLReader();
            }
            if(saxReader == null) {
                saxReader = createXMLReader();
            }

            NamespaceDeclarationStack namespaceDeclarationStack = new NamespaceDeclarationStack();
            NamespaceManager.setNamespaceDeclarationStack(namespaceDeclarationStack, executionContext);

            attachNamespaceDeclarationStack(saxReader, executionContext);
            attachXMLReader(saxReader, executionContext);

            configureReader(saxReader, saxHandler, executionContext, source);
            if(executionContext != null) {
                if(saxReader instanceof HierarchyChangeReader) {
                    ((HierarchyChangeReader)saxReader).setHierarchyChangeListener(new XMLReaderHierarchyChangeListener(executionContext));
                }
	            saxReader.parse(createInputSource(source, executionContext.getContentEncoding()));
            } else {
                saxReader.parse(createInputSource(source, Charset.defaultCharset().name()));
            }
        } finally {
            try {
                if(executionContext != null && saxReader instanceof HierarchyChangeReader) {
                    ((HierarchyChangeReader)saxReader).setHierarchyChangeListener(null);
                }
            } finally {
                try {
                    if(saxReader != null) {
                        try {
                            detachXMLReader(executionContext);
                        } finally {
                            deliveryConfig.returnXMLReader(saxReader);
                        }
                    }
                } finally {
                    saxHandler.detachHandler();
                }
            }
        }
    }

    public void cleanup() {
        if(saxHandler != null) {
            saxHandler.cleanup();
        }
    }
}