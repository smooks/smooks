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
package org.milyn.scribe.register;


/**
 * A abstract convenience implementation of the DaoRegister
 *
 * The {@link #getDefaultDao()} and {@link #getDao(String)} methods both throw a
 * {@link UnsupportedOperationException}. The {@link #returnDao(Object)} methods
 * does nothing.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 * @param <T> The DAO type
 */
public abstract class AbstractDaoRegister<D> implements DaoRegister<D> {

	/* (non-Javadoc)
	 * @see org.milyn.scribe.DaoRegister#getDao()
	 */
	public D getDefaultDao() {
		throw new UnsupportedOperationException("The getDefaultDao() method is not supported by this '" + this.getClass().getName() + "' DaoRegister.");
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.DaoRegister#getDao(java.lang.String)
	 */
	public D getDao(String name) {
		throw new UnsupportedOperationException("The getDao(String) method is not supported by this '" + this.getClass().getName() + "' DaoRegister.");
	}

	public void returnDao(D dao) {};

}
