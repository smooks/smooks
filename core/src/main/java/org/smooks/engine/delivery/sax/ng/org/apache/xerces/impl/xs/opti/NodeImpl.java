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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.opti;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.opti.DefaultNode;

/**
 * @xerces.internal  
 * 
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
public class NodeImpl extends DefaultNode {
    
    String prefix;
    String localpart;
    String rawname;
    String uri;
    short nodeType;
    boolean hidden;
    
    
    public NodeImpl() {
    }
    
    
    public NodeImpl(String prefix, String localpart, String rawname, String uri, short nodeType) {
        this.prefix = prefix;
        this.localpart = localpart;
        this.rawname = rawname;
        this.uri = uri;
        this.nodeType = nodeType;
    }


    public String getNodeName() {
        return rawname;
    }


    public String getNamespaceURI() {
        return uri;
    }


    public String getPrefix() {
        return prefix;
    }


    public String getLocalName() {
        return localpart;
    }
    
    
    public short getNodeType() {
        return nodeType;
    }


    // other methods
    
    public void setReadOnly(boolean hide, boolean deep) {
        hidden = hide;
    }
    
    public boolean getReadOnly() {
        return hidden;
    }
    
    /** NON-DOM method for debugging convenience. */
    public String toString() {
        return "[" + getNodeName() + ": " + getNodeValue() + "]";
    }
}