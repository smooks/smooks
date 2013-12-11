/*
	Milyn - Copyright (C) 2008

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
package org.milyn.yaml;

import org.apache.commons.lang.StringUtils;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;

public class KeyMapDigester {

    private static final String KEY_MAP_KEY_ELEMENT = "key";

    private static final String KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE = "from";

    private static final String KEY_MAP_KEY_ELEMENT_TO_ATTRIBUTE = "to";

    public static HashMap<String, String> digest(Element keyMapElement) {
        HashMap<String, String> keyMap = new HashMap<String, String>();

        NodeList keys = keyMapElement.getElementsByTagNameNS("*", KEY_MAP_KEY_ELEMENT);

        for (int i = 0; keys != null && i < keys.getLength(); i++) {
            Element keyElement = (Element) keys.item(i);

            String from = DomUtils.getAttributeValue(keyElement, KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE);

            if (StringUtils.isBlank(from)) {
                throw new SmooksConfigurationException("The '" + KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE + "' attribute isn't defined or is empty for the key element: " + keyElement);
            }
            from = from.trim();

            String value = DomUtils.getAttributeValue(keyElement, KEY_MAP_KEY_ELEMENT_TO_ATTRIBUTE);
            if (value == null) {
                value = DomUtils.getAllText(keyElement, true);
                if (StringUtils.isBlank(value)) {
                    value = null;
                }
            }
            keyMap.put(from, value);
        }

        return keyMap;
    }

}
