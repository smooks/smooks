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
package org.smooks.engine.resource.visitor.ctrl;

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.sax.SAXVisitAfter;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

/**
 * Pause Smooks processing for a period.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Pause implements SAXVisitAfter, DOMVisitAfter {

    private long period = 1000;

    @Inject // Default pause of 1 second
    public Pause setPeriod(Optional<Long> period) {
        this.period = period.orElse(this.period);
        return this;
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        pause();
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        pause();
    }

    private void pause() {
        try {
            Thread.sleep(period);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
 
