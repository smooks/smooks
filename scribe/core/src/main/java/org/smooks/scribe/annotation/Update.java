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
 * Indicates that this method updates entities.
 * <p>
 * The method must have only one parameter. This parameter is
 * for the entity that is going to be updated.
 * <p>
 * This annotation should only be used on classes that
 * are annotated with the {@link Dao} annotation.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Update {

	/**
	 * The name of the update operation. If it is not set then
	 * the name of the method will be the name of the operation.
	 *
	 * @return the operation name
	 */
	String name() default "";

	/**
	 * If this method is the default update method of the DAO.
	 * Only one method can be set to the default update method.
	 * If only one method with the {@link Delete} annotation is
	 * present then that method is automatically the default method.
	 *
	 * @return if the method is the default update method
	 */
	boolean isDefault() default false;
}
