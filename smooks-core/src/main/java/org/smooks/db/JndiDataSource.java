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
package org.smooks.db;

import org.smooks.SmooksException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.cdr.annotation.ConfigParam.Use;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.assertion.AssertArgument;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Jndi based DataSource.
 * <p />
 * Configure the JNDI datasource, transaction manager, transaction jndi (optional), etc.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@VisitBeforeReport(summary = "Bind JndiDataSource <b>${resource.parameters.datasource}</b> to ExecutionContext.", detailTemplate = "reporting/JndiDataSource_before.html")
@VisitAfterReport(summary = "Cleaning up JndiDataSource <b>${resource.parameters.datasource}</b>. Includes performing commit/rollback etc.", detailTemplate = "reporting/JndiDataSource_after.html")
public class JndiDataSource extends AbstractDataSource {

    @ConfigParam(name = "datasource")
    private String name;

    @ConfigParam(use=Use.OPTIONAL)
    private String datasourceJndi;

    @ConfigParam(use=Use.OPTIONAL)
    private String transactionJndi;

    @ConfigParam(defaultVal="false")
    private boolean autoCommit;

    @ConfigParam(defaultVal = "true")
    private boolean setAutoCommitAllowed;

    @ConfigParam(name = "transactionManager", defaultVal = TransactionManagerType.JDBC_STRING, decoder = TransactionManagerType.DataDecoder.class)
    private TransactionManagerType transactionManagerType;

    private DataSource datasource;

    public JndiDataSource(){
    }

    public JndiDataSource(String name, boolean autoCommit) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        this.name = name;
        this.autoCommit = autoCommit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Initialize
    public void intitialize() {
        if(datasourceJndi == null) {
        	datasourceJndi = name;
        }

        datasource = (DataSource) lookup(datasourceJndi);

        if(transactionManagerType == TransactionManagerType.JTA) {
        	if(transactionJndi == null || transactionJndi.length() == 0) {
        		throw new SmooksConfigurationException("The transactionJndi attribute must be set when the JTA transaction manager is set.");
        	}

        	//On JTA transaction manager then the autoCommit is always false
        	autoCommit = false;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
    	return datasource.getConnection();
    }

	private Object lookup(String jndi) {
		Context context = null;
		try {
			context = new InitialContext();
		    return context.lookup(jndi);
		} catch (NamingException e) {
		    throw new SmooksConfigurationException("JNDI Context lookup failed for '" + jndi + "'.", e);
		} finally {
		    if(context != null) {
		        try {
		            context.close();
		        } catch (NamingException e) {
		            throw new SmooksConfigurationException("Error closing Naming Context after looking up DataSource JNDI '" + datasourceJndi + "'.", e);
		        }
		    }
		}
	}

    @Override
    public boolean isAutoCommit() {
        return autoCommit;
    }

    @Override
    public TransactionManager createTransactionManager(Connection connection) {
    	switch(transactionManagerType) {
	    	case JDBC:
	    		return new JdbcTransactionManager(connection, isAutoCommit());
	    	case JTA:
	    		return new JtaTransactionManager(connection, (UserTransaction)lookup(transactionJndi), setAutoCommitAllowed);
	    	case EXTERNAL:
	    		return new ExternalTransactionManager(connection, isAutoCommit(), setAutoCommitAllowed);
	    	default:
	    		throw new SmooksException("The TransactionManager type '" + transactionManagerType + "' is unknown. This is probably a bug!");
    	}
    }
}
