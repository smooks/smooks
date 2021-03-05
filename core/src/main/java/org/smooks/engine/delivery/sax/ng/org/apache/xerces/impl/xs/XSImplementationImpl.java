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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.DOMMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.PSVIDOMImplementationImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSLoaderImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.LSInputListImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.StringListImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.LSInputList;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.StringList;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSImplementation;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSLoader;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.LSInput;

/**
 * Implements XSImplementation interface that allows one to retrieve an instance of <code>XSLoader</code>. 
 * This interface should be implemented on the same object that implements 
 * DOMImplementation.
 *
 * @xerces.internal 
 *
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class XSImplementationImpl extends PSVIDOMImplementationImpl 
 								  implements XSImplementation {

    //
    // Data
    //

    // static

    /** Dom implementation singleton. */
    static final XSImplementationImpl singleton = new XSImplementationImpl();

    //
    // Public methods
    //

    /** NON-DOM: Obtain and return the single shared object */
    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }  

    //
    // DOMImplementation methods
    //

    /** 
     * Test if the DOM implementation supports a specific "feature" --
     * currently meaning language and level thereof.
     * 
     * @param feature      The package name of the feature to test.
     * In Level 1, supported values are "HTML" and "XML" (case-insensitive).
     * At this writing, org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom supports only XML.
     *
     * @param version      The version number of the feature being tested.
     * This is interpreted as "Version of the DOM API supported for the
     * specified Feature", and in Level 1 should be "1.0"
     *
     * @return    true iff this implementation is compatable with the specified
     * feature and version.
     */
    public boolean hasFeature(String feature, String version) {
    	
        return (feature.equalsIgnoreCase("XS-Loader") && (version == null || version.equals("1.0")) ||
		super.hasFeature(feature, version));
    } // hasFeature(String,String):boolean
    
    /* (non-Javadoc)
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSImplementation#createXSLoader(org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.StringList)
     */
    public XSLoader createXSLoader(StringList versions) throws XSException {
    	XSLoader loader = new XSLoaderImpl();
    	if (versions == null){
			return loader;
    	}
    	for (int i=0; i<versions.getLength();i++){
    		if (!versions.item(i).equals("1.0")){
				String msg =
					DOMMessageFormatter.formatMessage(
						DOMMessageFormatter.DOM_DOMAIN,
						"FEATURE_NOT_SUPPORTED",
						new Object[] { versions.item(i) });
				throw new XSException(XSException.NOT_SUPPORTED_ERR, msg);
    		}
    	}
    	return loader;
    }
    
    public StringList createStringList(String[] values) {
        int length = (values != null) ? values.length : 0;
        return (length != 0) ? new StringListImpl((String[]) values.clone(), length) : StringListImpl.EMPTY_LIST;
    }
    
    public LSInputList createLSInputList(LSInput[] values) {
        int length = (values != null) ? values.length : 0;
        return (length != 0) ? new LSInputListImpl((LSInput[]) values.clone(), length) : LSInputListImpl.EMPTY_LIST;
    }

    /* (non-Javadoc)
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSImplementation#getRecognizedVersions()
     */
    public StringList getRecognizedVersions() {
        StringListImpl list = new StringListImpl(new String[]{"1.0"}, 1);
        return list;
    }

} // class XSImplementationImpl
