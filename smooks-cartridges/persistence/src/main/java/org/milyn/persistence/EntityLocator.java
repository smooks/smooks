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
package org.milyn.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.persistence.NonUniqueResultException;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXUtil;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.repository.BeanId;
import org.milyn.persistence.parameter.NamedParameterContainer;
import org.milyn.persistence.parameter.ParameterContainer;
import org.milyn.persistence.parameter.ParameterIndex;
import org.milyn.persistence.parameter.ParameterManager;
import org.milyn.persistence.parameter.PositionalParameterContainer;
import org.milyn.persistence.util.PersistenceUtil;
import org.milyn.scribe.invoker.DaoInvoker;
import org.milyn.scribe.invoker.DaoInvokerFactory;
import org.milyn.scribe.register.DaoRegister;
import org.milyn.util.CollectionsUtil;
import org.w3c.dom.Element;

/**
 * DAO Locator
 * <p />
 * This DAO locator uses lookup methods or methods that accept a query to
 * lookup entities from a data source. In case of a query it depends on the DAO
 * or the Scribe adapter what the query language is.
 *
 * <h3>Configuration</h3>
 * <b>Namespace:</b> http://www.milyn.org/xsd/smooks/persistence-1.2.xsd<br>
 * <b>Element:</b> locator<br>
 * <b>Attributes:</b>
 *
 * Take a look at the schema for all the information on the configurations parameters.
 *
 * <h3>Configuration Example</h3>
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;smooks-resource-list xmlns=&quot;http://www.milyn.org/xsd/smooks-1.1.xsd&quot;
 *    xmlns:dao=&quot;http://www.milyn.org/xsd/smooks/persistence-1.2.xsd&quot;&gt;
 *      &lt;dao:locator beanId=&quot;entity&quot; lookup=&quot;something&quot; lookupOnElement=&quot;b&quot;&gt;
 *      &lt;dao:params&gt;
 *         &lt;dao:value name=&quot;arg1&quot; decoder=&quot;Integer&quot; data=&quot;c&quot; /&gt;
 *         &lt;dao:expression name=&quot;arg2&quot;&gt;dAnde.d + dAnde.e&lt;/dao:expression&gt;
 *         &lt;dao:wiring name=&quot;arg3&quot; beanIdRef=&quot;dAnde&quot; wireOnElement=&quot;e&quot; /&gt;
 *         &lt;dao:value name=&quot;arg4&quot; data=&quot;f/@name&quot; /&gt;
 *         &lt;dao:value name=&quot;arg5&quot; decoder=&quot;Date&quot; data=&quot;g&quot; &gt;
 *            &lt;dao:decodeParam name=&quot;format&quot;&gt;yyyy-MM-dd HH:mm:ss&lt;/dao:decodeParam&gt;
 *         &lt;/dao:value&gt;
 *      &lt;/dao:params&gt;
 *  &lt;/dao:locator&gt;
 *
 *  &lt;dao:locator beanId=&quot;customer&quot; lookupOnElement=&quot;b&quot;&gt;
 *     &lt;dao:query&gt;from Customer c where c.id = :arg1&lt;/dao:query&gt;
 *     &lt;dao:params&gt;
 *        &lt;dao:value name=&quot;arg1&quot; decoder=&quot;Integer&quot; data=&quot;c&quot; /&gt;
 *     &lt;/dao:params&gt;
 *  &lt;/dao:locator&gt;
 * &lt;/smooks-resource-list&gt;
 * </pre>
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@VisitBeforeReport(summary = "Initializing parameter container to hold the parameters needed for the lookup.", detailTemplate="reporting/EntityLocator_before.html")
@VisitAfterReport(summary = "Looking up entity to put under beanId '${resource.parameters.beanId}'.", detailTemplate="reporting/EntityLocator_after.html")
public class EntityLocator implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, Producer, Consumer {

	@ConfigParam()
	private int id;

	@ConfigParam(name="beanId")
    private String beanIdName;

    @ConfigParam(name = "dao", use = Use.OPTIONAL)
    private String daoName;

    @ConfigParam(name="lookup", use = Use.OPTIONAL)
    private String lookupName;

    @ConfigParam(use = Use.OPTIONAL)
    private String query;

    @ConfigParam(defaultVal = OnNoResult.NULLIFY_STR, decoder = OnNoResult.DataDecoder.class)
    private OnNoResult onNoResult;

    @ConfigParam(defaultVal = "false")
    private boolean uniqueResult;

    @ConfigParam(defaultVal = ParameterListType.NAMED_STR, decoder = ParameterListType.DataDecoder.class)
    private ParameterListType parameterListType;

    @AppContext
    private ApplicationContext appContext;

    private ApplicationContextObjectStore objectStore;

    private ParameterIndex<?, ?> parameterIndex;

    private BeanId beanId;
    @Initialize
    public void initialize() throws SmooksConfigurationException {

    	if(StringUtils.isEmpty(lookupName) && StringUtils.isEmpty(query)) {
    		throw new SmooksConfigurationException("A lookup name or  a query  needs to be set to be able to lookup anything");
    	}

    	if(StringUtils.isNotEmpty(lookupName) && StringUtils.isNotEmpty(query)) {
    		throw new SmooksConfigurationException("Both the lookup name and the query can't be set at the same time");
    	}

    	beanId = appContext.getBeanIdStore().register(beanIdName);

    	parameterIndex = ParameterManager.initializeParameterIndex(id, parameterListType, appContext);

    	objectStore = new ApplicationContextObjectStore(appContext);
    }

    /* (non-Javadoc)
	 * @see org.milyn.delivery.ordering.Producer#getProducts()
	 */
	public Set<? extends Object> getProducts() {
		return CollectionsUtil.toSet(beanIdName);
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.ordering.Consumer#consumes(java.lang.String)
	 */
	public boolean consumes(Object object) {
		return parameterIndex.containsParameter(object);
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.dom.DOMVisitBefore#visitBefore(org.w3c.dom.Element, org.milyn.container.ExecutionContext)
	 */
	public void visitBefore(Element element, ExecutionContext executionContext)	throws SmooksException {

		initParameterContainer(executionContext);
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.sax.SAXVisitBefore#visitBefore(org.milyn.delivery.sax.SAXElement, org.milyn.container.ExecutionContext)
	 */
	public void visitBefore(SAXElement element,	ExecutionContext executionContext) throws SmooksException, IOException {

		initParameterContainer(executionContext);
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.dom.DOMVisitAfter#visitAfter(org.w3c.dom.Element, org.milyn.container.ExecutionContext)
	 */
	public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
		lookup(executionContext, new Fragment(element));
	}

	/* (non-Javadoc)
	 * @see org.milyn.delivery.sax.SAXVisitAfter#visitAfter(org.milyn.delivery.sax.SAXElement, org.milyn.container.ExecutionContext)
	 */
	public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		lookup(executionContext, new Fragment(element));
	}

	public void initParameterContainer(ExecutionContext executionContext) {
		ParameterManager.initializeParameterContainer(id, parameterListType, executionContext);
	}

	@SuppressWarnings("unchecked")
	public void lookup(ExecutionContext executionContext, Fragment source) {
		final DaoRegister emr = PersistenceUtil.getDAORegister(executionContext);

		Object dao = null;
		try {
			if(daoName == null) {
				dao = emr.getDefaultDao();
			} else {
				dao = emr.getDao(daoName);
			}

			if(dao == null) {
				throw new IllegalStateException("The DAO register returned null while getting the DAO '" + daoName + "'");
			}

			Object result = lookup(dao, executionContext);

			if(result != null && uniqueResult == true) {
				if(result instanceof Collection){
					Collection<Object> resultCollection = (Collection<Object>) result;

					if(resultCollection.size() == 0) {
						result = null;
					} else if(resultCollection.size() == 1) {
						for(Object value : resultCollection) {
							result = value;
						}
					} else {
						String exception;
						if(daoName == null) {
							exception = "The " + getDaoNameFromAdapter(dao) + " DAO";
						} else {
							exception = "The DAO '" + daoName + "'";
						}
						exception += " returned multiple results for the ";
						if(lookupName != null) {
							exception += "lookup '" + lookupName + "'";
						} else {
							exception += "query '" + query + "'";
						}
						throw new NonUniqueResultException(exception);
					}

				} else {
					throw new SmooksConfigurationException("The returned result doesn't implement the '" + Collection.class.getName() + "' interface " +
							"and there for the unique result check can't be done.");
				}
			}

			if(result == null && onNoResult == OnNoResult.EXCEPTION) {
				String exception;
				if(daoName == null) {
					exception = "The " + getDaoNameFromAdapter(dao) + " DAO";
				} else {
					exception = "The DAO '" + daoName + "'";
				}
				exception += " returned no results for lookup ";
				if(lookupName != null) {
					exception += "lookup '" + query + "'";
				} else {
					exception += "query '" + query + "'";
				}
				throw new NoLookupResultException(exception);
			}

			BeanContext beanContext = executionContext.getBeanContext();

			if(result == null) {
				beanContext.removeBean(beanId, source);
			} else {
				beanContext.addBean(beanId, result, source);
			}
		} finally {
			if(dao != null) {
				emr.returnDao(dao);
			}
		}
	}

	public Object lookup(Object dao, ExecutionContext executionContext) {
		ParameterContainer<?> container = ParameterManager.getParameterContainer(id, executionContext);
		DaoInvoker daoInvoker = DaoInvokerFactory.getInstance().create(dao, objectStore);

		if(query == null) {
			if(parameterListType == ParameterListType.NAMED) {
				return daoInvoker.lookup(lookupName, ((NamedParameterContainer) container).getParameterMap());
			} else {
				return daoInvoker.lookup(lookupName, ((PositionalParameterContainer) container).getValues());
			}
		} else {
			if(parameterListType == ParameterListType.NAMED) {
				return daoInvoker.lookupByQuery(query, ((NamedParameterContainer) container).getParameterMap());
			} else {
				return daoInvoker.lookupByQuery(query, ((PositionalParameterContainer) container).getValues());
			}
		}
	}

	private String getDaoNameFromAdapter(Object dao) {
		String className = dao.getClass().getSimpleName();

		className = className.replace("Dao", "");
		return className.replace("Adapter", "");
	}

}
