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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.milyn.assertion.AssertArgument;

/**
 * Default ProfileStore implementation.
 * 
 * @author tfennelly
 */
public class DefaultProfileStore implements ProfileStore {

    /**
	 * The store table.
	 */
	private Hashtable<String, ProfileSet> store = new Hashtable<String, ProfileSet>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.milyn.useragent.profile.ProfileStore#getProfileSet(java.lang.String)
	 */
	public ProfileSet getProfileSet(String profileMember)
			throws UnknownProfileMemberException {
		ProfileSet set = null;

		assertProfileMemberNameOK(profileMember);

		set = store.get(profileMember.trim());
		if (set == null) {
			throw new UnknownProfileMemberException(
					"Failed to get ProfileSet.  Unknown profile member name ["
							+ profileMember + "]");
		}

		return set;
	}

	/**
	 * Add a ProfileSet for the named profile member.
	 * 
	 * @param profileSet
	 *            The ProfileSet.
	 */
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
		Iterator storeIterator = store.entrySet().iterator();

		while (storeIterator.hasNext()) {
			Map.Entry entry = (Map.Entry) storeIterator.next();
			DefaultProfileSet profileSet = (DefaultProfileSet) entry.getValue();
			Iterator iterator = profileSet.values().iterator();
			Vector<DefaultProfileSet> addOns = new Vector<DefaultProfileSet>();

			while (iterator.hasNext()) {
				Profile profile = (Profile) iterator.next();

				try {
					DefaultProfileSet addOnProfileSet = (DefaultProfileSet) getProfileSet(profile
							.getName());
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
			for (int i = 0; i < addOns.size(); i++) {
				profileSet.addProfileSet((DefaultProfileSet) addOns.get(i));
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
	public String toString() {
		StringBuffer storeDescription = new StringBuffer();
		Iterator iterator = store.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String profileMemberName = (String) entry.getKey();
			ProfileSet profileSet = (ProfileSet) entry.getValue();

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
