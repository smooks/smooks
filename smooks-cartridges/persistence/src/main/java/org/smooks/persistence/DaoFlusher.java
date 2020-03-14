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
package org.smooks.persistence;

import org.smooks.SmooksException;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.cdr.annotation.ConfigParam.Use;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.delivery.annotation.VisitAfterIf;
import org.smooks.delivery.annotation.VisitBeforeIf;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.persistence.util.PersistenceUtil;
import org.smooks.scribe.invoker.DaoInvoker;
import org.smooks.scribe.invoker.DaoInvokerFactory;
import org.smooks.scribe.register.DaoRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * DAO Flusher
 * <p />
 * This DAO flusher calls the flush method of a DAO.
 *
 * <h3>Configuration</h3>
 * <b>Namespace:</b> https://www.smooks.org/xsd/smooks/persistence-1.5.xsd<br>
 * <b>Element:</b> flusher<br>
 * <b>Attributes:</b>
 * <ul>
 *  <li><b>flushOnElement</b> : The element selector to select the element when the flusher should execute. (<i>required</i>)
 * 	<li><b>dao</b> : The name of the DAO that needs to get flushed. If it is not set then the default DAO will be flushed. (<i>optional</i>)
 *  <li><b>flushBefore</b> : If the flusher should exeute on the 'before' event. (<i>default: false</i>)
 * </ul>
 * <h3>Configuration Example</h3>
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;smooks-resource-list xmlns=&quot;https://www.smooks.org/xsd/smooks-1.2.xsd&quot;
 *   xmlns:dao=&quot;https://www.smooks.org/xsd/smooks/persistence-1.5.xsd&quot;&gt;
 *
 *      &lt;dao:flusher dao=&quot;dao&quot; flushOnElement=&quot;root&quot; flushBefore=&quot;false&quot; /&gt;
 * &lt;/smooks-resource-list&gt;
 * </pre>
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@VisitBeforeIf(	condition = "parameters.containsKey('flushBefore') && parameters.flushBefore.value == 'true'")
@VisitAfterIf( condition = "!parameters.containsKey('flushBefore') || parameters.flushBefore.value != 'true'")
@VisitBeforeReport(summary = "Flushing <#if !resource.parameters.dao??>default </#if>DAO<#if resource.parameters.dao??> '${resource.parameters.dao}'</#if>.", detailTemplate="reporting/DaoFlusher.html")
@VisitAfterReport(summary = "Flushing <#if !resource.parameters.dao??>default </#if>DAO<#if resource.parameters.dao??> '${resource.parameters.dao}'</#if>.", detailTemplate="reporting/DaoFlusher.html")
public class DaoFlusher implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaoFlusher.class);

    @ConfigParam(name = "dao", use = Use.OPTIONAL)
    private String daoName;

    @AppContext
    private ApplicationContext appContext;

    private ApplicationContextObjectStore objectStore;

    @Initialize
    public void initialize() {
    	objectStore = new ApplicationContextObjectStore(appContext);
    }

    public void visitBefore(final Element element, final ExecutionContext executionContext) throws SmooksException {
    	flush(executionContext);
    }

    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
    	flush(executionContext);
    }

    public void visitBefore(final SAXElement element, final ExecutionContext executionContext) throws SmooksException, IOException {
    	flush(executionContext);
    }

    public void visitAfter(final SAXElement element, final ExecutionContext executionContext) throws SmooksException, IOException {
    	flush(executionContext);
    }

	/**
	 * @param executionContext
	 * @param bean
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void flush(final ExecutionContext executionContext) {

		if(LOGGER.isDebugEnabled()) {
			String msg = "Flushing org.smooks.persistence.test.dao";
			if(daoName != null) {
				msg += " with name '" + daoName + "'";
			}
			msg += ".";
			LOGGER.debug(msg);
		}

		final DaoRegister emr = PersistenceUtil.getDAORegister(executionContext);

		Object dao = null;
		try {
			if(daoName == null) {
				dao = emr.getDefaultDao();
			} else {
				dao = emr.getDao(daoName);
			}

			if(dao == null) {
				throw new IllegalStateException("The DAO register returned null while getting the DAO [" + daoName + "]");
			}

			flush(dao);

		} finally {
			if(dao != null) {
				emr.returnDao(dao);
			}
		}
	}

	/**
	 * @param org.smooks.persistence.test.dao
	 */
	private void flush(Object dao) {
		final DaoInvoker daoInvoker = DaoInvokerFactory.getInstance().create(dao, objectStore);

		daoInvoker.flush();
	}

}
