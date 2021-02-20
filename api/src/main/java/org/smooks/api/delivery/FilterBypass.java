/*-
 * ========================LICENSE_START=================================
 * Smooks API
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
package org.smooks.api.delivery;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;

/**
 * Filter bypass interface.
 * <p/>
 * In some cases, the Smooks fragment filtering process (SAX/DOM) can be bypassed
 * if there is just a single visitor resource applied to the <i>#document</i>
 * fragment.  This interface allows a visitor to mark itself as such a visitor.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface FilterBypass {

	/**
	 * Bypass the Smooks Filter process.
	 * <p/>
	 * If the Filter bypass was not applied, the normal Smooks Fragment Filtering
	 * process will be proceed.
	 * 
	 * @param executionContext Smooks execution context.
	 * @param source Filter Source.
	 * @param result Filter Result.
	 * @return True of the bypass was applied, otherwise false.
	 * @throws SmooksException An error occurred while apply the bypass transform.
	 */
	boolean bypass(ExecutionContext executionContext, Source source, Result result) throws SmooksException;
}
