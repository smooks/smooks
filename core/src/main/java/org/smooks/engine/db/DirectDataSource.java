/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.engine.db;

import org.smooks.assertion.AssertArgument;
import org.smooks.api.resource.visitor.VisitAfterReport;
import org.smooks.api.resource.visitor.VisitBeforeReport;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
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

    @Inject
    @Named("datasource")
    private String name;

    @Inject
    private Boolean autoCommit;

    @Inject
    private Class driver;

    @Inject
    private String url;

    @Inject
    private String username;

    @Inject
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

    @PostConstruct
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
            SQLException sqlE = new SQLException("Failed to register JDBC driver '" + driver + "'.  Unable to create instance of driver class.", e);
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
