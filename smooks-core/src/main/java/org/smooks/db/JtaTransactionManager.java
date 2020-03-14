package org.smooks.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.smooks.assertion.AssertArgument;

class JtaTransactionManager implements TransactionManager {

	private UserTransaction transaction;

	private Connection connection;

	private boolean newTransaction;

	private boolean setAutoCommitAllowed;

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
