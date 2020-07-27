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

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.config.Configurable;
import org.smooks.datatype.factory.DataTypeProviderFactory;
import org.smooks.javabean.decoders.LocaleAwareDateDecoder;

import javax.inject.Provider;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * {@link Calendar} data decoder.
 * <p/>
 * Decodes the supplied string into a {@link Calendar} value
 * based on the supplied "{@link SimpleDateFormat format}" parameter.
 * <p/>
 * This decoder is synchronized on its underlying {@link SimpleDateFormat} instance.
 *
 * @see {@link LocaleAwareDateDecoder}
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author Pavel Kadlec
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 *
 */
public class CalendarProviderFactory implements DataTypeProviderFactory<String, Calendar>, Configurable {

    private Properties config;
    
    @Override
    public Provider<Calendar> createProvider(final String value) {
        final LocaleAwareStringToDateProviderFactory<Calendar> localeAwareStringToDateProviderFactory = new LocaleAwareStringToDateProviderFactory<Calendar>() {
            @Override
            protected Provider<Calendar> doCreateProvider(Date date) {
                return () -> (Calendar) decoder.getCalendar().clone();
            }
        };
        
        if (config != null) {
            localeAwareStringToDateProviderFactory.setConfiguration(config);
        }
        
        return localeAwareStringToDateProviderFactory.createProvider(value);
    }

    @Override
    public void setConfiguration(final Properties config) throws SmooksConfigurationException {
        this.config = config;
    }

    @Override
    public Properties getConfiguration() {
        return config;
    }
}
