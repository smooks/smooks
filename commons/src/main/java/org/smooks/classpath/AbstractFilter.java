/*-
 * ========================LICENSE_START=================================
 * Commons
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
package org.smooks.classpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.support.ClassUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract classpath filter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
abstract class AbstractFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceOfFilter.class);

    private final List<Class> classes = new ArrayList<Class>();
    private String[] includeList = null;
    private String[] igrnoreList = defaultIgnoreList;
    private static final String[] defaultIgnoreList = new String[] {
            "charsets.jar", "javaws.jar", "jce.jar", "jsse.jar", "rt.jar", "dnsns.jar", "sunjce_provider.jar", "sunpkcs11.jar", "junit-", "servlet-api-", "idea_rt.jar",
            "java/", "javax/", "netscape/", "sun/", "com/sun", "org/omg", "org/xml", "org/w3c", "junit/", "org/apache/commons", "org/apache/log4j",
    };

    AbstractFilter() {
    }

    AbstractFilter(String[] ignoreList, String[] includeList) {
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
                LOGGER.debug("Resource '" + resourceName + "' presented to '" + InstanceOfFilter.class.getName() + "', but not loadable by classloader.  Ignoring.", throwable);
            }
        }
    }

    protected abstract boolean addClass(Class<?> clazz);

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
