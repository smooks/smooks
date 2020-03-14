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
import org.smooks.db.AbstractDataSource;
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
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.DataDecoder;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.repository.BeanId;
import org.smooks.util.CollectionsUtil;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * SQLExecutor Visitor.
 * <p/>
 * Supports extraction and persistence to a Database.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
@VisitBeforeIf(	condition = "parameters.containsKey('executeBefore') && parameters.executeBefore.value == 'true'")
@VisitAfterIf(	condition = "!parameters.containsKey('executeBefore') || parameters.executeBefore.value != 'true'")
@VisitBeforeReport(summary = "Execute statement '${resource.parameters.statement}' on Datasource '${resource.parameters.datasource}'.", detailTemplate = "reporting/SQLExecutor.html")
@VisitAfterReport(summary = "Execute statement '${resource.parameters.statement}' on Datasource '${resource.parameters.datasource}'.", detailTemplate = "reporting/SQLExecutor.html")
public class SQLExecutor implements SmooksResourceConfigurationFactory, SAXVisitBefore, SAXVisitAfter, DOMElementVisitor, Producer, Consumer {

    @ConfigParam
    private String datasource;

    @ConfigParam
    private String statement;
    private StatementExec statementExec;
    private String rsAppContextKey;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String resultSetName;

    @ConfigParam(defaultVal = "EXECUTION", choice = {"EXECUTION", "APPLICATION"}, decoder = ResultSetScopeDecoder.class)
    private ResultSetScope resultSetScope = ResultSetScope.EXECUTION;

    @ConfigParam(defaultVal = "900000")
    private long resultSetTTL = 900000L;

    private boolean executeBefore = false;

    @AppContext
    private ApplicationContext appContext;

    private BeanId resultSetBeanId;

    public SQLExecutor setDatasource(AbstractDataSource datasource) {
        AssertArgument.isNotNull(datasource, "datasource");
        this.datasource = datasource.getName();
        return this;
    }

    public SQLExecutor setStatement(String statement) {
        AssertArgument.isNotNullAndNotEmpty(statement, "statement");
        this.statement = statement;
        return this;
    }

    public SQLExecutor setResultSetName(String resultSetName) {
        AssertArgument.isNotNullAndNotEmpty(resultSetName, "resultSetName");
        this.resultSetName = resultSetName;
        return this;
    }

    public String getResultSetName() {
        return resultSetName;
    }

    public SQLExecutor setResultSetScope(ResultSetScope resultSetScope) {
        AssertArgument.isNotNull(resultSetScope, "resultSetScope");
        this.resultSetScope = resultSetScope;
        return this;
    }

    public SQLExecutor setResultSetTTL(long resultSetTTL) {
        this.resultSetTTL = resultSetTTL;
        return this;
    }

    public SQLExecutor setExecuteBefore(boolean executeBefore) {
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
        statementExec = new StatementExec(statement);
        if(statementExec.getStatementType() == StatementType.QUERY && resultSetName == null) {
            throw new SmooksConfigurationException("Sorry, query statements must be accompanied by a 'resultSetName' property, under whose value the query results are bound.");
        }

        if(resultSetName != null) {
	        resultSetBeanId = appContext.getBeanIdStore().register(resultSetName);
        }
        rsAppContextKey = datasource + ":" + statement;
    }

    public Set<?> getProducts() {
        if(statementExec.getStatementType() == StatementType.QUERY) {
            return CollectionsUtil.toSet(resultSetName);
        }

        return CollectionsUtil.toSet();
    }

    public boolean consumes(Object object) {
        return statement.contains(object.toString());
    }

    public void visitBefore(SAXElement saxElement, ExecutionContext executionContext) throws SmooksException
    {
            executeSQL(executionContext, new Fragment(saxElement));
        }

    public void visitAfter(SAXElement saxElement, ExecutionContext executionContext) throws SmooksException
    {
            executeSQL(executionContext, new Fragment(saxElement));
        }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
            executeSQL(executionContext, new Fragment(element));
        }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
            executeSQL(executionContext, new Fragment(element));
        }



	private void executeSQL(ExecutionContext executionContext, Fragment source) throws SmooksException {
        Connection connection = AbstractDataSource.getConnection(datasource, executionContext);
        BeanContext beanContext = executionContext.getBeanContext();

        Map<String, Object> beanMap = beanContext.getBeanMap();

        try {
            if(!statementExec.isJoin()) {
                if(statementExec.getStatementType() == StatementType.QUERY) {
                    if(resultSetScope == ResultSetScope.EXECUTION) {
                        beanContext.addBean(resultSetBeanId, statementExec.executeUnjoinedQuery(connection), source);
                    } else {
                        List<Map<String, Object>> resultMap;
                        // Cached in the application context...
                        ApplicationContext appContext = executionContext.getContext();
                        ResultSetContextObject rsContextObj = ResultSetContextObject.getInstance(rsAppContextKey, appContext);

                        if(rsContextObj.hasExpired()) {
                            synchronized (rsContextObj) {
                                if(rsContextObj.hasExpired()) {
                                    rsContextObj.resultSet = statementExec.executeUnjoinedQuery(connection);
                                    rsContextObj.expiresAt = System.currentTimeMillis() + resultSetTTL;
                                }
                            }
                        }
                        resultMap = rsContextObj.resultSet;
                        beanContext.addBean(resultSetBeanId, resultMap, source);
                    }
                } else {
                    statementExec.executeUnjoinedUpdate(connection);
                }
            } else {
                if(statementExec.getStatementType() == StatementType.QUERY) {
                    List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();
                    statementExec.executeJoinedQuery(connection, beanMap, resultMap);
                    beanContext.addBean(resultSetBeanId, resultMap, source);
                } else {
                    if(resultSetBeanId == null) {
                        statementExec.executeJoinedUpdate(connection, beanMap);
                    } else {
                        Object resultSetObj = beanContext.getBean(resultSetBeanId);
                        if(resultSetObj != null) {
                            try {
                            	@SuppressWarnings("unchecked")
                                List<Map<String, Object>> resultSet = (List<Map<String, Object>>) resultSetObj;
                                statementExec.executeJoinedStatement(connection, resultSet);
                            } catch(ClassCastException e) {
                                throw new SmooksException("Cannot execute joined statement '" + statementExec.getStatement() + "' on ResultSet '" + resultSetName + "'.  Must be of type 'List<Map<String, Object>>'.  Is of type '" + resultSetObj.getClass().getName() + "'.");
                            }
                        } else {
                            throw new SmooksException("Cannot execute joined statement '" + statementExec.getStatement() + "' on ResultSet '" + resultSetName + "'.  ResultSet not found in ExecutionContext.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new SmooksException("Error executing SQL Statement '" + statement + "'.", e);
        }
    }


    public static class ResultSetScopeDecoder implements DataDecoder {

        public Object decode(String data) throws DataDecodeException {
            ResultSetScope scope;

            data = data.trim();
            try {
                scope = ResultSetScope.valueOf(data);
            } catch (IllegalArgumentException e) {
                throw new DataDecodeException("Failed to decode ResultSetScope value '" + data + "'.  Allowed values are " + Arrays.asList(ResultSetScope.values()) + ".");
            }

            return scope;
        }
    }

    private static class ResultSetContextObject {
        private List<Map<String, Object>> resultSet;
        private long expiresAt = 0L;

        private boolean hasExpired() {
            return expiresAt <= System.currentTimeMillis();
        }

        private static ResultSetContextObject getInstance(String rsAppContextKey, ApplicationContext appContext) {
            ResultSetContextObject rsContextObj = (ResultSetContextObject) appContext.getAttribute(rsAppContextKey);

            if(rsContextObj == null) {
                synchronized (appContext) {
                    rsContextObj = (ResultSetContextObject) appContext.getAttribute(rsAppContextKey);
                    if(rsContextObj == null) {
                        rsContextObj = new ResultSetContextObject();
                        appContext.setAttribute(rsAppContextKey, rsContextObj);
                    }
                }
            }

            return rsContextObj;
        }
    }
}
