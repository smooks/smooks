/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.xml;

import org.milyn.assertion.AssertArgument;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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

    private LSResourceResolver schemaSourceResolver;
    private ErrorHandler errorHandler;
    private Schema schema;

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

    private SchemaFactory newSchemaFactory() throws SAXNotRecognizedException, SAXNotSupportedException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schemaFactory.setProperty("http://apache.org/xml/properties/security-manager", null);   // Need to turn this thing off, otherwise it throws stupid errors.
        } catch (SAXNotRecognizedException e) {
            // Ignore...
        }
        return schemaFactory;
    }
}
