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
package org.smooks.javabean.decoders;

import org.smooks.cdr.SmooksConfigurationException;

import java.text.*;
import java.util.*;

/**
 * LocaleAwareDateDecoder is a decoder 'helper' that can be subclassed by Date decoders to enable
 * them to use locale specific date formats.
 * <p/>
 * Usage (on Java Binding value config using the {@link org.smooks.javabean.decoders.DateDecoder}):
 * <pre>
 * &lt;jb:value property="date" decoder="Date" data="order/@date"&gt;
 *     &lt;-- Format: Defaults to "yyyy-MM-dd'T'HH:mm:ss" (SOAP) --&gt;
 *     &lt;jb:decodeParam name="format"&gt;EEE MMM dd HH:mm:ss z yyyy&lt;/jb:decodeParam&gt;
 *     &lt;-- Locale: Defaults to machine Locale --&gt;
 *     &lt;jb:decodeParam name="locale"&gt;sv-SE&lt;/jb:decodeParam&gt;
 *     &lt;-- Verify Locale: Default false --&gt;
 *     &lt;jb:decodeParam name="verify-locale"&gt;true&lt;/jb:decodeParam&gt;
 * &lt;/jb:value&gt;
 * </pre>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public abstract class LocaleAwareDateDecoder extends LocaleAwareDecoder
{
    /**
     * Date format configuration key.
     */
    public static final String FORMAT = "format";

    /**
     * Default date format string.
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    protected String format;

    /*
     * 	Need to initialize a default decoder as not calls can be make
     * 	directly to decode without calling setConfigurtion.
     */
    protected SimpleDateFormat decoder = new SimpleDateFormat( DEFAULT_DATE_FORMAT );

    public void setConfiguration(Properties resourceConfig) throws SmooksConfigurationException {
        super.setConfiguration(resourceConfig);

        format = resourceConfig.getProperty(FORMAT, DEFAULT_DATE_FORMAT);
        if (format == null) {
            throw new SmooksConfigurationException("Decoder must specify a 'format' parameter.");
        }

        Locale configuredLocale = getLocale();
        if(configuredLocale != null) {
            decoder = new SimpleDateFormat(format.trim(), configuredLocale);
        } else {
            decoder = new SimpleDateFormat(format.trim());
        }
    }
}
