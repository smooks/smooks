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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs;

/**
 * This exception might be thrown by any constraint checking method.
 *
 * @xerces.internal 
 *
 * @author Elena Litani, IBM
 *
 * @version $Id$
 */
public class XMLSchemaException extends Exception {

    /** Serialization version. */
    static final long serialVersionUID = -9096984648537046218L;
    
    // store a datatype error: error code plus the arguments
    String key;
    Object[] args;

    // report an error
    public XMLSchemaException(String key, Object[] args) {
        this.key = key;
        this.args = args;
    }

    public String getKey() {
        return key;
    }

    public Object[] getArgs() {
        return args;
    }

}
