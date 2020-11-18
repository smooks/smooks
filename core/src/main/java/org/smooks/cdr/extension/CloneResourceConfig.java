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
package org.smooks.cdr.extension;

import org.smooks.SmooksException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Create a new {@link org.smooks.cdr.SmooksResourceConfiguration} by cloning the current resource.
 * <p/>
 * The cloned {@link org.smooks.cdr.SmooksResourceConfiguration} is added to the {@link org.smooks.cdr.extension.ExtensionContext}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CloneResourceConfig implements DOMElementVisitor {

    @Inject
    private Optional<String> resource;

    @Inject
    private Optional<String[]> unset;

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);
        SmooksResourceConfiguration config = (SmooksResourceConfiguration) extensionContext.getResourceStack().peek().clone();

        if(unset.isPresent()) {
	        for(String property : unset.get()) {
	            ResourceConfigUtil.unsetProperty(config, property);
	        }
        }

        resource.ifPresent(config::setResource);

        extensionContext.addResource(config);
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext.getExtensionContext(executionContext).getResourceStack().pop();
    }
}
