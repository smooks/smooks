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

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;

import java.io.IOException;
import java.io.Writer;

public class FragmentWriter extends Writer {

    public static final long RESERVED_WRITE_FRAGMENT_ID = 0L;
    
    private final Writer delegateWriter;
    private final Fragment<?> fragment;
    private final ExecutionContext executionContext;
    private final Boolean tryPark;
    
    public FragmentWriter(final ExecutionContext executionContext, final Fragment<?> fragment) {
        this(executionContext, fragment, true);
    }

    public FragmentWriter(final ExecutionContext executionContext, final Fragment<?> fragment, final boolean tryPark) {
        this.executionContext = executionContext;
        this.delegateWriter = Stream.out(executionContext);
        this.fragment = fragment;
        this.tryPark = tryPark;
    }
    
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (park()) {
            delegateWriter.write(cbuf, off, len);
        }
    }

    @Override
    public void flush() throws IOException {
        delegateWriter.flush();
    }

    @Override
    public void close() throws IOException {
        delegateWriter.close();
    }
    
    public Writer getDelegateWriter() {
        return delegateWriter;
    }
    
    public boolean park() throws IOException {
        if (fragment.reserve(RESERVED_WRITE_FRAGMENT_ID, this)) {
            return true;
        }

        if (tryPark) {
            return false;
        }
        
        throw new IOException(String.format("Illegal access to fragment '%s': fragment is exclusively acquired by another writer. Hint: release fragment before acquiring it from a different writer", fragment.toString()));
    }

    public void unpark() throws IOException {
        fragment.release(RESERVED_WRITE_FRAGMENT_ID, this);
    }

    public Fragment<?> getFragment() {
        return fragment;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }
}
