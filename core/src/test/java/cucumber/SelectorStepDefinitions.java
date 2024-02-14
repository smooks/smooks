/*-
 * ========================LICENSE_START=================================
 * Core
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
package cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.resource.config.xpath.SelectorPathFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelectorStepDefinitions {

    private Document document;
    private Node currentNode;
    private boolean isMatch;
    private final Properties namespaces = new Properties();

    @Given("a fragment")
    public void a_fragment(String fragment) throws DocumentException {
        document = DocumentHelper.parseText(fragment);
    }

    @Given("the current node is {string}")
    public void the_current_node_is(String xpathExpression) throws DocumentException, XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        currentNode = (org.w3c.dom.Node) xpath.evaluate(xpathExpression, new DOMWriter().write(document),
                XPathConstants.NODE);
    }

    @Given("the Smooks namespaces are")
    public void the_smooks_namespaces_are(DataTable namespaces) {
        for (Map<String, String> namespace : namespaces.asMaps(String.class, String.class)) {
            this.namespaces.put(namespace.get("prefix"), namespace.get("uri"));
        }
    }

    @When("the selector {string} is applied")
    public void the_selector_is_applied(String selector) {
        isMatch = new NodeFragment(currentNode).isMatch(SelectorPathFactory.newSelectorPath(selector, namespaces), null);
    }

    @Then("the current node matches the selector")
    public void the_current_node_matches_the_selector() {
        assertTrue(isMatch);
    }

    @Then("the current node does not match the selector")
    public void the_current_node_does_not_match_the_selector() {
        assertFalse(isMatch);
    }
}
