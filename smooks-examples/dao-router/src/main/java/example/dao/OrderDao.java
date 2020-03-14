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
package example.dao;

import javax.persistence.EntityManager;

import org.smooks.scribe.annotation.Dao;
import org.smooks.scribe.annotation.Insert;

import example.jpa.entity.Order;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Dao
public class OrderDao {

	private final EntityManager em;

	/**
	 * @param em
	 */
	public OrderDao(EntityManager em) {
		this.em = em;
	}

	@Insert
	public void insertOrder(Order order) {
		em.persist(order);
	}

}
