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

package org.milyn.javabean.context.preinstalled;

import java.util.UUID;

/**
 * Pre-installed UUID bean.
 * <p/>
 * Installed under beanId "PUUID".
 * <p/>
 * Uses the {@link UUID} class to generate the IDs.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UniqueID {

    public static final String BEAN_ID = "PUUID";

    private String execContext;

    /**
     * Get the unique Id of the execution context.
     * @return Execution Context unique ID.
     */
    public String getExecContext() {
        if(execContext == null) {
            execContext = getRandom();
        }
        return execContext;
    }

    /**
     * Get a random unique Id.
     * @return A random unique ID.
     */
    private String getRandom() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "<noop>";
    }
}
