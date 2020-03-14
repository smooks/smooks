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

import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.assertion.AssertArgument;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Direct DataSource.
 * <p/>
 * Configured with a specific JDBC driver plus username etc.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@VisitBeforeReport(summary = "Bind DirectDataSource <b>${resource.parameters.datasource}</b> to ExecutionContext.", detailTemplate = "reporting/DirectDataSource_before.html")
@VisitAfterReport(summary = "Cleaning up DirectDataSource <b>${resource.parameters.datasource}</b>. Includes performing commit/rollback etc.", detailTemplate = "reporting/DirectDataSource_after.html")
public class DirectDataSource extends AbstractDataSource {

    @ConfigParam(name = "datasource")
    private String name;

    @ConfigParam
    private boolean autoCommit;

    @ConfigParam
    private Class driver;

    @ConfigParam
    private String url;

    @ConfigParam
    private String username;

    @ConfigParam
    private String password;

    public String getName() {
        return name;
    }

    public DirectDataSource setName(String name) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        this.name = name;
        return this;
    }

    public DirectDataSource setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    public DirectDataSource setDriver(Class driver) {
        AssertArgument.isNotNull(driver, "driver");
        this.driver = driver;
        return this;
    }

    public DirectDataSource setUrl(String url) {
        AssertArgument.isNotNullAndNotEmpty(url, "url");
        this.url = url;
        return this;
    }

    public DirectDataSource setUsername(String username) {
        AssertArgument.isNotNull(username, "username");
        this.username = username;
        return this;
    }

    public DirectDataSource setPassword(String password) {
        AssertArgument.isNotNull(password, "password");
        this.password = password;
        return this;
    }

    @Initialize
    public void registerDriver() throws SQLException {
        Driver driverInstance;

        AssertArgument.isNotNullAndNotEmpty(name, "name");
        AssertArgument.isNotNull(driver, "driver");
        AssertArgument.isNotNullAndNotEmpty(url, "url");
        AssertArgument.isNotNull(username, "username");
        AssertArgument.isNotNull(password, "password");

        try {
            driverInstance = (Driver) driver.newInstance();
        } catch (Exception e) {
            SQLException sqlE = new SQLException("Failed to register JDBC driver '" + driver + "'.  Unable to create instance of driver class.");
            sqlE.initCause(e);
            throw sqlE;
        }
        
        DriverManager.registerDriver(driverInstance);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }
}
