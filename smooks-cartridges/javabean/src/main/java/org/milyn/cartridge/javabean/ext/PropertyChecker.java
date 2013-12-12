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
package org.milyn.cartridge.javabean.ext;

import org.milyn.commons.SmooksException;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.commons.util.ClassUtil;
import org.milyn.commons.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

/**
 * Binding "property" attribute checker.
 * <p/>
 * The binding "property" attribute should not be specified when binding to a Collection/Array, and must be
 * specified when binding to a non-collection.  This visitor enforces these constraints.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class PropertyChecker implements DOMVisitBefore {

    private static enum BeanType {
        ARRAY,
        COLLECTION,
        MAP,
        OTHER
    }

    public void visitBefore(Element element, ExecutionContext execContext) throws SmooksException {
        BeanType beanType = getBeanType(element);
        boolean isPropertSpecified = (DomUtils.getAttributeValue(element, "property") != null);
        boolean isSetterMethodSpecified = (DomUtils.getAttributeValue(element, "setterMethod") != null);
        String bindingType = DomUtils.getName(element);

        if(isPropertSpecified && isSetterMethodSpecified) {
        	throw new SmooksConfigurationException("'" + bindingType + "' binding specifies a 'property' and a 'setterMethod' attribute.  Only one of both may be set.");
        }
        if(isPropertSpecified && beanType == BeanType.COLLECTION) {
            throw new SmooksConfigurationException("'" + bindingType + "' binding specifies a 'property' attribute.  This is not valid for a Collection target.");
        }
        if(isPropertSpecified && beanType == BeanType.ARRAY) {
            throw new SmooksConfigurationException("'" + bindingType + "' binding specifies a 'property' attribute.  This is not valid for an Array target.");
        }
        if(isSetterMethodSpecified && beanType == BeanType.COLLECTION) {
            throw new SmooksConfigurationException("'" + bindingType + "' binding specifies a 'setterMethod' attribute.  This is not valid for a Collection target.");
        }
        if(isSetterMethodSpecified && beanType == BeanType.ARRAY) {
            throw new SmooksConfigurationException("'" + bindingType + "' binding specifies a 'setterMethod' attribute.  This is not valid for an Array target.");
        }
        if(!isPropertSpecified && !isSetterMethodSpecified && beanType == BeanType.OTHER) {
            throw new SmooksConfigurationException("'" + bindingType + "' binding for bean class '" + getBeanTypeName(element) + "' must specify a 'property' or 'setterMethod' attribute.");
        }
    }

    private BeanType getBeanType(Element bindingElement) {
        String beanClassName = getBeanTypeName(bindingElement);

        if(beanClassName.endsWith("[]")) {
            return BeanType.ARRAY;
        } else {
            Class<?> beanClass = getBeanClass(bindingElement);

            if (Collection.class.isAssignableFrom(beanClass)) {
                return BeanType.COLLECTION;
            } else if (Map.class.isAssignableFrom(beanClass)) {
                return BeanType.MAP;
            } else {
                return BeanType.OTHER;
            }
        }
    }

    private Class<?> getBeanClass(Element bindingElement) {
        String beanClassName = getBeanTypeName(bindingElement);

        Class<?> beanClass;
        try {
            beanClass = ClassUtil.forName(beanClassName, getClass());
        } catch (ClassNotFoundException e) {
            throw new SmooksConfigurationException("Bean class '" + beanClassName + "' not avilable on classpath.");
        }
        return beanClass;
    }

    private String getBeanTypeName(Element bindingElement) {
        return ((Element)bindingElement.getParentNode()).getAttribute("class");
    }
}
