/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.profile;

/**
 * ProfileStore interface.
 * <p/>
 * The ProfileStore stores and provides access to
 * all the ProfileSet instances. A ProfileSet is the set of profiles associated
 * with a given "profile member". A "profile member" can be a member of multiple
 * profiles. This is what "profiling" is all about - dicing and slicing entity
 * sets into different groups by assigning them to profiles.
 *
 * @author tfennelly
 */
public interface ProfileStore {

    /**
     * Add a ProfileSet to the store.
     *
     * @param profileSet  The ProfileSet.
     */
    void addProfileSet(ProfileSet profileSet);

    /**
     * Get the ProfileSet associated with the specified profile member. <p/> A
     * profile "member" can be a member of multiple profiles. This method should
     * return that list of profiles. <p/> Implementations must be case
     * insensitive.
     *
     * @param baseProfile The base profile name.
     * @return The ProfileSet for the specified base profile.
     * @throws UnknownProfileMemberException There's no {@link ProfileSet} for the specified base profile.
     */
    ProfileSet getProfileSet(String baseProfile)
			throws UnknownProfileMemberException;
}
