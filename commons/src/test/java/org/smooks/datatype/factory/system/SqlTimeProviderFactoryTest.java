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
package org.smooks.datatype.factory.system;

import org.junit.Test;
import org.smooks.javabean.decoders.CalendarDecoder;
import org.smooks.javabean.decoders.LocaleAwareDecoder;

import java.sql.Time;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests for the SqlTimeDecoder class
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class SqlTimeProviderFactoryTest {

	@Test
    public void test_DateDecoder() {
        
        Properties config = new Properties();
        config.setProperty(CalendarDecoder.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");

        SqlTimeProviderFactory sqlTimeProviderFactory = new SqlTimeProviderFactory();
        config.setProperty(LocaleAwareDecoder.LOCALE_LANGUAGE_CODE, "en");
	    config.setProperty(LocaleAwareDecoder.LOCALE_COUNTRY_CODE, "IE");
        sqlTimeProviderFactory.setConfiguration(config);

        Object object = sqlTimeProviderFactory.createProvider("Wed Nov 15 13:45:28 EST 2006").get();
        assertTrue( object instanceof Time);
        
        Time time_a = sqlTimeProviderFactory.createProvider("Wed Nov 15 13:45:28 EST 2006").get();
        assertEquals(1163616328000L, time_a.getTime());
        Time date_b = sqlTimeProviderFactory.createProvider("Wed Nov 15 13:45:28 EST 2006").get();
        assertNotSame(time_a, date_b);
    }
    
}
