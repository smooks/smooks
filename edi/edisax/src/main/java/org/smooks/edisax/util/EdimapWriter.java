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
package org.smooks.edisax.util;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.smooks.edisax.model.internal.Component;
import org.smooks.edisax.model.internal.Delimiters;
import org.smooks.edisax.model.internal.Description;
import org.smooks.edisax.model.internal.Edimap;
import org.smooks.edisax.model.internal.Field;
import org.smooks.edisax.model.internal.Import;
import org.smooks.edisax.model.internal.MappingNode;
import org.smooks.edisax.model.internal.Segment;
import org.smooks.edisax.model.internal.SegmentGroup;
import org.smooks.edisax.model.internal.SubComponent;
import org.smooks.edisax.unedifact.UNEdifactInterchangeParser;
import org.smooks.util.ClassUtil;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * EdimapWriter
 * @author bardl
 */
public class EdimapWriter {

    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    static {
        docBuilderFactory.setValidating(false);
        docBuilderFactory.setNamespaceAware(true);
    }

    private static final String NS = "http://www.milyn.org/schema/edi-message-mapping-1.5.xsd";

    private Document doc;

    private EdimapWriter() throws ParserConfigurationException {
        doc = docBuilderFactory.newDocumentBuilder().newDocument();
    }

    public static void write(Edimap edimap, Writer writer) throws IOException {
        try {
            EdimapWriter edimapWriter = new EdimapWriter();

            edimapWriter.write(edimap);

            XmlUtil.serialize(edimapWriter.doc, true, writer);
            writer.flush();
        } catch (ParserConfigurationException e) {
            IOException ioE = new IOException("Error constructing EDI Mapping Model");
            ioE.initCause(e);
            throw ioE;
        }
    }

    public static void write(Segment segment, Writer writer) throws IOException {
        Edimap ediMap = new Edimap();

        SegmentGroup segments = new SegmentGroup();
        segments.getSegments().add(segment);

        ediMap.setSegments(segments);
        ediMap.setDelimiters(UNEdifactInterchangeParser.defaultUNEdifactDelimiters);
        ediMap.setDescription(new Description().setName("TODO").setVersion("TODO"));

        write(ediMap, writer);
    }

    private void write(Edimap edimap) {
        Element edimapEl = newElement("edimap", doc);

        addImports(edimap.getImports(), edimapEl);
        addDescription(edimap.getDescription(), edimapEl);
        addDelimiters(edimap.getDelimiters(), edimapEl);

        SegmentGroup segments = edimap.getSegments();
        Element segmentsEl = newElement("segments", edimapEl, segments);

        mapBeanProperties(segments, segmentsEl, "name", "xmltag");
        addChildSegments(segments, segmentsEl);
    }

    private void addImports(List<Import> imports, Element edimapEl) {
        for(Import importInst : imports) {
            mapBeanProperties(importInst, newElement("import", edimapEl), "resource", "namespace", "truncatableComponents", "truncatableFields", "truncatableSegments");
        }
    }

    private void addDescription(Description description, Element edimapEl) {
        Element descriptionElement = newElement("description", edimapEl);

        mapBeanProperties(description, descriptionElement, "name", "version");
        if (!StringUtils.isEmpty(description.getNamespace())) {
        	descriptionElement.setAttribute("namespace", description.getNamespace());
        }
    }

    private void addDelimiters(Delimiters delimiters, Element edimapEl) {
        mapBeanProperties(delimiters, newElement("delimiters", edimapEl), "segment", "field", "component", "subComponent|sub-component", "escape", "fieldRepeat");
    }

    private void addChildSegments(SegmentGroup segmentGroup, Element parentSegment) {
        List<SegmentGroup> childSegments = segmentGroup.getSegments();

        for(SegmentGroup childSegment : childSegments) {
            Element segmentEl;

            if(childSegment instanceof Segment) {
                segmentEl = newElement("segment", parentSegment, childSegment);
                mapBeanProperties(childSegment, segmentEl, "segcode", "nodeTypeRef", "description", "ignoreUnmappedFields", "truncatable");

                addFields(((Segment)childSegment).getFields(), segmentEl);
            } else {
                segmentEl = newElement("segmentGroup", parentSegment, childSegment);
            }

            mapBeanProperties(childSegment, segmentEl, "name", "xmltag", "minOccurs", "maxOccurs");

            addChildSegments(childSegment, segmentEl);
        }
    }

    private void addFields(List<Field> fields, Element segmentEl) {
        for(Field field : fields) {
            Element fieldEl = newElement("field", segmentEl, field);

            mapBeanProperties(field, fieldEl, "name", "xmltag", "nodeTypeRef", "truncatable", "maxLength", "minLength", "required", "dataType", "dataTypeParametersString|dataTypeParameters");
            addComponents(field.getComponents(), fieldEl);
        }
    }

    private void addComponents(List<Component> components, Element fieldEl) {
        for(Component component : components) {
            Element componentEl = newElement("component", fieldEl, component);

            mapBeanProperties(component, componentEl, "name", "xmltag", "nodeTypeRef", "truncatable", "maxLength", "minLength", "required", "dataType", "dataTypeParametersString|dataTypeParameters");
            addSubComponents(component.getSubComponents(), componentEl);
        }
    }

    private void addSubComponents(List<SubComponent> subComponents, Element componentEl) {
        for(SubComponent subComponent : subComponents) {
            Element subComponentEl = newElement("sub-component", componentEl, subComponent);

            mapBeanProperties(subComponent, subComponentEl, "name", "xmltag", "nodeTypeRef", "maxLength", "minLength", "required", "dataType", "dataTypeParametersString|dataTypeParameters");
        }
    }

    private void mapBeanProperties(Object bean, Element target, String... properties) {
        for(String property : properties) {
            String[] propertyTokens = property.split("\\|");
            String propertyName;
            String attributeName;

            if(propertyTokens.length == 2) {
                propertyName = propertyTokens[0];
                attributeName = propertyTokens[1];                
            } else {
                propertyName = property;
                attributeName = property;
            }

            Object value = getBeanValue(bean, propertyName);

            if(value != null) {
                target.setAttribute(attributeName, XmlUtil.removeEntities(value.toString()));
            }
        }
    }

    private Object getBeanValue(Object bean, String property) {
        String getterMethodName = ClassUtil.toGetterName(property);
        Method getterMethod = ClassUtil.getGetterMethod(getterMethodName, bean, null);

        if(getterMethod == null) {
            getterMethodName = ClassUtil.toIsGetterName(property);
            getterMethod = ClassUtil.getGetterMethod(getterMethodName, bean, null);
        }

        if(getterMethod != null) {
            try {
                return getterMethod.invoke(bean);
            } catch (Exception e) {
                throw new IllegalStateException("Error invoking getter method '" + getterMethodName + "' on Object type '" + bean.getClass().getName() + "'.", e);
            }
        }

        return null;
    }

    private Element newElement(String name, Node parent) {
        Element element =  doc.createElementNS(NS, "medi:" + name);
        parent.appendChild(element);
        return element;
    }

    private Element newElement(String name, Node parent, MappingNode mappingNode) {
        Element element = newElement(name, parent);

        if(mappingNode != null && mappingNode.getDocumentation() != null) {
            Element documentation = newElement("documentation", element);
            documentation.appendChild(doc.createTextNode(mappingNode.getDocumentation()));
        }

        return element;
    }
}
