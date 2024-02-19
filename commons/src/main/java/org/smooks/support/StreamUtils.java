/*-
 * ========================LICENSE_START=================================
 * Commons
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
package org.smooks.support;

import org.smooks.assertion.AssertArgument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Stream Utilities.
 * 
 * @author tfennelly
 */
public final class StreamUtils {
    
    private StreamUtils() {

    }

	/**
	 * Read the supplied InputStream and return as a byte array.
	 * 
	 * @param stream
	 *            The stream to read.
	 * @return byte array containing the Stream data.
	 * @throws IOException
	 *             Exception reading from the stream.
	 */
	public static byte[] readStream(InputStream stream) throws IOException {
        AssertArgument.isNotNull(stream, "stream");

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		byte[] byteBuf = new byte[1024];
		int readCount;

		while ((readCount = stream.read(byteBuf)) != -1) {
			bytesOut.write(byteBuf, 0, readCount);
		}

		return bytesOut.toByteArray();
	}

    /**
     * Read the supplied InputStream and return as a byte array.
     *
     * @param stream
     *            The stream to read.
     * @param encoding
     *            The encoding to use.
     * @return A String containing the Stream data.
     * @throws IOException
     *             Exception reading from the stream.
     */
    public static String readStreamAsString(final InputStream stream, final String encoding) throws IOException {
        AssertArgument.isNotNull(stream, "stream");
        AssertArgument.isNotNull(encoding, "encoding");

        return new String(readStream(stream), encoding);
    }

    public static String readStream(Reader stream) throws IOException {
        AssertArgument.isNotNull(stream, "stream");

        StringBuffer streamString = new StringBuffer();
        char[] readBuffer = new char[256];
        int readCount = 0;

        while ((readCount = stream.read(readBuffer)) != -1) {
            streamString.append(readBuffer, 0, readCount);
        }

        return streamString.toString();
    }
}
