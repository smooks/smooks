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
package org.smooks.javabean.factory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The AbstractCachingFactoryDefinitionParser caches all the
 * factories that are create with the createFactory method. This prevents
 * that factories are unnecessary created.
 *
 * This class is thread safe. Factories that are created with this class
 * should also be thread safe.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public abstract class AbstractCachingFactoryDefinitionParser implements FactoryDefinitionParser {

	private final ConcurrentHashMap<String, Factory<?>> factoryCache = new ConcurrentHashMap<String, Factory<?>>();

	public Factory<?> parse(String factoryDefinition) {
		Factory<?> factory = factoryCache.get(factoryDefinition);

		if(factory == null) {

			factory = createFactory(factoryDefinition);

			if(factory == null) {
				throw new NullPointerException("Null was returned by the createFactory method.");
			}

			// Make sure that we always return the same factory
			Factory<?> cachedFactory = factoryCache.putIfAbsent(factoryDefinition, factory);
			if(cachedFactory != null) {
				factory = cachedFactory;
			}
		}

		return factory;
	}

	protected abstract Factory<?> createFactory(String factoryDefinition);

}
