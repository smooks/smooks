/*
 * Milyn - Copyright (C) 2006 - 2011
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

package org.milyn.delivery;

/**
 * {@link org.milyn.delivery.ContentDeliveryConfigBuilder} lifecycle events.
 */
public enum ContentDeliveryConfigBuilderLifecycleEvent {
    /**
     * First Event.
     * <p/>
     * Handlers created (and sorted), but the builder instance is not yet created.
     */
    HANDLERS_CREATED,
    /**
     * Second Event.
     * <p/>
     * The builder instance (for the profile) is now created and ready to be used.
     */
    CONFIG_BUILDER_CREATED
}
