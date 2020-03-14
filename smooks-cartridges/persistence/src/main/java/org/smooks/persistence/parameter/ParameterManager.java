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
package org.smooks.persistence.parameter;

import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.persistence.ParameterListType;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class ParameterManager {

	private static final String PARAMETER_CONTAINER_CONTEXT_KEY = ParameterContainer.class.getName() + "#CONTEXT_KEY";

	private static final String PARAMETER_INDEX_CONTEXT_KEY = ParameterIndex.class.getName() + "#CONTEXT_KEY";


	public static String getParameterIndexName(int id) {
		return PARAMETER_INDEX_CONTEXT_KEY + "#" + id;
	}

	public static String getParameterContainerName(int id) {
		return PARAMETER_CONTAINER_CONTEXT_KEY + "#" + id;
	}


	public static ParameterIndex<?, ?> initializeParameterIndex(int id, ParameterListType type, ApplicationContext applicationContext) {

		ParameterIndex<?, ?> index;

		switch (type) {
		case NAMED:
			index = new NamedParameterIndex();
			break;
		case POSITIONAL:
			index = new PositionalParameterIndex();
			break;
		default:
			throw new IllegalStateException("Unknown ParameterListType '" + type + "'.");
		}

		applicationContext.setAttribute(getParameterIndexName(id), index);

		return index;
	}

	@SuppressWarnings("unchecked")
	public static ParameterIndex<?, ? extends Parameter<?>> getParameterIndex(int id, ApplicationContext applicationContext) {
		return (ParameterIndex<?, ? extends Parameter<?>>) applicationContext.getAttribute(getParameterIndexName(id));
	}

	public static void initializeParameterContainer(int id, ParameterListType type, ExecutionContext executionContext) {
		ParameterContainer<?> container = getParameterContainer(id, executionContext);

		if(container == null) {

			switch (type) {
			case NAMED:
				container = new NamedParameterContainer((NamedParameterIndex) getParameterIndex(id, executionContext.getContext()));
				break;
			case POSITIONAL:
				container = new PositionalParameterContainer((PositionalParameterIndex) getParameterIndex(id, executionContext.getContext()));
				break;
			default:
				throw new IllegalStateException("Unknown ParameterListType '" + type + "'.");
			}

			executionContext.setAttribute(getParameterContainerName(id), container);

		} else {
			container.clear();
		}

	}

	@SuppressWarnings("unchecked")
	public static ParameterContainer<Parameter<?>> getParameterContainer(int id, ExecutionContext executionContext) {
		return (ParameterContainer<Parameter<?>>) executionContext.getAttribute(getParameterContainerName(id));
	}


	private ParameterManager() {
	}

}
