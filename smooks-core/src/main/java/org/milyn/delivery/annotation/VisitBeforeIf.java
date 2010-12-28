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
package org.milyn.delivery.annotation;

import java.lang.annotation.*;

/**
 * Visit If annotation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public abstract @interface VisitBeforeIf {

    /**
     * The {@link org.milyn.cdr.SmooksResourceConfiguration} condition that
     * must evaluate to true in order for the visitBefore method to be called.
     * 
     * @return An inline <a href="http://mvel.codehaus.org/">MVEL</a> expression,
     * or a reference to a file resource on the classpath.
     */
    public String condition();
}
