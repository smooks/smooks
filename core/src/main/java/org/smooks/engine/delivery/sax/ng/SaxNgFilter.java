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
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.engine.delivery.AbstractFilter;
import org.smooks.engine.delivery.sax.ng.terminate.TerminateException;
import org.smooks.io.Stream;
import org.smooks.io.sink.DOMSink;
import org.smooks.io.sink.FilterSink;
import org.smooks.io.sink.StreamSink;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.DOMSource;
import org.smooks.io.source.FilterSource;
import org.smooks.io.source.JavaSource;
import org.smooks.io.source.ReaderSource;
import org.smooks.io.source.StreamSource;
import org.smooks.io.source.URLSource;
import org.smooks.support.DomUtils;
import org.smooks.support.XmlUtils;

import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.io.Writer;

public class SaxNgFilter extends AbstractFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaxNgFilter.class);

    protected final ExecutionContext executionContext;
    protected final boolean closeSource;
    protected final boolean closeSink;
    protected final SaxNgParser parser;

    public SaxNgFilter(ExecutionContext executionContext, DocumentBuilder documentBuilder, boolean closeSource, boolean closeSink) {
        this.executionContext = executionContext;
        this.closeSource = closeSource;
        this.closeSink = closeSink;
        parser = new SaxNgParser(executionContext, documentBuilder);
    }

    @Override
    public void doFilter() throws SmooksException {
        Source source = FilterSource.getSource(executionContext);
        Sink sink = FilterSink.getSink(executionContext, StreamSink.class);
        if (sink == null) {
            sink = FilterSink.getSink(executionContext, WriterSink.class);
            if (sink == null) {
                sink = FilterSink.getSink(executionContext, DOMSink.class);
            }
        }

        doFilter(source, sink);
    }

    protected void doFilter(Source source, Sink sink) {
        if (!(source instanceof StreamSource || source instanceof ReaderSource || source instanceof JavaSource || source instanceof DOMSource || source instanceof URLSource)) {
            throw new SmooksException(String.format("Unsupported [%s] source type: SAX NG filter supports StreamSource, JavaSource, DOMSource, and URLSource", source.getClass().getName()));
        }
        if (!(sink instanceof FilterSink)) {
            if (sink != null && !(sink instanceof StreamSink) && !(sink instanceof WriterSink) && !(sink instanceof DOMSink)) {
                throw new SmooksException(String.format("Unsupported [%s] sink type: SAX NG filter supports StreamSink and DOMSink", sink.getClass().getName()));
            }
        }

        try {
            final Writer writer = getWriter(sink, executionContext);
            executionContext.put(Stream.STREAM_WRITER_TYPED_KEY, writer);
            parser.parse(source, executionContext);

            if (sink instanceof DOMSink) {
                ((DOMSink) sink).setNode(XmlUtils.parseStream(new StringReader(writer.toString())));
            } else {
                writer.flush();
            }
        } catch (TerminateException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Terminated filtering on element {}", DomUtils.getXPath(e.getElement()));
            }
        } catch (Exception e) {
            throw new SmooksException("Failed to filter source", e);
        } finally {
            if (closeSource) {
                close(source);
            }
            if (closeSink) {
                close(sink);
            }
        }
    }

    @Override
    public void close() {
        parser.close();
    }
}
