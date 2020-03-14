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
package org.smooks.routing.db;

import org.smooks.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.SmooksResourceConfigurationFactory;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Fragment;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.delivery.annotation.VisitAfterIf;
import org.smooks.delivery.annotation.VisitBeforeIf;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.delivery.ordering.Consumer;
import org.smooks.delivery.ordering.Producer;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.expression.ExpressionEvaluator;
import org.smooks.expression.MVELExpressionEvaluator;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.context.BeanIdStore;
import org.smooks.javabean.decoders.MVELExpressionEvaluatorDecoder;
import org.smooks.javabean.repository.BeanId;
import org.smooks.util.CollectionsUtil;
import org.smooks.util.FreeMarkerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
@VisitBeforeIf(	condition = "!parameters.containsKey('executeBefore') || parameters.executeBefore.value == 'true'")
@VisitAfterIf(	condition = "parameters.containsKey('executeBefore') && parameters.executeBefore.value != 'true'")
public class ResultsetRowSelector implements SmooksResourceConfigurationFactory, SAXVisitBefore, SAXVisitAfter, DOMElementVisitor, Producer, Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsetRowSelector.class);

    @ConfigParam
    private String resultSetName;

    @ConfigParam(name = "where", decoder = MVELExpressionEvaluatorDecoder.class)
    private ExpressionEvaluator whereEvaluator;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private FreeMarkerTemplate failedSelectError;

    @ConfigParam(name="beanId")
    private String beanId;

    private boolean executeBefore = true;

    private BeanId resultSetBeanId;

    private BeanId beanIdObj;

    @AppContext
    private ApplicationContext appContext;

    public ResultsetRowSelector setResultSetName(String resultSetName) {
        AssertArgument.isNotNullAndNotEmpty(resultSetName, "resultSetName");
        this.resultSetName = resultSetName;
        return this;
    }

    public ResultsetRowSelector setSelector(SQLExecutor executor) {
        AssertArgument.isNotNull(executor, "executor");
        this.resultSetName = executor.getResultSetName();
        if(this.resultSetName == null) {
            throw new IllegalArgumentException("Invalid 'executor' argument.  Executor must specify a 'resultSetName' in order to be used by a ResultsetRowSelector.");
        }
        return this;
    }

    public ResultsetRowSelector setWhereClause(String whereClause) {
        AssertArgument.isNotNullAndNotEmpty(whereClause, "whereClause");
        this.whereEvaluator = new MVELExpressionEvaluator();
        this.whereEvaluator.setExpression(whereClause);
        return this;
    }

    public ResultsetRowSelector setWhereEvaluator(ExpressionEvaluator whereEvaluator) {
        AssertArgument.isNotNull(whereEvaluator, "whereEvaluator");
        this.whereEvaluator = whereEvaluator;
        return this;
    }

    public ResultsetRowSelector setFailedSelectError(String failedSelectError) {
        AssertArgument.isNotNullAndNotEmpty(failedSelectError, "failedSelectError");
        this.failedSelectError = new FreeMarkerTemplate(failedSelectError);
        return this;
    }

    public ResultsetRowSelector setBeanId(String beanId) {
        AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
        this.beanId = beanId;
        return this;
    }

    public ResultsetRowSelector setExecuteBefore(boolean executeBefore) {
        this.executeBefore = executeBefore;
        return this;
    }

    public SmooksResourceConfiguration createConfiguration() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();
        config.setParameter("executeBefore", Boolean.toString(executeBefore));
        return config;
    }

    @Initialize
    public void intitialize() throws SmooksConfigurationException {
    	BeanIdStore beanIdStore = appContext.getBeanIdStore();

    	beanIdObj = beanIdStore.register(beanId);
    	resultSetBeanId = beanIdStore.register(resultSetName);
    }

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(beanId);
    }

    public boolean consumes(Object object) {
        if(object.equals(resultSetName)) {
            return true;
        } else if(whereEvaluator != null && whereEvaluator.getExpression().indexOf(object.toString()) != -1) {
            return true;
        } else if(failedSelectError != null && failedSelectError.getTemplateText().indexOf(object.toString()) != -1) {
            return true;
        }

        return false;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        selectRow(executionContext, new Fragment(element));
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        selectRow(executionContext, new Fragment(element));
    }

    /* (non-Javadoc)
	 * @see org.smooks.delivery.dom.DOMVisitBefore#visitBefore(org.w3c.dom.Element, org.smooks.container.ExecutionContext)
	 */
	public void visitBefore(Element element, ExecutionContext executionContext)
			throws SmooksException {
		selectRow(executionContext, new Fragment(element));
	}

    /* (non-Javadoc)
	 * @see org.smooks.delivery.dom.DOMVisitAfter#visitAfter(org.w3c.dom.Element, org.smooks.container.ExecutionContext)
	 */
	public void visitAfter(Element element, ExecutionContext executionContext)
			throws SmooksException {
		selectRow(executionContext, new Fragment(element));
	}

    private void selectRow(ExecutionContext executionContext, Fragment source) throws SmooksException {
    	BeanContext beanRepository = executionContext.getBeanContext();

    	Map<String, Object> beanMapClone = new HashMap<String, Object>(beanRepository.getBeanMap());

        // Lookup the new current value for the bean...
        try {
        	@SuppressWarnings("unchecked")
            List<Map<String, Object>> resultSet = (List<Map<String, Object>>) beanRepository.getBean(resultSetBeanId);

            if(resultSet == null) {
                throw new SmooksException("Resultset '" + resultSetName + "' not found in bean context.  Make sure an appropriate SQLExecutor resource config wraps this selector config.");
            }

            try {
            	Object selectedRow = null;

            	Iterator<Map<String, Object>> resultIter = resultSet.iterator();
                while (selectedRow == null && resultIter.hasNext()) {
                	Map<String, Object> row = resultIter.next();

                	beanMapClone.put("row", row);

                    if(whereEvaluator.eval(beanMapClone)) {
                    	selectedRow = row;
                    	beanRepository.addBean(beanIdObj, selectedRow, source);
                    }
                }

                if(selectedRow == null && failedSelectError != null) {
                    throw new DataSelectionException(failedSelectError.apply(beanRepository.getBeanMap()));
                }

                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Selected resultset where '" + whereEvaluator.getExpression() + "': [" + selectedRow + "].");
                }
            } catch(ClassCastException e) {
                throw new SmooksException("Bean '" + resultSetName + "' cannot be used as a Reference Data resultset.  The resultset List must contain entries of type Map<String, Object>.");
            }
        } catch(ClassCastException e) {
            throw new SmooksException("Bean '" + resultSetName + "' cannot be used as a Reference Data resultset.  A resultset must be of type List<Map<String, Object>>. '" + resultSetName + "' is of type '" + beanRepository.getBean(resultSetBeanId).getClass().getName() + "'.");
        }
    }
}
