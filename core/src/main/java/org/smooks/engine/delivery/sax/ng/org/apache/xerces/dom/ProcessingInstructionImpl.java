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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.CharacterDataImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.CoreDocumentImpl;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Processing Instructions (PIs) permit documents to carry
 * processor-specific information alongside their actual content. PIs
 * are most common in XML, but they are supported in HTML as well.
 *
 * This class inherits from CharacterDataImpl to reuse its setNodeValue method.
 * 
 * @xerces.internal
 *
 * @version $Id$
 * @since  PR-DOM-Level-1-19980818.
 */
public class ProcessingInstructionImpl
    extends CharacterDataImpl
    implements ProcessingInstruction {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = 7554435174099981510L;

    //
    // Data
    //

    protected String target;

    //
    // Constructors
    //

    /** Factory constructor. */
    public ProcessingInstructionImpl(CoreDocumentImpl ownerDoc,
                                     String target, String data) {
        super(ownerDoc, data);
        this.target = target;
    }

    //
    // Node methods
    //

    /**
     * A short integer indicating what type of node this is. The named
     * constants for this value are defined in the org.w3c.dom.Node interface.
     */
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }

    /**
     * Returns the target
     */
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return target;
    }

    //
    // ProcessingInstruction methods
    //

    /**
     * A PI's "target" states what processor channel the PI's data
     * should be directed to. It is defined differently in HTML and XML.
     * <p>
     * In XML, a PI's "target" is the first (whitespace-delimited) token
     * following the "<?" token that begins the PI.
     * <p>
     * In HTML, target is always null.
     * <p>
     * Note that getNodeName is aliased to getTarget.
     */
    public String getTarget() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return target;

    } // getTarget():String

   /**
     * Returns the absolute base URI of this node or null if the implementation
     * wasn't able to obtain an absolute URI. Note: If the URI is malformed, a
     * null is returned.
     * 
     * @return The absolute base URI of this node or null.
     * @since DOM Level 3
     */
    public String getBaseURI() {

        if (needsSyncData()) {
            synchronizeData();
        }
        return ownerNode.getBaseURI();
    }


} // class ProcessingInstructionImpl
