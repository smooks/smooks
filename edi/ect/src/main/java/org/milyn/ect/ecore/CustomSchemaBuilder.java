/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.ect.ecore;

import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.xsd.ecore.EcoreSchemaBuilder;

/**
 * Extension of {@link EcoreSchemaBuilder} to simplify the namespace prefix
 * 
 * @author zubairov
 *
 */
public class CustomSchemaBuilder extends EcoreSchemaBuilder {

	public CustomSchemaBuilder(ExtendedMetaData extendedMetaData) {
		super(extendedMetaData);
	}

	@Override
	public String qualifiedPackageName(String namespace) {
		if (SmooksMetadata.ANNOTATION_TYPE.equals(namespace)) {
			return "s";
		}
		return super.qualifiedPackageName(namespace);
	}
	
}
