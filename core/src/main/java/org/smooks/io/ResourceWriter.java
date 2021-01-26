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
package org.smooks.io;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.container.TypedKey;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import static org.smooks.io.AbstractOutputStreamResource.OUTPUTSTREAM_CONTEXT_KEY_PREFIX;
import static org.smooks.io.AbstractOutputStreamResource.RESOURCE_CONTEXT_KEY_PREFIX;

public class ResourceWriter extends Writer {
    
    private final String resourceName;
    private Writer delegateWriter;
    
    public ResourceWriter(final ExecutionContext executionContext, final String resourceName) {
        this.resourceName = resourceName;
        this.delegateWriter = getOutputWriter(resourceName, executionContext);
    }
    
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (delegateWriter == null) {
            throw new SmooksException("OutputResource '" + resourceName + "' not bound to context.  Configure an '" + AbstractOutputStreamResource.class.getName() + "' implementation, or change resource ordering.");
        }
        delegateWriter.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        if (delegateWriter != null) {
            delegateWriter.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (delegateWriter != null) {
            delegateWriter.close();
        }
    }

    /**
     * Get a {@link Writer} to the named {@link OutputStream} Resource.
     * <p/>
     * Wraps the {@link OutputStream} in a {@link Writer}.  Uses the "writerEncoding"
     * param to set the encoding on the {@link Writer}.
     *
     * @param resourceName     The resource name.
     * @param executionContext The current ExececutionContext.
     * @return A {@link Writer} to the named {@link OutputStream} Resource.
     * @throws SmooksException Unable to access OutputStream.
     */
    protected Writer getOutputWriter(final String resourceName, final ExecutionContext executionContext) throws SmooksException {
        final TypedKey<Object> resourceKey = new TypedKey<>(OUTPUTSTREAM_CONTEXT_KEY_PREFIX + resourceName);
        final Object resourceIOObj = executionContext.get(resourceKey);

        if (resourceIOObj == null) {
            final AbstractOutputStreamResource resource = executionContext.get(new TypedKey<>(RESOURCE_CONTEXT_KEY_PREFIX + resourceName));
            final OutputStream outputStream = openOutputStream(resource, executionContext);
            if (outputStream != null) {
                Writer outputStreamWriter = new java.io.OutputStreamWriter(outputStream, resource.getWriterEncoding());
                executionContext.put(resourceKey, outputStreamWriter);
                return outputStreamWriter;
            } else {
                return null;
            }
        } else {
            if (resourceIOObj instanceof Writer) {
                return (Writer) resourceIOObj;
            } else if (resourceIOObj instanceof OutputStream) {
                throw new SmooksException("An OutputStream to the '" + resourceName + "' resource is already open.  Cannot open a Writer to this resource now!");
            } else {
                throw new RuntimeException("Invalid runtime ExecutionContext state. Value stored under context key '" + resourceKey + "' must be either and OutputStream or Writer.  Is '" + resourceIOObj.getClass().getName() + "'.");
            }
        }
    }

    protected OutputStream openOutputStream(AbstractOutputStreamResource resource, ExecutionContext executionContext) {
        if (resource != null) {
            try {
                return resource.getOutputStream(executionContext);
            } catch (IOException e) {
                throw new SmooksException("Unable to set outputstream for '" + resource.getResourceName() + "'.", e);
            }
        } else {
            return null;
        }
    }

    public Writer getDelegateWriter() {
        return delegateWriter;
    }
}
