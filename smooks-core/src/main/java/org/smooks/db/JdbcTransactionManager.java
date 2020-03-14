package org.smooks.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.smooks.assertion.AssertArgument;


class JdbcTransactionManager implements TransactionManager {

	private final Connection connection;

	private final boolean autoCommit;

	public JdbcTransactionManager(Connection connection, boolean autoCommit) {
		AssertArgument.isNotNull(connection, "connection");

		this.connection = connection;
		this.autoCommit = autoCommit;
	}

	public void begin() {
		try {
			if(connection.getAutoCommit() != autoCommit) {
				connection.setAutoCommit(autoCommit);
			}
		} catch (SQLException e) {
			throw new TransactionException("Exception while setting the autoCommit flag of the connection", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.smooks.db.TransactionManager#commit()
	 */
	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new TransactionException("Exception while committing the connection", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.smooks.db.TransactionManager#rollback()
	 */
	public void rollback() {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new TransactionException("Exception while rolling back the connection", e);
		}
	}

}
