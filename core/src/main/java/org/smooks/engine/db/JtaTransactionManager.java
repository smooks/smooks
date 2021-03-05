/*-
 * ========================LICENSE_START=================================
 * Core
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

import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.SQLException;

class JtaTransactionManager implements TransactionManager {

	private final UserTransaction transaction;

	private final Connection connection;

	private boolean newTransaction;

	private final boolean setAutoCommitAllowed;

	/**
	 * @param transaction
	 * @param connection
	 */
	public JtaTransactionManager(Connection connection, UserTransaction transaction, boolean setAutoCommitAllowed) {
		AssertArgument.isNotNull(connection, "connection");
		AssertArgument.isNotNull(transaction, "transaction");

		this.connection = connection;
		this.transaction = transaction;
		this.setAutoCommitAllowed = setAutoCommitAllowed;
	}

	public void begin() {
		try {
			newTransaction = transaction.getStatus() == Status.STATUS_NO_TRANSACTION;
			if(newTransaction) {
				transaction.begin();
			}
			if(setAutoCommitAllowed) {
				try {
					if(connection.getAutoCommit()) {
						connection.setAutoCommit(false);
					}
				} catch (SQLException e) {
					throw new TransactionException("Exception while setting the 'autoCommit' flag on the connection.", e);
				}
			}
		} catch (Exception e) {
			throw new TransactionException("Exception while beginning the exception", e);
		}
	}

	public void commit() {
		if(newTransaction) {
			try {
				transaction.commit();
			} catch (Exception e) {
				throw new TransactionException("Exception while committing the transaction.", e);
			}
		}
	}

	public void rollback() {
		if(newTransaction) {
			try {
				transaction.rollback();
			} catch (Exception e) {
				throw new TransactionException("Exception while rolling back the transaction.", e);
			}
		} else {
			try {
				transaction.setRollbackOnly();
			} catch (Exception e) {
				throw new TransactionException("Exception while setting the 'rollback only' flag on the transaction.", e);
			}
		}
	}
}
