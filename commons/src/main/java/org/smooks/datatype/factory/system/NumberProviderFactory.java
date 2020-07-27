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

import javax.inject.Provider;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * Abstract {@link Number} based DataDecoder.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class NumberProviderFactory<S, T> extends LocaleAwareDataTypeProviderFactory<S, T> {

    /**
     * Format.
     */
    public static final String FORMAT = "format";
    /**
     * Type.
     */
    public static final String TYPE = "type";
    private NumberType type;
    private NumberFormat numberFormat;

    @Override
    public Provider<T> createProvider(final S value) {
        return doCreateProvider(value, numberFormat != null ? getNumberFormat() : null);
    }
    
    protected abstract Provider<T> doCreateProvider(S value, NumberFormat numberFormat);

    public enum NumberType {
        RAW,
        PERCENTAGE,
        CURRENCY
    }
    
    @Override
    public void setConfiguration(final Properties config) throws SmooksConfigurationException {
        super.setConfiguration(config);
        String pattern = config.getProperty(FORMAT);
        String typeConfig = config.getProperty(TYPE);
        Locale locale = getLocale();

        if(locale == null && pattern == null) {
            // Don't create a formatter if neither locale or
            // pattern are configured...
            return;
        }

        if(locale == null) {
            locale = Locale.getDefault();
        }

        if(typeConfig == null) {
            type = NumberType.RAW;
        } else {
            try {
                type = NumberType.valueOf(typeConfig);
            } catch(Exception e) {
                throw new SmooksConfigurationException("Unsupported Number type specification '" + typeConfig + "'.  Must be one of '" + NumberType.values() + "'.");
            }
        }

        if(type == NumberType.PERCENTAGE || (pattern != null && pattern.indexOf('%') != -1)) {
            type = NumberType.PERCENTAGE;
            numberFormat = NumberFormat.getPercentInstance(locale);
        } else if(type == NumberType.CURRENCY) {
            numberFormat = NumberFormat.getCurrencyInstance(locale);
        } else {
            numberFormat = NumberFormat.getInstance(locale);
            numberFormat.setGroupingUsed(false);
            if(pattern != null && numberFormat instanceof DecimalFormat) {
                ((DecimalFormat) numberFormat).applyPattern(pattern);
            }
        }
    }

    /**
     * Get the {@link NumberFormat} instance, if one exists.
     * @return A clone of the {@link NumberFormat} instance, otherwise null.
     */
    public NumberFormat getNumberFormat() {
        if(numberFormat != null) {
            return (NumberFormat) numberFormat.clone();
        } else {
            return null;
        }
    }

    public NumberType getType() {
        return type;
    }

    public boolean isPercentage() {
        return type == NumberType.PERCENTAGE;
    }
    
}
