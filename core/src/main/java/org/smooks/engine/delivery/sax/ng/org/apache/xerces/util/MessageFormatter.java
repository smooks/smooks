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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.util;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * This interface provides a generic message formatting mechanism and
 * is useful for producing messages that must be localized and/or formatted
 * with replacement text.
 *
 * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public interface MessageFormatter {

    //
    // MessageFormatter methods
    //

    /**
     * Formats a message with the specified arguments using the given
     * locale information.
     * 
     * @param locale    The locale of the message.
     * @param key       The message key.
     * @param arguments The message replacement text arguments. The order
     *                  of the arguments must match that of the placeholders
     *                  in the actual message.
     * 
     * @return Returns the formatted message.
     *
     * @throws MissingResourceException Thrown if the message with the
     *                                  specified key cannot be found.
     */
    public String formatMessage(Locale locale, String key, Object[] arguments)
        throws MissingResourceException;

} // interface MessageFormatter
