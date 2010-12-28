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

import java.util.Date;

/**
 * Pre-installed Time bean.
 * <p/>
 * Installed under beanId "PTIME".
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Time {

    public static final String BEAN_ID = "PTIME";
    
    private long startMillis = System.currentTimeMillis();
    private long startNanos = System.nanoTime();
    private Date startDate;

    /**
     * Get the Execution Context "start" time in milli seconds.
     * <p/>
     * This is the time at which the message processing started.
     *
     * @return The Execution Context "start" time in milli seconds.
     */
    public long getStartMillis() {
        return startMillis;
    }

    /**
     * Get the Execution Context "start" time in nano seconds.
     * <p/>
     * This is the time at which the message processing started.
     *
     * @return The Execution Context "start" time in  nano seconds.
     */
    public long getStartNanos() {
        return startNanos;
    }

    /**
     * Get the Execution Context "start" time as a {@link Date}.
     * <p/>
     * This is the time at which the message processing started.
     *
     * @return The Execution Context "start" time as a {@link Date}.
     */
    public Date getStartDate() {
        if(startDate == null) {
            startDate = new Date(startMillis);
        }
        return startDate;
    }

    /**
     * Get the current time in milli seconds.
     *
     * @return The current time in milli seconds.
     */
    public long getNowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Get the current time in nano seconds.
     *
     * @return The current time in nano seconds.
     */
    public long getNowNanos() {
        return System.nanoTime();
    }

    /**
     * Get the current time as a {@link Date}.
     *
     * @return The current time as a {@link Date}.
     */
    public Date getNowDate() {
        return new Date();
    }

    @Override
    public String toString() {
        return "<noop>";
    }
}
