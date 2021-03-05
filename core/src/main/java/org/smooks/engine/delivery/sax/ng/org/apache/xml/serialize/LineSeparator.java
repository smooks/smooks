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


package org.smooks.engine.delivery.sax.ng.org.apache.xml.serialize;


/**
 * @deprecated This class was deprecated in Xerces 2.9.0. It is recommended 
 * that new applications use the DOM Level 3 LSSerializer or JAXP's Transformation 
 * API for XML (TrAX) for serializing XML. See the Xerces documentation for more 
 * information.
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio..com">Assaf Arkin</a>
 * @see OutputFormat
 */
public final class LineSeparator
{
    
    
    /**
     * Line separator for Unix systems (<tt>\n</tt>).
     */
    public static final String Unix = "\n";
    
    
    /**
     * Line separator for Windows systems (<tt>\r\n</tt>).
     */
    public static final String Windows = "\r\n";
    
    
    /**
     * Line separator for Macintosh systems (<tt>\r</tt>).
     */
    public static final String Macintosh = "\r";
    
    
    /**
     * Line separator for the Web (<tt>\n</tt>).
     */
    public static final String Web = "\n";
    
    
}


