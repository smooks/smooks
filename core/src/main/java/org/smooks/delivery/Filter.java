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
package org.smooks.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.cdr.ParameterAccessor;
import org.smooks.container.ExecutionContext;
import org.smooks.io.NullReader;
import org.smooks.io.NullWriter;
import org.smooks.io.Stream;
import org.smooks.payload.FilterResult;
import org.smooks.payload.FilterSource;
import org.smooks.thread.StackedThreadLocal;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Content filter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Filter.class);

    /**
     * Stream filter type config parameter.
     */
    public static final String STREAM_FILTER_TYPE = "stream.filter.type";
    
    /**
     * The Threadlocal storage instance for the ExecutionContext associated with the "current" SmooksDOMFilter thread instance.
     */
    private static final StackedThreadLocal<Filter> filterThreadLocal = new StackedThreadLocal<Filter>("Filter");

    public static final String CLOSE_SOURCE = "close.source";

    public static final String CLOSE_RESULT = "close.result";

    public static final String ENTITIES_REWRITE = "entities.rewrite";

    public static final String CLOSE_EMPTY_ELEMENTS = "close.empty.elements";

    public static final String DEFAULT_SERIALIZATION_ON = "default.serialization.on";

    public static final String MAINTAIN_ELEMENT_STACK = "maintain.element.stack";

    public static final String MAX_NODE_DEPTH = "max.node.depth";
    
    public static final String REVERSE_VISIT_ORDER_ON_VISIT_AFTER = "reverse.visit.order.on.visit.after";

    public static final String TERMINATE_ON_VISITOR_EXCEPTION = "terminate.on.visitor.exception";

    public static final String READER_POOL_SIZE = "reader.pool.size";

    /**
     * Filter the content in the supplied {@link javax.xml.transform.Source} instance, outputing the result
     * to the supplied {@link javax.xml.transform.Result} instance.
     * <p/>
     * Implementations use static methods on the {@link FilterSource} and {@link FilterResult} classes
     * to access the {@link Source} and {@link Result Results} objects.
     *
     * @throws SmooksException Failed to filter.
     */
    public abstract void doFilter() throws SmooksException;

    /**
     * Cleanup the Filter.
     */
    public abstract void cleanup();

    /**
     * Set the default stream filter type on the supplied Smooks instance.
     * @param smooks The Smooks instance.
     * @param filterType The filter type.
     */
    public static void setFilterType(Smooks smooks, org.smooks.StreamFilterType filterType) {
        ParameterAccessor.setParameter(STREAM_FILTER_TYPE, filterType.toString(), smooks);
    }

    /**
     * Get the {@link Filter} instance for the current thread.
     *
     * @return The thread-bound {@link Filter} instance.
     */
    public static Filter getFilter() {
        Filter filter = Filter.filterThreadLocal.get();
        if(filter == null) {
            throw new IllegalStateException("Call to getFilter() before the filter is set for the Thread.  This method can only be called within the context of a Smooks execution, which sets the filter.");
        }
        return filter;
    }

    /**
     * Set the {@link Filter} instance for the current thread.
     *
     * @param filter The thread-bound {@link Filter} instance.
     */
    public static void setFilter(Filter filter) {
        Filter.filterThreadLocal.set(filter);
    }

    /**
     * Remove the {@link Filter} bound to the current thread.
     */
    public static void removeCurrentFilter() {
        Filter.filterThreadLocal.remove();
    }
    
    protected Reader getReader(Source source, ExecutionContext executionContext) {
        if(source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource) source;
            if(streamSource.getReader() != null) {
                return streamSource.getReader();
            } else if(streamSource.getInputStream() != null) {
                try {
                    if(executionContext instanceof ExecutionContext) {
                        return new InputStreamReader(streamSource.getInputStream(), executionContext.getContentEncoding());
                    } else {
                        return new InputStreamReader(streamSource.getInputStream(), StandardCharsets.UTF_8);
                    }
                } catch(UnsupportedEncodingException e) {
                    throw new SmooksException("Unable to decode input stream.", e);
                }
            } else {
                throw new SmooksException("Invalid " + StreamSource.class.getName() + ".  No InputStream or Reader instance.");
            }
        }

        return new NullReader();
    }

    protected Writer getWriter(final Result result, final ExecutionContext executionContext) {
        if (result instanceof StreamResult) {
            StreamResult streamResult = (StreamResult) result;
            if (streamResult.getWriter() != null) {
                return streamResult.getWriter();
            } else if (streamResult.getOutputStream() != null) {
                try {
                    if (executionContext != null) {
                        return new OutputStreamWriter(streamResult.getOutputStream(), executionContext.getContentEncoding());
                    } else {
                        return new OutputStreamWriter(streamResult.getOutputStream(), StandardCharsets.UTF_8);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new SmooksException("Unable to encode output stream.", e);
                }
            } else {
                throw new SmooksException("Invalid " + StreamResult.class.getName() + ".  No OutputStream or Writer instance.");
            }
        } else if (result instanceof DOMResult) {
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
        if (source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource) source;
            try {
                if(streamSource.getReader() != null) {
                    streamSource.getReader().close();
                } else if(streamSource.getInputStream() != null) {
                    InputStream inputStream = streamSource.getInputStream();
                    if(inputStream != System.in) {
                        inputStream.close();
                    }
                }
            } catch (Throwable throwable) {
                LOGGER.debug("Failed to close input stream/reader.", throwable);
            }
        }
    }

    protected void close(Result result) {
        if (result instanceof StreamResult) {
            StreamResult streamResult = ((StreamResult) result);

            try {
                if (streamResult.getWriter() != null) {
                    Writer writer = streamResult.getWriter();
                    try {
                        writer.flush();
                    } finally {
                        writer.close();
                    }
                } else if (streamResult.getOutputStream() != null) {
                    OutputStream stream = streamResult.getOutputStream();
                    try {
                        stream.flush();
                    } finally {
                        // Close the stream as long as it's not sysout or syserr...
                        if(stream != System.out && stream != System.err) {
                            stream.close();
                        }
                    }
                }
            } catch (Throwable throwable) {
                LOGGER.debug("Failed to close output stream/writer.  May already be closed.", throwable);
            }
        }
    }
}
