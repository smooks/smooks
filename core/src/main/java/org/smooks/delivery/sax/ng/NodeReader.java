/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.delivery.sax.ng;

import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.StreamFilterType;
import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.ResourceConfigList;
import org.smooks.cdr.XMLConfigDigester;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.container.TypedKey;
import org.smooks.container.standalone.DefaultApplicationContextBuilder;
import org.smooks.delivery.DOMInputSource;
import org.smooks.delivery.SAXWriter;
import org.smooks.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.delivery.interceptor.InterceptorVisitorDefinition;
import org.smooks.delivery.sax.ng.session.Session;
import org.smooks.delivery.sax.ng.session.SessionInterceptor;
import org.smooks.xml.SmooksXMLReader;
import org.w3c.dom.Node;
import org.xml.sax.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NodeReader implements SmooksXMLReader {
    private static final Map<String, Smooks> SMOOKS_READERS = new ConcurrentHashMap<>();
    
    private final TypedKey<Writer> contentHandlerTypedKey = new TypedKey<>();
    private final TypedKey<ExecutionContext> executionContextTypedKey = new TypedKey<>();
    
    private ContentHandler contentHandler;
    private Smooks readerSmooks;
    private ErrorHandler errorHandler;
    private ExecutionContext executionContext;

    @Inject
    private ResourceConfig resourceConfig;

    @Inject
    private ApplicationContext applicationContext;

    @PostConstruct
    public void postConstruct() {
        final String smooksResourceList = "<smooks-resource-list xmlns=\"https://www.smooks.org/xsd/smooks-2.0.xsd\">" + resourceConfig.getParameter("resourceConfigs", String.class).getValue() + "</smooks-resource-list>";
        readerSmooks = SMOOKS_READERS.computeIfAbsent(smooksResourceList, k -> {
            final ResourceConfigList resourceConfigList;
            try {
                resourceConfigList = XMLConfigDigester.digestConfig(new ByteArrayInputStream(smooksResourceList.getBytes(StandardCharsets.UTF_8)), "./", new HashMap<>(), applicationContext.getClassLoader());
            } catch (URISyntaxException | SAXException | IOException e) {
                throw new SmooksException(e);
            }
            final Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).setClassLoader(applicationContext.getClassLoader()).build());
            smooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX_NG).setCloseResult(false).setReaderPoolSize(1));
            for (ResourceConfig resourceConfig : resourceConfigList) {
                smooks.addConfiguration(resourceConfig);
            }

            final InterceptorVisitorChainFactory interceptorVisitorChainFactory = new InterceptorVisitorChainFactory();
            InterceptorVisitorDefinition interceptorVisitorDefinition = new InterceptorVisitorDefinition();
            interceptorVisitorDefinition.setSelector(Optional.of("*"));
            interceptorVisitorDefinition.setClass(SessionInterceptor.class);
            interceptorVisitorChainFactory.getInterceptorVisitorDefinitions().add(interceptorVisitorDefinition);
            
            smooks.getApplicationContext().getRegistry().registerObject(interceptorVisitorChainFactory);

            return smooks;
        });
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
        final Node node;
        if (inputSource instanceof DOMInputSource) {
            node = ((DOMInputSource) inputSource).getNode();
        } else {
            try {
                node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
            } catch (ParserConfigurationException e) {
                throw new SmooksException(e);
            }
        }
        
        ExecutionContext readerExecutionContext = executionContext.get(executionContextTypedKey);
        if (readerExecutionContext == null) {
            readerExecutionContext = readerSmooks.createExecutionContext();
            executionContext.put(executionContextTypedKey, readerExecutionContext);   
        }
        
        if (Session.isSession(node)) {
            final Session session = new Session(node);
            readerExecutionContext.put(session.getSourceKey(), session.getSourceValue(executionContext));
        }
        
        if (executionContext.get(contentHandlerTypedKey) == null) {
            executionContext.put(contentHandlerTypedKey, new SAXWriter(contentHandler));
        }
        StreamResult streamResult = new StreamResult();
        streamResult.setWriter(executionContext.get(contentHandlerTypedKey));
        readerSmooks.filterSource(readerExecutionContext, new DOMSource(node), streamResult);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {

    }
}