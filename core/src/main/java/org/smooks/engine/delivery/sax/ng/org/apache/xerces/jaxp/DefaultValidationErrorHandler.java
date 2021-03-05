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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @version $Id$
 */

class DefaultValidationErrorHandler extends DefaultHandler {
    static private int ERROR_COUNT_LIMIT = 10;
    private int errorCount = 0;

    // XXX Fix message i18n
    public void error(SAXParseException e) throws SAXException {
        if (errorCount >= ERROR_COUNT_LIMIT) {
            // Ignore all errors after reaching the limit
            return;
        } else if (errorCount == 0) {
            // Print a warning before the first error
            System.err.println("Warning: validation was turned on but an org.xml.sax.ErrorHandler was not");
            System.err.println("set, which is probably not what is desired.  Parser will use a default");
            System.err.println("ErrorHandler to print the first " +
                               ERROR_COUNT_LIMIT +               " errors.  Please call");
            System.err.println("the 'setErrorHandler' method to fix this.");
        }

        String systemId = e.getSystemId();
        if (systemId == null) {
            systemId = "null";
        }
        String message = "Error: URI=" + systemId +
            " Line=" + e.getLineNumber() +
            ": " + e.getMessage();
        System.err.println(message);
        errorCount++;
    }
}
