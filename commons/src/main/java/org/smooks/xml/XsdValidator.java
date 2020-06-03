/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.xml;

import org.smooks.assertion.AssertArgument;
import org.smooks.util.ClassUtil;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.Collection;

/**
 * XSD Validator.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XsdValidator {

    public static final String SCHEMA_FACTORY = XsdValidator.class.getName() + ".SchemaFactory";

    private SchemaFactory installedSchemaFactory;

    private LSResourceResolver schemaSourceResolver;
    private ErrorHandler errorHandler;
    private Schema schema;

    public void setSchemaFactory(SchemaFactory installedSchemaFactory) {
        this.installedSchemaFactory = installedSchemaFactory;
    }

    public void setSchemaSourceResolver(LSResourceResolver schemaSourceResolver) throws SAXException {
        assertSchemaNotInitialized();

        SchemaFactory schemaFactory = newSchemaFactory();

        this.schemaSourceResolver = schemaSourceResolver;
        this.schema = schemaFactory.newSchema();
    }

    /**
     * Set the XSD/Schema Sources.
     * @param xsdSources The schema sources.
     */
    public void setXSDSources(Collection<Source> xsdSources) throws SAXException {
        assertSchemaNotInitialized();

        AssertArgument.isNotNullAndNotEmpty(xsdSources, "xsdSources");
        Source[] xsdSourcesArray = xsdSources.toArray(new Source[xsdSources.size()]);
        SchemaFactory schemaFactory = newSchemaFactory();

        this.schema = schemaFactory.newSchema(xsdSourcesArray);
    }

    /**
     * Set the validation error handler.
     * @param errorHandler The validation error handler.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Validate the supplied source against the namespaces referenced in it.
     * @throws org.xml.sax.SAXException Validation error.
     * @throws java.io.IOException Error reading the XSD Sources.
     */
    public void validate(Source source) throws SAXException, IOException {
        AssertArgument.isNotNull(source, "source");

        if (schema == null) {
            throw new IllegalStateException("Invalid call to validate.  XSD sources not set.");
        }

        // Create the merged Schema instance and from that, create the Validator instance...
        Validator validator = schema.newValidator();

        if(schemaSourceResolver != null) {
            validator.setResourceResolver(schemaSourceResolver);
        }
        if(errorHandler != null) {
            validator.setErrorHandler(errorHandler);
        }

        // Validate the source...
        validator.validate(source);
    }

    private void assertSchemaNotInitialized() {
        if (this.schema != null) {
            throw new IllegalStateException("Schema already initialised.");
        }
    }

    private SchemaFactory newSchemaFactory() {
        if (installedSchemaFactory != null) {
            return installedSchemaFactory;
        } else {
            String schemaFactoryClass = System.getProperty(SCHEMA_FACTORY);

            if (schemaFactoryClass != null) {
                try {
                    return (SchemaFactory) ClassUtil.forName(schemaFactoryClass, getClass()).newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to create an instance of SchemaFactory '" + schemaFactoryClass + "'.", e);
                }
            } else {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                try {
                    schemaFactory.setProperty("http://apache.org/xml/properties/security-manager", null);   // Need to turn this thing off, otherwise it throws stupid errors.
                } catch (SAXException e) {
                    // Ignore...
                }
                return schemaFactory;
            }
        }
    }
}
