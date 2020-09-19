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
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.annotation.VisitAfterIf;
import org.smooks.delivery.annotation.VisitBeforeIf;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXStreamDeliveryProvider;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class VisitIfAndIfNotTest {

    private SAXStreamDeliveryProvider saxStreamDeliveryProvider = new SAXStreamDeliveryProvider();

    @Test
    public void test_sax_visitBefore() {
        assertTrue(saxStreamDeliveryProvider.visitBeforeAnnotationsOK(new MySAXVisitBeforeVisitor1("true")));

        assertFalse(saxStreamDeliveryProvider.visitBeforeAnnotationsOK(new MySAXVisitBeforeVisitor1(null)));
        
        assertFalse(saxStreamDeliveryProvider.visitBeforeAnnotationsOK(new MySAXVisitBeforeVisitor1("false")));
    }

    @Test
    public void test_sax_visitAfter() {
        assertFalse(saxStreamDeliveryProvider.visitAfterAnnotationsOK(new MySAXVisitAfterVisitor1("true")));

        assertTrue(saxStreamDeliveryProvider.visitAfterAnnotationsOK(new MySAXVisitAfterVisitor1(null)));
        
        assertTrue(saxStreamDeliveryProvider.visitAfterAnnotationsOK(new MySAXVisitAfterVisitor1("false")));
    }

    /* ====================================================================================================
             Test classes - visitor impls
       ==================================================================================================== */

    @VisitBeforeIf(condition = "visitBefore == 'true'")
    public static class MySAXVisitBeforeVisitor1 implements SAXVisitBefore {
        private String visitBefore;
        
        public MySAXVisitBeforeVisitor1(String visitBefore) {
            this.visitBefore = visitBefore;
        }
        
        public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        }

        public String getVisitBefore() {
            return visitBefore;
        }
    }

    @VisitAfterIf(condition = "visitBefore != 'true'")
    public static class MySAXVisitAfterVisitor1 implements SAXVisitAfter {
        private String visitBefore;

        public MySAXVisitAfterVisitor1(String visitBefore) {
            this.visitBefore = visitBefore;
        }
        
        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        }

        public String getVisitBefore() {
            return visitBefore;
        }
    }
}
