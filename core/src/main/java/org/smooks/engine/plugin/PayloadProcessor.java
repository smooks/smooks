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
package org.smooks.engine.plugin;

import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.ExecutionContext;
import org.smooks.io.sink.StringSink;
import org.smooks.io.sink.ByteSink;
import org.smooks.io.sink.JavaSink;
import org.smooks.io.source.JavaSource;

/**
 * Processor class for an abstract payload type.
 * <p/>
 * This class can be used to ease Smooks integration with application
 * containers (for example ESBs).  It works out how to filter the supplied Object payload
 * through Smooks, to produce the desired {@link SinkType sink type}.
 * <p/>
 * The "payload" object supplied to the {@link #process(Object, ExecutionContext)}
 * method can be one of type:
 * <ul>
 * <li>{@link String},</li>
 * <li>{@link Byte} array,</li>
 * <li>{@link java.io.Reader},</li>
 * <li>{@link java.io.InputStream},</li>
 * <li>{@link Source},</li>
 * <li>{@link SourceSink}, or</li>
 * <li>any Java user type (gets wrapped in a {@link JavaSource}).</li>
 * </ul>
 * <p/>
 * The {@link SourceSink} payload type allows full control over the filter
 * {@link Source} and {@link Sink}.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class PayloadProcessor {

    private final Smooks smooks;
    private final SinkType sinkType;
    private String javaResultBeanId;

    /**
     * Public constructor.
     *
     * @param smooks     The Smooks instance to be used.
     * @param sinkType The required sink type.
     */
    public PayloadProcessor(final Smooks smooks, final SinkType sinkType) {
        AssertArgument.isNotNull(smooks, "smooks");
        AssertArgument.isNotNull(sinkType, "sinkType");
        this.smooks = smooks;
        this.sinkType = sinkType;
    }

    /**
     * Set the bean ID to be unpacked from a {@link JavaSink}.
     * <p/>
     * Only relevant for {@link SinkType#JAVA}.  If not specified, the
     * complete {@link JavaSink#getResultMap() result Map}
     * will be returned as the result of the {@link #process(Object, ExecutionContext)}
     * method call.
     *
     * @param javaResultBeanId The bean ID to be unpacked.
     */
    public void setJavaResultBeanId(final String javaResultBeanId) {
        AssertArgument.isNotNullAndNotEmpty(javaResultBeanId, "javaResultBeanId");
        this.javaResultBeanId = javaResultBeanId;
    }

    /**
     * Process the supplied payload.
     * <p/>
     * See class level javadoc.
     *
     * @param payload The payload to be filtered. See class level javadoc for supported data types.
     * @return The filter result. Will be "unpacked" as per the {@link SinkType} supplied in the
     * {@link #PayloadProcessor(org.smooks.Smooks, SinkType) constructor}.
     * @throws SmooksException
     */
    public final Object process(final Object payload, final ExecutionContext executionContext) throws SmooksException {
        AssertArgument.isNotNull(payload, "payload");

        Source source;
        Sink sink;

        if (payload instanceof SourceSink) {
            SourceSink sourceSink = (SourceSink) payload;
            source = sourceSink.getSource();
            sink = sourceSink.getSink();
        } else {
            source = SourceFactory.getInstance().createSource(payload);
            sink = SinkFactory.getInstance().createSink(sinkType);
        }

        // Filter it through Smooks...
        smooks.filterSource(executionContext, source, sink);

        // Extract the sink...
        if (sink instanceof JavaSink) {
            if (javaResultBeanId != null) {
                return ((JavaSink) sink).getResultMap().get(javaResultBeanId);
            } else {
                return ((JavaSink) sink).getResultMap();
            }
        } else if (sink instanceof StringSink) {
            return ((StringSink) sink).getResult();
        } else if (sink instanceof ByteSink) {
            return ((ByteSink) sink).getResult();
        }

        return sink;
    }
}
