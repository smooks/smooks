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
package org.smooks.templating.freemarker;

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.ext.dom.NodeModel;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Element;
import org.smooks.xml.XmlUtil;

/**
 * Serialize a NodeModel variable to the template stream.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class NodeModelSerializer implements TemplateDirectiveModel {

    public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        TemplateModel nodeModelVariable = (TemplateModel) map.get("nodeModel");
        if(nodeModelVariable == null) {
            throw new TemplateModelException("'nodeModel' variable not defined on 'serializer' directive.");
        } else if(nodeModelVariable instanceof NodeModel) {
            Element element = (Element) ((NodeModel)nodeModelVariable).getWrappedObject();
            TemplateModel format = (TemplateModel) map.get("format");

            if(format instanceof TemplateBooleanModel) {
                XmlUtil.serialize(element, ((TemplateBooleanModel)format).getAsBoolean(), environment.getOut());
            } else {
                XmlUtil.serialize(element, false, environment.getOut());
            }
        } else {
            throw new TemplateModelException("Invalid NodeModel variable reference.  Not a NodeModel.");
        }
    }
}
