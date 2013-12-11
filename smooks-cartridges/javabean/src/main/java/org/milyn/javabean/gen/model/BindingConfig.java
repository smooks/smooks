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
package org.milyn.javabean.gen.model;

import org.milyn.commons.javabean.DataDecoder;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BindingConfig {

    private Field property;
    private String wireBeanId;

    public BindingConfig(Field property) {
        this.property = property;
    }

    public BindingConfig(String wireBeanId) {
        this.wireBeanId = wireBeanId;
    }

    public BindingConfig(Field property, String wireBeanId) {
        this.property = property;
        this.wireBeanId = wireBeanId;
    }

    public Field getProperty() {
        return property;
    }

    public String getSelector() {
        if(wireBeanId != null) {
            return wireBeanId;
        }

        return "$TODO$";
    }

    public boolean isWiring() {
        return (wireBeanId != null);
    }

    public boolean isBoundToProperty() {
        return (property != null);
    }

    public String getType() {
        Class type = property.getType();

        if(type.isArray()) {
            return "$DELETE:NOT-APPLICABLE$";
        }

        Class<? extends DataDecoder> decoder = DataDecoder.Factory.getInstance(type);

        if(type.isPrimitive() || type.getPackage().equals(String.class.getPackage())) {
            String typeAlias = decoder.getSimpleName();

            if(typeAlias.endsWith("Decoder")) {
                return typeAlias.substring(0, typeAlias.length() - "Decoder".length());
            }
        }

        return "$TODO$";
    }
}
