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
import org.smooks.delivery.annotation.VisitAfterIf;
import org.smooks.delivery.annotation.VisitBeforeIf;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.delivery.dom.serialize.SerializationUnit;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.expression.MVELExpressionEvaluator;

public abstract class AbstractStreamDeliveryProvider implements StreamDeliveryProvider {

    protected boolean visitBeforeAnnotationsOK(SmooksResourceConfiguration resourceConfig, ContentHandler contentHandler) {
        Class<? extends ContentHandler> handlerClass = contentHandler.getClass();
        VisitBeforeIf visitBeforeIf = handlerClass.getAnnotation(VisitBeforeIf.class);

        if (visitBeforeIf != null) {
            MVELExpressionEvaluator conditionEval = new MVELExpressionEvaluator();

            conditionEval.setExpression(visitBeforeIf.condition());
            return conditionEval.eval(resourceConfig);
        }

        return true;
    }

    protected boolean visitAfterAnnotationsOK(SmooksResourceConfiguration resourceConfig, ContentHandler contentHandler) {
        Class<? extends ContentHandler> handlerClass = contentHandler.getClass();
        VisitAfterIf visitAfterIf = handlerClass.getAnnotation(VisitAfterIf.class);

        if (visitAfterIf != null) {
            MVELExpressionEvaluator conditionEval = new MVELExpressionEvaluator();

            conditionEval.setExpression(visitAfterIf.condition());
            return conditionEval.eval(resourceConfig);
        }

        return true;
    }

    protected boolean isSAXVisitor(ContentHandler contentHandler) {
        // Intentionally not checking for SAXVisitChildren.  Must be incorporated into a visit before or after...
        return (contentHandler instanceof SAXVisitBefore || contentHandler instanceof SAXVisitAfter);
    }

    protected boolean isDOMVisitor(ContentHandler contentHandler) {
        return (contentHandler instanceof DOMVisitBefore || contentHandler instanceof DOMVisitAfter || contentHandler instanceof SerializationUnit);
    }
}
