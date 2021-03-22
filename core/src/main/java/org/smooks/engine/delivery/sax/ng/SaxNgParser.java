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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.ExecutionContext;
import org.smooks.engine.delivery.AbstractParser;
import org.smooks.api.delivery.ReaderPool;
import org.smooks.engine.delivery.XMLReaderHierarchyChangeListener;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.smooks.engine.xml.NamespaceManager;
import org.smooks.xml.hierarchy.HierarchyChangeReader;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Source;
import java.io.Closeable;
import java.io.IOException;

public class SaxNgParser extends AbstractParser implements Closeable {

    private final DocumentBuilder documentBuilder;
    private SaxNgHandler saxHandler;

    public SaxNgParser(final ExecutionContext executionContext, final DocumentBuilder documentBuilder) {
        super(executionContext);
        this.documentBuilder = documentBuilder;
    }

    protected void parse(Source source, ExecutionContext executionContext) throws SAXException, IOException {
        saxHandler = new SaxNgHandler(getExecutionContext(), documentBuilder);
        ReaderPool readerPool = executionContext.getContentDeliveryRuntime().getReaderPool();
        
        XMLReader saxReader = null;
        try {
            saxReader = readerPool.borrowXMLReader();
            if (saxReader == null) {
                saxReader = createXMLReader();
            }

            executionContext.put(NamespaceManager.NAMESPACE_DECLARATION_STACK_TYPED_KEY, new NamespaceDeclarationStack());
            configureReader(saxReader, saxHandler, executionContext, source);
            if (saxReader instanceof HierarchyChangeReader) {
                ((HierarchyChangeReader) saxReader).setHierarchyChangeListener(new XMLReaderHierarchyChangeListener(executionContext));
            }
            saxReader.parse(createInputSource(source, executionContext.getContentEncoding()));
        } finally {
            try {
                if (saxReader instanceof HierarchyChangeReader) {
                    ((HierarchyChangeReader) saxReader).setHierarchyChangeListener(null);
                }
            } finally {
                try {
                    if (saxReader != null) {
                        try {
                            detachXMLReader(executionContext);
                        } finally {
                            readerPool.returnXMLReader(saxReader);
                        }
                    }
                } finally {
                    saxHandler.detachHandler();
                }
            }
        }
    }

    @Override
    public void close() {
        if (saxHandler != null) {
            saxHandler.close();
        }
    }
}