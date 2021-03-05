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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.opti.ElementImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.NamespaceSupport;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLSymbols;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.NamespaceContext;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class customizes the behaviour of the util.NamespaceSupport
 * class in order to easily implement some features that we need for
 * efficient schema handling.  It will not be generally useful.  
 *
 * @xerces.internal 
 *
 * @author Neil Graham, IBM
 *
 * @version $Id$
 */
public class SchemaNamespaceSupport 
    extends NamespaceSupport {
    
    private SchemaRootContext fSchemaRootContext = null;

    public SchemaNamespaceSupport (Element schemaRoot, SymbolTable symbolTable) {
        super();
        if (schemaRoot != null && !(schemaRoot instanceof ElementImpl)) {
            Document ownerDocument = schemaRoot.getOwnerDocument();
            if (ownerDocument != null && schemaRoot != ownerDocument.getDocumentElement()) {
                fSchemaRootContext = new SchemaRootContext(schemaRoot, symbolTable);
            }
        }
    } // constructor

    // more effecient than NamespaceSupport(NamespaceContext)
    public SchemaNamespaceSupport(SchemaNamespaceSupport nSupport) {
        fSchemaRootContext = nSupport.fSchemaRootContext;
        fNamespaceSize = nSupport.fNamespaceSize;
        if (fNamespace.length < fNamespaceSize)
            fNamespace = new String[fNamespaceSize];
        System.arraycopy(nSupport.fNamespace, 0, fNamespace, 0, fNamespaceSize);
        fCurrentContext = nSupport.fCurrentContext;
        if (fContext.length <= fCurrentContext)
            fContext = new int[fCurrentContext+1];
        System.arraycopy(nSupport.fContext, 0, fContext, 0, fCurrentContext+1);
    } // end constructor
    
    /**
     * This method takes a set of Strings, as stored in a
     * NamespaceSupport object, and "fools" the object into thinking
     * that this is one unified context.  This is meant to be used in
     * conjunction with things like local elements, whose declarations
     * may be deeply nested but which for all practical purposes may
     * be regarded as being one level below the global <schema>
     * element--at least with regard to namespace declarations.
     * It's worth noting that the context from which the strings are
     * being imported had better be using the same SymbolTable.
     */
    public void setEffectiveContext (String [] namespaceDecls) {
        if(namespaceDecls == null || namespaceDecls.length == 0) return;
        pushContext();
        int newSize = fNamespaceSize + namespaceDecls.length;
        if (fNamespace.length < newSize) {
            // expand namespace's size...
            String[] tempNSArray = new String[newSize];
            System.arraycopy(fNamespace, 0, tempNSArray, 0, fNamespace.length);
            fNamespace = tempNSArray;
        }
        System.arraycopy(namespaceDecls, 0, fNamespace, fNamespaceSize,
                         namespaceDecls.length);
        fNamespaceSize = newSize;
    } // setEffectiveContext(String):void

    /** 
     * This method returns an array of Strings, as would be stored in
     * a NamespaceSupport object.  This array contains all
     * declarations except those at the global level.
     */
    public String [] getEffectiveLocalContext() {
        // the trick here is to recognize that all local contexts
        // happen to start at fContext[3].
        // context 1: empty
        // context 2: decls for xml and xmlns;
        // context 3: decls on <xs:schema>: the global ones
        String[] returnVal = null;
        if (fCurrentContext >= 3) {
            int bottomLocalContext = fContext[3];
            int copyCount = fNamespaceSize - bottomLocalContext;
            if (copyCount > 0) {
                returnVal = new String[copyCount];
                System.arraycopy(fNamespace, bottomLocalContext, returnVal, 0,
                                 copyCount);
            }
        }
        return returnVal;
    } // getEffectiveLocalContext():String

    // This method removes from this object all the namespaces
    // returned by getEffectiveLocalContext. 
    public void makeGlobal() {
        if (fCurrentContext >= 3) {
            fCurrentContext = 3;
            fNamespaceSize = fContext[3];
        }
    } // makeGlobal
    
    public String getURI(String prefix) {
        String uri = super.getURI(prefix);
        if (uri == null && fSchemaRootContext != null) {
            if (!fSchemaRootContext.fDOMContextBuilt) {
                fSchemaRootContext.fillNamespaceContext();
                fSchemaRootContext.fDOMContextBuilt = true;
            }
            if (fSchemaRootContext.fNamespaceSize > 0 && 
                !containsPrefix(prefix)) {
                uri = fSchemaRootContext.getURI(prefix);
            }
        }
        return uri;
    }
    
    /**
     * This class keeps track of the namespace bindings 
     * declared on ancestors of the schema root.
     */
    static final class SchemaRootContext {
        
        //
        // Data
        //

        /** 
         * Namespace binding information. This array is composed of a
         * series of tuples containing the namespace binding information:
         * &lt;prefix, uri&gt;.
         */
        String[] fNamespace = new String[16 * 2];

        /** The size of the namespace information array. */
        int fNamespaceSize = 0;
        
        /** 
         * Flag indicating whether the namespace context 
         * has been from the root node's ancestors.
         */
        boolean fDOMContextBuilt = false;
        
        /** Schema root. **/
        private final Element fSchemaRoot;
        
        /** Symbol table. **/
        private final SymbolTable fSymbolTable;
        
        /** Temporary storage for attribute QNames. **/
        private final QName fAttributeQName = new QName();
        
        SchemaRootContext(Element schemaRoot, SymbolTable symbolTable) {
            fSchemaRoot = schemaRoot;
            fSymbolTable = symbolTable;
        }
        
        void fillNamespaceContext() {
            if (fSchemaRoot != null) {
                Node currentNode = fSchemaRoot.getParentNode();
                while (currentNode != null) {
                    if (Node.ELEMENT_NODE == currentNode.getNodeType()) {
                        NamedNodeMap attributes = currentNode.getAttributes();
                        final int attrCount = attributes.getLength();
                        for (int i = 0; i < attrCount; ++i) {
                            Attr attr = (Attr) attributes.item(i);
                            String value = attr.getValue();
                            if (value == null) {
                                value = XMLSymbols.EMPTY_STRING;
                            }
                            fillQName(fAttributeQName, attr);
                            // REVISIT: Should we be looking at non-namespace attributes
                            // for additional mappings? Should we detect illegal namespace
                            // declarations and exclude them from the context? -- mrglavas
                            if (fAttributeQName.uri == NamespaceContext.XMLNS_URI) {
                                // process namespace attribute
                                if (fAttributeQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                                    declarePrefix(fAttributeQName.localpart, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                                }
                                else {
                                    declarePrefix(XMLSymbols.EMPTY_STRING, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                                }
                            }
                        }
                        
                    }
                    currentNode = currentNode.getParentNode();
                }
            }
        }
        
        String getURI(String prefix) {
            // find prefix in the DOM context
            for (int i = 0; i < fNamespaceSize; i += 2) {
                if (fNamespace[i] == prefix) {
                    return fNamespace[i + 1];
                }
            }
            // prefix not found
            return null;
        }
        
        private void declarePrefix(String prefix, String uri) {           
            // resize array, if needed
            if (fNamespaceSize == fNamespace.length) {
                String[] namespacearray = new String[fNamespaceSize * 2];
                System.arraycopy(fNamespace, 0, namespacearray, 0, fNamespaceSize);
                fNamespace = namespacearray;
            }

            // bind prefix to uri in current context
            fNamespace[fNamespaceSize++] = prefix;
            fNamespace[fNamespaceSize++] = uri;
        }
        
        private void fillQName(QName toFill, Node node) {
            final String prefix = node.getPrefix();
            final String localName = node.getLocalName();
            final String rawName = node.getNodeName();
            final String namespace = node.getNamespaceURI();
            toFill.prefix = (prefix != null) ? fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
            toFill.localpart = (localName != null) ? fSymbolTable.addSymbol(localName) : XMLSymbols.EMPTY_STRING;
            toFill.rawname = (rawName != null) ? fSymbolTable.addSymbol(rawName) : XMLSymbols.EMPTY_STRING; 
            toFill.uri = (namespace != null && namespace.length() > 0) ? fSymbolTable.addSymbol(namespace) : null;
        }
    }
    
} // class NamespaceSupport
