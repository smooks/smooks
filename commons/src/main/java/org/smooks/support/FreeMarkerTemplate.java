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
package org.smooks.support;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.smooks.api.SmooksException;
import org.smooks.assertion.AssertArgument;

import java.io.*;

/**
 *  FreeMarker template.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
*/
public class FreeMarkerTemplate {

    public static final String DEFAULT_MACHINE_READABLE_NUMBER_FORMAT = "#.##########";

    private final String templateText;
    private final Template template;

    public FreeMarkerTemplate(final String templateText) {
        this(templateText, createDefaultConfiguration());
    }

    public FreeMarkerTemplate(final String templateText, final Configuration config) {
        AssertArgument.isNotNullAndNotEmpty(templateText, "templateText");
        this.templateText = templateText;
        final Reader templateReader = new StringReader(templateText);

        try {
            try {
                template = new Template("free-marker-template", templateReader, config);
            } finally {
                templateReader.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Exception creating FreeMarker Template instance for template:\n\n[" + templateText + "]\n\n", e);
        }
    }

    public FreeMarkerTemplate(final String templatePath, final Class basePath) {
        this(templatePath, basePath, createDefaultConfiguration());
    }

    public FreeMarkerTemplate(final String templatePath, Class basePath, final Configuration config) {
        AssertArgument.isNotNullAndNotEmpty(templatePath, "templatePath");
        this.templateText = templatePath;

        try {
            if (basePath != null) {
                config.setClassForTemplateLoading(basePath, "");
            }
            template = config.getTemplate(templatePath);
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException.", e);
        }
    }

    public String getTemplateText() {
        return templateText;
    }

    public String apply(final Object contextObject) {
        final StringWriter outputWriter = new StringWriter();
        apply(contextObject, outputWriter);
        return outputWriter.toString();
    }

    public void apply(final Object contextObject, final Writer outputWriter) {
        try {
            template.process(contextObject, outputWriter);
        } catch (TemplateException e) {
            throw new SmooksException("Failed to apply template.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException.", e);
        }
    }

    private static Configuration createDefaultConfiguration() {
        final Configuration config = new Configuration(Configuration.VERSION_2_3_30);
        config.setNumberFormat(DEFAULT_MACHINE_READABLE_NUMBER_FORMAT);
        return config;
    }
}
