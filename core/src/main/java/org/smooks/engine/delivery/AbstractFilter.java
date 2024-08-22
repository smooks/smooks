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
package org.smooks.engine.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.Filter;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.io.NullReader;
import org.smooks.io.NullWriter;
import org.smooks.io.Stream;
import org.smooks.io.sink.DOMSink;
import org.smooks.io.sink.StreamSink;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.ReaderSource;
import org.smooks.io.source.StreamSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Content filter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class AbstractFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFilter.class);

    /**
     * Set the default stream filter type on the supplied Smooks instance.
     *
     * @param smooks     The Smooks instance.
     * @param filterType The filter type.
     */
    public static void setFilterType(Smooks smooks, org.smooks.StreamFilterType filterType) {
        ParameterAccessor.setParameter(STREAM_FILTER_TYPE, filterType.toString(), smooks);
    }

    protected Reader getReader(Source source, ExecutionContext executionContext) {
        if (source instanceof ReaderSource) {
            ReaderSource<?> readerSource = (ReaderSource<?>) source;
            if (readerSource.getReader() != null) {
                return readerSource.getReader();
            } else {
                throw new SmooksException("Invalid " + ReaderSource.class.getName() + ".  No Reader instance.");
            }
        } else if (source instanceof StreamSource) {
            StreamSource<?> streamSource = (StreamSource<?>) source;
            if (streamSource.getInputStream() != null) {
                try {
                    if (executionContext != null) {
                        return new InputStreamReader(streamSource.getInputStream(), executionContext.getContentEncoding());
                    } else {
                        return new InputStreamReader(streamSource.getInputStream(), StandardCharsets.UTF_8);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new SmooksException("Unable to decode input stream.", e);
                }
            } else {
                throw new SmooksException("Invalid " + StreamSource.class.getName() + ".  No Reader instance.");
            }
        } else {
            return new NullReader();
        }
    }

    protected Writer getWriter(final Sink sink, final ExecutionContext executionContext) {
        if (sink instanceof WriterSink) {
            WriterSink<?> writerSink = (WriterSink<?>) sink;
            Writer writer = writerSink.getWriter();
            if (writer != null) {
                return writerSink.getWriter();
            } else {
                throw new SmooksException(String.format("Invalid [%s]. No Writer instance.", sink.getClass().getName()));
            }
        } else if (sink instanceof StreamSink) {
            StreamSink<?> streamSink = (StreamSink<?>) sink;
            OutputStream outputStream = streamSink.getOutputStream();
            if (outputStream != null) {
                try {
                    if (executionContext != null) {
                        return new OutputStreamWriter(streamSink.getOutputStream(), executionContext.getContentEncoding());
                    } else {
                        return new OutputStreamWriter(streamSink.getOutputStream(), StandardCharsets.UTF_8);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new SmooksException("Unable to encode output stream.", e);
                }
            } else {
                throw new SmooksException(String.format("Invalid [%s]. No OutputStream instance.", sink.getClass().getName()));
            }
        } else if (sink instanceof DOMSink) {
            return new StringWriter();
        } else {
            final Writer writer = Stream.out(executionContext);
            if (writer != null) {
                return writer;
            } else {
                return new NullWriter();
            }
        }
    }

    protected void close(Source source) {
        try {
            if (source instanceof StreamSource) {
                StreamSource<?> streamSource = (StreamSource<?>) source;
                try {
                    if (streamSource.getInputStream() != null) {
                        InputStream inputStream = streamSource.getInputStream();
                        if (inputStream != System.in) {
                            inputStream.close();
                        }
                    }
                } catch (Throwable throwable) {
                    LOGGER.debug("Failed to close input stream/reader.", throwable);
                }
            } else if (source instanceof ReaderSource) {
                ReaderSource<?> readerSource = (ReaderSource<?>) source;
                if (readerSource.getReader() != null) {
                    readerSource.getReader().close();
                }
            }
        } catch (Throwable throwable) {
            LOGGER.debug("Failed to close input stream/reader.", throwable);
        }
    }

    protected void close(Sink sink) {
        try {
            if (sink instanceof StreamSink) {
                StreamSink<?> streamSink = ((StreamSink<?>) sink);
                if (streamSink.getOutputStream() != null) {
                    OutputStream outputStream = streamSink.getOutputStream();
                    try {
                        outputStream.flush();
                    } finally {
                        // Close the stream as long as it's not sysout or syserr...
                        if (outputStream != System.out && outputStream != System.err) {
                            outputStream.close();
                        }
                    }
                }
            } else if (sink instanceof WriterSink) {
                WriterSink<?> writerSink = ((WriterSink<?>) sink);
                try (Writer writer = writerSink.getWriter()) {
                    writer.flush();
                }
            }
        } catch (Throwable throwable) {
            LOGGER.debug("Failed to close output stream/writer.  May already be closed.", throwable);
        }
    }
}
