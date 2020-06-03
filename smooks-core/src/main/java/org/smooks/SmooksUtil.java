/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks;

import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.profile.ProfileSet;
import org.smooks.profile.ProfileStore;
import org.smooks.profile.UnknownProfileMemberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link org.smooks.Smooks} utilities.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class SmooksUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(Smooks.class);

    private SmooksUtil() {
    }

    /**
     * Manually register a set of profiles with the profile store associated with the supplied {@link org.smooks.Smooks} instance.
     * <p/>
     * ProfileSets will typically be registered via the config, but it is useful
     * to be able to perform this task manually.
     *
     * @param profileSet The profile set to be registered.
     * @param smooks     The {@link org.smooks.Smooks} instance on which to perform the configuration operation.
     */
    public static void registerProfileSet(ProfileSet profileSet, Smooks smooks) {
        AssertArgument.isNotNull(profileSet, "profileSet");

        ProfileStore profileStore = smooks.getApplicationContext().getProfileStore();
        try {
            profileStore.getProfileSet(profileSet.getBaseProfile());
            LOGGER.debug("ProfileSet [" + profileSet.getBaseProfile() + "] already registered.  Not registering new profile set.");
        } catch (UnknownProfileMemberException e) {
            // It's an unregistered profileset...
            profileStore.addProfileSet(profileSet);
        }
    }

    /**
     * Register a {@link org.smooks.cdr.SmooksResourceConfiguration} on the supplied {@link Smooks} instance.
     *
     * @param resourceConfig The Resource Configuration to be  registered.
     * @param smooks         The {@link org.smooks.Smooks} instance on which to perform the configuration operation.
     */
    public static void registerResource(SmooksResourceConfiguration resourceConfig, Smooks smooks) {
        if (resourceConfig == null) {
            throw new IllegalArgumentException("null 'resourceConfig' arg in method call.");
        }
        smooks.getApplicationContext().getStore().registerResource(resourceConfig);
    }

    /**
     * Utility method to filter the content in the specified {@link InputStream} for the specified {@link org.smooks.container.ExecutionContext}.
     * <p/>
     * Useful for testing purposes.  In a real scenario, use
     * {@link Smooks#filter(org.smooks.container.ExecutionContext,javax.xml.transform.Source,javax.xml.transform.Result)}.
     * <p/>
     * The content of the returned String is totally dependent on the configured
     * {@link org.smooks.delivery.dom.DOMElementVisitor} and {@link org.smooks.delivery.dom.serialize.SerializationUnit}
     * implementations.
     *
     * @param executionContext Execution context for the filter.
     * @param stream           Stream to be processed.  Will be closed before returning.
     * @param smooks           The {@link Smooks} instance through which to perform the filter and serialize operations.
     * @return The Smooks processed content buffer.
     * @throws IOException     Exception using or closing the supplied InputStream.
     * @throws SmooksException Excepting processing content stream.
     */
    public static String filterAndSerialize(ExecutionContext executionContext, InputStream stream, Smooks smooks) throws SmooksException {
        String responseBuf = null;
        CharArrayWriter writer = new CharArrayWriter();
        try {
            smooks.filterSource(executionContext, new StreamSource(stream), new StreamResult(writer));
            responseBuf = writer.toString();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    new SmooksException("Failed to close stream...", e);
                }
            }
            writer.close();
        }

        return responseBuf;
    }
}
