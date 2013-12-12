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
package org.milyn.commons.classpath;

import org.milyn.commons.util.ClassUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Abstract classpath filter.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
abstract class AbstractFilter implements Filter {

    private static Log logger = LogFactory.getLog(InstanceOfFilter.class);
    private List<Class> classes = new ArrayList<Class>();
    private String[] includeList = null;
    private String[] igrnoreList = defaultIgnoreList;
    private static String[] defaultIgnoreList = new String[] {
            "charsets.jar", "javaws.jar", "jce.jar", "jsse.jar", "rt.jar", "dnsns.jar", "sunjce_provider.jar", "sunpkcs11.jar", "junit-", "servlet-api-", "idea_rt.jar",  
            "java/", "javax/", "netscape/", "sun/", "com/sun", "org/omg", "org/xml", "org/w3c", "junit/", "org/apache/commons", "org/apache/log4j",
    };

    public AbstractFilter() {
    }

    public AbstractFilter(String[] ignoreList, String[] includeList) {
        if(ignoreList != null) {
            this.igrnoreList = ignoreList;
        }
        this.includeList = includeList;
    }

    public void filter(String resourceName) {
        if(resourceName.endsWith(".class") && !isIgnorable(resourceName)) {
            String className = ClasspathUtils.toClassName(resourceName);

            try {
                Class clazz = ClassUtil.forName(className, InstanceOfFilter.class);
                if(addClass(clazz)) {
                    classes.add(clazz);
                }
            } catch (Throwable throwable) {
                logger.debug("Resource '" + resourceName + "' presented to '" + InstanceOfFilter.class.getName() + "', but not loadable by classloader.  Ignoring.", throwable);
            }
        }
    }

    protected abstract boolean addClass(Class clazz);

    public boolean isIgnorable(String resourceName) {
        boolean isJar = resourceName.endsWith(".jar");

        if(includeList != null) {
            for(String include : includeList) {
                if(isJar && resourceName.startsWith(include)) {
                    return false;
                } else if(!isJar && (resourceName.length() < include.length() || resourceName.startsWith(include))) {
                    return false;
                }
            }
            return true;
        }

        for(String ignore : igrnoreList) {
            if(isJar && resourceName.startsWith(ignore)) {
                return true;
            } else if(!isJar && (resourceName.length() >= ignore.length() && resourceName.startsWith(ignore))) {
                return true;
            }
        }

        return false;
    }

    public List<Class> getClasses() {
        return classes;
    }
}
