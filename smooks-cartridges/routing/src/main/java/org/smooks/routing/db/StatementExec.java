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

import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.util.DollarBraceDecoder;
import org.smooks.util.MVELTemplate;
import org.smooks.xml.XmlUtil;
import org.smooks.javabean.expression.BeanMapExpressionEvaluator;

import java.sql.*;
import java.util.*;

/**
 * SQL Statement Executor.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StatementExec {

    private String statement;
    private StatementType statementType;
    private boolean isJoin;
    private List<BeanMapExpressionEvaluator> statementExpressionEvaluators = new ArrayList<BeanMapExpressionEvaluator>();
    private MVELTemplate updateStatementTemplate;

    public StatementExec(String statementString) throws SmooksConfigurationException {
        AssertArgument.isNotNull(statementString, "statementString");

        statement = XmlUtil.removeEntities(statementString).trim();
        if (statement.toLowerCase().startsWith("select")) {
            statementType = StatementType.QUERY;
        } else {
            statementType = StatementType.UPDATE;
            updateStatementTemplate = new MVELTemplate(statement);
        }

        // The input payload can be a List<Map> (result set)and the statement can look
        // like "select * from ORDER_DETAIL_SOURCE where ORD_ID = ${ORD_ID} and ORD_CD = ${ORD_CD}",
        // where the ${} tokens denote one or more field/column names in the payload List<Map> rows. These
        // tokens will be used to extract values from the input rows (Map) to populate the PreparedStatment.
        List<String> statementExecFields = DollarBraceDecoder.getTokens(statement);
        intitialiseStatementExpressions(statementExecFields);
        statement = DollarBraceDecoder.replaceTokens(statement, "?");
        isJoin = !statementExecFields.isEmpty();
    }

    private void intitialiseStatementExpressions(List<String> statementExecFields) {
        for (String statementExecField : statementExecFields) {
            BeanMapExpressionEvaluator expression = new BeanMapExpressionEvaluator();
            expression.setExpression(statementExecField);
            statementExpressionEvaluators.add(expression);
        }
    }

    public String getStatement() {
        return statement;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public List<Map<String, Object>> executeUnjoinedQuery(Connection dbConnection, Object... params) throws SQLException {
        return executeUnjoinedQuery(dbConnection, Arrays.asList(params));
    }

    public List<Map<String, Object>> executeUnjoinedQuery(Connection dbConnection, List<Object> params) throws SQLException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(statement);

        try {
            ResultSet resultSet;

            // initialise and execute the query...
            for (int i = 0; params != null && i < params.size(); i++) {
                preparedStatement.setObject((i + 1), params.get(i));
            }

            resultSet = preparedStatement.executeQuery();
            try {
                List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();
                mapResultSet(resultSet, resultMap);
                return resultMap;
            } finally {
                resultSet.close();
            }
        } finally {
            preparedStatement.close();
        }
    }

    public int executeUnjoinedUpdate(Connection dbConnection, Object... params) throws SQLException {
        return executeUnjoinedUpdate(dbConnection, Arrays.asList(params));
    }

    public int executeUnjoinedUpdate(Connection dbConnection, List<Object> params) throws SQLException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(statement);

        try {
            // initialise and execute the statement...
            for (int i = 0; params != null && i < params.size(); i++) {
                preparedStatement.setObject((i + 1), params.get(i));
            }
            return preparedStatement.executeUpdate();
        } finally {
            preparedStatement.close();
        }
    }

    public void executeJoinedStatement(Connection dbConnection, List<Map<String, Object>> resultSet) throws SQLException {
        for (Map<String, Object> row : resultSet) {
            executeJoinedStatement(dbConnection, row);
        }
    }

    public void executeJoinedStatement(Connection dbConnection, Map<String, Object> beanMap) throws SQLException {
        if (getStatementType() == StatementType.QUERY) {
            executeJoinedQuery(dbConnection, beanMap);
        } else {
            executeJoinedUpdate(dbConnection, beanMap);
        }
    }

    public void executeJoinedQuery(Connection dbConnection, Map<String, Object> beanMap) throws SQLException {
        executeJoinedQuery(dbConnection, beanMap, null);
    }

    public void executeJoinedQuery(Connection dbConnection, Map<String, Object> beanMap, List<Map<String, Object>> resultMap) throws SQLException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(statement);
        try {
            ResultSet resultSet;
            // initialise and execute the query...
            setStatementParamaters(preparedStatement, beanMap);
            resultSet = preparedStatement.executeQuery();

            try {
                if (resultMap == null) {
                    if (resultSet.next()) {
                        mapResultSetRowToMap(resultSet, beanMap);
                    }
                } else {
                    mapResultSet(resultSet, resultMap);
                }
            } finally {
                resultSet.close();
            }
        } finally {
            preparedStatement.close();
        }
    }

    public int executeJoinedUpdate(Connection dbConnection, Map<String, Object> beanMap) throws SQLException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(statement);
        try {
            // initialise and execute the query...
            setStatementParamaters(preparedStatement, beanMap);
            return preparedStatement.executeUpdate();
        } finally {
            preparedStatement.close();
        }
    }

    private void setStatementParamaters(PreparedStatement preparedStatement, Map<String, Object> beanMap) throws SQLException {
        // The query params are coming from other fields in
        // the row (the "join fields")...
        for (int i = 0; i < statementExpressionEvaluators.size(); i++) {
            Object value;
            try {
                value = statementExpressionEvaluators.get(i).getValue(beanMap);
            } catch(Throwable t) {
                SQLException e =  new SQLException("Error evaluting expression '" + statementExpressionEvaluators.get(i).getExpression() + "' on map " + beanMap);
                e.initCause(t);
                throw e;
            }
            preparedStatement.setObject((i + 1), value);
        }
    }

    public String getUpdateStatement(Map<String, Object> beanMap) {
        if (updateStatementTemplate == null) {
            throw new RuntimeException("Illegal call to getUpdateStatement().  This is not an 'update' statement.");
        }
        return updateStatementTemplate.apply(beanMap);
    }

    private void mapResultSet(ResultSet resultSet, List<Map<String, Object>> resultMap) throws SQLException {
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();

            mapResultSetRowToMap(resultSet, row);
            resultMap.add(row);
        }
    }

    private void mapResultSetRowToMap(ResultSet resultSet, Map<String, Object> beanMap) throws SQLException {
        ResultSetMetaData resultSetMD = resultSet.getMetaData();
        int columnCount = resultSetMD.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            String colName = resultSetMD.getColumnName(i + 1);
            Object rowValue = resultSet.getObject(i + 1);
            beanMap.put(colName, rowValue);
        }
    }

}
