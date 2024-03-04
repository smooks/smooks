/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.converter;

import org.smooks.api.SmooksConfigException;
import org.smooks.api.resource.config.Configurable;
import org.smooks.api.converter.TypeConverter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

/**
 * LocaleAwareDecoder is a decoder 'helper' that can be subclassed by any DataDecoder
 * implementation that relies on {@link Locale} information to perform
 * Data Decoding.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public abstract class LocaleAwareTypeConverter<S, T> implements TypeConverter<S, T>, Configurable {

    /**
     * Locale.  Hyphen separated ISO Language Code and Country Code e.g. "en-IE".
     */
    public static final String LOCALE = "locale";

    /**
     * ISO Language Code. Lower case two-letter code defined by ISO-639
     */
    public static final String LOCALE_LANGUAGE_CODE = "locale-language";

    /**
     * ISO Country Code. Upper case two-letter code defined by ISO-3166
     */
    public static final String LOCALE_COUNTRY_CODE = "locale-country";

    /**
     * True or false(default).
     * Whether or not a check should be performed to verify that
     * the specified locale is installed. This operation can take some
     * time and should be turned off in a production evironment
     */
    public static final String VERIFY_LOCALE = "verify-locale";

    /**
     * The Locale instance.
     */
    private Locale locale;

    private boolean verifyLocale;
    private Properties configuration;

    @Override
    public void setConfiguration(Properties properties) throws SmooksConfigException {
        if (properties != null) {
            final String locale = properties.getProperty(LOCALE);
            final String languageCode;
            final String countryCode;

            if (locale != null) {
                String[] localTokens;

                if (locale.indexOf('-') != -1) {
                    localTokens = locale.split("-");
                } else {
                    localTokens = locale.split("_");
                }

                languageCode = localTokens[0];
                if (localTokens.length == 2) {
                    countryCode = localTokens[1];
                } else {
                    countryCode = null;
                }
            } else {
                languageCode = properties.getProperty(LOCALE_LANGUAGE_CODE);
                countryCode = properties.getProperty(LOCALE_COUNTRY_CODE);
            }

            verifyLocale = Boolean.parseBoolean(properties.getProperty(VERIFY_LOCALE, "false"));


            this.locale = getLocale(languageCode, countryCode);
            this.configuration = properties;
        }
    }

    @Override
    public Properties getConfiguration() {
        return configuration;
    }

    /**
     * Get the configured {@link Locale}.
     * <p/>
     * Does not return the default locale if locale is not configured.  The implementation
     * can interpret non-configuration in whatever way makes sense to that implementation,
     * including defaulting it to the default locale.
     *
     * @return The configured {@link Locale}, or <code>null</code> if the locale
     * is not configured.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns a Locale matching the passed in languageCode, and countryCode
     *
     * @param languageCode lowercase two-letter ISO-639 code.
     * @param countryCode  uppercase two-letter ISO-3166 code.
     * @return Locale        matching the passed in languageCode and optionally the
     * countryCode. If languageCode is null the default Locale
     * will be returned.
     * @throws SmooksConfigException if the Locale is not installed on the system
     */
    protected Locale getLocale(final String languageCode, final String countryCode) {
        Locale locale;

        if (languageCode == null) {
            return null;
        } else if (countryCode == null) {
            locale = new Locale(languageCode.trim());
        } else {
            locale = new Locale(languageCode.trim(), countryCode.trim());
        }

        if (verifyLocale) {
            if (!isLocalInstalled(locale)) {
                throw new SmooksConfigException("Locale " + locale + " is not available on this system.");
            }
        }

        return locale;
    }

    protected boolean isLocalInstalled(final Locale locale) {
        return Arrays.asList(Locale.getAvailableLocales()).contains(locale);
    }
}
