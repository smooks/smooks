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
package org.smooks.engine.resource.reader;

import com.thoughtworks.xstream.io.xml.SaxWriter;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.reader.JavaXMLReader;
import org.smooks.io.payload.JavaSource;
import org.xml.sax.*;

import jakarta.annotation.PostConstruct;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * XStream based {@link JavaXMLReader}.
 * <p/>
 * This is the default Java {@link XMLReader} for Smooks.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XStreamXMLReader implements JavaXMLReader {

    @Inject
    private Boolean includeEnclosingDocument = true;

    private SaxWriter xstreamReader;

    @PostConstruct
    public void postConstruct() {
        xstreamReader = new SaxWriter(includeEnclosingDocument);
    }

    @Override
    public void setSourceObjects(List<Object> sourceObjects) throws SmooksConfigException {
        try {
            xstreamReader.setProperty(SaxWriter.SOURCE_OBJECT_LIST_PROPERTY, sourceObjects);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new SmooksConfigException("Unable to set source Java Objects on the underlying XStream SaxWriter.", e);
        }
    }

    @Override
    public void setExecutionContext(ExecutionContext executionContext) {
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return xstreamReader.getFeature(name);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // Need to ignore some features....
        if (name.equals(JavaSource.FEATURE_GENERATE_EVENT_STREAM)) {
            return;
        }
        xstreamReader.setFeature(name, value);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return xstreamReader.getProperty(name);
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        xstreamReader.setProperty(name, value);
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        xstreamReader.setEntityResolver(resolver);
    }

    @Override
    public EntityResolver getEntityResolver() {
        return xstreamReader.getEntityResolver();
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        xstreamReader.setDTDHandler(handler);
    }

    @Override
    public DTDHandler getDTDHandler() {
        return xstreamReader.getDTDHandler();
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        xstreamReader.setContentHandler(handler);
    }

    @Override
    public ContentHandler getContentHandler() {
        return xstreamReader.getContentHandler();
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        xstreamReader.setErrorHandler(handler);
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return xstreamReader.getErrorHandler();
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        xstreamReader.parse(input);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {
        xstreamReader.parse(systemId);
    }
}
