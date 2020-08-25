/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.util.ClassUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * {@link java.net.URI} resource locator.
 * <p/>
 * Loads resources from a {@link java.net.URI} i.e. "file://", "http://", "classpath:/" etc. <p/> Note,
 * it adds support for referencing classpath based resources through a
 * {@link java.net.URI} e.g. "classpath:/org/smooks/x/my-resource.xml" references
 * a "/org/smooks/x/my-resource.xml" resource on the classpath.
 * <p/>
 * This class resolves resources based on whether or not the requested resource {@link URI} has
 * a URI scheme specified.  If it has a scheme, it simply resolves the resource by creating a
 * {@link URL} instance from the URI and opening a stream on that URL.  If the URI doesn't have a scheme,
 * this class will attempt to resolve the resource against the local filesystem and classpath
 * (in that order).  In all cases (scheme or no scheme), the resource URI is first resolved
 * against base URI, with the resulting URI being the one that's used.
 * <p/>
 * As already stated, all resource URIs are
 * {@link URI#resolve(String) resolved} against a "base URI".  This base URI can be set through the
 * {@link #setBaseURI(java.net.URI)} method, or via the System property "org.smooks.resource.baseuri".
 * The default base URI is simply "./", which has no effect on the input URI when resolved against it.
 *
 * @author tfennelly
 */
public class URIResourceLocator implements ContainerResourceLocator {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(URIResourceLocator.class);

    /**
     * Scheme name for classpath based resources.
     */
    public static final String SCHEME_CLASSPATH = "classpath";

    /**
     * System property key for the base URI. Defaults to "./".
     */
    public static final String BASE_URI_SYSKEY = "org.smooks.resource.baseuri";

    public static final URI DEFAULT_BASE_URI = URI.create("./");

    private URI baseURI = getSystemBaseURI();

    public InputStream getResource(String configName, String defaultUri)
            throws IllegalArgumentException, IOException {
        return getResource(defaultUri);
    }

	public InputStream getResource(String uri) throws IllegalArgumentException, IOException {
        ResolvedURI resolvedURI = new ResolvedURI(uri, resolveURI(uri));

        return getResource(resolvedURI);
    }

    private InputStream getResource(ResolvedURI uri) throws IllegalArgumentException, IOException {
        URL url;
        String scheme = uri.resolvedURI.getScheme();
        InputStream stream;

        // Try the filesystem first, based on both the resolved and unresolved URIs,
        // ... then try the classpath (if there's no scheme or the scheme is "classpath"),
        // ... then try it as a URL...
        File fileUnresolved = new File(uri.inputURI);
        File fileResolved = null;
        StringBuilder errorBuilder = new StringBuilder();

        errorBuilder.append("\tFile System: ").append(fileUnresolved.getAbsolutePath()).append("\n");
        if(scheme == null) {
            fileResolved = new File(uri.resolvedURI.getPath());
            errorBuilder.append("\tFile System: ").append(fileResolved.getAbsolutePath()).append("\n");
        }
        
        boolean unresolvedExists = false;
        boolean resolvedExists = false;
        try {
        	unresolvedExists = fileUnresolved.exists();
        } catch (Exception e) {
        	// On GAE we will get a security exception
        }
        try {
        	resolvedExists = fileResolved.exists();
        } catch (Exception e) {
        	// On GAE we will get a security exception
        }
		if (unresolvedExists) {
            stream = new FileInputStream(fileUnresolved);
        } else if (fileResolved != null && resolvedExists) {
            stream = new FileInputStream(fileResolved);
        } else if (scheme == null || scheme.equals(SCHEME_CLASSPATH)) {
            String path = uri.resolvedURI.getPath();

            if (path == null || path.trim().equals("")) {
                throw new IllegalArgumentException("Unable to locate resource [" + uri +
                        "].  Resource path not specified in URI.");
            }
            if (path.charAt(0) != '/') {
                path = "/" + path;
            }
            errorBuilder.append("\tClasspath: ").append(path).append("\n");
            stream = ClassUtil.getResourceAsStream(path, getClass());
        } else {
            boolean isHttp = ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
            url = uri.resolvedURI.toURL();
            URLConnection connection = url.openConnection();
            errorBuilder.append("\tURL: ").append(url).append("\n");

            if (isHttp) {
                ((HttpURLConnection)connection).setInstanceFollowRedirects(false);
            }

            stream = connection.getInputStream();

            if (isHttp) {
                int responseCode = ((HttpURLConnection)connection).getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    if (stream != null) {
                        try {
                            stream.close();
                            stream = null;
                        } catch (IOException e) {
                            LOGGER.error("Error closing stream for failed resource read.", e);
                        }
                    }
                }
            }
        }

        if (stream == null) {
            throw new IOException("Failed to access data stream for resource [" + uri.inputURI + "]. Tried (in order):\n" + errorBuilder);
        }

        return stream;
    }

    /**
     * Resolve the supplied uri against the baseURI.
     * <p/>
     * Only resolved against the base URI if 'uri' is not absolute.
     *
     * @param uri URI to be resolved.
     * @return The resolved URI.
     */
    public URI resolveURI(String uri) {
        URI uriObj;

        if (uri == null || uri.trim().equals("")) {
            throw new IllegalArgumentException(
                    "null or empty 'uri' paramater in method call.");
        }

        if (uri.charAt(0) == '\\' || uri.charAt(0) == '/') {
            uri = uri.substring(1);
            return URI.create(uri);
        } else {
	        uriObj = URI.create(uri);
	        if (!uriObj.isAbsolute()) {
	            // Resolve the supplied URI against the baseURI...
	            uriObj = baseURI.resolve(uriObj);
	        }
        }

        return uriObj;
    }

    /**
     * Allows overriding of the baseURI (current dir).
     *
     * @param baseURI New baseURI.
     */
    public void setBaseURI(URI baseURI) {
        if (baseURI == null) {
            throw new IllegalArgumentException(
                    "null 'baseURI' arg in method call.");
        }
        String baseURIString = baseURI.toString();
        char lastChar = baseURIString.charAt(baseURIString.length() - 1);

        // Make sure the base URI refers to a directory
        if (lastChar != '/' && lastChar != '\\') {
            this.baseURI = URI.create(baseURIString + '/');
        } else {
            this.baseURI = baseURI;
		}
	}

    /**
     * Get the base URI for this locator instance.
     * @return The base URI for the locator instance.
     */
    public URI getBaseURI() {
    	return baseURI;
    }

    /**
     * Get the system defined base URI.
     * <p/>
     * Defined by the system property {@link #BASE_URI_SYSKEY}.
	 * @return System base URI.
	 */
	public static URI getSystemBaseURI() {
		return URI.create(System.getProperty(BASE_URI_SYSKEY, "./"));
	}
    
    /**
     * Extract the base URI from the supplied resource URI.
     * @param resourceURI The resource URI.
     * @return The base URI for the supplied resource URI.
     */
    public static URI extractBaseURI(String resourceURI) {
        URI uri = URI.create(resourceURI);
		return extractBaseURI(uri);
    }

    /**
     * Extract the base URI from the supplied resource URI.
     * @param resourceURI The resource URI.
     * @return The base URI for the supplied resource URI.
     */
	public static URI extractBaseURI(URI resourceURI) {
		File resFile = new File(resourceURI.getPath());
        
        try {
        	File configFolder = resFile.getParentFile();
        	if(configFolder != null) {
        		return new URI(resourceURI.getScheme(), resourceURI.getUserInfo(), resourceURI.getHost(), resourceURI.getPort(), configFolder.getPath().replace('\\', '/'), resourceURI.getQuery(), resourceURI.getFragment());
        	}
		} catch (URISyntaxException e) {
			LOGGER.debug("Error extracting base URI.", e);
		}
    	
		return DEFAULT_BASE_URI;
	}

    private static class ResolvedURI {
        private final String inputURI;
        private final URI resolvedURI;

        private ResolvedURI(String inputURI, URI resolvedURI) {
            this.inputURI = inputURI;
            this.resolvedURI = resolvedURI;
        }
    }
}
