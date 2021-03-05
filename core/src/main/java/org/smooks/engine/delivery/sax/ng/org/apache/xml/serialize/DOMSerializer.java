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


import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;



/**
 * Interface for a DOM serializer implementation.
 * 
 * @deprecated This class was deprecated in Xerces 2.9.0. It is recommended 
 * that new applications use the DOM Level 3 LSSerializer or JAXP's Transformation 
 * API for XML (TrAX) for serializing XML. See the Xerces documentation for more 
 * information.
 * @version $Revision$ $Date$
 * @author <a href="mailto:Scott_Boag/CAM/Lotus@lotus.com">Scott Boag</a>
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
public interface DOMSerializer
{


    /**
     * Serialized the DOM element. Throws an exception only if
     * an I/O exception occured while serializing.
     *
     * @param elem The element to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public void serialize( Element elem )
        throws IOException;


    /**
     * Serializes the DOM document. Throws an exception only if
     * an I/O exception occured while serializing.
     *
     * @param doc The document to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public void serialize( Document doc )
        throws IOException;


    /**
     * Serializes the DOM document fragment. Throws an exception
     * only if an I/O exception occured while serializing.
     *
     * @param frag The document fragment to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public void serialize( DocumentFragment frag )
        throws IOException;


}



