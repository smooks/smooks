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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.dtd;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.DatatypeValidator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.dtd.ListDatatypeValidator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.dtd.XML11IDDatatypeValidator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.dtd.XML11IDREFDatatypeValidator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.dtd.XML11NMTOKENDatatypeValidator;

/**
 * the factory to create/return built-in XML 1.1 DVs and create user-defined DVs
 * 
 * @xerces.internal  
 *
 * @author Neil Graham, IBM
 *
 * @version $Id$
 */
public class XML11DTDDVFactoryImpl extends DTDDVFactoryImpl {

    static final Hashtable fXML11BuiltInTypes = new Hashtable();

    /**
     * return a dtd type of the given name
     * This will call the super class if and only if it does not
     * recognize the passed-in name.  
     *
     * @param name  the name of the datatype
     * @return      the datatype validator of the given name
     */
    public DatatypeValidator getBuiltInDV(String name) {
        if(fXML11BuiltInTypes.get(name) != null) {
            return (DatatypeValidator)fXML11BuiltInTypes.get(name);
        }
        return (DatatypeValidator)fBuiltInTypes.get(name);
    }

    /**
     * get all built-in DVs, which are stored in a hashtable keyed by the name
     * New XML 1.1 datatypes are inserted.
     *
     * @return      a hashtable which contains all datatypes
     */
    public Hashtable getBuiltInTypes() {
        Hashtable toReturn = (Hashtable)fBuiltInTypes.clone();
        Iterator entries = fXML11BuiltInTypes.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object key = entry.getKey();
            Object dv = entry.getValue();
            toReturn.put(key, dv);
        }
        return toReturn;
    }

    static {
        fXML11BuiltInTypes.put("XML11ID", new XML11IDDatatypeValidator());
        DatatypeValidator dvTemp = new XML11IDREFDatatypeValidator();
        fXML11BuiltInTypes.put("XML11IDREF", dvTemp);
        fXML11BuiltInTypes.put("XML11IDREFS", new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.dtd.ListDatatypeValidator(dvTemp));
        dvTemp = new XML11NMTOKENDatatypeValidator();
        fXML11BuiltInTypes.put("XML11NMTOKEN", dvTemp);
        fXML11BuiltInTypes.put("XML11NMTOKENS", new ListDatatypeValidator(dvTemp));
    } // <clinit>


}//XML11DTDDVFactoryImpl

