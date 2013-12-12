/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.cartridge.javabean.factory;

/**
 * Creates MVELFactory objects from a factory definition.
 *
 * The MVELFactory is cached so that it is only created once for a definition.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Alias("mvel")
public class MVELFactoryDefinitionParser extends AbstractCachingFactoryDefinitionParser {

	@Override
	protected Factory<?> createFactory(String factoryDefinition) {
		return new MVELFactory<Object>(factoryDefinition);
	}

}
