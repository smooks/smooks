/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.xpointer;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.MessageFormatter;

/**
 * XPointerMessageFormatter provides error messages for the XPointer Framework
 * and element() Scheme Recommendations.
 * 
 * @xerces.internal
 * 
 * @version $Id$
 */
final class XPointerMessageFormatter implements MessageFormatter {

    public static final String XPOINTER_DOMAIN = "http://www.w3.org/TR/XPTR";

    // private objects to cache the locale and resource bundle
    private Locale fLocale = null;

    private ResourceBundle fResourceBundle = null;

    /**
     * Formats a message with the specified arguments using the given locale
     * information.
     * 
     * @param locale
     *            The locale of the message.
     * @param key
     *            The message key.
     * @param arguments
     *            The message replacement text arguments. The order of the
     *            arguments must match that of the placeholders in the actual
     *            message.
     * 
     * @return Returns the formatted message.
     * 
     * @throws MissingResourceException
     *             Thrown if the message with the specified key cannot be found.
     */
    public String formatMessage(Locale locale, String key, Object[] arguments)
            throws MissingResourceException {

        if (locale == null) {
            locale = Locale.getDefault();
        }
        if (locale != fLocale) {
            fResourceBundle = ResourceBundle.getBundle("org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XPointerMessages", locale);
            // memorize the most-recent locale
            fLocale = locale;
        }

        String msg = fResourceBundle.getString(key);
        if (arguments != null) {
            try {
                msg = java.text.MessageFormat.format(msg, arguments);
            } catch (Exception e) {
                msg = fResourceBundle.getString("FormatFailed");
                msg += " " + fResourceBundle.getString(key);
            }
        }

        if (msg == null) {
            msg = fResourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(msg,
                    "org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XPointerMessages", key);
        }

        return msg;
    }
}