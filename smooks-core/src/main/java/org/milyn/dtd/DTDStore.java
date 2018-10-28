/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/

package org.milyn.dtd;

import com.wutka.dtd.*;
import org.milyn.profile.ProfileSet;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * DTD Datastore class.
 * <p/>
 * Simple class providing a query interface to the underlying DTD DOM implementation.
 * The intension is to hide this underlying implementation as much as possible so it
 * can be changed in the future.  See {@link DTDObjectContainer}.
 * <p/>
 * At the moment this class uses the com.wutka DTD parser.  We've also tried some
 * other DTD parsers but they all had the same missing feature which was that they
 * didn't keep attribute typing info (implemented using entities).  This would
 * be a very valuable feature for the purposes of this module.
 * <p/>
 * <b>Example</b>:<br/>
 * In the example below, it would have been usefull to have been able to look at
 * the xmlns attibute and recognise the fact that it was used to hold URI data.<br/>
 * <pre>
 * &lt;!ELEMENT html (head, body)&gt;
 * &lt;!ATTLIST html
 * 		%i18n;
 * 		id          ID             #IMPLIED
 * 		xmlns       %URI;          #FIXED 'http://www.w3.org/1999/xhtml'
 * &gt;
 * </pre>
 * where <i>URI</i> is defined as:<br/>
 * <pre>
 * &lt;!ENTITY % URI "CDATA"&gt;
 * 		&lt;!-- a Uniform Resource Identifier, see [RFC2396] --&gt;
 * </pre>
 * If this information was available we could target {@link org.milyn.delivery.ContentHandler}s
 * at elements containing "URI" attributes.
 * <p/>
 * Another examples of this idea of using DTD ENTITY definitions to target
 * {@link org.milyn.delivery.ContentHandler}s at specific elements (rather than using the element names)
 * might be using ENTITYs like the following (from http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd):
 * <pre>
 * &lt;!ENTITY % heading "h1|h2|h3|h4|h5|h6"&gt;
 * &lt;!ENTITY % lists "ul | ol | dl | menu | dir"&gt;
 * &lt;!ENTITY % blocktext "pre | hr | blockquote | address | center | noframes"&gt;
 *
 * &lt;!ENTITY % block
 *     "p | %heading; | div | %lists; | %blocktext; | isindex |fieldset | table"&gt;
 *
 * &lt;!-- %Flow; mixes block and inline and is used for list items etc. --&gt;
 * &lt;!ENTITY % Flow "(#PCDATA | %block; | form | %inline; | %misc;)*"&gt;</pre>
 *
 * @author tfennelly
 */
public class DTDStore {

	/**
	 * Loaded DTDs
	 */
	private static Hashtable<ProfileSet, DTD> dtds = new Hashtable<ProfileSet, DTD>();

	/**
	 * Add the DTD for the profileSet device
	 * @param profileSet Device Context
     * @param stream DTD data stream.
     */
	public static void addDTD(ProfileSet profileSet, InputStream stream) {
		try {
			com.wutka.dtd.DTDParser parser = new com.wutka.dtd.DTDParser(new InputStreamReader(stream));
			DTD dtd = parser.parse();
			dtds.put(profileSet, dtd);
		} catch(Exception excep) {
			throw new IllegalStateException("Error parsing dtd for [" + profileSet.getBaseProfile() + "].", excep);
        }
	}

	/**
	 * Get the DTD Object for the profile, wrapped in a {@link DTDObjectContainer}
	 * instance.
	 * @param profileSet Profile set.
	 * @return The DTD Object reference container for the deviceContext device.
	 */
	public static DTDObjectContainer getDTDObject(ProfileSet profileSet) {
		return new DTDObjectContainer(getDTD(profileSet));
	}

	/**
	 * Get the DTD for the profile.
	 * @param profileSet Profile set.
	 * @return The DTD for the deviceContext device.
	 */
	private static DTD getDTD(ProfileSet profileSet) {
		DTD dtd;

		if(profileSet == null) {
			throw new IllegalArgumentException("null 'profileSet' arg in method call.");
		}

		dtd = dtds.get(profileSet);
		if(dtd == null) {
            throw new IllegalStateException("Error loading device dtd for [" + profileSet.getBaseProfile() + "].");
		}

		return dtd;
	}

	/**
	 * Container class for the underlying DTD implementation.
	 * <p/>
	 * We're trying to hide the underlying implementation in the belief that it will
	 * probably change in the future.
	 * @author tfennelly
	 */
	public static class DTDObjectContainer {

		/**
		 * DTD Object ref.
		 */
		private DTD wutkaDTDObject;
		/**
		 * DTD Element child element names. An optimization to save iterating over
		 * the DTD element contents (DTDItem etc).
		 * <p/>
		 * "*" for PC Data.
		 */
		private Hashtable elementElements = new Hashtable();
		/**
		 * DTD Element attribute names. An optimization to save iterating over
		 * the DTD element contents (DTDItem etc).
		 */
		private Hashtable elementAttributes = new Hashtable();

		/**
		 * Private Constructor.
		 * @param wutkaDTDObject wutka DTD Object.
		 */
		private DTDObjectContainer(DTD wutkaDTDObject) {
			this.wutkaDTDObject = wutkaDTDObject;
		}

		/**
		 * Get the child elements for the named element.
		 * <p/>
		 * PCData is returned as a "*" list entry.
		 * @param elementName Element name.
		 * @return List of allowed element names ({@link String}s).
		 */
		@SuppressWarnings({ "WeakerAccess", "unchecked" })
		public List getChildElements(String elementName) {
			Vector childElements = (Vector)elementElements.get(elementName);

			if(childElements == null) {
				DTDElement element = getElement(elementName);

				if(element != null) {
					childElements = new Vector();
					elementElements.put(elementName, childElements);
					if(element.content instanceof DTDContainer) {
						DTDContainer container = (DTDContainer)element.content;
						Vector itemsVec = container.getItemsVec();
						for(int i = 0; i < itemsVec.size(); i++) {
							Object item = itemsVec.elementAt(i);
							if(item instanceof DTDName) {
								childElements.add(((DTDName)item).getValue());
							}
						}
					}
				}
			}

			return childElements;
		}

		/**
		 * Get the defined element attributes for the named element.
		 * @param elementName The element name.
		 * @return The list of attribute names ({@link String}s) for the named element.
		 * @throws ElementNotDefined Element not defined.  Calls shouldn't be made to this
		 * function for undefined elements.
		 */
		@SuppressWarnings({ "WeakerAccess", "unchecked" })
		public List getElementAttributes(String elementName) throws ElementNotDefined {
			Vector attributes = (Vector)elementAttributes.get(elementName);

			if(attributes == null) {
				DTDElement element = getElement(elementName);

				if(element != null) {
					attributes = new Vector();
					elementAttributes.put(elementName, attributes);
					attributes.addAll(element.attributes.keySet());
				} else {
					throw new ElementNotDefined("Element [" + elementName + "] not defined in DTD.");
				}
			}

			return attributes;
		}

		/**
		 * Get the DTDElement for the named element.
		 * @param elementName The element name.
		 * @return The DTDElement for the specified element name, or null
		 * if not defined.
		 */
		private DTDElement getElement(String elementName) {

			for (final Object o : wutkaDTDObject.elements.entrySet())
			{
				Map.Entry element = (Map.Entry) o;
				DTDElement dtdElement = (DTDElement) element.getValue();

				if (dtdElement.name.equalsIgnoreCase(elementName))
				{
					return dtdElement;
				}
			}

			return null;
		}

		/**
		 * Get the DTD elements whose content spec is represented in the DTD DOM
		 * by the specified runtime class.
		 * <p/>
		 * Runtimes - DTDEmpty, DTDAny, DTDMixed, DTDPCData
		 * @param dtdItemRuntime - DTDEmpty, DTDAny, DTDMixed, DTDPCData
		 * @param isInstance Desired results of the isinstance check.
		 * @return Array of elements names.
		 */
		@SuppressWarnings({ "unchecked", "SuspiciousToArrayCall" })
		private String[] getElements(Class dtdItemRuntime, boolean isInstance) {
			Iterator iterator;
			Vector elements = new Vector();
			String[] returnVal;

			iterator = wutkaDTDObject.elements.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry element = (Map.Entry)iterator.next();
				com.wutka.dtd.DTDElement dtdElement = (com.wutka.dtd.DTDElement)element.getValue();

				if(dtdItemRuntime.isInstance(dtdElement.getContent()) == isInstance) {
					elements.addElement(dtdElement.getName());
				}
			}

			returnVal = new String[elements.size()];
			elements.toArray(returnVal);

			return returnVal;
		}

		/**
		 * Get the list of DTD elements whose content spec is defined as being EMPTY.
		 * @return Array of elements names.
		 */
		public String[] getEmptyElements() {
			return getElements(DTDEmpty.class, true);
		}

		/**
		 * Get the list of DTD elements whose content spec is not defined as being EMPTY.
		 * @return Array of elements names.
		 */
		public String[] getNonEmptyElements() {
			return getElements(DTDEmpty.class, false);
		}

		/**
		 * Get the list of DTD elements whose content spec is defined as being ANY.
		 * @return Array of elements names.
		 */
		public String[] getAnyElements() {
			return getElements(DTDAny.class, true);
		}

		/**
		 * Get the list of DTD elements whose content spec is not defined as being ANY.
		 * @return Array of elements names.
		 */
		public String[] getNonAnyElements() {
			return getElements(DTDAny.class, false);
		}

		/**
		 * Get the list of DTD elements whose content spec is defined as being MIXED.
		 * @return Array of elements names.
		 */
		public String[] getMixedElements() {
			return getElements(DTDMixed.class, true);
		}

		/**
		 * Get the list of DTD elements whose content spec is not defined as being MIXED.
		 * @return Array of elements names.
		 */
		public String[] getNonMixedElements() {
			return getElements(DTDMixed.class, false);
		}

		/**
		 * Get the list of DTD elements whose content spec is defined as being #PCDATA.
		 * @return Array of elements names.
		 */
		public String[] getPCDataElements() {
			return getElements(DTDPCData.class, true);
		}

		/**
		 * Get the list of DTD elements whose content spec is defined as being #PCDATA.
		 * @return Array of elements names.
		 */
		public String[] getNonPCDataElements() {
			return getElements(DTDPCData.class, false);
		}
	}
}
