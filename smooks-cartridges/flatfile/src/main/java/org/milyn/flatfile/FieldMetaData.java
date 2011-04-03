/*
 * Milyn - Copyright (C) 2006 - 2010
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

package org.milyn.flatfile;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.milyn.assertion.AssertArgument;
import org.milyn.function.StringFunctionExecutor;

/**
 * Flat file record field metadata.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FieldMetaData {

    public static final String IGNORE_FIELD = "$ignore$";

    private String name;
    private boolean ignore;
    private int ignoreCount;
    private StringFunctionExecutor stringFunctionExecutor;

    public FieldMetaData(String name) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        this.name = name;
        ignore = name.startsWith(IGNORE_FIELD);
        if(ignore) {
            ignoreCount = parseIgnoreFieldDirective(name);
        }
    }

    public String getName() {
        return name;
    }

    public boolean ignore() {
        return ignore;
    }

    public int getIgnoreCount() {
        return ignoreCount;
    }

    public StringFunctionExecutor getStringFunctionExecutor() {
        return stringFunctionExecutor;
    }

    public FieldMetaData setStringFunctionExecutor(StringFunctionExecutor stringFunctionExecutor) {
        this.stringFunctionExecutor = stringFunctionExecutor;
        return this;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("name", name)
               .append("stringFunctionExecutor", stringFunctionExecutor);
        return builder.toString();
    }

    private int parseIgnoreFieldDirective(String field) {
        String op = field.substring(IGNORE_FIELD.length());
        int toSkip = 0;
        if (op.length() == 0) {
            toSkip = 1;
        } else if ("+".equals(op)) {
            toSkip = Integer.MAX_VALUE;
        } else {
            toSkip = Integer.parseInt(op);
        }
        return toSkip;

    }
}
