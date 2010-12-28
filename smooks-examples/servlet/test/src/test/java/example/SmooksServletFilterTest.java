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
package example;

import com.meterware.httpunit.*;
import junit.framework.*;
import org.milyn.io.*;
import org.xml.sax.*;

import java.io.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksServletFilterTest extends TestCase {

    public void test_firefox() throws IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest("http://localhost:19191/smooks-test/index.html");

        req.setHeaderField("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11");
        WebResponse resp = wc.getResponse(req);

        InputStream expected = getClass().getResourceAsStream("test-exp-firefox.xml");
        InputStream actual = new ByteArrayInputStream(resp.getText().getBytes());
        assertTrue(StreamUtils.compareCharStreams(expected, actual));
    }

    public void x_test_msie() throws IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest("http://localhost:19191/smooks-test/index.html");

        req.setHeaderField("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
        WebResponse resp = wc.getResponse(req);

        InputStream expected = getClass().getResourceAsStream("test-exp-msie.xml");
        InputStream actual = new ByteArrayInputStream(resp.getText().getBytes());
        assertTrue("Comparison failed.  Got: " + resp, StreamUtils.compareCharStreams(expected, actual));
    }
}
