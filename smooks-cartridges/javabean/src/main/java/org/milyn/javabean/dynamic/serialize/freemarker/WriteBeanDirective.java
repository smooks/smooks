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

package org.milyn.javabean.dynamic.serialize.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.javabean.dynamic.BeanMetadata;
import org.milyn.javabean.dynamic.BeanRegistrationException;
import org.milyn.javabean.dynamic.Model;
import org.milyn.javabean.dynamic.serialize.BeanWriter;
import org.milyn.commons.xml.XmlUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

/**
 * Write bean directive.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class WriteBeanDirective extends AbstractBeanDirective {

    private static Log logger = LogFactory.getLog(WriteBeanDirective.class);

    public void execute(Environment environment, Map params, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        Object bean = getBeanObject(environment, params, "writeBean");

        SimpleScalar indentScalar = (SimpleScalar) params.get("indent");
        int indent = 0;
        if(indentScalar != null) {
            String indentParamVal = indentScalar.getAsString().trim();
            try {
                indent = Integer.parseInt(indentParamVal);
                indent = Math.min(indent, 100);
            } catch(NumberFormatException e) {
                logger.debug("Invalid <@writeNamespaces> 'indent' parameter value '" + indentParamVal + "'.  Must be a valid integer (<= 100).");
            }
        }

        BeanModel modelBeanModel = (BeanModel) environment.getDataModel().get(FreeMarkerBeanWriter.MODEL_CTX_KEY);
        Model model = (Model) modelBeanModel.getWrappedObject();
        BeanMetadata beanMetadata = model.getBeanMetadata(bean);

        if(beanMetadata == null) {
            BeanRegistrationException.throwUnregisteredBeanInstanceException(bean);
        }

        BeanWriter beanWriter = beanMetadata.getWriter();

        if(beanMetadata.getPreText() != null) {
            environment.getOut().write(beanMetadata.getPreText());
        }

        if(indent > 0) {
            StringWriter beanWriteBuffer = new StringWriter();

            beanWriteBuffer.write('\n');
            beanWriter.write(bean, beanWriteBuffer, model);

            environment.getOut().write(XmlUtil.indent(beanWriteBuffer.toString(), indent));
        } else {
            beanWriter.write(bean, environment.getOut(), model);
        }
    }

}
