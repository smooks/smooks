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
