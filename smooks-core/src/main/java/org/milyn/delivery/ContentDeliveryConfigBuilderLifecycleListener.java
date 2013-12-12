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

import org.milyn.commons.cdr.SmooksConfigurationException;

/**
 * Lifecycle listener for events fired during building of the {@link ContentDeliveryConfig}
 * for a profile.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface ContentDeliveryConfigBuilderLifecycleListener {

    /**
     * Event handler.
     *
     * @param event The event.
     * @throws SmooksConfigurationException Smooks configuration exception.
     */
    void handle(final ContentDeliveryConfigBuilderLifecycleEvent event) throws SmooksConfigurationException;
}
