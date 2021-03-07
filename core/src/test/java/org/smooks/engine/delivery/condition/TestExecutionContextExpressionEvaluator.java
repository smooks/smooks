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
package org.smooks.engine.delivery.condition;

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.expression.ExecutionContextExpressionEvaluator;
import org.smooks.api.expression.ExpressionEvaluationException;
import org.smooks.api.expression.ExpressionEvaluator;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TestExecutionContextExpressionEvaluator implements ExecutionContextExpressionEvaluator {

    public String condition;
    public boolean evalResult = true;
    public static ExecutionContext context;

    @Override
    public ExpressionEvaluator setExpression(String conditionExpression) throws SmooksConfigException {
        condition = conditionExpression;
        evalResult = conditionExpression.trim().equals("true");
        return this;
    }

    @Override
    public String getExpression() {
        return condition;
    }

    @Override
    public boolean eval(Object contextObject) throws ExpressionEvaluationException {
        return false;
    }

    @Override
    public Object getValue(Object contextObject) throws ExpressionEvaluationException {
        return null;
    }

    @Override
    public boolean eval(ExecutionContext context) {
        TestExecutionContextExpressionEvaluator.context = context;
        return evalResult;
    }

    @Override
    public Object getValue(ExecutionContext context) throws ExpressionEvaluationException {
        return null;
    }
}
