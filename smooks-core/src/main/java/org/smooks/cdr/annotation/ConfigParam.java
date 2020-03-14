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
package org.smooks.cdr.annotation;

import org.smooks.javabean.DataDecoder;

import java.lang.annotation.*;

/**
 * Configuration parameter field annotation.
 * <p/>
 * Helps supports reflective injection of {@link org.smooks.delivery.ContentHandler} parameters
 * from its {@link org.smooks.cdr.SmooksResourceConfiguration} instance.  To inject the whole
 * {@link org.smooks.cdr.SmooksResourceConfiguration} instance, use the {@link @org.smooks.cdr.annotation.Config}
 * annotation.
 *
 * <h3>Usage</h3>
 * Where the parameter name is the same as the field name:
 * <pre>
 *     &#64;ConfigParam(decoder={@link org.smooks.javabean.decoders.IntegerDecoder}.class)
 *     private int maxDigits;
 * </pre>
 * Where the parameter name is NOT the same as the field name:
 * <pre>
 *     &#64;ConfigParam(name="max-digits", decoder={@link org.smooks.javabean.decoders.IntegerDecoder}.class)
 *     private int maxDigits;
 * </pre>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see Configurator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ConfigParam {

    /**
     * The paramater name as defined in the resource configuration.  If not defined,
     * the name defaults to the name of the field.
     * @return The paramater name.
     */
    public String name() default AnnotationConstants.NULL_STRING;

    /**
     * Paramater required or optional.
     * <p/>
     * Defaults to required.
     *
     * @return Paramater usage.
     */
    public Use use() default Use.REQUIRED;

    /**
     * The default paramater value.
     * <p/>
     * Only relevant when use=OPTIONAL and the paramater is not defined on the configuration..
     *
     * @return The default paramater value (un-decoded).
     */
    public String defaultVal() default AnnotationConstants.UNASSIGNED;

    /**
     * Paramater choice values.
     *
     * @return List of valid choices (un-decoded).
     */
    public String[] choice() default AnnotationConstants.NULL_STRING;

    /**
     * The {@link DataDecoder} class to use when decoding the paramater value.
     * @return The {@link DataDecoder} class.
     */
    public Class<? extends DataDecoder> decoder() default DataDecoder.class;

    /**
     * Configuration paramater use.
     */
    public static enum Use {
        /**
         * Parameter is required.
         */
        REQUIRED,

        /**
         * Parameter is optional.
         */
        OPTIONAL,
    }
}
