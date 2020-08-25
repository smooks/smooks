/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.util;

import org.hsqldb.Server;
import org.hsqldb.ServerConstants;
import org.hsqldb.jdbcDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

/**
 * @author
 */
public class HsqlServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HsqlServer.class);

    private Server hsqlServer;

    private final String url;
    private final String username = "sa";
    private final String password = "";

    private final Connection connection;
    
    private final CountDownLatch startGate = new CountDownLatch(1);
    
    public HsqlServer(final int port) throws Exception {
        final String databaseName = "milyn-hsql-" + port;

        url = "jdbc:hsqldb:hsql://localhost:" + port + "/" + databaseName +";shutdown=true";
        LOGGER.info("Starting Hypersonic Database '" + url + "'.");
        new Thread() {
            @Override
            public void run() {
                Server server = new Server();
                Logger targetLogger = LoggerFactory.getLogger("org.hsqldb");
                server.setLogWriter(new PrintWriter(new StdoutToLog4jFilter(server.getLogWriter(), targetLogger)));
                server.setDatabasePath(0, "target/hsql/" + databaseName);
                server.setDatabaseName(0, databaseName);
                server.setNoSystemExit( true );
                server.setSilent( true );
                server.setPort(port);
                server.start();


                hsqlServer = server;
                startGate.countDown();
            }
        }.start();

        startGate.await();

        DriverManager.registerDriver(new jdbcDriver());
        connection = DriverManager.getConnection(url, username, password);
    }

    public void stop() throws Exception {
        try {
            hsqlServer.signalCloseAllServerConnections();
            connection.close();
        } catch (final SQLException ignored) {
            LOGGER.debug(ignored.getMessage(), ignored);
        } 
        finally {
            hsqlServer.stop();
            org.hsqldb.DatabaseManager.closeDatabases(0);
            while(hsqlServer.getState() != ServerConstants.SERVER_STATE_SHUTDOWN) {
                Thread.sleep(100L);
            }
        }
    }

    public boolean execScript(InputStream script) throws SQLException {
        String scriptString;
        try {
            scriptString = StreamUtils.readStream(new InputStreamReader(script));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Statement statement = connection.createStatement();
        try {
            return statement.execute(scriptString);
        } finally {
            statement.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getState() {
        if (hsqlServer == null) {
            throw new IllegalStateException("hsqlServer was null. Perhaps there was an error upon startup?");
        }
        return hsqlServer.getState();
    }
}
