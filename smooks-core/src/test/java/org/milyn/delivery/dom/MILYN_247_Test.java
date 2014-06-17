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
package org.milyn.delivery.dom;

import junit.framework.TestCase;

import org.milyn.Smooks;
import org.milyn.FilterSettings;
import org.milyn.StreamFilterType;
import org.milyn.lang.LangUtil;
import org.milyn.payload.StringSource;
import org.milyn.payload.StringResult;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN_247_Test extends TestCase {

    public void test_MILYN_247_01() {
        if (LangUtil.getJavaVersion() != 1.5) {
            return;
        }

        Smooks smooks = new Smooks();

        smooks.setFilterSettings(new FilterSettings().setFilterType(StreamFilterType.DOM).setRewriteEntities(true));

        StringResult stringResult = new StringResult();
        smooks.filterSource(new StringSource("<a attrib=\"an &amp; 'attribute\">Bigger &gt; is better!</a>"), stringResult);
        assertEquals("<a attrib=\"an &amp; &apos;attribute\">Bigger &gt; is better!</a>", stringResult.getResult());
    }

    public void test_MILYN_247_02() {
        if (LangUtil.getJavaVersion() != 1.5) {
            return;
        }

        Smooks smooks = new Smooks();

        smooks.setFilterSettings(new FilterSettings().setFilterType(StreamFilterType.DOM).setRewriteEntities(false));

        StringResult stringResult = new StringResult();
        smooks.filterSource(new StringSource("<a attrib=\"an &amp; 'attribute\">Bigger &gt; is better!</a>"), stringResult);
        assertEquals("<a attrib=\"an & 'attribute\">Bigger &#62; is better!</a>", stringResult.getResult());
    }

    public void test_MILYN_247_03() {
        if (LangUtil.getJavaVersion() != 1.5) {
            return;
        }

        Smooks smooks = new Smooks();

        smooks.setFilterSettings(new FilterSettings().setFilterType(StreamFilterType.SAX).setRewriteEntities(true));

        StringResult stringResult = new StringResult();
        smooks.filterSource(new StringSource("<a attrib=\"an &amp; 'attribute\">Bigger &gt; is better!</a>"), stringResult);
        assertEquals("<a attrib=\"an &amp; &apos;attribute\">Bigger &gt; is better!</a>", stringResult.getResult());
    }

    public void test_MILYN_247_04() {
        if (LangUtil.getJavaVersion() != 1.5) {
            return;
        }

        Smooks smooks = new Smooks();

        smooks.setFilterSettings(new FilterSettings().setFilterType(StreamFilterType.SAX).setRewriteEntities(false));

        StringResult stringResult = new StringResult();
        smooks.filterSource(new StringSource("<a attrib=\"an &amp; 'attribute\">Bigger &gt; is better!</a>"), stringResult);
        assertEquals("<a attrib=\"an & 'attribute\">Bigger &#62; is better!</a>", stringResult.getResult());
    }
}
