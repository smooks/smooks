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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.delivery.sax.SmooksSAXFilter;
import org.smooks.engine.delivery.sax.ng.terminate.TerminateException;
import org.smooks.io.Stream;
import org.smooks.io.payload.FilterResult;
import org.smooks.io.payload.FilterSource;
import org.smooks.io.payload.JavaSource;
import org.smooks.support.DomUtils;
import org.smooks.support.XmlUtil;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.Writer;

public class SaxNgFilter extends SmooksSAXFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaxNgFilter.class);

    private final SaxNgParser parser;

    public SaxNgFilter(ExecutionContext executionContext) {
        super(executionContext);
        parser = new SaxNgParser(executionContext);
    }

    @Override
    public void doFilter() throws SmooksException {
        Source source = FilterSource.getSource(executionContext);
        Result result;

        result = FilterResult.getResult(executionContext, StreamResult.class);
        if (result == null) {
            result = FilterResult.getResult(executionContext, DOMResult.class);
        }

        doFilter(source, result);
    }

    @Override
    protected void doFilter(final Source source, final Result result) {
        if (!(source instanceof StreamSource || source instanceof JavaSource || source instanceof DOMSource)) {
            throw new IllegalArgumentException("Unsupported " + source.getClass().getName() + " source type: SAX NG filter supports StreamSource, JavaSource, and DOMSource");
        }
        if (!(result instanceof FilterResult)) {
            if (result != null && !(result instanceof StreamResult) && !(result instanceof DOMResult)) {
                throw new IllegalArgumentException("Unsupported " + result.getClass().getName() + " result type: SAX NG filter supports StreamResult and DOMResult.");
            }
        }

        try {
            final Writer writer = getWriter(result, executionContext);
            executionContext.put(Stream.STREAM_WRITER_TYPED_KEY, writer);
            parser.parse(source, executionContext);
            
            if (result instanceof DOMResult) {
                ((DOMResult) result).setNode(XmlUtil.parseStream(new StringReader(writer.toString())));
            } else {
                writer.flush();
            }
        } catch (TerminateException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Terminated filtering on element '" + DomUtils.getXPath(e.getElement()) + "'.");
            }
        } catch (Exception e) {
            throw new SmooksException("Failed to filter source", e);
        } finally {
            if (closeSource) {
                close(source);
            }
            if (closeResult) {
                close(result);
            }
        }
    }
}
