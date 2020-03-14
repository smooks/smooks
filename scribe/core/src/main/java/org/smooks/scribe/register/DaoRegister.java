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
package org.smooks.scribe.register;

/**
 * The DAO Register
 * <p>
 * Makes it possible to retrieve a default unnamed DAO or
 * one or more named DAO's.
 * <p>
 * DAO's retrieved from a DaoRegister should always be returned
 * to the DaoRegister by calling the {@link DaoRegister#returnDao(Object)}
 * method.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 * @param <T> the DAO type
 */
public interface DaoRegister<T> {

	/**
	 * Returns the default DAO .
	 *
	 * @return the default DAO
	 * @throws UnsupportedOperationException if the <tt>getDao()</tt> operation is
     *	          not supported by this DaoRegister.
	 */
	T getDefaultDao();

	/**
	 * Returns the DAO with the specified name.
	 *
	 * @param name the name of the DAO
	 * @return the DAO with the specified name
	 * @throws UnsupportedOperationException if the <tt>getDao(String)</tt> operation is
     *	          not supported by this DaoRegister.
	 */
	T getDao(String name);


	/**
	 * Returns the DAO to the register. This is
	 * useful if the register has some
	 * locking or pooling mechanism. If it isn't necessary
	 * for DAO to be returned to the register then this
	 * method shouldn't do anything.
	 *
	 * @param dao the DAO to return
	 */
	void returnDao(T dao);

}
