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

package org.smooks.edisax.v1_1.groups;

import org.smooks.edisax.AbstractEDIParserTestCase;

import java.io.IOException;

/**
 * Test for http://jira.codehaus.org/browse/MILYN-441:
 * SegmentGroup fails when only first segment exists in message.
 * 
 * @author bardl
 */
public class MILYN_441_Test extends AbstractEDIParserTestCase {
    
    public void test_groups_08() throws IOException {
        test("test_groups_08");
    }
    
    public void test_groups_09() throws IOException {
        test("test_groups_09");
    }
}