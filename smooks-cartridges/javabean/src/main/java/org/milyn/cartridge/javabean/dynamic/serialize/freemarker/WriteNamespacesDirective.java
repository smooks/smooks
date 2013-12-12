/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.cartridge.javabean.dynamic.serialize.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.*;
import org.milyn.cartridge.javabean.dynamic.Model;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
class WriteNamespacesDirective implements TemplateDirectiveModel {

    public void execute(Environment environment, Map params, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        Writer writer = environment.getOut();
        BeanModel modelBeanModel = (BeanModel) environment.getDataModel().get(FreeMarkerBeanWriter.MODEL_CTX_KEY);
        Model model = (Model) modelBeanModel.getWrappedObject();
        Map<String, String> namespaces = model.getNamespacePrefixMappings();
        Set<Map.Entry<String, String>> nsEntries = namespaces.entrySet();
        boolean addNewline = false;
        SimpleScalar indentScalar = (SimpleScalar) params.get("indent");
        int indent = 12;

        if(indentScalar != null) {
            String indentParamVal = indentScalar.getAsString().trim();
            try {
                indent = Integer.parseInt(indentParamVal);
                indent = Math.min(indent, 100);
            } catch(NumberFormatException e) {
                indent = 12;
            }
        }


        for(Map.Entry<String, String> nsEntry : nsEntries) {
            if(addNewline) {
                writer.write('\n');
                for(int i = 0; i < indent; i++) {
                    writer.write(' ');
                }
            }

            String uri = nsEntry.getKey();
            String prefix = nsEntry.getValue();

            if(prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX) || prefix.equals("xmlns")) {
                writer.write("xmlns=");
            } else {
                writer.write("xmlns:" + prefix + "=");
            }
            writer.write('"');
            writer.write(uri);
            writer.write('"');
            
            addNewline = true;
        }
    }
}