/*-
 * ========================LICENSE_START=================================
 * Smooks API
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
package org.smooks.api.delivery.sax;

import java.io.IOException;
import java.io.Writer;

/**
 * SAX Text.
 * <p/>
 * This class provides a wrapper around the char array supplied by the
 * SAXParser/XMLReader.  It help provide an optimization by allowing the process
 * to avoid String construction where possible.
 * <p/>
 * <i><b><u>NOTE</u></b>: References to instances of this type should not be cached.  If you
 * need to cache the character data housed in this class, you should use either the
 * {@link #getText()} or {@link #toString()} methods.</i>
 *
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface SAXText {


    void setText(char[] characters, int offset, int length, TextType type);

    String getText();

    TextType getType();

    char[] getCharacters();

    int getOffset();

    int getLength();

    void toWriter(Writer writer) throws IOException;

    void toWriter(Writer writer, boolean encodeSpecialChars) throws IOException;

    SAXText copy();
}
