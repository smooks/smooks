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
package org.smooks.scribe.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this method looks entities up.
 * <p>
 * The method can have one or more parameters. With the {@link Param} annotation
 * the parameters can be 'named'. This makes it possible to reference the parameter
 * by its name instead of its position. If one parameter is annotated with the {@link Param}
 * parameter then all the parameters need to be annotated. If no parameters are annotated
 * then the parameters need to be referenced by there position, starting by zero.
 * <p>
 * This annotation should only be used on classes that
 * are annotated with the {@link Dao} annotation.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lookup {

	/**
	 * The name of the lookup operation. If it is not set then
	 * the name of the method will be the name of the operation.
	 *
	 * @return the operation name
	 */
	String name() default "";
}
