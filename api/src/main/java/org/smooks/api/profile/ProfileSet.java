/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api.profile;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Profile Set.
 * <p/>
 * A profile set consists of a "base" profile and a set of sub-profiles.  The base
 * profile fans out into a set of sub profiles.
 * 
 * @author tfennelly
 */
public interface ProfileSet extends Serializable {

    /**
     * Get the name of the base profile for this profile set.
     * @return The base profile name.
     */
    String getBaseProfile();

    /**
	 * Is the specified profile a member of this profile set.
     * <p/>
     * A profile is said to be a member of a profile set if it is the base
     * profile of the profile set, or one of its sub profiles.
	 * 
	 * @param profile
	 *            The profile to check against.
	 * @return True if the associated device a member of the specified profile,
	 *         otherwise false.
	 */
    boolean isMember(String profile);

	/**
	 * Add a profile to the ProfileSet.
	 * 
	 * @param profile
	 *            The profile to add.
	 */
    void addProfile(Profile profile);

	/**
	 * Get a profile from the {@link ProfileSet}.
	 * 
	 * @param profile
	 *            The name of the profile.
	 * @return The requested Profile, or null if the profile is not a member of
	 *         the {@link ProfileSet}.
	 */
    Profile getProfile(String profile);

	/**
	 * Get an {@link Iterator} to allow iteration over the
	 * {@link Profile Profiles}in this {@link ProfileSet}.
	 * 
	 * @return An {@link Iterator} that allows iteration over the
	 *         {@link Profile Profiles}in this {@link ProfileSet}.
	 */
    Iterator iterator();
}
