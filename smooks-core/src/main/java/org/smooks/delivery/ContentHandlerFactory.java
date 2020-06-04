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
package org.smooks.delivery;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.SmooksConfigurationException;


/**
 * ContentHandler factory interface.
 *
 * @author tfennelly
 */
public interface ContentHandlerFactory extends ContentHandler {

    /**
     * Name of the param used on a ContentHandlerFactory config that specifies
     * the resource type that the creator is adding support for.  This is different
     * from the type attribute on the resource element.  In the case of a ContentHandlerFactory
     * configuration, the ContentHandlerFactory impl resource type is "class", but it's adding
     * support for something else (e.g. "xsl").  This is why we can't use the type attribute for this
     * purpose.
     */
    public static final String PARAM_RESTYPE = "restype";

	/**
	 * Create the content handler instance.
	 * @param resourceConfig The SmooksResourceConfiguration for the {@link ContentHandler}
     * to be created.
	 * @return Content handler instance.
     * @throws SmooksConfigurationException Successfully created ContentHandler, but an error occured during configuration.
	 * @throws InstantiationException Unable to create ContentHandler instance.
	 */
	public Object create(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException, InstantiationException;
}
