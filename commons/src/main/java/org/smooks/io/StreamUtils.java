/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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

import org.smooks.assertion.AssertArgument;

import java.io.*;

/**
 * Stream Utilities.
 * 
 * @author tfennelly
 */
public abstract class StreamUtils {

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
		int readCount = 0;

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

    /**
     * Read the contents of the specified file.
     * @param file The file to read.
     * @return The file contents.
     * @throws IOException Error readiong file.
     * @deprecated Use {@link org.smooks.io.FileUtils#readFile(java.io.File)}.
     */
    public static byte[] readFile(File file) throws IOException {
        return FileUtils.readFile(file);
    }

    public static void writeFile(File file, byte[] data) throws IOException {
        AssertArgument.isNotNull(file, "file");
        AssertArgument.isNotNull(data, "data");

        OutputStream stream = new FileOutputStream(file);
        try {
            stream.write(data);
        } finally {
            try {
                stream.flush();
            } finally {
                stream.close();
            }
        }
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

    /**
     * Compares the 2 streams.
     * <p/>
     * Calls {@link #trimLines(InputStream)} on each stream before comparing.
     * @param s1 Stream 1.
     * @param s2 Stream 2.
     * @return True if the streams are equal not including leading and trailing
     * whitespace on each line and blank lines, otherwise returns false.
     */
    public static boolean compareCharStreams(InputStream s1, InputStream s2) {
        StringBuffer s1Buf, s2Buf;

        try {
            s1Buf = trimLines(s1);
            s2Buf = trimLines(s2);

            return s1Buf.toString().equals(s2Buf.toString());
        } catch (IOException e) {
            // fail the comparison
        }

        return false;
    }

    /**
     * Compares the 2 streams.
     * <p/>
     * Calls {@link #trimLines(java.io.Reader)} on each stream before comparing.
     * @param s1 Stream 1.
     * @param s2 Stream 2.
     * @return True if the streams are equal not including leading and trailing
     * whitespace on each line and blank lines, otherwise returns false.
     */
    public static boolean compareCharStreams(Reader s1, Reader s2) {
        StringBuffer s1Buf, s2Buf;

        try {
            s1Buf = trimLines(s1);
            s2Buf = trimLines(s2);

            return s1Buf.toString().equals(s2Buf.toString());
        } catch (IOException e) {
            // fail the comparison
        }

        return false;
    }


    /**
     * Compares the 2 streams.
     * <p/>
     * Calls {@link #trimLines(java.io.Reader)} on each stream before comparing.
     * @param s1 Stream 1.
     * @param s2 Stream 2.
     * @return True if the streams are equal not including leading and trailing
     * whitespace on each line and blank lines, otherwise returns false.
     */
    public static boolean compareCharStreams(String s1, String s2) {
        return compareCharStreams(new StringReader(s1), new StringReader(s2));
    }

    /**
     * Read the lines lines of characters from the stream and trim each line
     * i.e. remove all leading and trailing whitespace.
     * @param charStream Character stream.
     * @return StringBuffer containing the line trimmed stream.
     * @throws IOException
     */
    public static StringBuffer trimLines(Reader charStream) throws IOException {
        StringBuffer stringBuf = new StringBuffer();
        BufferedReader reader = new BufferedReader(charStream);
        String line;

        while((line = reader.readLine()) != null) {
            stringBuf.append(line.trim());
        }

        return stringBuf;
    }

    /**
     * Read the lines lines of characters from the supplied string, trim each line (optional)
     * and add a single newline character.
     * @param string The String.
     * @param trim Trim each line i.e. to ignore leading and trailing whitespace.
     * @return String containing the line trimmed stream.
     * @throws IOException
     */
    public static String normalizeLines(String string, boolean trim) throws IOException {
    	return normalizeLines(new StringReader(string), trim);
    }

    /**
     * Read the lines lines of characters from the stream, trim each line (optional)
     * and add a single newline character.
     * @param charStream Character stream.
     * @param trim Trim each line i.e. to ignore leading and trailing whitespace.
     * @return String containing the line trimmed stream.
     * @throws IOException
     */
    public static String normalizeLines(Reader charStream, boolean trim) throws IOException {
        StringBuffer stringBuf = new StringBuffer();
        BufferedReader reader = new BufferedReader(charStream);
        String line;

        while((line = reader.readLine()) != null) {
            if(trim) {
                stringBuf.append(line.trim());
            } else {
                stringBuf.append(line);
            }
            stringBuf.append('\n');
        }

        return stringBuf.toString();
    }

    /**
     * Read the lines lines of characters from the stream and trim each line
     * i.e. remove all leading and trailing whitespace.
     * @param charStream Character stream.
     * @return StringBuffer containing the line trimmed stream.
     * @throws IOException
     */
    public static StringBuffer trimLines(InputStream charStream) throws IOException {
        return trimLines(new InputStreamReader(charStream, "UTF-8"));
    }

    /**
     * Read the lines lines of characters from the stream and trim each line
     * i.e. remove all leading and trailing whitespace.
     * @param charStream Character stream.
     * @return String containing the line trimmed stream.
     * @throws IOException
     */
    public static String trimLines(String charStream) throws IOException {
        return trimLines(new StringReader(charStream)).toString();
    }
}
