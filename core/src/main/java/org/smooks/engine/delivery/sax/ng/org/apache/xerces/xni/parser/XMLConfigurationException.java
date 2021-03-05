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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

/**
 * An XNI parser configuration exception. This exception class extends
 * <code>XNIException</code> in order to differentiate between general
 * parsing errors and configuration errors.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class XMLConfigurationException
    extends XNIException {

    /** Serialization version. */
    static final long serialVersionUID = -5437427404547669188L;
    
    //
    // Constants
    //

    /** Exception type: identifier not recognized. */
    public static final short NOT_RECOGNIZED = 0;

    /** Exception type: identifier not supported. */
    public static final short NOT_SUPPORTED = 1;

    //
    // Data
    //

    /** Exception type. */
    protected short fType;

    /** Identifier. */
    protected String fIdentifier;

    //
    // Constructors
    //

    /** 
     * Constructs a configuration exception with the specified type
     * and feature/property identifier.
     *
     * @param type       The type of the exception.
     * @param identifier The feature or property identifier.
     *
     * @see #NOT_RECOGNIZED
     * @see #NOT_SUPPORTED
     */
    public XMLConfigurationException(short type, String identifier) {
        super(identifier);
        fType = type;
        fIdentifier = identifier;
    } // <init>(short,String)

    /** 
     * Constructs a configuration exception with the specified type,
     * feature/property identifier, and error message
     *
     * @param type       The type of the exception.
     * @param identifier The feature or property identifier.
     * @param message    The error message.
     *
     * @see #NOT_RECOGNIZED
     * @see #NOT_SUPPORTED
     */
    public XMLConfigurationException(short type, String identifier,
                                     String message) {
        super(message);
        fType = type;
        fIdentifier = identifier;
    } // <init>(short,String,String)

    //
    // Public methods
    //

    /** 
     * Returns the exception type. 
     *
     * @see #NOT_RECOGNIZED
     * @see #NOT_SUPPORTED
     */
    public short getType() {
        return fType;
    } // getType():short

    /** Returns the feature or property identifier. */
    public String getIdentifier() {
        return fIdentifier;
    } // getIdentifier():String

} // class XMLConfigurationException
