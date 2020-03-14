/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.javabean.decoders;

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.config.Configurable;
import org.smooks.javabean.DataDecoder;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

/**
 * LocaleAwareDecoder is a decoder 'helper' that can be subclassed by any DataDecoder
 * implementation that relies on {@link java.util.Locale} information to perform
 * Data Decoding.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public abstract class LocaleAwareDecoder implements DataDecoder, Configurable
{

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

    public void setConfiguration(Properties resourceConfig) throws SmooksConfigurationException {
        final String locale = resourceConfig.getProperty(LOCALE);
        final String languageCode;
        final String countryCode;

        if(locale != null) {
            String[] localTokens;

            if(locale.indexOf('-') != -1) {
                localTokens = locale.split("-");
            } else {
                localTokens = locale.split("_");                
            }

            languageCode = localTokens[0];
            if(localTokens.length == 2) {
                countryCode = localTokens[1];
            } else {
                countryCode = null;
            }
        } else {
            languageCode = resourceConfig.getProperty(LOCALE_LANGUAGE_CODE);
            countryCode = resourceConfig.getProperty(LOCALE_COUNTRY_CODE);
        }

        verifyLocale = Boolean.parseBoolean(resourceConfig.getProperty(VERIFY_LOCALE, "false"));


        this.locale = getLocale( languageCode, countryCode );
        this.configuration = resourceConfig;
    }

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
     * @param languageCode	lowercase two-letter ISO-639 code.
     * @param countryCode	uppercase two-letter ISO-3166 code.
     * @return Locale		matching the passed in languageCode and optionally the
     * 						countryCode. If languageCode is null the default Locale
     * 						will be returned.
     * @throws org.smooks.cdr.SmooksConfigurationException
     * 						if the Locale is not installed on the system
     */
    protected Locale getLocale(final String languageCode, final String countryCode ) {
    	Locale locale = null;

    	if ( languageCode == null ) {
    		return null;
        } else if ( countryCode == null  ) {
    		locale = new Locale( languageCode.trim() );
        } else {
    		locale =  new Locale( languageCode.trim(), countryCode.trim() );
        }

    	if ( verifyLocale ) {
    		if ( !isLocalInstalled( locale ) ) {
    			throw new SmooksConfigurationException( "Locale " + locale + " is not available on this system.");
            }
        }
        
    	return locale;
    }

    protected boolean isLocalInstalled(final Locale locale )
    {
    	return Arrays.asList( Locale.getAvailableLocales() ).contains( locale );
    }
}