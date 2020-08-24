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
package org.smooks.delivery.ordering;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.smooks.delivery.Visitor;
import org.smooks.delivery.ContentHandlerConfigMap;
import org.smooks.delivery.ordering.testvisitors.TestProducer;
import org.smooks.delivery.ordering.testvisitors.TestVisitor;
import org.smooks.delivery.ordering.testvisitors.TestConsumer;
import org.smooks.delivery.ordering.testvisitors.TestProducerConsumer;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.SmooksConfigurationException;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SorterTest {

    private List<ContentHandlerConfigMap<Visitor>> sortList = new ArrayList<ContentHandlerConfigMap<Visitor>>();
    private List<ContentHandlerConfigMap<Visitor>> originalList = new ArrayList<ContentHandlerConfigMap<Visitor>>();

    @Before
    public void setUp() throws Exception {
        sortList.clear();
        originalList.clear();
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

        Sorter.sort(sortList, Sorter.SortOrder.PRODUCERS_FIRST);
        assertEquals(originalList.get(5), sortList.get(0));
        assertEquals(originalList.get(2), sortList.get(1));
        assertEquals(originalList.get(8), sortList.get(2));
        assertEquals(originalList.get(1), sortList.get(3));
        assertEquals(originalList.get(3), sortList.get(4));
        assertEquals(originalList.get(7), sortList.get(5));
        assertEquals(originalList.get(0), sortList.get(6));
        assertEquals(originalList.get(4), sortList.get(7));
        assertEquals(originalList.get(6), sortList.get(8));
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

        Sorter.sort(sortList, Sorter.SortOrder.PRODUCERS_FIRST);
        assertEquals(originalList.get(8), sortList.get(0));
        assertEquals(originalList.get(3), sortList.get(1));
        assertEquals(originalList.get(4), sortList.get(2));
        assertEquals(originalList.get(0), sortList.get(3));
        assertEquals(originalList.get(6), sortList.get(4));
        assertEquals(originalList.get(7), sortList.get(5));
        assertEquals(originalList.get(1), sortList.get(6));
        assertEquals(originalList.get(2), sortList.get(7));
        assertEquals(originalList.get(5), sortList.get(8));
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

        Sorter.sort(sortList, Sorter.SortOrder.CONSUMERS_FIRST);
        assertEquals(originalList.get(8), sortList.get(8));
        assertEquals(originalList.get(3), sortList.get(7));
        assertEquals(originalList.get(4), sortList.get(6));
        assertEquals(originalList.get(0), sortList.get(5));
        assertEquals(originalList.get(6), sortList.get(4));
        assertEquals(originalList.get(7), sortList.get(3));
        assertEquals(originalList.get(1), sortList.get(2));
        assertEquals(originalList.get(2), sortList.get(1));
        assertEquals(originalList.get(5), sortList.get(0));
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

        Sorter.sort(sortList, Sorter.SortOrder.PRODUCERS_FIRST);
        assertEquals(originalList.get(0), sortList.get(0));
        assertEquals(originalList.get(1), sortList.get(1));
        assertEquals(originalList.get(2), sortList.get(2));
        assertEquals(originalList.get(3), sortList.get(3));
        assertEquals(originalList.get(4), sortList.get(4));
        assertEquals(originalList.get(5), sortList.get(5));
        assertEquals(originalList.get(6), sortList.get(6));
        assertEquals(originalList.get(7), sortList.get(7));
        assertEquals(originalList.get(8), sortList.get(8));
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

        Sorter.sort(sortList, Sorter.SortOrder.CONSUMERS_FIRST);
        assertEquals(originalList.get(0), sortList.get(8));
        assertEquals(originalList.get(1), sortList.get(7));
        assertEquals(originalList.get(2), sortList.get(6));
        assertEquals(originalList.get(3), sortList.get(5));
        assertEquals(originalList.get(4), sortList.get(4));
        assertEquals(originalList.get(5), sortList.get(3));
        assertEquals(originalList.get(6), sortList.get(2));
        assertEquals(originalList.get(7), sortList.get(1));
        assertEquals(originalList.get(8), sortList.get(0));
    }

    @Test
    public void test_sort_2way_dependency_01() {
        addVisitor(new TestProducerConsumer().setProducts("a").setConsumes("b"));
        addVisitor(new TestProducerConsumer().setProducts("b").setConsumes("a"));

        try {
            Sorter.sort(sortList, Sorter.SortOrder.PRODUCERS_FIRST);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals(("Invalid 2-Way/Circular Visitor Producer/Consumer dependency detected in configuration.\n" +
                    "\tTarget Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [0], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\tdepends-on: Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [1], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\tdepends-on: Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [0], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]").trim(), e.getMessage().trim());
        }
    }

    @Test
    public void test_sort_2way_dependency_02() {
        addVisitor(new TestProducerConsumer().setProducts("a").setConsumes("b"));
        addVisitor(new TestProducerConsumer().setProducts("b").setConsumes("c"));
        addVisitor(new TestProducerConsumer().setProducts("c").setConsumes("d"));
        addVisitor(new TestProducerConsumer().setProducts("d").setConsumes("a"));

        try {
            Sorter.sort(sortList, Sorter.SortOrder.PRODUCERS_FIRST);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals(("Invalid 2-Way/Circular Visitor Producer/Consumer dependency detected in configuration.\n" +
                    "\tTarget Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [0], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\tdepends-on: Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [1], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\tdepends-on: Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [2], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\t\tdepends-on: Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [3], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]\n" +
                    "\t\t\t\t\tdepends-on: Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [0], Selector Namespace URI: [null], Resource: [org.smooks.delivery.ordering.testvisitors.TestProducerConsumer], Num Params: [0]").trim(), e.getMessage().trim());
        }
    }

    private void addVisitor(Visitor visitor) {
        ContentHandlerConfigMap<Visitor> listEntry = new ContentHandlerConfigMap<Visitor>(visitor, new SmooksResourceConfiguration("" + sortList.size(), visitor.getClass().getName()));
        sortList.add(listEntry);
        originalList.add(listEntry);
    }
}
