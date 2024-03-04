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

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Default ProfileSet implementation.
 *
 * @author tfennelly
 */
public class DefaultProfileSet extends LinkedHashMap<String, Profile> implements ProfileSet {

    private static final long serialVersionUID = 1L;
    private final String baseProfile;

    /**
     * Public constructor.
     *
     * @param baseProfile The base profile for the profile set.
     */
    public DefaultProfileSet(String baseProfile) {
        this.baseProfile = baseProfile;
    }

    public DefaultProfileSet(String baseProfile, String[] subProfiles) {
        this(baseProfile);
        addProfiles(subProfiles);
    }

    /**
     * Get the base profile for this profile set.
     *
     * @return Base profile name.
     */
    @Override
    public String getBaseProfile() {
        return baseProfile;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.smooks.useragent.profile.ProfileSet#isMember(java.lang.String)
     */
    @Override
    public boolean isMember(String profile) {
        if (profile == null) {
            throw new IllegalArgumentException(
                    "null 'profile' arg in method call.");
        }

        if (profile.equalsIgnoreCase(baseProfile)) {
            return true;
        } else {
            return containsKey(profile.trim());
        }
    }

    /**
     * Add profile to the ProfileSet.
     *
     * @param profile The profile to add.
     */
    public void addProfile(String profile) {
        addProfile(new BasicProfile(profile));
    }

    /**
     * Add profile to the ProfileSet.
     *
     * @param profile The profile to add.
     */
    @Override
    public void addProfile(Profile profile) {
        if (profile == null) {
            throw new IllegalArgumentException(
                    "null 'profile' arg in method call.");
        }

        put(profile.getName(), profile);
    }

    /**
     * Get a profile from the {@link ProfileSet}.
     *
     * @param profile The name of the profile.
     * @return The requested Profile, or null if the profile is not a member of
     * the {@link ProfileSet}.
     */
    @Override
    public Profile getProfile(String profile) {
        return get(profile);
    }

    /**
     * Get an {@link Iterator} to allow iteration over the
     * {@link Profile Profiles}in this {@link ProfileSet}.
     *
     * @return An {@link Iterator} that allows iteration over the
     * {@link Profile Profiles}in this {@link ProfileSet}.
     */
    @Override
    public Iterator<Profile> iterator() {
        return values().iterator();
    }

    /**
     * Add the profiles of the supplied DefaultProfileSet to this ProfileSet.
     *
     * @param profileSet The DefaultProfileSet whose profiles are to be added.
     */
    protected void addProfileSet(DefaultProfileSet profileSet) {
        if (profileSet == null) {
            throw new IllegalArgumentException(
                    "null 'profileSet' arg in method call.");
        }

        putAll(profileSet);
    }

    /**
     * Add a list of subProfiles to the ProfileSet.
     *
     * @param subProfiles The array of sub Profiles to add.
     */
    public void addProfiles(String[] subProfiles) {
        if (subProfiles == null) {
            // No sub profiles - that's OK
            return;
        }

        for (String subProfile : subProfiles) {
            addProfile(new BasicProfile(subProfile));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder setDescription = new StringBuilder();
        Iterator<String> iterator = keySet().iterator();

        while (iterator.hasNext()) {
            String profile = iterator.next();

            setDescription.append(profile);
            if (iterator.hasNext()) {
                setDescription.append(",");
            }
        }

        return setDescription.toString();
    }
}
