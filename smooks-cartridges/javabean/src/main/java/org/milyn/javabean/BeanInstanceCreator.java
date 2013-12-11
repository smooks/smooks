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
package org.milyn.javabean;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.commons.SmooksException;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.cdr.Parameter;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentDeliveryConfigBuilderLifecycleEvent;
import org.milyn.delivery.ContentDeliveryConfigBuilderLifecycleListener;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.javabean.BeanRuntimeInfo.Classification;
import org.milyn.javabean.binding.model.ModelSet;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.ext.BeanConfigUtil;
import org.milyn.javabean.factory.Factory;
import org.milyn.javabean.factory.FactoryDefinitionParser.FactoryDefinitionParserFactory;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;
import org.milyn.commons.util.CollectionsUtil;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

/**
 * Bean instance creator visitor class.
 * <p/>
 * Targeted via {@link org.milyn.javabean.BeanPopulator} expansion configuration.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@VisitBeforeReport(summary = "Created <b>${resource.parameters.beanId!'undefined'}</b> bean instance.  Associated lifecycle if wired to another bean.",
        detailTemplate = "reporting/BeanInstanceCreatorReport_Before.html")
@VisitAfterReport(condition = "parameters.containsKey('setOn') || parameters.beanClass.value.endsWith('[]')",
        summary = "Ended bean lifecycle. Set bean on any targets.",
        detailTemplate = "reporting/BeanInstanceCreatorReport_After.html")
public class BeanInstanceCreator implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, ContentDeliveryConfigBuilderLifecycleListener, Producer, VisitLifecycleCleanable {

    private static Log logger = LogFactory.getLog(BeanInstanceCreator.class);

    public static final String INIT_VAL_EXPRESSION = "initValExpression";

    private String id;

    @ConfigParam(name="beanId")
    private String beanIdName;

    @ConfigParam(name= BeanConfigUtil.BEAN_CLASS_CONFIG, use=Use.OPTIONAL)
    private String beanClassName;

    @ConfigParam(name="beanFactory", use=Use.OPTIONAL)
    private String beanFactoryDefinition;

    @ConfigParam(defaultVal = "true")
    private boolean retain = true;

    @Config
    private SmooksResourceConfiguration config;

    @AppContext
    private ApplicationContext appContext;

    private BeanRuntimeInfo beanRuntimeInfo;

    private BeanId beanId;

    private MVELExpressionEvaluator initValsExpression;

    private Factory<?> factory;

    /**
     * Public default constructor.
     */
    public BeanInstanceCreator() {
    }

    /**
     * Public default constructor.
     * @param beanId The beanId under which the bean instance is registered in the bean context.
     * @param beanClass The bean runtime class.
     */
    public BeanInstanceCreator(String beanId, Class<?> beanClass) {
        this(beanId, beanClass, null);
    }

    /**
     * Public default constructor.
     * @param beanId The beanId under which the bean instance is registered in the bean context.
     * @param beanClass The bean runtime class.
     */
    public <T> BeanInstanceCreator(String beanId, Class<T> beanClass, Factory<? extends T> factory) {
        AssertArgument.isNotNull(beanId, "beanId");
        AssertArgument.isNotNull(beanClass, "beanClass");

        this.beanIdName = beanId;
        this.beanClassName = toClassName(beanClass);
        this.factory = factory;
    }

    /**
     * Get the beanId of this Bean configuration.
     *
     * @return The beanId of this Bean configuration.
     */
    public String getBeanId() {
        return beanIdName;
    }

    public SmooksResourceConfiguration getConfig() {
        return config;
    }

    /**
     * Set the resource configuration on the bean populator.
     * @throws SmooksConfigurationException Incorrectly configured resource.
     */
    @Initialize
    public void initialize() throws SmooksConfigurationException {
    	buildId();

        beanId = appContext.getBeanIdStore().register(beanIdName);
        beanId.setCreateResourceConfiguration(config);

        if(StringUtils.isNotBlank(beanFactoryDefinition)) {
            String alias = null;
            String definition = beanFactoryDefinition;

            if (definition.indexOf("#") == -1) {
                try {
                    URI definitionURI = new URI(definition);
                    if (definitionURI.getScheme() == null) {
                        // Default it to MVEL...
                        definition = "mvel:" + definition;
                    }
                } catch (URISyntaxException e) {
                    // Let it run...
                }
            }

            int aliasSplitterIndex = definition.indexOf(':');
            if(aliasSplitterIndex > 0) {
                alias = definition.substring(0, aliasSplitterIndex);
                definition = definition.substring(aliasSplitterIndex+1);
            }

    		factory = FactoryDefinitionParserFactory.getInstance(alias, appContext).parse(definition);
    	}

    	beanRuntimeInfo = BeanRuntimeInfo.getBeanRuntimeInfo(beanIdName, beanClassName, appContext);

    	if(factory == null) {
    		checkForDefaultConstructor();
    	} else if (beanRuntimeInfo.getClassification() == Classification.ARRAY_COLLECTION) {
    		throw new SmooksConfigurationException("Using a factory with an array is not supported");
    	}

        if(logger.isDebugEnabled()) {
        	logger.debug("BeanInstanceCreator created for [" + beanIdName + "]. BeanRuntimeInfo: " + beanRuntimeInfo);
        }

        List<Parameter> initValExpressions = config.getParameters(INIT_VAL_EXPRESSION);
        if(initValExpressions != null && !initValExpressions.isEmpty()) {
        	StringBuilder initValsExpressionString = new StringBuilder();

        	for(Parameter initValExpression : initValExpressions) {
        		initValsExpressionString.append(initValExpression.getValue());
        		initValsExpressionString.append("\n");
        	}

        	initValsExpression = new MVELExpressionEvaluator();
        	initValsExpression.setExpression(initValsExpressionString.toString());
        }
    }

    public void handle(ContentDeliveryConfigBuilderLifecycleEvent event) throws SmooksConfigurationException {
        if(event == ContentDeliveryConfigBuilderLifecycleEvent.CONFIG_BUILDER_CREATED) {
            ModelSet.build(appContext);
        }
    }

    /**
     * Get the bean runtime information.
     * @return The bean runtime information.
     */
    public BeanRuntimeInfo getBeanRuntimeInfo() {
        return beanRuntimeInfo;
    }

    private void buildId() {
    	StringBuilder idBuilder = new StringBuilder();
    	idBuilder.append(BeanInstanceCreator.class.getName());
    	idBuilder.append("#");
    	idBuilder.append(beanIdName);

    	id = idBuilder.toString();
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        createAndSetBean(executionContext, new Fragment(element));
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        createAndSetBean(executionContext, new Fragment(element));
    }


	/* (non-Javadoc)
	 * @see org.milyn.delivery.dom.DOMVisitAfter#visitAfter(org.w3c.dom.Element, org.milyn.container.ExecutionContext)
	 */
	public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
		visitAfter(executionContext, new Fragment(element));
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.sax.SAXVisitAfter#visitAfter(org.milyn.delivery.sax.SAXElement, org.milyn.container.ExecutionContext)
	 */
	public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		visitAfter(executionContext, new Fragment(element));
	}

	public void visitAfter(ExecutionContext executionContext, Fragment source) {
        Classification thisBeanType = beanRuntimeInfo.getClassification();
        boolean isBeanTypeArray = (thisBeanType == Classification.ARRAY_COLLECTION);

        BeanContext beanContext = executionContext.getBeanContext();
        beanContext.setBeanInContext(beanId, false);

        if(isBeanTypeArray) {
            Object bean  = beanContext.getBean(beanId);

            if(logger.isDebugEnabled()) {
                logger.debug("Converting bean [" + beanIdName + "] to an array and rebinding to context.");
            }
            bean = convert(executionContext, bean, source);
        }
	}


    private Object convert(ExecutionContext executionContext, Object bean, Fragment source) {

        bean = BeanUtils.convertListToArray((List<?>)bean, beanRuntimeInfo.getArrayType());

        executionContext.getBeanContext().changeBean(beanId, bean, source);

    	return bean;
    }

	private void createAndSetBean(ExecutionContext executionContext, Fragment source) {
        Object bean;
        BeanContext beanContext = executionContext.getBeanContext();

        bean = createBeanInstance(executionContext);

        executionContext.getBeanContext().notifyObservers(new BeanContextLifecycleEvent(executionContext,
                source, BeanLifecycle.START_FRAGMENT, beanId, bean));

        if(initValsExpression != null) {
        	initValsExpression.exec(bean);
        }

        beanContext.setBeanInContext(beanId, false);
        beanContext.addBean(beanId, bean, source);
        beanContext.setBeanInContext(beanId, true);

        if (logger.isDebugEnabled()) {
            logger.debug("Bean [" + beanIdName + "] instance created.");
        }
    }

    /**
     * Create a new bean instance, generating relevant configuration exceptions.
     *
     * @return A new bean instance.
     */
    private Object createBeanInstance(ExecutionContext executionContext) {
        Object bean;

        if(factory == null) {
	        try {
	            bean = beanRuntimeInfo.getPopulateType().newInstance();
	        } catch (InstantiationException e) {
	            throw new SmooksConfigurationException("Unable to create bean instance [" + beanIdName + ":" + beanRuntimeInfo.getPopulateType().getName() + "].", e);
	        } catch (IllegalAccessException e) {
	            throw new SmooksConfigurationException("Unable to create bean instance [" + beanIdName + ":" + beanRuntimeInfo.getPopulateType().getName() + "].", e);
	        }
        } else {
        	try {
				bean = factory.create(executionContext);
			} catch (RuntimeException e) {
				throw new SmooksConfigurationException("The factory was unable to create the bean instance [" + beanIdName + "] using the factory '" + factory + "'.", e);
			}
        }

        return bean;
    }

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(beanIdName);
    }

    private String getId() {
		return id;
	}

    @Override
    public String toString() {
    	return getId();
    }

    private static String toClassName(Class<?> beanClass) {
    	if(!beanClass.isArray()){
            return beanClass.getName();
        } else {
        	return beanClass.getComponentType().getName() + "[]";
        }
    }

	/**
	 *  Checks if the class has a default constructor
	 */
	private void checkForDefaultConstructor() {
		try {
			beanRuntimeInfo.getPopulateType().getConstructor();
		} catch (NoSuchMethodException e) {
		    throw new SmooksConfigurationException("Invalid Smooks bean configuration.  Bean class " + beanRuntimeInfo.getPopulateType().getName() + " doesn't have a public default constructor.");
		}
	}

	public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        BeanContext beanContext = executionContext.getBeanContext();
        Object bean = beanContext.getBean(beanId);

        beanContext.notifyObservers(new BeanContextLifecycleEvent(executionContext,
                fragment, BeanLifecycle.END_FRAGMENT, beanId, bean));

        if(!retain) {
            beanContext.removeBean(beanId, null);
        }
    }
}
