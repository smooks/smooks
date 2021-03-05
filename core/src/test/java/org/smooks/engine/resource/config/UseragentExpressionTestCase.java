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
package org.smooks.engine.resource.config;

import org.junit.Test;
import org.smooks.api.resource.config.ProfileTargetingExpression;
import org.smooks.engine.profile.DefaultProfileSet;

import static org.junit.Assert.*;

public class UseragentExpressionTestCase {

	@Test
    public void testUseragentExpression() {
        DefaultProfileSet profileSet1 = new DefaultProfileSet("device1");
        DefaultProfileSet profileSet2 = new DefaultProfileSet("device2");
		ProfileTargetingExpression expression;

		// Add a few profiles
		profileSet1.addProfile("profile1");
		profileSet1.addProfile("profile2");
		profileSet2.addProfile("profile2");
		profileSet2.addProfile("accept:application/xhtml+xml");
		
		// Match against exact device name
		expression = new DefaultProfileTargetingExpression("device1");
		assertTrue(expression.isMatch(profileSet1));
		assertFalse(expression.isMatch(profileSet2));
		assertEquals(new Double(100.0), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));
		
		// Match against wildcard
		expression = new DefaultProfileTargetingExpression("*");
		assertTrue(expression.isMatch(profileSet1));
		assertTrue(expression.isMatch(profileSet2));
		assertEquals(new Double(5), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(5), new Double(expression.getSpecificity(profileSet2)));

		// Match against a profile
		expression = new DefaultProfileTargetingExpression("profile1");
		assertTrue(expression.isMatch(profileSet1));
		assertFalse(expression.isMatch(profileSet2));
		assertEquals(new Double(10), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));

		// Match against a profile and the device name
		expression = new DefaultProfileTargetingExpression("profile1 AND device1");
		assertTrue(expression.isMatch(profileSet1));
		assertFalse(expression.isMatch(profileSet2));
		assertEquals(new Double(110), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));

		// Match against 2 profiles
		expression = new DefaultProfileTargetingExpression("profile1 AND profile2");
		assertTrue(expression.isMatch(profileSet1));
		assertFalse(expression.isMatch(profileSet2));
		assertEquals(new Double(20), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));

		// Match against 1 profile and "not" a device.
		expression = new DefaultProfileTargetingExpression("profile2 AND not:device1");
		assertFalse(expression.isMatch(profileSet1));
		assertTrue(expression.isMatch(profileSet2));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(11), new Double(expression.getSpecificity(profileSet2)));

		// Match against 1 profile and "not" a profile.
		expression = new DefaultProfileTargetingExpression("accept:application/xhtml+xml AND not:profile1");
		assertFalse(expression.isMatch(profileSet1));
		assertTrue(expression.isMatch(profileSet2));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(11), new Double(expression.getSpecificity(profileSet2)));
	}
}
