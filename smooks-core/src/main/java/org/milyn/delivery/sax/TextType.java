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
package org.milyn.delivery.sax;

/**
 * Text data type.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public enum TextType {
    /**
     * Raw text node.
     */
    TEXT,
    /**
     * CDATA Text node (&lt;![CDATA[<b>text</b>]]&gt;).
     */
    CDATA,
    /**
     * Comment Text node (&lt;!--<b>text</b>--&gt;).
     */
    COMMENT,
    /**
     * Character Entity Text node (&amp;xx;).
     */
    ENTITY,
}
