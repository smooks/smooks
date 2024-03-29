/*-
 * ========================LICENSE_START=================================
 * Commons
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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * DTD resolver for local DTD's.
 *
 * @author tfennelly
 */
public class LocalDTDEntityResolver extends LocalEntityResolver {

    /**
     * DTD package for locating DTDs in the classpath.
     */
    private static final String DTD_CP_PACKAGE = "/org/smooks/dtd/";

    /**
     * Public default Constructor
     */
    public LocalDTDEntityResolver() {
        super(DTD_CP_PACKAGE);
    }

    /**
     * Public default Constructor <p/> This constructor allows specification of
     * a local file system folder from which DTDs can be loaded.
     *
     * @param localDTDFolder Local DTD folder.
     */
    public LocalDTDEntityResolver(File localDTDFolder) {
        super(localDTDFolder);
    }


    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        setDocType(systemId);
        return super.resolveEntity(publicId, systemId);
    }
}
