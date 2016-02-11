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

import org.milyn.javabean.DataDecoder;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BindingConfig {

    private Field property;
    private String wireBeanId;
    private boolean isWiring;

    public BindingConfig(Field property) {
        this.property = property;
        this.isWiring = false;
    }

    public BindingConfig(String wireBeanId) {
        this.wireBeanId = wireBeanId;
        this.isWiring = (wireBeanId != null);
    }

    public BindingConfig(Field property, String wireBeanId) {
        this.property = property;
        this.wireBeanId = wireBeanId;
        this.isWiring = (wireBeanId != null);
    }

    public BindingConfig(Field property, String wireBeanId, boolean isWiring) {
        this.property = property;
        this.wireBeanId = isWiring ? wireBeanId : capitalizeFirstLetter(wireBeanId);
        this.isWiring = isWiring;
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
        return isWiring;
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

    private String capitalizeFirstLetter(String text) {
        if (text != null && text.length() > 0) {
            String cap = text.substring(0, 1).toUpperCase();
            if (text.length() > 1) {
                cap = cap + text.substring(1);
            }
            return cap;
        }
        else {
            return text;
        }
    }
}
