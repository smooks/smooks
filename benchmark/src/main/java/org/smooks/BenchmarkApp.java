/*-
 * ========================LICENSE_START=================================
 * Benchmark
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
package org.smooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class BenchmarkApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkApp.class);

    public static void main(String... args) throws IOException {
        final URL url = new URL("https://dblp.org/xml/release/dblp-2015-03-02.xml.gz");
        final URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        final Smooks smooks = new Smooks();
        final long startTime = System.currentTimeMillis();
        LOGGER.info("Filtering...");
        smooks.filterSource(new StreamSource(new GZIPInputStream(connection.getInputStream())));
        final long duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Filtering completed in {} minutes", TimeUnit.MILLISECONDS.toMinutes(duration));
    }
}