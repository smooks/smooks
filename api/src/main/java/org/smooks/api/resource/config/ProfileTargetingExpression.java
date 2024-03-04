/*-
 * ========================LICENSE_START=================================
 * API
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.api.resource.config;

import org.smooks.api.profile.ProfileSet;

/**
 * Represents a single parsed profile targeting expression.
 * <p/>
 * The <b>target-profile</b> attribute can contain multiple comma seperated "profile targeting expressions" i.e.
 * a list of them.  See {@link ResourceConfig} docs.  This class represents
 * a single expression within a list of expressions.
 * <p/>
 * So, a single expression is composed of 1 or more "expression tokens" seperated by
 * "AND".  The expression arg to the constructor will be in one of
 * the following forms:
 * <ol>
 * 	<li>"profileX" i.e. a single entity.</li>
 * 	<li>"profileX AND profileY" i.e. a compound entity.</li>
 * 	<li>"profileX AND not:profileY" i.e. a compound entity.</li>
 * </ol>
 * Note, we only supports "AND" operations between the tokens, but a token can be
 * negated by prefixing it with "not:".
 * <p/>
 * See {@link ProfileTargetingExpression.ExpressionToken}.
 *
 * @author tfennelly
 */
public interface ProfileTargetingExpression {
    boolean isMatch(ProfileSet profileSet);

    double getSpecificity(ProfileSet profileSet);

    String getExpression();
}
