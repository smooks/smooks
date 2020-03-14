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

package org.smooks.javabean.pojogen;

import java.util.*;

/**
 * Pojogen utility methods.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
class PojoGenUtil {

    public static String getTypeDecl(String pre, Set<JType> types) {
        if(types.isEmpty()) {
            return "";
        }

        StringBuilder typeDecl = new StringBuilder();

        typeDecl.append(" ");
        typeDecl.append(pre);
        typeDecl.append(" ");

        List<JType> typesList = new ArrayList<JType>(types);
        Set<Class<?>> addedTypes = new LinkedHashSet<Class<?>>();
        for(int i = 0; i < types.size(); i++) {
            Class<?> type = typesList.get(i).getType();
            if(!addedTypes.contains(type)) {
                if(i > 0) {
                    typeDecl.append(", ");
                }
                typeDecl.append(type.getSimpleName());
                addedTypes.add(type);
            }
        }

        return typeDecl.toString();
    }
}
