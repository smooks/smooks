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
package org.smooks.event.types;

/**
 * Smooks DOM filter Lifecycle event.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see org.smooks.event.types.DOMFilterLifecycleEvent.DOMEventType
 */
public class DOMFilterLifecycleEvent extends FilterLifecycleEvent {

    public static enum DOMEventType {
        /**
         * The filtering process has started.
         */
        ASSEMBLY_STARTED,
        /**
         * The filtering process has finished.
         */
        PROCESSING_STARTED,
        /**
         * The filtering process has finished.
         */
        SERIALIZATION_STARTED,
    }

    private DOMEventType eventType;

    public DOMFilterLifecycleEvent(DOMEventType eventType) {
        this.eventType = eventType;
    }

    public DOMEventType getDOMEventType() {
        return eventType;
    }

    public String toString() {
        return eventType.toString();
    }
}