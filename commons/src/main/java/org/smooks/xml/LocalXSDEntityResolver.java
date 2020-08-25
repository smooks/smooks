/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.xml;

import org.smooks.assertion.AssertArgument;

import javax.xml.transform.Source;

/**
 * XSD resolver for local XSD's.
 *
 * @author tfennelly
 */
public class LocalXSDEntityResolver extends LocalEntityResolver {

	/**
	 * XSD package for locating XSDs in the classpath.
	 */
	private static final String XSD_CP_PACKAGE = "/org/smooks/xsd/";
    /**
     * Schema sources for this entity resolver.
     */
    private final Source[] schemaSources;

    /**
	 * Public Constructor.
     * @param schemaSources Schema sources.
	 */
	public LocalXSDEntityResolver(Source[] schemaSources) {
        super(XSD_CP_PACKAGE);
        AssertArgument.isNotNull(schemaSources, "schemaSources");
        if(schemaSources.length == 0) {
            throw new IllegalArgumentException("Empty list of schemas supplied in arg 'schemaSources'.");
        }
        this.schemaSources = schemaSources;
        setDocType(schemaSources[0].getSystemId());
    }

    /**
     * Get the schema sources associated with this resolver instance.
     * @return The schema sources.
     */
    public Source[] getSchemaSources() {
        return schemaSources;
    }
}
