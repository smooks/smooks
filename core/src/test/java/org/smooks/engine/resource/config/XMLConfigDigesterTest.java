/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.engine.resource.config;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.lookup.ResourceConfigListsLookup;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the ArciveDef class.
 * @author tfennelly
 */
public class XMLConfigDigesterTest {
    
	@Test
    public void test_digestConfig_v20() throws SAXException, IOException, URISyntaxException {
        // Valid doc
        ResourceConfigSeq resList = XMLConfigDigester.digestConfig(getClass().getResourceAsStream("testconfig2.cdrl"), "test");

        assertResourceConfigOK(resList);

        // Check the profiles...
        List<ProfileSet> profiles = resList.getProfiles();
        assertEquals(2, profiles.size());
        assertEquals("profileA", profiles.get(0).getBaseProfile());
        assertTrue(profiles.get(0).isMember("profileA"));
        assertTrue(profiles.get(0).isMember("profile1"));
        assertTrue(profiles.get(0).isMember("profile2"));
        assertFalse(profiles.get(0).isMember("profile100"));
        assertEquals("profileB", profiles.get(1).getBaseProfile());
        assertTrue(profiles.get(1).isMember("profile3"));
        assertTrue(profiles.get(1).isMember("profileA"));
        assertFalse(profiles.get(1).isMember("profile1")); // not expanded
    }

	@Test
    public void test_profile_expansion() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.addConfigurations("testconfig2.cdrl", getClass().getResourceAsStream("testconfig2.cdrl"));
        assertProfilesOK(smooks);
    }

	@Test
    public void test_import_filesys() throws IOException, SAXException, URISyntaxException {
        Smooks smooks = new Smooks("src/test/java/org/smooks/engine/resource/config/testconfig3.cdrl");
        Iterator<ResourceConfigSeq> listIt = smooks.getApplicationContext().getRegistry().lookup(new ResourceConfigListsLookup()).iterator();
        ResourceConfigSeq list = null;

        while(listIt.hasNext()) {
            list = listIt.next();
        }

        assertResourceConfigOK(list);
    }

	@Test
    public void test_import_classpath() throws IOException, SAXException, URISyntaxException {
        Smooks smooks = new Smooks("/org/smooks/engine/resource/config/testconfig3.cdrl");
        Iterator<ResourceConfigSeq> listIt = smooks.getApplicationContext().getRegistry().lookup(new ResourceConfigListsLookup()).iterator();
        ResourceConfigSeq list = null;

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
        assertFalse(profileA.isMember("profileB"));
        assertFalse(profileA.isMember("profile3"));

        execContext = smooks.createExecutionContext("profileB");
        ProfileSet profileB = execContext.getTargetProfiles();
        assertTrue(profileB.isMember("profileB"));
        assertTrue(profileB.isMember("profile3"));
        assertTrue(profileB.isMember("profileA"));
        assertTrue(profileB.isMember("profile1"));
        assertTrue(profileB.isMember("profile2"));
    }

    private void assertResourceConfigOK(ResourceConfigSeq resList) {
        assertEquals(3, resList.size());

        // Test the overridden attribute values from the 1st config entry.
        assertEquals("smooks:a", resList.get(0).getSelectorPath().getSelector());
        assertEquals("xxx", resList.get(0).getProfileTargetingExpressions()[0].getExpression());
        assertEquals("x.txt", resList.get(0).getResource());
        assertEquals("https://www.smooks.org", resList.get(0).getSelectorPath().getNamespaces().getProperty("smooks"));

        // Test the default inherited attribute values from the 2nd config entry.
        assertEquals("smooks-default:b", resList.get(1).getSelectorPath().getSelector());
        assertEquals("yyy", resList.get(1).getProfileTargetingExpressions()[0].getExpression());
        assertEquals("/org/smooks/engine/resource/config/test-resource.txt", resList.get(1).getResource());
        assertEquals("Hi there :-)", new String(resList.get(1).getBytes()));
        assertEquals("https://www.smooks.org-default", resList.get(1).getSelectorPath().getNamespaces().getProperty("smooks-default"));

        // Test the parameters on the 2nd config entry.
        assertEquals("param1Val", resList.get(1).getParameterValue("param1", String.class));
        assertEquals("true", resList.get(1).getParameterValue("param2", String.class, "false"));
        assertEquals("false", resList.get(1).getParameterValue("param3", String.class, "true"));
        assertEquals("false", resList.get(1).getParameterValue("param4", String.class, "false"));

        // Test the 3rd config entry.
        assertEquals("abc", resList.get(2).getResourceType());
        assertEquals("Howya", new String(resList.get(2).getBytes()));
    }

    
}
