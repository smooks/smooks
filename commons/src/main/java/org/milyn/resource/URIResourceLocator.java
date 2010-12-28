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

package org.milyn.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.resource.ContainerResourceLocator;
import org.milyn.util.ClassUtil;

/**
 * {@link java.net.URI} resource locator.
 * <p/>
 * Loads resources from a {@link java.net.URI} i.e. "file://", "http://", "classpath:/" etc. <p/> Note,
 * it adds support for referencing classpath based resources through a
 * {@link java.net.URI} e.g. "classpath:/org/milyn/x/my-resource.xml" references
 * a "/org/milyn/x/my-resource.xml" resource on the classpath.
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
 * {@link #setBaseURI(java.net.URI)} method, or via the System property "org.milyn.resource.baseuri".
 * The default base URI is simply "./", which has no effect on the input URI when resolved against it.
 *
 * @author tfennelly
 */
public class URIResourceLocator implements ContainerResourceLocator {

    /**
     * Logger.
     */
    private static final Log logger = LogFactory.getLog(URIResourceLocator.class);

    /**
     * Scheme name for classpath based resources.
     */
    public static String SCHEME_CLASSPATH = "classpath";

    /**
     * System property key for the base URI. Defaults to "./".
     */
    public static final String BASE_URI_SYSKEY = "org.milyn.resource.baseuri";

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

        errorBuilder.append("\tFile System: " + fileUnresolved.getAbsolutePath() + "\n");
        if(scheme == null) {
            fileResolved = new File(uri.resolvedURI.getPath());
            errorBuilder.append("\tFile System: " + fileResolved.getAbsolutePath() + "\n");
        }

        if (fileUnresolved.exists()) {
            stream = new FileInputStream(fileUnresolved);
        } else if (fileResolved != null && fileResolved.exists()) {
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
            errorBuilder.append("\tClasspath: " + path + "\n");
            stream = ClassUtil.getResourceAsStream(path, getClass());
        } else {
            url = uri.resolvedURI.toURL();
            URLConnection connection = url.openConnection();

            errorBuilder.append("\tURL: " + url + "\n");
            stream = connection.getInputStream();
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
			logger.warn("Error extracting base URI.", e);
		}
    	
		return DEFAULT_BASE_URI;
	}

    private static class ResolvedURI {
        private String inputURI;
        private URI resolvedURI;

        private ResolvedURI(String inputURI, URI resolvedURI) {
            this.inputURI = inputURI;
            this.resolvedURI = resolvedURI;
        }
    }
}
