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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.transform.stax.StAXResult;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLDocumentHandler;

/**
 * <p>An extension to XMLDocumentHandler for building StAX structures.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
interface StAXDocumentHandler extends XMLDocumentHandler {
    
    /**
     * <p>Sets the <code>StAXResult</code> object which will be receiving the output.</p>
     */
    public void setStAXResult(StAXResult result);
    
    public void startDocument(XMLStreamReader reader) throws XMLStreamException;
    public void endDocument(XMLStreamReader reader) throws XMLStreamException;
    public void comment(XMLStreamReader reader) throws XMLStreamException;
    public void processingInstruction(XMLStreamReader reader) throws XMLStreamException;
    public void entityReference(XMLStreamReader reader) throws XMLStreamException;
    
    public void startDocument(StartDocument event) throws XMLStreamException;
    public void endDocument(EndDocument event) throws XMLStreamException;
    public void doctypeDecl(DTD event) throws XMLStreamException;
    public void characters(Characters event) throws XMLStreamException;
    public void cdata(Characters event) throws XMLStreamException;
    public void comment(Comment event) throws XMLStreamException;
    public void processingInstruction(ProcessingInstruction event) throws XMLStreamException;
    public void entityReference(EntityReference event) throws XMLStreamException;
    
    public void setIgnoringCharacters(boolean ignore);

} // StAXDocumentHandler
