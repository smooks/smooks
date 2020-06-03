/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.javabean.context.preinstalled;

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
