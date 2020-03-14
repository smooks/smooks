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

package org.smooks.profile;

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
    public String getBaseProfile();

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
	public boolean isMember(String profile);

	/**
	 * Add a profile to the ProfileSet.
	 * 
	 * @param profile
	 *            The profile to add.
	 */
	public abstract void addProfile(Profile profile);

	/**
	 * Get a profile from the {@link ProfileSet}.
	 * 
	 * @param profile
	 *            The name of the profile.
	 * @return The requested Profile, or null if the profile is not a member of
	 *         the {@link ProfileSet}.
	 */
	public abstract Profile getProfile(String profile);

	/**
	 * Get an {@link Iterator} to allow iteration over the
	 * {@link Profile Profiles}in this {@link ProfileSet}.
	 * 
	 * @return An {@link Iterator} that allows iteration over the
	 *         {@link Profile Profiles}in this {@link ProfileSet}.
	 */
	public abstract Iterator iterator();
}
