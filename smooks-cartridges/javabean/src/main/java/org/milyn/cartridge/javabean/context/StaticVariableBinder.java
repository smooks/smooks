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
package org.milyn.cartridge.javabean.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.milyn.commons.SmooksException;
import org.milyn.cdr.Parameter;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXElementVisitor;
import org.milyn.delivery.sax.SAXText;
import org.milyn.delivery.sax.SAXUtil;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.repository.BeanId;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Static variable binding visitor.
 * <p/>
 * Binds resource paramater variables into the bean context (managed by the
 * {@link org.milyn.javabean.context.BeanContext}).  The paramater values are all bound
 * into a bean accessor Map named "statvar", so variables bound in this way
 * can be referenced in expressions or templates as e.g "<i>${statvar.<b>xxx</b>}</i>"
 * (for static variable "xxx").
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StaticVariableBinder implements SAXElementVisitor, DOMElementVisitor {

    private static final String STATVAR = "statvar";

    private BeanId beanId;

    @Config
    private SmooksResourceConfiguration config;


    @AppContext
    private ApplicationContext appContext;

    @Initialize
    public void initialize() throws SmooksConfigurationException {

        beanId = appContext.getBeanIdStore().getBeanId(STATVAR);

        if(beanId == null) {
        	beanId = appContext.getBeanIdStore().register(STATVAR);
        }


    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        bindParamaters(executionContext, new Fragment(element));
    }

    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        bindParamaters(executionContext, new Fragment(element));
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
    }

    private void bindParamaters(ExecutionContext executionContext, Fragment source) {
        List<?> params = config.getParameterList();

        for (Object parameter : params) {
            // It's either an object, or list of objects...
            if (parameter instanceof List<?>) {
                // Bind the first paramater...
                bindParameter((Parameter) ((List<?>) parameter).get(0), executionContext, source);
            } else if (parameter instanceof Parameter) {
                bindParameter((Parameter) parameter, executionContext, source);
            }
        }
    }


	private void bindParameter(Parameter parameter, ExecutionContext executionContext, Fragment source) {
        Map<String, Object> params = null;

        BeanContext beanContext = executionContext.getBeanContext();

        try {
        	@SuppressWarnings("unchecked")
        	Map<String, Object> castParams = (Map<String, Object>) beanContext.getBean(beanId);

        	params = castParams;
        } catch(ClassCastException e) {
            throw new SmooksException("Illegal use of reserved beanId '" + STATVAR + "'.  Must be a Map.  Is a " + params.getClass().getName(), e);
        }

        if(params == null) {
            params = new HashMap<String, Object>();
            beanContext.addBean(beanId, params, source);
        }

        params.put(parameter.getName(), parameter.getValue(executionContext.getDeliveryConfig()));
    }
}
