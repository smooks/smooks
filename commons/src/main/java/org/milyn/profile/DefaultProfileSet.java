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

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Default ProfileSet implementation.
 * 
 * @author tfennelly
 */
public class DefaultProfileSet extends LinkedHashMap implements ProfileSet {

	private static final long serialVersionUID = 1L;
    private String baseProfile;

    /**
     * Public constructor.
     * @param baseProfile The base profile for the profile set.
     */
    public DefaultProfileSet(String baseProfile) {
        this.baseProfile = baseProfile;
    }

    /**
     * Get the base profile for this profile set.
     * @return Base profile name.
     */
    public String getBaseProfile() {
        return baseProfile;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.milyn.useragent.profile.ProfileSet#isMember(java.lang.String)
	 */
	public boolean isMember(String profile) {
		if (profile == null) {
			throw new IllegalArgumentException(
					"null 'profile' arg in method call.");
		}

        if(profile.equalsIgnoreCase(baseProfile)) {
            return true;
        } else {
            return containsKey(profile.trim());
        }
    }

	/**
	 * Add profile to the ProfileSet.
	 * 
	 * @param profile
	 *            The profile to add.
	 */
	public void addProfile(String profile) {
		addProfile(new BasicProfile(profile));
	}

	/**
	 * Add profile to the ProfileSet.
	 * 
	 * @param profile
	 *            The profile to add.
	 */
	@SuppressWarnings("unchecked")
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
	 * @param profile
	 *            The name of the profile.
	 * @return The requested Profile, or null if the profile is not a member of
	 *         the {@link ProfileSet}.
	 */
	public Profile getProfile(String profile) {
		return (Profile) get(profile);
	}

	/**
	 * Get an {@link Iterator} to allow iteration over the
	 * {@link Profile Profiles}in this {@link ProfileSet}.
	 * 
	 * @return An {@link Iterator} that allows iteration over the
	 *         {@link Profile Profiles}in this {@link ProfileSet}.
	 */
	public Iterator iterator() {
		return values().iterator();
	}

	/**
	 * Add the profiles of the supplied DefaultProfileSet to this ProfileSet.
	 * 
	 * @param profileSet
	 *            The DefaultProfileSet whose profiles are to be added.
	 */
	@SuppressWarnings("unchecked")
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
     * @param subProfiles
     *            The array of sub Profiles to add.
     */
    public void addProfiles(String[] subProfiles) {
        if(subProfiles == null) {
            // No sub profiles - that's OK
            return;
        }

        for (int i = 0; i < subProfiles.length; i++) {
            addProfile(new BasicProfile(subProfiles[i]));
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer setDescription = new StringBuffer();
		Iterator iterator = keySet().iterator();

		while (iterator.hasNext()) {
			String profile = (String) iterator.next();

			setDescription.append(profile);
			if (iterator.hasNext()) {
				setDescription.append(",");
			}
		}

		return setDescription.toString();
	}

    /**
     * Utility method for creating a profile set.
     * @param baseProfile The base profile.
     * @param subProfiles The sub profiles.
     * @return The profile set.
     */
    public static DefaultProfileSet create(String baseProfile, String[] subProfiles) {
        DefaultProfileSet profileSet = new DefaultProfileSet(baseProfile);
        profileSet.addProfiles(subProfiles);
        return profileSet;
    }
}
