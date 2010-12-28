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
package org.milyn.javabean.factory;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.expression.BeanMapExpressionEvaluator;

/**
 * The MVELFactory uses MVEL to create the objects using a MVEL expression.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MVELFactory<T> implements Factory<T> {

	private BeanMapExpressionEvaluator expressionEvaluator;

	/**
	 *
	 */
	public MVELFactory() {
	}

	public MVELFactory(String expression) {
		expressionEvaluator = new BeanMapExpressionEvaluator(expression);
	}

	/* (non-Javadoc)
	 * @see org.milyn.javabean.factory.Factory#create()
	 */

	public T create(ExecutionContext executionContext) {
		@SuppressWarnings("unchecked")
		T result = (T) expressionEvaluator.getValue(executionContext);

		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("expressionEvaluator", expressionEvaluator)
					.toString();
	}

}
