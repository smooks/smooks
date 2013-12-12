package org.milyn.json;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class KeyMapDigester {

	private static final String KEY_MAP_KEY_ELEMENT = "key";

	private static final String KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE = "from";

	private static final String KEY_MAP_KEY_ELEMENT_TO_ATTRIBUTE = "to";

	public static HashMap<String, String> digest(Element keyMapElement) {
		HashMap<String, String> keyMap = new HashMap<String, String>();

		NodeList keys = keyMapElement.getElementsByTagNameNS("*", KEY_MAP_KEY_ELEMENT);

        for (int i = 0; keys != null && i < keys.getLength(); i++) {
        	Element keyElement = (Element)keys.item(i);

        	String from = DomUtils.getAttributeValue(keyElement, KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE);

        	if(StringUtils.isBlank(from)) {
        		throw new SmooksConfigurationException("The '"+ KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE +"' attribute isn't defined or is empty for the key element: " + keyElement);
        	}
        	from = from.trim();

        	String value = DomUtils.getAttributeValue(keyElement, KEY_MAP_KEY_ELEMENT_TO_ATTRIBUTE);
        	if(value == null) {
        		value = DomUtils.getAllText(keyElement, true);
        		if(StringUtils.isBlank(value)) {
        			value = null;
        		}
        	}
        	keyMap.put(from, value);
        }

		return keyMap;
	}

}
