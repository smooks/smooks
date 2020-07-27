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
package org.smooks.datatype;

import org.junit.Test;
import org.smooks.datatype.factory.DataTypeProviderFactory;
import org.smooks.datatype.factory.system.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DataTypeProviderFactoryLoaderTest {

	@Test
    public void test() {
        DataTypeProviderFactoryLoader dataTypeProviderFactoryLoader = DataTypeProviderFactoryLoader.getInstance();
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, String.class) instanceof StringProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Integer.class) instanceof StringToIntegerProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(Integer.class, String.class) instanceof IntegerToStringProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Float.class) instanceof StringToFloatProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(Float.class, String.class) instanceof FloatToStringProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Double.class) instanceof StringToDoubleProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(Double.class, String.class) instanceof DoubleToStringProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Character.class) instanceof CharacterProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, BigDecimal.class) instanceof StringToBigDecimalProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(BigDecimal.class, String.class) instanceof BigDecimalToStringProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Date.class) instanceof StringToDateProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(Date.class, String.class) instanceof DateToStringProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Calendar.class) instanceof CalendarProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, String[].class) instanceof CsvProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Charset.class) instanceof CharsetProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, File.class) instanceof FileProviderFactory);
        assertTrue(dataTypeProviderFactoryLoader.get(String.class, Class.class) instanceof ClassProviderFactory);
        assertTrue((DataTypeProviderFactory) dataTypeProviderFactoryLoader.get(this.getClass(), this.getClass()) instanceof DefaultProviderFactory);
    }
}
