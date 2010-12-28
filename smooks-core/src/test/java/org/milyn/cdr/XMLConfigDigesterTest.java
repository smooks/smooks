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

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.profile.ProfileSet;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

/**
 * Unit tests forthe ArciveDef class.
 * @author tfennelly
 */
public class XMLConfigDigesterTest extends TestCase {

	public void test_digestConfig_v10() throws SAXException, IOException, URISyntaxException {
		// Valid doc
        SmooksResourceConfigurationList resList = XMLConfigDigester.digestConfig(getClass().getResourceAsStream("testconfig1.cdrl"), "test");
        
        assertResourceConfigOK(resList);
	}

    public void test_digestConfig_v20() throws SAXException, IOException, URISyntaxException {
        // Valid doc
        SmooksResourceConfigurationList resList = XMLConfigDigester.digestConfig(getClass().getResourceAsStream("testconfig2.cdrl"), "test");

        assertResourceConfigOK(resList);

        // Check the profiles...
        List<ProfileSet> profiles = resList.getProfiles();
        assertEquals(2, profiles.size());
        assertEquals("profileA", profiles.get(0).getBaseProfile());
        assertTrue(profiles.get(0).isMember("profileA"));
        assertTrue(profiles.get(0).isMember("profile1"));
        assertTrue(profiles.get(0).isMember("profile2"));
        assertTrue(!profiles.get(0).isMember("profile100"));
        assertEquals("profileB", profiles.get(1).getBaseProfile());
        assertTrue(profiles.get(1).isMember("profile3"));
        assertTrue(profiles.get(1).isMember("profileA"));
        assertTrue(!profiles.get(1).isMember("profile1")); // not expanded
    }

    public void test_profile_expansion() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.addConfigurations("testconfig2.cdrl", getClass().getResourceAsStream("testconfig2.cdrl"));
        assertProfilesOK(smooks);
    }

    public void test_import_filesys() throws IOException, SAXException, URISyntaxException {
        Smooks smooks = new Smooks("src/test/java/org/milyn/cdr/testconfig3.cdrl");
        Iterator<SmooksResourceConfigurationList> listIt = smooks.getApplicationContext().getStore().getSmooksResourceConfigurationLists();
        SmooksResourceConfigurationList list = null;

        while(listIt.hasNext()) {
            list = listIt.next();
        }

        assertResourceConfigOK(list);
    }

    public void test_import_classpath() throws IOException, SAXException, URISyntaxException {
        Smooks smooks = new Smooks("/org/milyn/cdr/testconfig3.cdrl");
        Iterator<SmooksResourceConfigurationList> listIt = smooks.getApplicationContext().getStore().getSmooksResourceConfigurationLists();
        SmooksResourceConfigurationList list = null;

        while(listIt.hasNext()) {
            list = listIt.next();
        }

        assertResourceConfigOK(list);
    }

    private void assertProfilesOK(Smooks smooks) {
        ExecutionContext execContext;
        execContext = smooks.createExecutionContext("profileA");
        ProfileSet profileA = execContext.getTargetProfiles();
        assertTrue(profileA.isMember("profileA"));
        assertTrue(profileA.isMember("profile1"));
        assertTrue(profileA.isMember("profile2"));
        assertTrue(!profileA.isMember("profileB"));
        assertTrue(!profileA.isMember("profile3"));

        execContext = smooks.createExecutionContext("profileB");
        ProfileSet profileB = execContext.getTargetProfiles();
        assertTrue(profileB.isMember("profileB"));
        assertTrue(profileB.isMember("profile3"));
        assertTrue(profileB.isMember("profileA"));
        assertTrue(profileB.isMember("profile1"));
        assertTrue(profileB.isMember("profile2"));
    }

    private void assertResourceConfigOK(SmooksResourceConfigurationList resList) {
        assertEquals(3, resList.size());

        // Test the overridden attribute values from the 1st config entry.
        assertEquals("a", resList.get(0).getSelector());
        assertEquals("xxx", resList.get(0).getProfileTargetingExpressions()[0].getExpression());
        assertEquals("x.txt", resList.get(0).getResource());
        assertEquals("http://milyn.codehaus.org/smooks", resList.get(0).getSelectorNamespaceURI());

        // Test the default inherited attribute values from the 2nd config entry.
        assertEquals("b", resList.get(1).getSelector());
        assertEquals("yyy", resList.get(1).getProfileTargetingExpressions()[0].getExpression());
        assertEquals("/org/milyn/cdr/test-resource.txt", resList.get(1).getResource());
        assertEquals("Hi there :-)", new String(resList.get(1).getBytes()));
        assertEquals("http://milyn.codehaus.org/smooks-default", resList.get(1).getSelectorNamespaceURI());

        // Test the parameters on the 2nd config entry.
        assertEquals("param1Val", resList.get(1).getStringParameter("param1"));
        assertEquals(true, resList.get(1).getBoolParameter("param2", false));
        assertEquals(false, resList.get(1).getBoolParameter("param3", true));
        assertEquals(false, resList.get(1).getBoolParameter("param4", false));

        // Test the 3rd config entry.
        assertEquals("abc", resList.get(2).getResourceType());
        assertEquals("Howya", new String(resList.get(2).getBytes()));
    }

    
}
