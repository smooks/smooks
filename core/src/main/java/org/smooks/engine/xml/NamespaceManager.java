/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.xml;

import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.ApplicationContext;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.TypedKey;
import org.smooks.engine.lookup.NamespaceManagerLookup;
import org.smooks.engine.lookup.ResourceConfigSeqsLookup;
import org.smooks.namespace.NamespaceDeclarationStack;

import jakarta.annotation.PostConstruct;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Properties;

/**
 * Namespace Mappings.
 * <p/>
 * This handler loads namespace prefix-to-uri mappings into the {@link ApplicationContext}.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class NamespaceManager {

    public static final TypedKey<NamespaceDeclarationStack> NAMESPACE_DECLARATION_STACK_TYPED_KEY = TypedKey.of();

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceManager.class);

    @Inject
    private ResourceConfig resourceConfig;

    @Inject
    private ApplicationContext applicationContext;

    /**
     * Load the namespace prefix-to-uri mappings into the {@link ApplicationContext}.
     */
    @PostConstruct
    public void postConstruct() throws SAXPathException {
        final Properties newNamespaces = resourceConfig.toProperties();

        for (ResourceConfigSeq resourceConfigSeq : applicationContext.getRegistry().lookup(new ResourceConfigSeqsLookup())) {
            for (int i = 0; i < resourceConfigSeq.size(); i++) {
                resourceConfigSeq.get(i).getSelectorPath().setNamespaces(newNamespaces);
            }
        }

        LOGGER.debug("Adding namespace prefix-to-uri mappings: " + newNamespaces);
        final Optional<Properties> currentNamespaces = applicationContext.getRegistry().lookup(new NamespaceManagerLookup());
        if (currentNamespaces.isPresent()) {
            currentNamespaces.get().putAll(newNamespaces);
        } else {
            applicationContext.getRegistry().registerObject(NamespaceManager.class, newNamespaces);
        }
    }
}
