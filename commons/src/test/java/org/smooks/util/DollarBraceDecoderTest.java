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
package org.smooks.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DollarBraceDecoderTest {

	@Test
    public void test_getTokens() {
        assertEquals("[]", DollarBraceDecoder.getTokens("aaaaaa").toString());
        assertEquals("[x]", DollarBraceDecoder.getTokens("aaa${x}aaa").toString());
        assertEquals("[x, x]", DollarBraceDecoder.getTokens("aaa${x}a${x}aa").toString());
        assertEquals("[x, x, y]", DollarBraceDecoder.getTokens("aaa${x}a${x}a${y}a").toString());
        assertEquals("[x, x, y]", DollarBraceDecoder.getTokens("a}aa${x}a${x}a${y}a").toString());
        assertEquals("[a${x, x, y]", DollarBraceDecoder.getTokens("a}a${a${x}a${x}a${y}a").toString());
        assertEquals("[a${x, x, y]", DollarBraceDecoder.getTokens("a}a${a${x}a${x}a${y}a${").toString());
        assertEquals("[orderDetail.orderNum, accounts[0].USERID[2], orderDetail.date]", DollarBraceDecoder.getTokens( "INSERT INTO ORDERS VALUES(${orderDetail.orderNum}, ${accounts[0].USERID[2]}, ${orderDetail.date})").toString());
    }

	@Test
    public void test_replaceTokens() {
        assertEquals("aaaaaa", DollarBraceDecoder.replaceTokens("aaaaaa", "?"));
        assertEquals("aaa?aaa", DollarBraceDecoder.replaceTokens("aaa${x}aaa", "?"));
        assertEquals("aaa?a?aa", DollarBraceDecoder.replaceTokens("aaa${x}a${x}aa", "?"));
        assertEquals("aaa?a?a?a", DollarBraceDecoder.replaceTokens("aaa${x}a${x}a${y}a", "?"));
        assertEquals("test-?.txt", DollarBraceDecoder.replaceTokens( "test-${currentDate.date?string('yyyy')}.txt", "?"));
        assertEquals("test-?.txt", DollarBraceDecoder.replaceTokens( "test-${currentDate.date?string('yyyy-MM-dd-HH-mm-sss')}.txt", "?"));
        assertEquals("test-?.txt", DollarBraceDecoder.replaceTokens( "test-${currentDate.date?string(\"yyyy-MM-dd-HH-mm-sss\")}.txt", "?"));
        assertEquals("INSERT INTO ORDERS VALUES(?, ?, ?)", DollarBraceDecoder.replaceTokens( "INSERT INTO ORDERS VALUES(${orderDetail.orderNum + \"-\" + product.ID}, ${accounts[0].USERID[2]}, ${orderDetail.date})", "?"));
    }
    
}
