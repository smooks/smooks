/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.javabean.dynamic.visitor;

import org.milyn.SmooksException;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.dom.serialize.DefaultSerializationUnit;
import org.milyn.io.StreamUtils;
import org.milyn.javabean.dynamic.BeanMetadata;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.w3c.dom.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unknown element data reaper.
 * <p/>
 * Models can sometimes be created from XML which contains valid elements that are not being mapped into the model.
 * We don't want to loose this data in the model, so we capture it as "pre
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UnknownElementDataReaper {

    public static String getPreText(Element element, List<BeanMetadata> beanMetadataSet, BeanContextLifecycleEvent event) {
        StringWriter serializeWriter = new StringWriter();
        List<Node> toSerializeNodes = new ArrayList<Node>();
        Node current = element;

        // Skip back through the siblings until we get an element that has an associated
        // bean...
        while(current != null) {
            current = current.getPreviousSibling();

            if(current == null) {
                // This will result in all siblings back to the start
                // of this sibling set...
                break;
            }

            if(current instanceof Element) {
                if(isOnModelSourcePath(new Fragment((Element) current), beanMetadataSet)) {
                    // The "previous" element is associated with the creation/population of a bean in the
                    // model, so stop here...
                    break;
                }
            }

            toSerializeNodes.add(0, current);
        }

        for(Node node : toSerializeNodes) {
            try {
                serialize(node, serializeWriter);
            } catch (IOException e) {
                throw new SmooksException("Unexpected pre-text node serialization exception.", e);
            }
        }

        // Get rid of leading and space characters (only spaces - not all whitespace).
        // This helps eliminate ugly indentation issues in the serialized XML...
        String xml;
        try {
            xml = normalizeLines(serializeWriter.toString());
        } catch (IOException e) {
            throw new SmooksException("Unexpected pre-text node serialization exception while attempting to remove excess whitespace.", e);
        }
        StringBuilder trimEnd = new StringBuilder(xml);
        while(trimEnd.length() > 0 && trimEnd.charAt(0) == ' ') {
            trimEnd.deleteCharAt(0);
        }
        while(trimEnd.length() > 1 && trimEnd.charAt(0) == '\n' && trimEnd.charAt(1) == '\n') {
            trimEnd.deleteCharAt(0);
        }
        while(trimEnd.length() > 0 && trimEnd.charAt(trimEnd.length() - 1) == ' ') {
            trimEnd.deleteCharAt(trimEnd.length() - 1);
        }
        while(trimEnd.length() > 1 && trimEnd.charAt(trimEnd.length() - 1) == '\n' && trimEnd.charAt(trimEnd.length() - 2) == '\n') {
            trimEnd.deleteCharAt(trimEnd.length() - 1);
        }

        return trimEnd.toString();
    }

    private static boolean isOnModelSourcePath(Fragment fragment, List<BeanMetadata> beanMetadataSet) {
        for(BeanMetadata beanMetadata : beanMetadataSet) {
            if(fragment.equals(beanMetadata.getCreateSource())) {
                return true;
            }

            for(Fragment populateSource : beanMetadata.getPopulateSources()) {
                if(fragment.isParentFragment(populateSource)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String normalizeLines(String xml) throws IOException {
        StringBuffer stringBuf = new StringBuffer();
        int xmlLength = xml.length();

        for(int i = 0; i < xmlLength; i++) {
            char character = xml.charAt(i);
            if(character != '\r') {
                stringBuf.append(character);
            }
        }

        return stringBuf.toString();
    }

    private static DefaultSerializationUnit serializationUnit;
    static {
        serializationUnit = new DefaultSerializationUnit();
        serializationUnit.setCloseEmptyElements(true);
        serializationUnit.setRewriteEntities(true); 
    }
    private static void serialize(Node node, Writer writer) throws IOException {
        switch(node.getNodeType()) {
            case Node.ELEMENT_NODE: {
                Element element = (Element) node;
                NodeList children = element.getChildNodes();
                int childCount = children.getLength();

                serializationUnit.writeElementStart(element, writer);

                // Write the child nodes...
                for(int i = 0; i < childCount; i++) {
                    serialize(children.item(i), writer);
                }

                serializationUnit.writeElementEnd(element, writer);
                break;
            }
            case Node.TEXT_NODE: {
                serializationUnit.writeElementText((Text)node, writer, null);
                break;
            }
            case Node.COMMENT_NODE: {
                serializationUnit.writeElementComment((Comment)node, writer, null);
                break;
            }
            case Node.CDATA_SECTION_NODE: {
                serializationUnit.writeElementCDATA((CDATASection)node, writer, null);
                break;
            }
            case Node.ENTITY_REFERENCE_NODE: {
                serializationUnit.writeElementEntityRef((EntityReference)node, writer, null);
                break;
            }
            default: {
                break;
            }
        }
    }
}
