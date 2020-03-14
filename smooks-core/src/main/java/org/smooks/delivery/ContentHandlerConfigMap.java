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

package org.smooks.delivery;

import org.smooks.cdr.SmooksResourceConfiguration;

/**
 * Mapping between a resource configuration and its corresponding resource
 * configuration.
 * <p/>
 * Obviously this class is only relevant when the resource configuration refers to
 * a {@link ContentHandler}.
 * @author tfennelly
 */
public class ContentHandlerConfigMap<T extends ContentHandler> {

    private T contentHandler;
    private boolean isLifecycleInitializable;
    private boolean isLifecycleCleanable;
    private SmooksResourceConfiguration resourceConfig;

    /**
     * Public constructor.
     * @param contentHandler The content handler instance.
     * @param resourceConfig The defining resource configuration.
     */
    public ContentHandlerConfigMap(T contentHandler, SmooksResourceConfiguration resourceConfig) {
        this.contentHandler = contentHandler;
        this.resourceConfig = resourceConfig;
        isLifecycleInitializable = (contentHandler instanceof ExecutionLifecycleInitializable);
        isLifecycleCleanable = (contentHandler instanceof ExecutionLifecycleCleanable);
    }

    /**
     * Get the content handler.
     * @return The {@link ContentHandler}.
     */
    public T getContentHandler() {
        return contentHandler;
    }

    /**
     * Get the resource configuration.
     * @return The {@link SmooksResourceConfiguration}.
     */
    public SmooksResourceConfiguration getResourceConfig() {
        return resourceConfig;
    }

    /**
     * Does the ContentHandler implement {@link ExecutionLifecycleInitializable}.
     * @return True if the ContentHandler implements {@link ExecutionLifecycleInitializable}, otherwise false.
     */
    public boolean isLifecycleInitializable() {
        return isLifecycleInitializable;
    }

    /**
     * Does the ContentHandler implement {@link ExecutionLifecycleCleanable}.
     * @return True if the ContentHandler implements {@link ExecutionLifecycleCleanable}, otherwise false.
     */
    public boolean isLifecycleCleanable() {
        return isLifecycleCleanable;
    }
}
