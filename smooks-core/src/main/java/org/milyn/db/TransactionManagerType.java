package org.milyn.db;

import org.milyn.commons.javabean.DataDecodeException;

public enum TransactionManagerType {
    JDBC,
    JTA,
    EXTERNAL;

    public static final String JDBC_STRING = "JDBC";
    public static final String JTA_STRING = "JTA";
    public static final String EXTERNAL_STRING = "EXTERNAL";

    /**
     * A Data decoder for this Enum
     *
     * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
     */
    public static class DataDecoder implements org.milyn.commons.javabean.DataDecoder {

        /* (non-Javadoc)
         * @see org.milyn.commons.javabean.DataDecoder#decode(java.lang.String)
         */
        public Object decode(final String data) throws DataDecodeException {
            final String value = data.toUpperCase();

            return valueOf(value);
        }

    }
}
