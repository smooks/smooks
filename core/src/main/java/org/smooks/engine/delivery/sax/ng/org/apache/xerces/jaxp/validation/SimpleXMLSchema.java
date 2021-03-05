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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.AbstractXMLSchema;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.grammars.Grammar;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.grammars.XMLGrammarPool;

/**
 * <p>Implementation of Schema for W3C XML Schemas which 
 * contains schema components from one target namespace.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
final class SimpleXMLSchema extends AbstractXMLSchema implements XMLGrammarPool {
    
    /** Zero length grammar array. */
    private static final Grammar [] ZERO_LENGTH_GRAMMAR_ARRAY = new Grammar [0];

    private final Grammar fGrammar;
    private final Grammar[] fGrammars;
    private final XMLGrammarDescription fGrammarDescription;
    
    public SimpleXMLSchema(Grammar grammar) {
        fGrammar = grammar;
        fGrammars = new Grammar[] {grammar};
        fGrammarDescription = grammar.getGrammarDescription();
    }
    
    /*
     * XMLGrammarPool methods
     */

    public Grammar[] retrieveInitialGrammarSet(String grammarType) {
        return XMLGrammarDescription.XML_SCHEMA.equals(grammarType) ? 
                (Grammar[]) fGrammars.clone() : ZERO_LENGTH_GRAMMAR_ARRAY;
    }

    public void cacheGrammars(String grammarType, Grammar[] grammars) {}

    public Grammar retrieveGrammar(XMLGrammarDescription desc) {
        return fGrammarDescription.equals(desc) ? fGrammar : null;
    }

    public void lockPool() {}

    public void unlockPool() {}

    public void clear() {}
    
    /*
     * XSGrammarPoolContainer methods
     */

    public XMLGrammarPool getGrammarPool() {
        return this;
    }

    public boolean isFullyComposed() {
        return true;
    }

} // SimpleXMLSchema
