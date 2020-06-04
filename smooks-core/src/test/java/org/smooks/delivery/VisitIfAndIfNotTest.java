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

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.SmooksException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.annotation.VisitAfterIf;
import org.smooks.delivery.annotation.VisitBeforeIf;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class VisitIfAndIfNotTest {

	@Test
    public void test_sax_visitBefore() {
        SmooksResourceConfiguration resourceConfig;

        resourceConfig = new SmooksResourceConfiguration ();
        resourceConfig.setParameter("visitBefore", "true");
        assertTrue(VisitorConfigMap.visitBeforeAnnotationsOK(resourceConfig, new MySAXVisitBeforeVisitor1()));

        resourceConfig = new SmooksResourceConfiguration ();
        assertFalse(VisitorConfigMap.visitBeforeAnnotationsOK(resourceConfig, new MySAXVisitBeforeVisitor1()));

        resourceConfig = new SmooksResourceConfiguration ();
        resourceConfig.setParameter("visitBefore", "false");
        assertFalse(VisitorConfigMap.visitBeforeAnnotationsOK(resourceConfig, new MySAXVisitBeforeVisitor1()));
    }

	@Test
    public void test_sax_visitAfter() {
        SmooksResourceConfiguration resourceConfig;

        resourceConfig = new SmooksResourceConfiguration ();
        resourceConfig.setParameter("visitBefore", "true");
        assertFalse(VisitorConfigMap.visitAfterAnnotationsOK(resourceConfig, new MySAXVisitAfterVisitor1()));

        resourceConfig = new SmooksResourceConfiguration ();
        assertTrue(VisitorConfigMap.visitAfterAnnotationsOK(resourceConfig, new MySAXVisitAfterVisitor1()));

        resourceConfig = new SmooksResourceConfiguration ();
        resourceConfig.setParameter("visitBefore", "false");
        assertTrue(VisitorConfigMap.visitAfterAnnotationsOK(resourceConfig, new MySAXVisitAfterVisitor1()));
    }

    /* ====================================================================================================
             Test classes - visitor impls
       ==================================================================================================== */

    @VisitBeforeIf(condition = "parameters.containsKey('visitBefore') && parameters.visitBefore.value == 'true'")
    private class MySAXVisitBeforeVisitor1 implements SAXVisitBefore {
        public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        }
    }

    @VisitAfterIf(condition = "!parameters.containsKey('visitBefore') || parameters.visitBefore.value != 'true'")
    private class MySAXVisitAfterVisitor1 implements SAXVisitAfter {
        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        }
    }
}
