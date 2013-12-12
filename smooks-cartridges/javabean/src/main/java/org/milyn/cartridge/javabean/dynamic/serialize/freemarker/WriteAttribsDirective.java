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
import freemarker.ext.beans.StringModel;
import freemarker.template.*;
import org.milyn.commons.util.ClassUtil;
import org.milyn.commons.xml.XmlUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Write attributes directive.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class WriteAttribsDirective implements TemplateDirectiveModel {

    public void execute(Environment environment, Map params, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        Object beanParam = params.get("bean");
        Object bean;

        if(beanParam == null) {
            if(params.containsKey("bean")) {
                throw new TemplateException("<@writeAttribs> directive parameter 'bean' is defined, but the bean is not visible in the model.  Should be a valid model object reference (no quotes) e.g. <@writeAttribs bean=customer.address ... />.", environment);
            }

            // Use the "bean" bean...
            BeanModel beanModel = (BeanModel) environment.getDataModel().get("bean");
            bean = beanModel.getWrappedObject();
        } else {
            if(!(beanParam instanceof StringModel)) {
                throw new TemplateException("<@writeAttribs> directive parameter 'bean' not defined properly.  Should be a valid model object reference (no quotes) e.g. <@writeAttribs bean=customer.address ... />.", environment);
            }

            StringModel beanModel = (StringModel) beanParam;
            bean = beanModel.getWrappedObject();
        }

        SimpleScalar attribsScalar = (SimpleScalar) params.get("attribs");
        if(attribsScalar == null) {
            if(params.containsKey("attribs")) {
                throw new TemplateException("Mandatory <@writeAttribs> directive parameter 'attribs' not defined properly.  Should be a simple String literal value (comma separated, within quotes) e.g. <@writeAttribs attribs='a,b' ... />.", environment);
            } else {
                throw new TemplateException("Mandatory <@writeAttribs> directive parameter 'attribs' not defined.  Should be a simple String literal value (comma separated, within quotes) e.g. <@writeAttribs attribs='a,b' ... />.", environment);
            }
        }

        String attribsParamVal = attribsScalar.getAsString().trim();
        String[] attribs = attribsParamVal.split(",");

        for(int i = 0; i < attribs.length; i++) {
            String[] attribTokens = attribs[i].split("@");
            String propertyName;
            String attributeName;

            if(attribTokens.length == 2) {
                propertyName = attribTokens[0];
                attributeName = attribTokens[1];
            } else {
                propertyName = attribTokens[0];
                attributeName = attribTokens[0];
            }

            Method getterMethod = ClassUtil.getGetterMethodByProperty(propertyName, bean.getClass(), null);

            if(getterMethod == null) {
                throw new TemplateException("<@writeAttribs> directive unable to locate getter method for attribute property '" + propertyName + "' on bean class type '" + bean.getClass().getName() + "'.", environment);
            }

            try {
                Object attribVal = getterMethod.invoke(bean);

                if(attribVal != null) {
                    char[] attribStringVal = attribVal.toString().toCharArray();
                    if(i > 0) {
                        environment.getOut().write(' ');
                    }
                    environment.getOut().write(attributeName);
                    environment.getOut().write("=\"");
                    XmlUtil.encodeAttributeValue(attribStringVal, 0, attribStringVal.length, environment.getOut());
                    environment.getOut().write("\"");
                }
            } catch (IllegalAccessException e) {
                throw new TemplateException("<@writeAttribs> directive getter method '" + getterMethod + "' for attribute property '" + propertyName + "' failed.", e, environment);
            } catch (InvocationTargetException e) {
                throw new TemplateException("<@writeAttribs> directive getter method '" + getterMethod + "' for attribute property '" + propertyName + "' failed.", e, environment);
            }
        }
    }
}