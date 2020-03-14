
package org.smooks.delivery.sax;

import org.smooks.SmooksException;

/**
 * {@link SAXElement} element writer access excecption.
 * <p/>
 * See {@link org.smooks.delivery.sax.SAXElement}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXWriterAccessException extends SmooksException {

    public SAXWriterAccessException(String message) {
        super(message);
    }

    public SAXWriterAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
