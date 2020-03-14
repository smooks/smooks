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
package org.smooks.xml;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;

import java.util.List;
import java.io.Writer;
import java.io.IOException;

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
        List<SmooksResourceConfiguration> docTypeUDs = executionContext.getDeliveryConfig().getSmooksResourceConfigurations("doctype");
        SmooksResourceConfiguration docTypeSmooksResourceConfiguration = null;

        if(docTypeUDs != null && docTypeUDs.size() > 0) {
            docTypeSmooksResourceConfiguration = docTypeUDs.get(0);
        }

        // Only use the cdrdef if the override flag is set.  The override flag will
        // cause this DOCTYPE to override any DOCYTPE decl from the source doc.
        if(docTypeSmooksResourceConfiguration != null && docTypeSmooksResourceConfiguration.getBoolParameter("override", true)) {
            String name = docTypeSmooksResourceConfiguration.getStringParameter("name", "!!DOCTYPE name undefined - fix smooks-resource!!");
            String publicId = docTypeSmooksResourceConfiguration.getStringParameter("publicId", "!!DOCTYPE publicId undefined - fix smooks-resource!!");
            String systemId = docTypeSmooksResourceConfiguration.getStringParameter("systemId", "!!DOCTYPE systemId undefined - fix smooks-resource!!");
            String xmlns = docTypeSmooksResourceConfiguration.getStringParameter("xmlns");
            boolean omit = docTypeSmooksResourceConfiguration.getBoolParameter("omit", false);

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
        
        private String name;
        private String publicId;
        private String systemId;
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
