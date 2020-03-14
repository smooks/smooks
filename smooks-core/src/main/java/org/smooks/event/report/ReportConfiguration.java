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

    public static File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

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
