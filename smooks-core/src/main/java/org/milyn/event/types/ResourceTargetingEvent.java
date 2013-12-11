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

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.delivery.VisitSequence;
import org.milyn.event.ElementProcessingEvent;
import org.milyn.event.ResourceBasedEvent;

import java.util.Arrays;

/**
 * Resource targeting event.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ResourceTargetingEvent extends ElementProcessingEvent implements ResourceBasedEvent {

    private SmooksResourceConfiguration resourceConfig;
    private Object[] metadata;
    private VisitSequence sequence;

    /**
     * Event constructor.
     * @param element The element ({@link org.milyn.delivery.sax.SAXElement}/{@link org.w3c.dom.Element})
     * being targeted by the resource.
     * @param resourceConfig The resource configuration.
     * @param metadata Optional event metadata.
     */
    public ResourceTargetingEvent(Object element, SmooksResourceConfiguration resourceConfig, Object... metadata) {
        super(element);
        this.resourceConfig = resourceConfig;
        this.metadata = metadata;
    }

    /**
     * Event constructor.
     * @param element The element ({@link org.milyn.delivery.sax.SAXElement}/{@link org.w3c.dom.Element})
     * being targeted by the resource.
     * @param resourceConfig The resource configuration.
     * @param metadata Optional event metadata.
     */
    public ResourceTargetingEvent(Object element, SmooksResourceConfiguration resourceConfig, VisitSequence sequence, Object... metadata) {
        this(element, resourceConfig, metadata);
        this.sequence = sequence;
    }

    /**
     * Get the tagreted resource configuration.
     * @return The targeted resource configuration.
     */
    public SmooksResourceConfiguration getResourceConfig() {
        return resourceConfig;
    }

    /**
     * Set event metadata.
     * @param metadata Event metadata.
     */
    public void setMetadata(Object... metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the optional event metadata.
     * @return Event metadata.
     */
    public Object[] getMetadata() {
        return metadata;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Target: ").append(getElement()).append(". ");
        builder.append("Resource: ").append(resourceConfig).append(". ");
        if(metadata != null) {
            builder.append("Event Metadata: ").append(Arrays.asList(metadata)).append(".");
        }

        return builder.toString();
    }

    public VisitSequence getSequence() {
        return sequence;
    }
}
