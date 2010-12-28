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
package org.milyn.delivery.nested;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.AbstractParser;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.SmooksContentHandler;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.annotation.Uninitialize;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.DynamicSAXElementVisitorList;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXHandler;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;
import org.milyn.util.CollectionsUtil;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Nested Smooks execution visitor.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NestedExecutionVisitor implements SAXVisitBefore, VisitLifecycleCleanable, Producer {

    @ConfigParam
    private String smooksConfig;

    @ConfigParam
    private String[] mapBeans;
    private List<BeanId> mapBeanIds = new ArrayList<BeanId>();

    @ConfigParam(defaultVal = "true")
    private boolean inheritBeanContext;

    @AppContext
    private ApplicationContext applicationContext;

    private volatile Smooks smooksInstance;

    public void setSmooksConfig(String smooksConfig) {
        this.smooksConfig = smooksConfig;
    }

    public void setSmooksInstance(Smooks smooksInstance) {
        this.smooksInstance = smooksInstance;
    }

    @Initialize
    public void preRegBeanIds() {
        for(String preRegBeanId : mapBeans) {
            mapBeanIds.add(applicationContext.getBeanIdStore().register(preRegBeanId));
        }
    }

    @Uninitialize
    public void closeSmooksInstance() {
        if(smooksInstance != null) {
            smooksInstance.close();
        }
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        Smooks smooks = getSmooksInstance();
        ExecutionContext nestedExecutionContext = smooks.createExecutionContext();

        // In case there's an attached event listener...
        nestedExecutionContext.setEventListener(executionContext.getEventListener());

        SmooksContentHandler parentContentHandler = SmooksContentHandler.getHandler(executionContext);

        if(parentContentHandler.getNestedContentHandler() != null) {
            throw new SmooksException("Illegal use of more than one nested content handler fired on the same element.");
        }

        SmooksContentHandler nestedContentHandler = new SAXHandler(nestedExecutionContext, element.getWriter(this), parentContentHandler);

        DynamicSAXElementVisitorList.propogateDynamicVisitors(executionContext, nestedExecutionContext);

        // Attach the XMLReader instance to the nested ExecutionContext and then swap the content handler on
        // the XMLReader to be the nested handler created here.  All events wll be forwarded to the ..
        XMLReader xmlReader = AbstractParser.getXMLReader(executionContext);
        AbstractParser.attachXMLReader(xmlReader, nestedExecutionContext);
        xmlReader.setContentHandler(nestedContentHandler);

        executionContext.setAttribute(NestedExecutionVisitor.class, nestedExecutionContext);

        // Note we do not execute the Smooks filterSource methods for a nested instance... we just install
        // the content handler and redirect the reader events to it...
    }

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(mapBeans);
    }

    private Smooks getSmooksInstance() {
        // Lazily create the Smooks instance...
        if(smooksInstance == null) {
            synchronized (this) {
                if(smooksInstance == null) {
                    try {
                        smooksInstance = new Smooks(smooksConfig);
                    } catch (Exception e) {
                        throw new SmooksException("Error creating nested Smooks instance for Smooks configuration '" + smooksConfig + "'.", e);
                    }
                }
            }
        }
        return smooksInstance;
    }

    public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        ExecutionContext nestedExecutionContext = (ExecutionContext) executionContext.getAttribute(NestedExecutionVisitor.class);

        try {
            if(nestedExecutionContext != null) {
                BeanContext parentBeanContext = executionContext.getBeanContext();
                BeanContext nestedBeanContext = nestedExecutionContext.getBeanContext();

                for(BeanId beanId : mapBeanIds) {
                    Object bean = nestedBeanContext.getBean(beanId.getName());

                    // Add the bean from the nested context onto the parent context and then remove
                    // it again.  This is enough to fire the wiring and end events...
                    parentBeanContext.notifyObservers(new BeanContextLifecycleEvent(executionContext, null, BeanLifecycle.START_FRAGMENT, beanId, bean));
                    parentBeanContext.addBean(beanId, bean);
                    parentBeanContext.notifyObservers(new BeanContextLifecycleEvent(executionContext, null, BeanLifecycle.END_FRAGMENT, beanId, bean));
                    parentBeanContext.removeBean(beanId, null);
                }
            }
        } finally {
            executionContext.removeAttribute(NestedExecutionVisitor.class);            
        }
    }
}
