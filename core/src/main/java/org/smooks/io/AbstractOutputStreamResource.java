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
package org.smooks.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.ordering.Consumer;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.lifecycle.PostExecutionLifecycle;
import org.smooks.api.lifecycle.PostFragmentLifecycle;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * AbstractOuputStreamResource is the base class for handling output stream
 * resources in Smooks.
 * <p/>
 * Note that a {@link Writer} can also be opened on a stream resource.  If a {@link Writer}
 * has been opened on a resource, an {@link OutputStream} cannot also be opened (and visa versa).
 * <p/>
 * Example configuration:
 * <pre>
 * &lt;resource-config selector="#document"&gt;
 *    &lt;resource&gt;org.smooks.io.ConcreateImpl&lt;/resource&gt;
 *    &lt;param name="resourceName"&gt;resourceName&lt;/param&gt;
 *    &lt;param name="writerEncoding"&gt;UTF-8&lt;/param&gt; &lt;!-- Optional --&gt;
 * &lt;/resource-config&gt;
 * </pre>
 * <p>
 * Description of configuration properties:
 * <ul>
 * <li><code>resource</code>: should be a concreate implementation of this class</li>
 * <li><code>resourceName</code>: the name of this resouce. Will be used to identify this resource</li>
 * <li><code>writerEncoding</code>: (Optional) the encoding to be used by any writers opened on this resource (Default is "UTF-8")</li>
 * </ul>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public abstract class AbstractOutputStreamResource implements DOMVisitBefore, Consumer, PostFragmentLifecycle, PostExecutionLifecycle, BeforeVisitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOutputStreamResource.class);

    static final String RESOURCE_CONTEXT_KEY_PREFIX = AbstractOutputStreamResource.class.getName() + "#outputresource:";
    static final String OUTPUTSTREAM_CONTEXT_KEY_PREFIX = AbstractOutputStreamResource.class.getName() + "#outputstream:";

    @Inject
    private String resourceName;

    @Inject
    private Charset writerEncoding = StandardCharsets.UTF_8;

    /**
     * Retrieve/create an output stream that is appropriate for the concreate implementation
     *
     * @param executionContext Execution Context.
     * @return OutputStream specific to the concreate implementation
     */
    public abstract OutputStream getOutputStream(final ExecutionContext executionContext) throws IOException;

    /**
     * Get the name of this resource
     *
     * @return The name of the resource
     */
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public boolean consumes(Object object) {
        return object.equals(resourceName);
    }

    /**
     * Set the name of this resource
     *
     * @param resourceName The name of the resource
     */
    public AbstractOutputStreamResource setResourceName(String resourceName) {
        AssertArgument.isNotNullAndNotEmpty(resourceName, "resourceName");
        this.resourceName = resourceName;
        return this;
    }

    public AbstractOutputStreamResource setWriterEncoding(Charset writerEncoding) {
        AssertArgument.isNotNull(writerEncoding, "writerEncoding");
        this.writerEncoding = writerEncoding;
        return this;
    }

    public Charset getWriterEncoding() {
        return writerEncoding;
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) throws SmooksException {
        bind(executionContext);
    }

    @Override
    public void onPostFragment(Fragment<?> fragment, ExecutionContext executionContext) {
        if (closeCondition(executionContext)) {
            closeResource(executionContext);
        }
    }

    @Override
    public void onPostExecution(ExecutionContext executionContext) {
        closeResource(executionContext);
    }

    protected boolean closeCondition(ExecutionContext executionContext) {
        return true;
    }

    /**
     * Close the resource output stream.
     * <p/>
     * Classes overriding this method must call super on this method. This will
     * probably need to be done before performing any aditional cleanup.
     *
     * @param executionContext Smooks ExecutionContext
     */
    protected void closeResource(final ExecutionContext executionContext) {
        try {
            Closeable output = executionContext.get(TypedKey.of(OUTPUTSTREAM_CONTEXT_KEY_PREFIX + getResourceName()));
            close(output);
        } finally {
            executionContext.remove(TypedKey.of(OUTPUTSTREAM_CONTEXT_KEY_PREFIX + getResourceName()));
            executionContext.remove(TypedKey.of(RESOURCE_CONTEXT_KEY_PREFIX + getResourceName()));
        }
    }

    private void bind(final ExecutionContext executionContext) {
        executionContext.put(TypedKey.of(RESOURCE_CONTEXT_KEY_PREFIX + getResourceName()), this);
    }

    private void close(final Closeable closeable) {
        if (closeable == null) {
            return;
        }

        if (closeable instanceof Flushable) {
            try {
                ((Flushable) closeable).flush();
            } catch (IOException e) {
                LOGGER.warn("IOException while trying to flush output resource '" + resourceName + "': ", e);
            }
        }

        try {
            closeable.close();
        } catch (IOException e) {
            LOGGER.warn("IOException while trying to close output resource '" + resourceName + "': ", e);
        }
    }
}
