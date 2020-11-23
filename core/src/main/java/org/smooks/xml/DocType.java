/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.xml;

import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ExecutionContext;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * DOCTYPE utility class.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class DocType {

    private static final String DOCTYPE_KEY = DocType.class.getName() + "#DOCTYPE_KEY";

    public static void setDocType(String name, String publicId, String systemId, String xmlns, ExecutionContext executionContext) {
        executionContext.setAttribute(DOCTYPE_KEY, new DocumentTypeData(name, publicId, systemId, xmlns));
    }

    public static DocumentTypeData getDocType(ExecutionContext executionContext) {
        List<ResourceConfig> docTypeUDs = executionContext.getDeliveryConfig().getResourceConfigs("doctype");
        ResourceConfig docTypeResourceConfig = null;

        if(docTypeUDs != null && docTypeUDs.size() > 0) {
            docTypeResourceConfig = docTypeUDs.get(0);
        }

        // Only use the cdrdef if the override flag is set.  The override flag will
        // cause this DOCTYPE to override any DOCYTPE decl from the source doc.
        if(docTypeResourceConfig != null && docTypeResourceConfig.getParameterValue("override", Boolean.class, true)) {
            String name = docTypeResourceConfig.getParameterValue("name", String.class, "!!DOCTYPE name undefined - fix smooks-resource!!");
            String publicId = docTypeResourceConfig.getParameterValue("publicId", String.class, "!!DOCTYPE publicId undefined - fix smooks-resource!!");
            String systemId = docTypeResourceConfig.getParameterValue("systemId", String.class, "!!DOCTYPE systemId undefined - fix smooks-resource!!");
            String xmlns = docTypeResourceConfig.getParameterValue("xmlns",String.class);
            boolean omit = docTypeResourceConfig.getParameterValue("omit", Boolean.class, false);

            return new DocumentTypeData(name, publicId, systemId, xmlns, omit);
        }

        return (DocumentTypeData) executionContext.getAttribute(DOCTYPE_KEY);
    }

    public static void serializeDoctype(DocumentTypeData docTypeData, Writer writer) throws IOException {
        if(docTypeData != null && !docTypeData.omit) {
            writer.write("<?xml version='1.0'?>\n");
            writer.write("<!DOCTYPE ");
            writer.write(docTypeData.getName());
            writer.write(' ');
            if(docTypeData.getPublicId() != null) {
                writer.write("PUBLIC \"");
                writer.write(docTypeData.getPublicId());
                writer.write("\" ");
            }
            if(docTypeData.getSystemId() != null) {
                writer.write('"');
                writer.write(docTypeData.getSystemId());
                writer.write('"');
            }
            writer.write('>');
            writer.write('\n');
        }
    }

    public static class DocumentTypeData {
        
        private final String name;
        private final String publicId;
        private final String systemId;
        private String xmlns;
        private boolean omit = false;

        public DocumentTypeData(String name, String publicId, String systemId, String xmlns) {
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
            this.xmlns = xmlns;
        }

        public DocumentTypeData(String name, String publicId, String systemId, String xmlns, boolean omit) {
            this(name, publicId, systemId, xmlns);
            this.omit = omit;
        }

        public String getName() {
            return name;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getSystemId() {
            return systemId;
        }

        public String getXmlns() {
            return xmlns;
        }

        public void setXmlns(String xmlns) {
            this.xmlns = xmlns;
        }
    }
}
