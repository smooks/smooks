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
package org.smooks.engine.resource.reader;

import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.StreamFilterType;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedKey;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.delivery.sax.ng.bridge.Bridge;
import org.smooks.engine.resource.config.SystemResourceConfigSeqFactory;
import org.smooks.io.DocumentInputSource;
import org.smooks.io.SAXWriter;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import jakarta.annotation.PostConstruct;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class DelegateReader implements SmooksXMLReader {
    private final TypedKey<Writer> contentHandlerTypedKey = TypedKey.of();
    private final TypedKey<ExecutionContext> executionContextTypedKey = TypedKey.of();

    private ContentHandler contentHandler;
    private Smooks readerSmooks;
    private ErrorHandler errorHandler;
    private ExecutionContext executionContext;

    @Inject
    private ResourceConfig resourceConfig;

    @Inject
    private ApplicationContext applicationContext;

    private DocumentBuilder documentBuilder;

    @PostConstruct
    public void postConstruct() {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SmooksException(e);
        }
        final String smooksResourceList = "<smooks-resource-list xmlns=\"https://www.smooks.org/xsd/smooks-2.0.xsd\">" + resourceConfig.getParameter("resourceConfigs", String.class).getValue() + "</smooks-resource-list>";
        ResourceConfigSeq resourceConfigSeq = applicationContext.getResourceConfigLoader().load(new ByteArrayInputStream(smooksResourceList.getBytes(StandardCharsets.UTF_8)), "./", applicationContext.getClassLoader());

        readerSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).withClassLoader(applicationContext.getClassLoader()).build());
        readerSmooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX_NG).setCloseResult(false).setReaderPoolSize(-1));
        readerSmooks.getApplicationContext().getRegistry().registerResourceConfigSeq(new SystemResourceConfigSeqFactory("/nested-smooks-interceptors.xml",
                readerSmooks.getApplicationContext().getClassLoader(), applicationContext.getResourceLocator(), applicationContext.getResourceConfigLoader()).create());

        for (ResourceConfig resourceConfig : resourceConfigSeq) {
            readerSmooks.addResourceConfig(resourceConfig);
        }
    }

    @Override
    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {

    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setDTDHandler(DTDHandler dtdHandler) {
    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(final ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void parse(final InputSource inputSource) throws IOException, SAXException {
        final Document document;
        if (inputSource instanceof DocumentInputSource) {
            document = ((DocumentInputSource) inputSource).getDocument();
        } else {
            document = documentBuilder.parse(inputSource);
            document.setStrictErrorChecking(false);
        }

        ExecutionContext readerExecutionContext = executionContext.get(executionContextTypedKey);
        if (readerExecutionContext == null) {
            readerExecutionContext = readerSmooks.createExecutionContext();
            executionContext.put(executionContextTypedKey, readerExecutionContext);
        }

        if (Bridge.isBridge(document.getFirstChild())) {
            final Bridge bridge = new Bridge(document.getFirstChild());
            readerExecutionContext.put(bridge.getSourceKey(), bridge.getSourceValue(executionContext));
        }

        if (executionContext.get(contentHandlerTypedKey) == null) {
            executionContext.put(contentHandlerTypedKey, new SAXWriter(contentHandler));
        }
        StreamResult streamResult = new StreamResult();
        streamResult.setWriter(executionContext.get(contentHandlerTypedKey));
        readerSmooks.filterSource(readerExecutionContext, new DOMSource(document), streamResult);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {

    }
}
