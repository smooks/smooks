/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 - 2024 Smooks
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
package org.smooks.engine.delivery.java;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class XmlCompareUtil {

    public static boolean compare(String xmlString1, String xmlString2) throws ParserConfigurationException, IOException, SAXException {
        Document xml1 = parseXML(xmlString1);
        Document xml2 = parseXML(xmlString2);
        return compareChildren(xml1.getDocumentElement(), xml2.getDocumentElement());
    }

    private static Document parseXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        String transformedXML = "<root>" + xml + "</root>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(transformedXML.getBytes()));
    }


    private static boolean compareChildren(Element element1, Element element2) { //
        List<Node> children1 = getOrderedChildren(element1.getChildNodes());
        List<Node> children2 = getOrderedChildren(element2.getChildNodes());
        if (children1.size() != children2.size()) {
            return false;
        }
        if (children1.isEmpty()) {
            return true;
        }

        for (int iterator = 0; iterator < children1.size(); iterator++) {
            Node child1 = children1.get(iterator);
            Node child2 = children2.get(iterator);
            if (!child1.getNodeName().equals(child2.getNodeName())) {
                return false;
            }
            if (child1.hasChildNodes()) {
                if (!compareChildren((Element) child1, (Element) child2)) {
                    return false;
                }
            } else if (!child1.getNodeValue().equals(child2.getNodeValue())) {
                return false;
            }
        }

        return true;
    }

    private static List<Node> getOrderedChildren(NodeList children) {
        List<Node> result = new ArrayList<>(children.getLength());
        for (int iterator = 0; iterator < children.getLength(); iterator++) {
            result.add(children.item(iterator));
        }
        result.sort(Comparator.comparing(Node::getNodeName));
        return result;
    }
}
