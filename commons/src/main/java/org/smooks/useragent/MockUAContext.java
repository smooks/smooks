/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.useragent;

import org.smooks.profile.BasicProfile;
import org.smooks.profile.DefaultProfileSet;
import org.smooks.profile.ProfileSet;

/**
 * Mock {@link org.smooks.useragent.UAContext} implemntation.
 * 
 * @author tfennelly
 */
public class MockUAContext implements UAContext {

	private static final long serialVersionUID = 1L;

	public String commonName;

	public DefaultProfileSet profileSet;

	public MockUAContext(String commonName) {
		this.commonName = commonName;
		profileSet = new DefaultProfileSet(commonName);
	}

	/**
	 * Add profile to the ProfileSet.
	 * 
	 * @param profile
	 *            The profile to add.
	 */
	public void addProfile(String profile) {
		profileSet.addProfile(new BasicProfile(profile));
	}

	/**
	 * Add a list of profiles to the ProfileSet.
	 * 
	 * @param profiles
	 *            The array of profiles to add.
	 */
	public void addProfiles(String[] profiles) {
		for (int i = 0; i < profiles.length; i++) {
			profileSet.addProfile(new BasicProfile(profiles[i]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smooks.useragent.UAContext#getCommonName()
	 */
	public String getCommonName() {
		return commonName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smooks.useragent.UAContext#getProfileSet()
	 */
	public ProfileSet getProfileSet() {
		return profileSet;
	}
}
