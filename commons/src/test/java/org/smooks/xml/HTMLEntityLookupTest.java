// HTMLEntityLookupTest.java

package org.smooks.xml;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * HTMLEntityLookupTest
 * <p>
 * Relations: HTMLEntityLookup extends java.lang.Object <br>
 *
 * @author Tom Fennelly
 * @see org.smooks.xml.HTMLEntityLookup
 */

public class HTMLEntityLookupTest {


	/**
	 * Test method: java.lang.Character getCharacterCode(String)
	 */
	@Test
	public void testGetCharacterCode() {
		assertEquals('\u00A4', HTMLEntityLookup.getCharacterCode("curren") .charValue());
		assertEquals('\u0026', HTMLEntityLookup.getCharacterCode("amp") .charValue());
		assertEquals('\u00A0', HTMLEntityLookup.getCharacterCode("nbsp") .charValue());
        assertEquals('\'', HTMLEntityLookup.getCharacterCode("apos").charValue());
        assertEquals('\u0022', HTMLEntityLookup.getCharacterCode("quot").charValue());
	}

	/**
	 * Test method: String getEntityRef(char)
	 */
	@Test
	public void testGetEntityRef() {
		assertEquals("curren", HTMLEntityLookup.getEntityRef('\u00A4'));
		assertEquals("amp", HTMLEntityLookup.getEntityRef('\u0026'));
		assertEquals("nbsp", HTMLEntityLookup.getEntityRef('\u00A0'));
        assertEquals("apos", HTMLEntityLookup.getEntityRef('\''));
		assertEquals("quot", HTMLEntityLookup.getEntityRef('\u0022'));
	}
}
