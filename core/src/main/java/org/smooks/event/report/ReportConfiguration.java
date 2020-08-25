/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
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
package org.smooks.event.report;

import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.event.ExecutionEvent;
import org.smooks.event.types.ConfigBuilderEvent;
import org.smooks.event.types.ElementVisitEvent;

import java.io.File;
import java.io.Writer;

/**
 * Report generation configuration.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("WeakerAccess")
public class ReportConfiguration {

    public static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    private Writer outputWriter;
    private boolean escapeXMLChars = false;
    private boolean showDefaultAppliedResources = false;
    private Class<? extends ExecutionEvent>[] filterEvents;
    private boolean autoCloseWriter = true;
    private File tempOutDir = TEMP_DIR;

    @SuppressWarnings("unchecked")
    public ReportConfiguration(Writer outputWriter) {
        AssertArgument.isNotNull(outputWriter, "outputWriter");
        this.outputWriter = outputWriter;
        filterEvents = new Class[] {ConfigBuilderEvent.class, ElementVisitEvent.class};
    }

    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    public Writer getOutputWriter() {
        return outputWriter;
    }

    @SuppressWarnings("unused")
    public boolean escapeXMLChars() {
        return escapeXMLChars;
    }

    @SuppressWarnings("unused")
    public void setEscapeXMLChars(boolean escapeXMLChars) {
        this.escapeXMLChars = escapeXMLChars;
    }

    public boolean showDefaultAppliedResources() {
        return showDefaultAppliedResources;
    }

    @SuppressWarnings("unused")
    public void setShowDefaultAppliedResources(boolean showDefaultAppliedResources) {
        this.showDefaultAppliedResources = showDefaultAppliedResources;
    }


    /**
     * Set a list of {@link org.smooks.event.ExecutionEvent event} types on which to filter.
     * <p/>
     * The listener will only capture {@link org.smooks.event.ExecutionEvent event} types
     * provided in this list.  If not set, all events will be captured.
     *
     * @param filterEvents Filter events.
     */
    @SuppressWarnings("unused")
    public void setFilterEvents(Class<? extends ExecutionEvent>... filterEvents) {
        this.filterEvents = filterEvents;
    }

    public Class<? extends ExecutionEvent>[] getFilterEvents() {
        return filterEvents;
    }

    public boolean autoCloseWriter() {
        return autoCloseWriter;
    }

    /**
     * Should the writer be closed automatically after the report is completed.
     * <p/>
     * Default true.
     *
     * @param autoCloseWriter True if the writer is to be closed, otherwise false.
     */
    @SuppressWarnings("unused")
    public void setAutoCloseWriter(boolean autoCloseWriter) {
        this.autoCloseWriter = autoCloseWriter;
    }

    @SuppressWarnings("unused")
    public File getTempOutDir() {
        if(tempOutDir == null) {
            throw new SmooksConfigurationException("Temp OutDir not set.");
        }
        return tempOutDir;
    }

    public void setTempOutDir(File tempOutDir) {
        this.tempOutDir = tempOutDir;
    }
}
