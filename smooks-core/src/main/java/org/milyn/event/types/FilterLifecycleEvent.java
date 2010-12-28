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

/**
 * Smooks filter Lifecycle event.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see EventType
 */
public class FilterLifecycleEvent implements ExecutionEvent {

    public static enum EventType {
        /**
         * The filtering process has started.
         */
        STARTED, 
        /**
         * The filtering process has finished.
         */
        FINISHED,
    }

    private EventType eventType;

    protected FilterLifecycleEvent() {
        // Allow package level extension...
    }

    public FilterLifecycleEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String toString() {
        return eventType.toString();
    }
}
