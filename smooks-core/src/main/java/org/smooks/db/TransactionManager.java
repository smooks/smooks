package org.smooks.db;

/**
 * The transaction manager manages the transaction
 * of a data source
 * <p />
 *
 * This transaction manager does nothing and has a default level
 * because it can change in future versions of Smooks.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public interface TransactionManager {

	/**
	 * Begin the transaction
	 *
	 * @throws TransactionException If an exception got thrown while beginning the exception
	 */
    void begin();
    /**
	 * Commit the transaction
	 *
	 * @throws TransactionException If an exception got thrown while committing the exception
	 */
    void commit();

    /**
	 * Rollback the transaction
	 *
	 * @throws TransactionException If an exception got thrown while rollingback the exception
	 */
    void rollback();

}
