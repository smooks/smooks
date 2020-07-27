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
import org.smooks.javabean.DataDecodeException;

import javax.inject.Provider;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Properties;

/**
 * Integer Decoder.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StringToIntegerProviderFactory implements DataTypeProviderFactory<String, Integer>, Configurable {

    private Properties config;

    @Override
    public Provider<Integer> createProvider(final String value) {
        final NumberProviderFactory<String, Integer> numberProviderFactory = new NumberProviderFactory<String, Integer>() {

            @Override
            protected Provider<Integer> doCreateProvider(String value, NumberFormat numberFormat) {
                return () -> {
                    if (numberFormat != null) {
                        try {
                            final Number number = numberFormat.parse(value.trim());
                            if (isPercentage()) {
                                return (int) (number.doubleValue() * 100);
                            } else {
                                return number.intValue();
                            }
                        } catch (ParseException e) {
                            throw new DataDecodeException("Failed to decode Integer value '" + value + "' using NumberFormat instance " + numberFormat + ".", e);
                        }
                    } else {
                        try {
                            return Integer.parseInt(value.trim());
                        } catch (NumberFormatException e) {
                            throw new DataDecodeException("Failed to decode Integer value '" + value + "'.", e);
                        }
                    }
                };
            }
        };

        if (config != null) {
            numberProviderFactory.setConfiguration(config);
        }
        return numberProviderFactory.createProvider(value);
    }

    @Override
    public void setConfiguration(Properties config) throws SmooksConfigurationException {
        this.config = config;
    }

    @Override
    public Properties getConfiguration() {
        return config;
    }
}
