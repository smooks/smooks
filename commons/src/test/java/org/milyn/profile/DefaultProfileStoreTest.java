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

import org.milyn.profile.DefaultProfileSet;
import org.milyn.profile.DefaultProfileStore;
import org.milyn.profile.UnknownProfileMemberException;

import junit.framework.TestCase;

/**
 * 
 * @author tfennelly
 */
public class DefaultProfileStoreTest extends TestCase {

	public DefaultProfileStoreTest(String arg0) {
		super(arg0);
	}

	public void testAddGetProfileSet() {
		ProfileStore store = new DefaultProfileStore();
		DefaultProfileSet set1 = new DefaultProfileSet("device1");
		DefaultProfileSet set2 = new DefaultProfileSet("device2");

		try {
			DefaultProfileStore.UnitTest.addProfileSet(store, null);
			fail("no IllegalArgumentException on null devicename");
		} catch (IllegalArgumentException e) {
		}

		DefaultProfileStore.UnitTest.addProfileSet(store, set1);
		DefaultProfileStore.UnitTest.addProfileSet(store, set2);
		try {
			store.getProfileSet("device3");
			fail("no UnknownProfileMemberException");
		} catch (UnknownProfileMemberException e) {
		}
		try {
			assertEquals(set1, store.getProfileSet("device1"));
		} catch (UnknownProfileMemberException e1) {
			fail("failed to get set");
		}
		try {
			assertEquals(set2, store.getProfileSet("device2"));
		} catch (UnknownProfileMemberException e1) {
			fail("failed to get set");
		}
		try {
			DefaultProfileStore.UnitTest.addProfileSet(store, set1);
			assertEquals(set1, store.getProfileSet("device2"));
		} catch (UnknownProfileMemberException e1) {
			fail("failed to get set");
		}
	}
}
