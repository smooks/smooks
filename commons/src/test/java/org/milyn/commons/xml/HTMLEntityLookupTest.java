// HTMLEntityLookupTest.java

package org.milyn.commons.xml;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * HTMLEntityLookupTest
 * <p/>
 * Relations: HTMLEntityLookup extends java.lang.Object <br>
 *
 * @author Tom Fennelly
 * @see org.milyn.xml.HTMLEntityLookup
 */

public class HTMLEntityLookupTest extends TestCase {


    /**
     * Constructor (needed for JTest)
     *
     * @param name Name of Object
     */
    public HTMLEntityLookupTest(String name) {
        super(name);
    }

    /**
     * Test method: java.lang.Character getCharacterCode(String)
     */
    public void testGetCharacterCode() {
        assertEquals('\u00A4', HTMLEntityLookup.getCharacterCode("curren").charValue());
        assertEquals('\u0026', HTMLEntityLookup.getCharacterCode("amp").charValue());
        assertEquals('\u00A0', HTMLEntityLookup.getCharacterCode("nbsp").charValue());
        assertEquals('\'', HTMLEntityLookup.getCharacterCode("apos").charValue());
        assertEquals('\u0022', HTMLEntityLookup.getCharacterCode("quot").charValue());
    }

    /**
     * Test method: String getEntityRef(char)
     */
    public void testGetEntityRef() {
        assertEquals("curren", HTMLEntityLookup.getEntityRef('\u00A4'));
        assertEquals("amp", HTMLEntityLookup.getEntityRef('\u0026'));
        assertEquals("nbsp", HTMLEntityLookup.getEntityRef('\u00A0'));
        assertEquals("apos", HTMLEntityLookup.getEntityRef('\''));
        assertEquals("quot", HTMLEntityLookup.getEntityRef('\u0022'));
    }

    /**
     * Main method needed to make a self runnable class
     *
     * @param args This is required for main method
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(HTMLEntityLookupTest.class));
    }
}
