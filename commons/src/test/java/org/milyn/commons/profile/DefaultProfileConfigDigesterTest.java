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

package org.milyn.commons.profile;

import junit.framework.TestCase;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author tfennelly
 */
public class DefaultProfileConfigDigesterTest extends TestCase {

    /**
     * @param arg0
     */
    public DefaultProfileConfigDigesterTest(String arg0) {
        super(arg0);
    }

    public void testParse_exception_null_stream() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();

        try {
            digester.parse(null);
            fail("failed to throw arg exception on null stream");
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_fail_no_name() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_fail_no_name.xml");

        try {
            digester.parse(stream);
            fail("failed to throw SAXException on no name");
        } catch (SAXException e) {
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_fail_no_list() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_fail_no_list.xml");

        try {
            digester.parse(stream);
            fail("failed to throw SAXException on no list");
        } catch (SAXException e) {
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_fail_no_attribs() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_fail_no_attribs.xml");

        try {
            digester.parse(stream);
            fail("failed to throw SAXException on no attributes");
        } catch (SAXException e) {
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_fail_empty_list() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_fail_empty_list.xml");

        try {
            digester.parse(stream);
            fail("failed to throw SAXException on empty list");
        } catch (SAXException e) {
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_fail_empty_name() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_fail_empty_name.xml");

        try {
            digester.parse(stream);
            fail("failed to throw SAXException on empty name");
        } catch (SAXException e) {
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_fail_bad_list() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_fail_bad_list.xml");

        try {
            digester.parse(stream);
            fail("failed to throw SAXException on bad list");
        } catch (SAXException e) {
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_fail_no_profile() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_fail_no_profile.xml");

        try {
            digester.parse(stream);
            fail("failed to throw SAXException on no profile");
        } catch (SAXException e) {
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testParse_success_all_list_seperators() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_success_all_list_separators.xml");

        try {
            digester.parse(stream);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }
    }

    public void testParse_success_two_profiles() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_success_two_profiles.xml");
        ProfileStore store = null;

        try {
            store = digester.parse(stream);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }
        try {
            ProfileSet device = store.getProfileSet("MSIE5");
            assertTrue(device.isMember("html"));
            assertTrue(!device.isMember("wml"));
        } catch (UnknownProfileMemberException e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }
        try {
            ProfileSet device = store.getProfileSet("EricssonA2618");
            assertTrue(device.isMember("wml"));
            assertTrue(!device.isMember("html"));
        } catch (UnknownProfileMemberException e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }
    }

    public void testParse_success_one_profile() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_success_one_profile.xml");
        ProfileStore store = null;

        try {
            store = digester.parse(stream);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }

        try {
            ProfileSet MSIE5 = store.getProfileSet("MSIE5");
            assertTrue(MSIE5.isMember("html"));
            assertTrue(!MSIE5.isMember("wml"));
        } catch (UnknownProfileMemberException e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }
    }

    public void testParse_success_many_profiles() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_success_many_profiles.xml");
        ProfileStore store = null;

        try {
            store = digester.parse(stream);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }

        try {
            ProfileSet MSIE5 = store.getProfileSet("MSIE5");
            assertTrue(MSIE5.isMember("html"));
            assertTrue(MSIE5.isMember("css-enabled"));
            assertTrue(MSIE5.isMember("msie"));
            assertTrue(MSIE5.isMember("large"));
            assertTrue(MSIE5.isMember("html4"));
            assertTrue(!MSIE5.isMember("wml"));
            assertTrue(!MSIE5.isMember("wml"));
            assertTrue(!MSIE5.isMember("PDA"));
        } catch (UnknownProfileMemberException e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }
    }

    public void testParse_success_many_profiles_nested() {
        DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
        InputStream stream = getClass().getResourceAsStream(
                "profiles_success_many_profiles_nested.xml");
        ProfileStore store = null;

        try {
            store = digester.parse(stream);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception: " + e.getMessage());
        }
    }
}
