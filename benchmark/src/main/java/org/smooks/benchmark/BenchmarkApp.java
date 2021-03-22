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
package org.smooks.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.Smooks;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

public class BenchmarkApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkApp.class);

    private static class CountingInputStream extends InputStream {
        private final InputStream inputStream;
        private long byteCount;
        
        public CountingInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        @Override
        public int read(byte[] b) throws IOException {
            int bytesRead = inputStream.read(b);
            if (bytesRead != -1) {
                byteCount += bytesRead;
            }
            return bytesRead;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int bytesRead = inputStream.read(b, off, len);
            if (bytesRead != -1) {
                byteCount += bytesRead;
            }
            return bytesRead;
        }

        @Override
        public long skip(long n) throws IOException {
            long bytesRead = inputStream.skip(n);
            byteCount += bytesRead;
            return bytesRead;
        }

        @Override
        public int available() throws IOException {
            return inputStream.available();
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            inputStream.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            inputStream.reset();
        }

        @Override
        public boolean markSupported() {
            return inputStream.markSupported();
        }

        @Override
        public int read() throws IOException {
            int bytesRead = inputStream.read();
            if (bytesRead != -1) {
                byteCount++;
            }
            return bytesRead;
        }

        public long getByteCount() {
            return byteCount;
        }
    }
    
    public static void main(String... args) throws IOException, SAXException {
        final URL url = new URL("https://dblp.org/xml/release/dblp-2015-03-02.xml.gz");
        final URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        final File testDatasetFile = File.createTempFile("dblp-2015-03-02", ".xml.gz");
        final FileOutputStream fileOutputStream = new FileOutputStream(testDatasetFile);

        LOGGER.info("Downloading test dataset...");
        byte[] buf = new byte[8192];
        int length;
        while ((length = connection.getInputStream().read(buf)) > 0) {
            fileOutputStream.write(buf, 0, length);
        }

        final Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(true).build());
        smooks.addConfigurations(BenchmarkApp.class.getResourceAsStream("/smooks-config.xml"));
        IntStream.range(0, Math.min(2, Runtime.getRuntime().availableProcessors())).parallel().forEach(value -> {
            try {
                final CountingInputStream inputStream = new CountingInputStream(new GZIPInputStream(new FileInputStream(testDatasetFile)));
                LOGGER.info("Filtering...");
                final long startTime = System.currentTimeMillis();
                smooks.filterSource(new StreamSource(inputStream));
                final long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("Filtered {} MBs in {} minutes", (inputStream.getByteCount() / 1024) / 1024, TimeUnit.MILLISECONDS.toMinutes(duration));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }
}