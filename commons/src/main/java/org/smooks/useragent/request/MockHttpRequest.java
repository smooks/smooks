package org.smooks.useragent.request;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Mock object for a Http request.
 * 
 * @author Tom Fennelly
 */

public class MockHttpRequest implements HttpRequest {
	Hashtable<String, String> headers = new Hashtable<String, String>();

	Hashtable<String, String> params = new Hashtable<String, String>();

	public void setHeader(String header, String value) {
		headers.put(header, value);
	}

	public void setParameter(String parameter, String value) {
		params.put(parameter, value);
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public String getParameter(String name) {
		return params.get(name);
	}

    public Enumeration getParameterNames() {
        return params.keys();
    }

    public String[] getParameterValues(String name) {
        String value = getParameter(name);

        if(value != null) {
            return new String[] {value};
        } else {
            return null;
        }

    }

    public void reset() {
		headers.clear();
		params.clear();
	}
}
