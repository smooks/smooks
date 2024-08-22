/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.io.source;

import org.smooks.assertion.AssertArgument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Filtration/Transformation {@link org.smooks.api.io.Source}.
 *
 * <h3 id="eventcontrol">Controlling Event Stream Generation</h3>
 * The Java Objects used to create this source can be used to generate a stream of SAX
 * events, which can then be analysed by Smooks in the normal manner.  Generation of the
 * SAX event stream can also be turned off via the {@link #setEventStreamRequired(boolean)}
 * method, or by configuring the 'http://www.smooks.org/sax/features/generate-java-event-stream'
 * feature on the reader in the Smoooks configuration as follows:
 *
 * <pre>
 * &lt;reader&gt;
 *     &lt;features&gt;
 *         &lt;setOff feature="http://www.smooks.org/sax/features/generate-java-event-stream" /&gt;
 *     &lt;/features&gt;
 * &lt;/reader&gt;
 * </pre>
 * <p>
 * Turning off event stream generation can make sense in many situations.  In some use cases event stream
 * generation may add no value and may just reduces performance e.g. where you simply wish to apply a
 * template to the supplied Java Object(s).
 * <p/>
 * Smooks must generate at least 1 event to which configured resources can be targeted (e.g. a templating
 * resource).  When event stream generation is turned off, Smooks simply generates what we call a "Null Source"
 * document event.  In XML, it could be represented as "&lt;nullsource-document/&gt;".  So when event stream
 * generation is turned off, simply target resources at "<b>nullsource-document</b>" or "<b>#document</b>".
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaSource extends FilterSource {

    /**
     *
     */
    public static final String FEATURE_GENERATE_EVENT_STREAM = "http://www.smooks.org/sax/features/generate-java-event-stream";

    private final List<Object> sourceObjects;
    private Map<String, Object> beans;
    private boolean eventStreamRequired = true;

    /**
     * Construct a JavaSource from the supplied source object.
     *
     * @param sourceObject The source object.
     */
    @SuppressWarnings("unchecked")
    public JavaSource(Object sourceObject) {
        AssertArgument.isNotNull(sourceObject, "sourceObject");
        sourceObjects = new ArrayList<Object>();
        sourceObjects.add(sourceObject);
        if (sourceObject instanceof Map) {
            beans = (Map<String, Object>) sourceObject;
        } else {
            beans = new HashMap<String, Object>();
            beans.put(toPropertyName(sourceObject.getClass().getSimpleName()), sourceObject);
        }
    }

    /**
     * Construct a JavaSource from the supplied source object.
     *
     * @param objectName   The object name the sourceObject is known under
     * @param sourceObject The source object.
     */
    public JavaSource(String objectName, Object sourceObject) {
        AssertArgument.isNotNull(sourceObject, "sourceObject");
        sourceObjects = new ArrayList<Object>();
        sourceObjects.add(sourceObject);
        beans = new HashMap<String, Object>();
        beans.put(objectName, sourceObject);
    }

    /**
     * Construct a JavaSource from the supplied source object.
     *
     * @param sourceObjects The source object list.
     */
    public JavaSource(List<Object> sourceObjects) {
        AssertArgument.isNotNull(sourceObjects, "sourceObjects");
        this.sourceObjects = sourceObjects;
        beans = new HashMap<String, Object>();
        beans.put("objects", sourceObjects);
    }

    /**
     * Is SAX event stream generation required for the processing of this JavaSource.
     * <p/>
     * Default is <code>true</code>.
     *
     * @return True if SAX event stream generation is required, otherwise false.
     */
    public boolean isEventStreamRequired() {
        return eventStreamRequired;
    }

    /**
     * Turn on/off SAX event stream generation for the processing of this JavaSource.
     *
     * @param eventStreamRequired True if SAX event stream generation is required, otherwise false.
     */
    public void setEventStreamRequired(boolean eventStreamRequired) {
        this.eventStreamRequired = eventStreamRequired;
    }

    /**
     * Get the source object list.
     *
     * @return The source object list.
     */
    public List<Object> getSourceObjects() {
        return sourceObjects;
    }

    /**
     * Get the input bean map for the transform.
     *
     * @return The bean map.
     */
    public Map<String, Object> getBeans() {
        return beans;
    }

    /**
     * Set the input bean map for the transform.
     *
     * @param beans The bean map.
     */
    public void setBeans(Map<String, Object> beans) {
        this.beans = beans;
    }

    private String toPropertyName(String simpleName) {
        StringBuilder stringBuilder = new StringBuilder(simpleName);
        int stringLength = stringBuilder.length();

        for (int i = 0; i < stringLength; i++) {
            char charAt = stringBuilder.charAt(i);
            if (Character.isLowerCase(charAt)) {
                break;
            }
            stringBuilder.setCharAt(i, Character.toLowerCase(charAt));
        }

        return stringBuilder.toString();
    }
}
