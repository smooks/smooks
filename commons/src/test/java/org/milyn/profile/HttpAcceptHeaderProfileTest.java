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

import org.milyn.profile.HttpAcceptHeaderProfile;

import org.junit.Test;
import static org.junit.Assert.*;

public class HttpAcceptHeaderProfileTest {

	@Test
	public void testHttpAcceptHeaderProfile() {
		HttpAcceptHeaderProfile profile = new HttpAcceptHeaderProfile(
				"text/plain", new String[] { "q=0.9", "", " level=1 " });

		assertEquals("Invalid profile name.", "accept:text/plain", profile
				.getName());
		if (profile.getParamNumeric("q", 1.0) != 0.9) {
			fail("Invalid qvalue param value.");
		}
		if ((int) profile.getParamNumeric("level", 2.0) != 1) {
			fail("Invalid level param value.");
		}
		if (profile.getParamNumeric("xxx", 2.0) != 2.0) {
			fail("Invalid level param value.");
		}
	}
}
