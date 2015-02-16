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

package org.milyn.cdr;

import org.milyn.profile.DefaultProfileSet;

import org.junit.Test;
import static org.junit.Assert.*;

public class UseragentExpressionTest {

	@Test
    public void testUseragentExpression() {
        DefaultProfileSet profileSet1 = new DefaultProfileSet("device1");
        DefaultProfileSet profileSet2 = new DefaultProfileSet("device2");
		ProfileTargetingExpression expression;

		// Add a few profiles
		profileSet1.addProfile("profile1");
		profileSet1.addProfile("profile2");
		profileSet2.addProfile("profile2");
		profileSet2.addProfile("accept:application/xhtml+xml");
		
		// Match against exact device name
		expression = new ProfileTargetingExpression("device1");
		assertTrue(expression.isMatch(profileSet1));
		assertTrue(!expression.isMatch(profileSet2));
		assertEquals(new Double(100.0), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));
		
		// Match against wildcard
		expression = new ProfileTargetingExpression("*");
		assertTrue(expression.isMatch(profileSet1));
		assertTrue(expression.isMatch(profileSet2));
		assertEquals(new Double(5), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(5), new Double(expression.getSpecificity(profileSet2)));

		// Match against a profile
		expression = new ProfileTargetingExpression("profile1");
		assertTrue(expression.isMatch(profileSet1));
		assertTrue(!expression.isMatch(profileSet2));
		assertEquals(new Double(10), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));

		// Match against a profile and the device name
		expression = new ProfileTargetingExpression("profile1 AND device1");
		assertTrue(expression.isMatch(profileSet1));
		assertTrue(!expression.isMatch(profileSet2));
		assertEquals(new Double(110), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));

		// Match against 2 profiles
		expression = new ProfileTargetingExpression("profile1 AND profile2");
		assertTrue(expression.isMatch(profileSet1));
		assertTrue(!expression.isMatch(profileSet2));
		assertEquals(new Double(20), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet2)));

		// Match against 1 profile and "not" a device.
		expression = new ProfileTargetingExpression("profile2 AND not:device1");
		assertTrue(!expression.isMatch(profileSet1));
		assertTrue(expression.isMatch(profileSet2));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(11), new Double(expression.getSpecificity(profileSet2)));

		// Match against 1 profile and "not" a profile.
		expression = new ProfileTargetingExpression("accept:application/xhtml+xml AND not:profile1");
		assertTrue(!expression.isMatch(profileSet1));
		assertTrue(expression.isMatch(profileSet2));
		assertEquals(new Double(0), new Double(expression.getSpecificity(profileSet1)));
		assertEquals(new Double(11), new Double(expression.getSpecificity(profileSet2)));
	}
}
