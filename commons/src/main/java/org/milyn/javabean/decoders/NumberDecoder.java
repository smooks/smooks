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

package org.milyn.javabean.decoders;

import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DataEncoder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * Abstract {@link Number} based DataDecoder.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class NumberDecoder extends LocaleAwareDecoder implements DataEncoder {

    /**
     * Date numberFormat configuration key.
     */
    public static final String FORMAT = "format";

    private NumberFormat numberFormat;
    private boolean isPercentage;

    public void setConfiguration(Properties config) throws SmooksConfigurationException {
        super.setConfiguration(config);
        String pattern = config.getProperty(FORMAT);
        Locale locale = getLocale();

        if(locale == null && pattern == null) {
            // Don't create a formatter if neither locale or
            // pattern are configured...
            return;
        }

        if(locale == null) {
            locale = Locale.getDefault();
        }

        if(pattern != null && pattern.indexOf('%') != -1) {
            numberFormat = NumberFormat.getPercentInstance(locale);
            isPercentage = true;
        } else {
            numberFormat = NumberFormat.getInstance(locale);
            numberFormat.setGroupingUsed(false);
        }


        if(pattern != null && numberFormat instanceof DecimalFormat) {
            ((DecimalFormat) numberFormat).applyPattern(pattern);
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

    public boolean isPercentage() {
        return isPercentage;
    }

    public String encode(Object object) throws DataDecodeException {
        if(numberFormat != null) {
            return getNumberFormat().format(object);
        } else {
            return object.toString();
        }
    }
}
