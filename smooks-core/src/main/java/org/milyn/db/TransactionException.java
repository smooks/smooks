package org.milyn.db;

/**
 * Exception if something goes wrong with the transaction or with
 * setting the transaction settings.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
class TransactionException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 * @param cause
	 */
	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public TransactionException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public TransactionException(Throwable cause) {
		super(cause);
	}



}
