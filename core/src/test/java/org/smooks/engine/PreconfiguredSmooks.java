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
package org.smooks.engine;

import java.io.IOException;

import org.smooks.Smooks;
import org.smooks.support.SmooksUtil;
import org.smooks.engine.profile.DefaultProfileSet;
import org.xml.sax.SAXException;

public class PreconfiguredSmooks extends Smooks {

	/**
	 * Public Constructor.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public PreconfiguredSmooks() throws SAXException, IOException {
        SmooksUtil.registerProfileSet(new DefaultProfileSet("msie6w", new String[] {"msie6", "html4", "html"}), this);
        SmooksUtil.registerProfileSet(new DefaultProfileSet("msie6m", new String[] {"msie6", "html4", "html"}), this);
        SmooksUtil.registerProfileSet(new DefaultProfileSet("msie6", new String[] {"html4", "html"}), this);
        SmooksUtil.registerProfileSet(new DefaultProfileSet("firefox", new String[] {"html4", "html"}), this);

        addConfigurations("/org/smooks/parameters.cdrl", getClass().getResourceAsStream("/org/smooks/parameters.cdrl"));
        addConfigurations("/org/smooks/test.cdrl", getClass().getResourceAsStream("/org/smooks/test.cdrl"));
	}

}
