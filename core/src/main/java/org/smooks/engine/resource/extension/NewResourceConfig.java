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
package org.smooks.engine.resource.extension;

import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.resource.config.DefaultResourceConfigFactory;
import org.smooks.api.resource.config.ResourceConfigFactory;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.support.DomUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Create a new {@link ResourceConfig}.
 * <p/>
 * The new {@link ResourceConfig} is added to the {@link ExtensionContext}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NewResourceConfig implements DOMElementVisitor {

    public static final String PARAMETER_TARGET_PROFILE = "targetProfile";

    @Inject
    private Optional<String> resource;

    @Inject
    private ResourceConfigFactory resourceConfigFactory = new DefaultResourceConfigFactory();

    @Inject
    private Boolean isTemplate = false;

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext extensionContext = executionContext.get(ExtensionContext.EXTENSION_CONTEXT_TYPED_KEY);

        String targetProfile = DomUtils.getAttributeValue(element, PARAMETER_TARGET_PROFILE);
        if (targetProfile == null) {
            targetProfile = extensionContext.getDefaultProfile();
        }

        ResourceConfig resourceConfig = resourceConfigFactory.create(targetProfile, element);
        resourceConfig.setResource(resource.orElse(null));
        resourceConfig.getSelectorPath().setConditionEvaluator(extensionContext.getDefaultConditionEvaluator());

        if (isTemplate) {
            extensionContext.addResourceTemplate(resourceConfig);
        } else {
            extensionContext.addResource(resourceConfig);
        }
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        executionContext.get(ExtensionContext.EXTENSION_CONTEXT_TYPED_KEY).getResourceStack().pop();
    }
}
