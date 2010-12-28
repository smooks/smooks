/*
 Milyn - Copyright (C) 2006 - 2010

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License (version 2.1) as published by the Free Software 
 Foundation.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 
 See the GNU Lesser General Public License for more details:    
 http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.profile;

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
    public void addProfileSet(ProfileSet profileSet);

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
    public ProfileSet getProfileSet(String baseProfile)
			throws UnknownProfileMemberException;
}
