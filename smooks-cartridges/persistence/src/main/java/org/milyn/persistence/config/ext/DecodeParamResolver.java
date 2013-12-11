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
package org.milyn.persistence.config.ext;

import java.util.UUID;

import org.milyn.commons.SmooksException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.extension.ExtensionContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.persistence.Constants;
import org.milyn.commons.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
/**
 * Type decoder parameter mapping visitor.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DecodeParamResolver implements DOMVisitBefore {

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        NodeList decodeParams = element.getElementsByTagNameNS(Constants.PERSISTENCE_NAMESPACE, "decodeParam");

        if(decodeParams.getLength() > 0) {
            ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);
            SmooksResourceConfiguration populatorConfig = extensionContext.getResourceStack().peek();
            SmooksResourceConfiguration decoderConfig = new SmooksResourceConfiguration();

            extensionContext.addResource(decoderConfig);
            try {
                String type = populatorConfig.getStringParameter("type");
                DataDecoder decoder = DataDecoder.Factory.create(type);
                String reType = UUID.randomUUID().toString();

                // Need to retype the populator configuration so as to get the
                // value binding BeanInstancePopulator to lookup the new decoder
                // config that we're creating here...
                populatorConfig.removeParameter("type"); // Need to remove because we only want 1
                populatorConfig.setParameter("type", reType);

                // Configure the new decoder config...
                decoderConfig.setSelector("decoder:" + reType);
                decoderConfig.setTargetProfile(extensionContext.getDefaultProfile());
                decoderConfig.setResource(decoder.getClass().getName());
                for(int i = 0; i < decodeParams.getLength(); i++) {
                    Element decoderParam = (Element) decodeParams.item(i);
                    decoderConfig.setParameter(decoderParam.getAttribute("name"), DomUtils.getAllText(decoderParam, true));
                }
            } finally {
                extensionContext.getResourceStack().pop();
            }
        }
    }
}