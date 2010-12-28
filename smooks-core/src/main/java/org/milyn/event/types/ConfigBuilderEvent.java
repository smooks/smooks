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
package org.milyn.event.types;

import org.milyn.event.ExecutionEvent;
import org.milyn.cdr.SmooksResourceConfiguration;

/**
 * Configuration Builder Event.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConfigBuilderEvent implements ExecutionEvent {

    private SmooksResourceConfiguration resourceConfig;
    private String message;
    private Throwable thrown;

    public ConfigBuilderEvent(String message) {
        this.message = message;
    }

    public ConfigBuilderEvent(SmooksResourceConfiguration resourceConfig, String message) {
        this.resourceConfig = resourceConfig;
        this.message = message;
    }

    public ConfigBuilderEvent(SmooksResourceConfiguration resourceConfig, String message, Throwable thrown) {
        this(resourceConfig, message);
        this.thrown = thrown;
    }

    public SmooksResourceConfiguration getResourceConfig() {
        return resourceConfig;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrown() {
        return thrown;
    }
}
