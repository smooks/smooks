/*-
 * ========================LICENSE_START=================================
 * Smooks :: All
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
package org.smooks;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * SmooksServiceFactory is a {@link ServiceFactory} implementation that
 * enables the creation of per-bundle {@link SmooksOSGIFactory} implementations.
 * </p>
 * 
 * @author Daniel Bevenius
 *
 */
public class SmooksServiceFactory implements ServiceFactory
{
    /**
     * Will return a {@link SmooksOSGIFactory} instance configured with the {@link Bundle}. 
     * 
     * @param bundle the OSGi bundle.
     * @param registration the OSGi registration.
     * @return {@link SmooksOSGIFactory} that has been configured using the passed-in Bundle.
     * 
     */
    public Object getService(final Bundle bundle, final ServiceRegistration registration)
    {
        return new SmooksOSGIFactory(bundle);
    }

    public void ungetService(final Bundle bundle, final ServiceRegistration registration, final Object service)
    {
        //Noop
    }

}
