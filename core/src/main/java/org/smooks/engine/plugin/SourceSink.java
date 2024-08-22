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

import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;

/**
 * {@link Source} - {@link Sink} value Object.
 * <p/>
 * This class allows users of the {@link PayloadProcessor} class to explicitly specify
 * both the {@link Source} and {@link Sink} payload carrier types.  This can be used
 * in situations where the required {@link Source} or {@link Sink} are not supported
 * amoung the default payload types supported by the {@link PayloadProcessor}
 * (for the {@link Source}), or by the {@link SinkType} (for the {@link Sink}).
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 * @since 1.0
 */
public class SourceSink {
    private Source source;
    private Sink sink;

    public SourceSink() {
    }

    public SourceSink(final Source source, final Sink sink) {
        this.source = source;
        this.sink = sink;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public Sink getSink() {
        return sink;
    }

    public void setSink(final Sink sink) {
        this.sink = sink;
    }

}
