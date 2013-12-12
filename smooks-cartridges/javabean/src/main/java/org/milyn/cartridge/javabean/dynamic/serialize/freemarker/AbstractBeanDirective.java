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
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;

import java.util.Map;

/**
 * Abstract bean directive.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class AbstractBeanDirective implements TemplateDirectiveModel {

    public Object getBeanObject(Environment environment, Map params, String directiveName) throws TemplateException {
        Object beanParam = params.get("bean");

        if(beanParam == null) {
            if(params.containsKey("bean")) {
                throw new TemplateException("Mandatory <@" + directiveName + "> directive parameter 'bean' is defined, but the bean is not visible in the model.  Should be a valid model object reference (no quotes) e.g. <@" + directiveName + " bean=customer.address />.", environment);
            } else {
                throw new TemplateException("Mandatory <@" + directiveName + "> directive parameter 'bean' is not defined.  Should be a valid model object reference (no quotes) e.g. <@" + directiveName + " bean=customer.address />.", environment);
            }
        }

        if(!(beanParam instanceof StringModel)) {
            throw new TemplateException("Mandatory <@" + directiveName + "> directive parameter 'bean' not defined properly.  Should be a valid model object reference (no quotes) e.g. <@" + directiveName + " bean=customer.address />.", environment);
        }

        StringModel beanModel = (StringModel) beanParam;

        return beanModel.getWrappedObject();
    }
}
