/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.ordering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.delivery.DefaultContentHandlerBinding;
import org.smooks.engine.delivery.ordering.testvisitors.TestConsumer;
import org.smooks.engine.delivery.ordering.testvisitors.TestProducer;
import org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer;
import org.smooks.engine.delivery.ordering.testvisitors.TestVisitor;
import org.smooks.engine.resource.config.DefaultResourceConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SorterTestCase {

    private final List<ContentHandlerBinding<Visitor>> sortedContentHandlerBindings = new ArrayList<>();
    private final List<ContentHandlerBinding<Visitor>> unsortedContentHandlerBindings = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        sortedContentHandlerBindings.clear();
        unsortedContentHandlerBindings.clear();
    }

    @Test
    public void test_sort_01_producer_first() {
        addVisitor(new TestConsumer("e", "f"));
        addVisitor(new TestProducerConsumer().setProducts("h").setConsumes("g"));
        addVisitor(new TestProducer("a", "b", "c"));
        addVisitor(new TestVisitor());
        addVisitor(new TestConsumer("d", "g"));
        addVisitor(new TestProducer("d", "e"));
        addVisitor(new TestConsumer("b"));
        addVisitor(new TestVisitor());
        addVisitor(new TestProducerConsumer().setProducts("g").setConsumes("c"));

        Sorter.sort(sortedContentHandlerBindings, Sorter.SortOrder.PRODUCERS_FIRST);
        assertEquals(unsortedContentHandlerBindings.get(5), sortedContentHandlerBindings.get(0));
        assertEquals(unsortedContentHandlerBindings.get(2), sortedContentHandlerBindings.get(1));
        assertEquals(unsortedContentHandlerBindings.get(8), sortedContentHandlerBindings.get(2));
        assertEquals(unsortedContentHandlerBindings.get(1), sortedContentHandlerBindings.get(3));
        assertEquals(unsortedContentHandlerBindings.get(3), sortedContentHandlerBindings.get(4));
        assertEquals(unsortedContentHandlerBindings.get(7), sortedContentHandlerBindings.get(5));
        assertEquals(unsortedContentHandlerBindings.get(0), sortedContentHandlerBindings.get(6));
        assertEquals(unsortedContentHandlerBindings.get(4), sortedContentHandlerBindings.get(7));
        assertEquals(unsortedContentHandlerBindings.get(6), sortedContentHandlerBindings.get(8));
    }

    @Test
    public void test_sort_02_producer_first() {
        addVisitor(new TestProducerConsumer().setProducts("h").setConsumes("g"));
        addVisitor(new TestConsumer("e", "f"));
        addVisitor(new TestConsumer("b"));
        addVisitor(new TestProducer("d", "e"));
        addVisitor(new TestProducerConsumer().setProducts("g").setConsumes("c"));
        addVisitor(new TestConsumer("d", "g"));
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestProducer("a", "b", "c"));

        Sorter.sort(sortedContentHandlerBindings, Sorter.SortOrder.PRODUCERS_FIRST);
        assertEquals(unsortedContentHandlerBindings.get(8), sortedContentHandlerBindings.get(0));
        assertEquals(unsortedContentHandlerBindings.get(3), sortedContentHandlerBindings.get(1));
        assertEquals(unsortedContentHandlerBindings.get(4), sortedContentHandlerBindings.get(2));
        assertEquals(unsortedContentHandlerBindings.get(0), sortedContentHandlerBindings.get(3));
        assertEquals(unsortedContentHandlerBindings.get(6), sortedContentHandlerBindings.get(4));
        assertEquals(unsortedContentHandlerBindings.get(7), sortedContentHandlerBindings.get(5));
        assertEquals(unsortedContentHandlerBindings.get(1), sortedContentHandlerBindings.get(6));
        assertEquals(unsortedContentHandlerBindings.get(2), sortedContentHandlerBindings.get(7));
        assertEquals(unsortedContentHandlerBindings.get(5), sortedContentHandlerBindings.get(8));
    }

    @Test
    public void test_sort_01_consumers_first() {
        addVisitor(new TestProducerConsumer().setProducts("h").setConsumes("g"));
        addVisitor(new TestConsumer("e", "f"));
        addVisitor(new TestConsumer("b"));
        addVisitor(new TestProducer("d", "e"));
        addVisitor(new TestProducerConsumer().setProducts("g").setConsumes("c"));
        addVisitor(new TestConsumer("d", "g"));
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestProducer("a", "b", "c"));

        Sorter.sort(sortedContentHandlerBindings, Sorter.SortOrder.CONSUMERS_FIRST);
        assertEquals(unsortedContentHandlerBindings.get(8), sortedContentHandlerBindings.get(8));
        assertEquals(unsortedContentHandlerBindings.get(3), sortedContentHandlerBindings.get(7));
        assertEquals(unsortedContentHandlerBindings.get(4), sortedContentHandlerBindings.get(6));
        assertEquals(unsortedContentHandlerBindings.get(0), sortedContentHandlerBindings.get(5));
        assertEquals(unsortedContentHandlerBindings.get(6), sortedContentHandlerBindings.get(4));
        assertEquals(unsortedContentHandlerBindings.get(7), sortedContentHandlerBindings.get(3));
        assertEquals(unsortedContentHandlerBindings.get(1), sortedContentHandlerBindings.get(2));
        assertEquals(unsortedContentHandlerBindings.get(2), sortedContentHandlerBindings.get(1));
        assertEquals(unsortedContentHandlerBindings.get(5), sortedContentHandlerBindings.get(0));
    }

    @Test
    public void test_sort_no_producers_consumers_producersfirst() {
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());

        Sorter.sort(sortedContentHandlerBindings, Sorter.SortOrder.PRODUCERS_FIRST);
        assertEquals(unsortedContentHandlerBindings.get(0), sortedContentHandlerBindings.get(0));
        assertEquals(unsortedContentHandlerBindings.get(1), sortedContentHandlerBindings.get(1));
        assertEquals(unsortedContentHandlerBindings.get(2), sortedContentHandlerBindings.get(2));
        assertEquals(unsortedContentHandlerBindings.get(3), sortedContentHandlerBindings.get(3));
        assertEquals(unsortedContentHandlerBindings.get(4), sortedContentHandlerBindings.get(4));
        assertEquals(unsortedContentHandlerBindings.get(5), sortedContentHandlerBindings.get(5));
        assertEquals(unsortedContentHandlerBindings.get(6), sortedContentHandlerBindings.get(6));
        assertEquals(unsortedContentHandlerBindings.get(7), sortedContentHandlerBindings.get(7));
        assertEquals(unsortedContentHandlerBindings.get(8), sortedContentHandlerBindings.get(8));
    }

    @Test
    public void test_sort_no_producers_consumers_consumersfirst() {
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());
        addVisitor(new TestVisitor());

        Sorter.sort(sortedContentHandlerBindings, Sorter.SortOrder.CONSUMERS_FIRST);
        assertEquals(unsortedContentHandlerBindings.get(0), sortedContentHandlerBindings.get(8));
        assertEquals(unsortedContentHandlerBindings.get(1), sortedContentHandlerBindings.get(7));
        assertEquals(unsortedContentHandlerBindings.get(2), sortedContentHandlerBindings.get(6));
        assertEquals(unsortedContentHandlerBindings.get(3), sortedContentHandlerBindings.get(5));
        assertEquals(unsortedContentHandlerBindings.get(4), sortedContentHandlerBindings.get(4));
        assertEquals(unsortedContentHandlerBindings.get(5), sortedContentHandlerBindings.get(3));
        assertEquals(unsortedContentHandlerBindings.get(6), sortedContentHandlerBindings.get(2));
        assertEquals(unsortedContentHandlerBindings.get(7), sortedContentHandlerBindings.get(1));
        assertEquals(unsortedContentHandlerBindings.get(8), sortedContentHandlerBindings.get(0));
    }

    @Test
    public void test_sort_2way_dependency_01() {
        addVisitor(new TestProducerConsumer().setProducts("a").setConsumes("b"));
        addVisitor(new TestProducerConsumer().setProducts("b").setConsumes("a"));

        try {
            Sorter.sort(sortedContentHandlerBindings, Sorter.SortOrder.PRODUCERS_FIRST);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertEquals(("Invalid 2-Way/Circular Visitor Producer/Consumer dependency detected in configuration.\n" +
                    "\tTarget Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\tdepends-on: Target Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\tdepends-on: Target Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]").trim(), e.getMessage().trim());
        }
    }

    @Test
    public void test_sort_2way_dependency_02() {
        addVisitor(new TestProducerConsumer().setProducts("a").setConsumes("b"));
        addVisitor(new TestProducerConsumer().setProducts("b").setConsumes("c"));
        addVisitor(new TestProducerConsumer().setProducts("c").setConsumes("d"));
        addVisitor(new TestProducerConsumer().setProducts("d").setConsumes("a"));

        try {
            Sorter.sort(sortedContentHandlerBindings, Sorter.SortOrder.PRODUCERS_FIRST);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertEquals(("Invalid 2-Way/Circular Visitor Producer/Consumer dependency detected in configuration.\n" +
                    "\tTarget Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\tdepends-on: Target Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\tdepends-on: Target Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\t\tdepends-on: Target Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\t\t\tdepends-on: Target Profile: [[org.smooks.api.profile.Profile#default_profile]], Selector: [none], Resource: [org.smooks.engine.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]").trim(), e.getMessage().trim());
        }
    }

    private void addVisitor(Visitor visitor) {
        ContentHandlerBinding<Visitor> contentHandlerBinding = new DefaultContentHandlerBinding<>(visitor, new DefaultResourceConfig(ResourceConfig.SELECTOR_NONE, new Properties(), visitor.getClass().getName()));
        sortedContentHandlerBindings.add(contentHandlerBinding);
        unsortedContentHandlerBindings.add(contentHandlerBinding);
    }
}
