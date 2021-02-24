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
package org.smooks.io;

import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import static org.smooks.io.AbstractOutputStreamResource.OUTPUTSTREAM_CONTEXT_KEY_PREFIX;
import static org.smooks.io.AbstractOutputStreamResource.RESOURCE_CONTEXT_KEY_PREFIX;

public class ResourceOutputStream extends OutputStream {

    private final OutputStream delegateOutputStream;

    public ResourceOutputStream(final ExecutionContext executionContext, final String resourceName) {
        this.delegateOutputStream = getOutputStream(resourceName, executionContext);
    }
    
    @Override
    public void write(int b) throws IOException {
        
    }

    /**
     * Get an {@link OutputStream} to the named Resource.
     *
     * @param resourceName     The resource name.
     * @param executionContext The current ExececutionContext.
     * @return An {@link OutputStream} to the named Resource.
     * @throws SmooksException Unable to access OutputStream.
     */
    protected OutputStream getOutputStream(final String resourceName, final ExecutionContext executionContext) throws SmooksException {
        TypedKey<Object> resourceKey = new TypedKey<>(OUTPUTSTREAM_CONTEXT_KEY_PREFIX + resourceName);
        Object resourceIOObj = executionContext.get(resourceKey);

        if (resourceIOObj == null) {
            AbstractOutputStreamResource resource = executionContext.get(new TypedKey<>(RESOURCE_CONTEXT_KEY_PREFIX + resourceName));
            OutputStream outputStream = openOutputStream(resource, resourceName, executionContext);

            executionContext.put(resourceKey, outputStream);
            return outputStream;
        } else {
            if (resourceIOObj instanceof OutputStream) {
                return (OutputStream) resourceIOObj;
            } else if (resourceIOObj instanceof Writer) {
                throw new SmooksException("An Writer to the '" + resourceName + "' resource is already open.  Cannot open an OutputStream to this resource now!");
            } else {
                throw new RuntimeException("Invalid runtime ExecutionContext state. Value stored under context key '" + resourceKey + "' must be either and OutputStream or Writer.  Is '" + resourceIOObj.getClass().getName() + "'.");
            }
        }
    }

    protected OutputStream openOutputStream(AbstractOutputStreamResource resource, String resourceName, ExecutionContext executionContext) {
        if (resource == null) {
            throw new SmooksException("OutputResource '" + resourceName + "' not bound to context.  Configure an '" + AbstractOutputStreamResource.class.getName() + "' implementation, or change resource ordering.");
        }

        try {
            return resource.getOutputStream(executionContext);
        } catch (IOException e) {
            throw new SmooksException("Unable to set outputstream for '" + resource.getResourceName() + "'.", e);
        }
    }

    public OutputStream getDelegateOutputStream() {
        return delegateOutputStream;
    }
}
