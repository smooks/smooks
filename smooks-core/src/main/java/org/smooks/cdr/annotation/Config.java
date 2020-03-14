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

import java.lang.annotation.*;

/**
 * Inject the {@link org.smooks.cdr.SmooksResourceConfiguration} instance onto the
 * annotated field.
 * <p/>
 * The field must be of type {@link org.smooks.cdr.SmooksResourceConfiguration}.  To inject
 * specific {@link org.smooks.cdr.SmooksResourceConfiguration#getParameter(String) parameters} onto
 * fields, use the {@link @org.smooks.cdr.annotation.ConfigParam()} annotation.
 *
 * <h3>Usage</h3>
 * <pre>
 *     &#64;Config
 *     private {@link org.smooks.cdr.SmooksResourceConfiguration} config;
 * </pre>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see Configurator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {
}
