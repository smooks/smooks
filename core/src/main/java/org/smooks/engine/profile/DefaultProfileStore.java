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
package org.smooks.engine.profile;

import org.smooks.api.profile.Profile;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.profile.ProfileStore;
import org.smooks.api.profile.UnknownProfileMemberException;
import org.smooks.assertion.AssertArgument;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default ProfileStore implementation.
 * 
 * @author tfennelly
 */
public class DefaultProfileStore implements ProfileStore {

    /**
	 * The store table.
	 */
	private final Map<String, ProfileSet> store = new ConcurrentHashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smooks.useragent.profile.ProfileStore#getProfileSet(java.lang.String)
	 */
	@Override
	public ProfileSet getProfileSet(String profileMember) throws UnknownProfileMemberException {
		ProfileSet set;

		assertProfileMemberNameOK(profileMember);

		set = store.get(profileMember.trim());
		if (set == null) {
			throw new UnknownProfileMemberException("Failed to get ProfileSet.  Unknown profile member name [" + profileMember + "]");
		}

		return set;
	}

	/**
	 * Add a ProfileSet for the named profile member.
	 * 
	 * @param profileSet
	 *            The ProfileSet.
	 */
	@Override
	public void addProfileSet(ProfileSet profileSet) {
        AssertArgument.isNotNull(profileSet, "profileSet");

        assertProfileMemberNameOK(profileSet.getBaseProfile());
		if (profileSet == null) {
			throw new IllegalArgumentException(
					"null 'profileSet' arg in method call.");
		}
		if (!(profileSet instanceof DefaultProfileSet)) {
			throw new IllegalArgumentException(
					"'profileSet' arg must be an instanceof DefaultProfileSet.");
		}

		store.put(profileSet.getBaseProfile().trim(), profileSet);
        expandProfiles();
    }

    /**
     * Expand out the profile sets in this profile store.
     * <p/>
     * Expanding means taking sub-profiles that are in themselves the base profile
     * of another profile set, and inserting the sub-profiles into the profile set.
     */
    public void expandProfiles() {
		for (Map.Entry<String, ProfileSet> profileSetEntry : store.entrySet()) {
			DefaultProfileSet profileSet = (DefaultProfileSet) profileSetEntry.getValue();
			Iterator<Profile> iterator = profileSet.values().iterator();
			List<DefaultProfileSet> addOns = new ArrayList<>();

			while (iterator.hasNext()) {
				Profile profile = iterator.next();

				try {
					DefaultProfileSet addOnProfileSet = (DefaultProfileSet) getProfileSet(profile.getName());
					if (addOnProfileSet != null) {
						addOns.add(addOnProfileSet);
					}
				} catch (UnknownProfileMemberException e) {
					// Ignore - not an expandable profile
				}
			}
			// perfomed after the above iteration simply to avoid concurrent mod
			// exceptions
			// on the ProfileSet.
			for (DefaultProfileSet addOn : addOns) {
				profileSet.addProfileSet(addOn);
			}
		}
	}

	/**
	 * Profile member name String assertion method.
	 * 
	 * @param profileMember
	 *            The profile member name String.
	 * @throws IllegalArgumentException
	 *             If the deviceName is null or empty.
	 */
	private void assertProfileMemberNameOK(String profileMember)
			throws IllegalArgumentException {
		AssertArgument.isNotNullAndNotEmpty(profileMember, "profileMember");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder storeDescription = new StringBuilder();

		for (Map.Entry<String, ProfileSet> entry : store.entrySet()) {
			String profileMemberName = entry.getKey();
			ProfileSet profileSet = entry.getValue();

			storeDescription.append(profileMemberName).append(": ").append(
					profileSet).append("\r\n");
		}

		return storeDescription.toString();
	}

	/**
	 * Unit testing static inner to provide access.
	 *
	 * @author tfennelly
	 */
	static class UnitTest {
		public static void addProfileSet(ProfileStore store, ProfileSet profileSet) {
			store.addProfileSet(profileSet);
		}
	}
}
