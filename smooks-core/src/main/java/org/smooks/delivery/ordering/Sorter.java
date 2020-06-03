/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.delivery.ordering;

import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.ContentHandlerConfigMap;

import java.util.*;

/**
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class Sorter {

    public enum SortOrder {
        PRODUCERS_FIRST,
        CONSUMERS_FIRST
    }

    private Sorter() {
    }

    public static <T extends ContentHandler> void sort(List<ContentHandlerConfigMap<T>> visitors, SortOrder sortOrder) throws SmooksConfigurationException {
        List<DependencySpec> dependancySpecs;

        dependancySpecs = buildDependencyMap(visitors);
        assertNo2WayDependencies(dependancySpecs);
        sortDependancyMap(dependancySpecs, sortOrder);
        remapList(dependancySpecs, visitors);
    }

    @SuppressWarnings("unchecked")
    private static <T extends ContentHandler> List<DependencySpec> buildDependencyMap(List<ContentHandlerConfigMap<T>> visitors) {
        List<DependencySpec> dependancySpecs = new ArrayList<DependencySpec>();

        for(ContentHandlerConfigMap<T> visitor : visitors) {
            dependancySpecs.add(new DependencySpec(visitor));
        }

        for(DependencySpec outer : dependancySpecs) {
            if(outer.visitor.getContentHandler() instanceof Producer) {
                Set<?> outerProducts = ((Producer) outer.visitor.getContentHandler()).getProducts();

                for(DependencySpec inner : dependancySpecs) {
                    if(inner != outer && inner.visitor.getContentHandler() instanceof Consumer) {
                        Consumer innerConsumer = (Consumer) inner.visitor.getContentHandler();
                        for(Object product : outerProducts) {
                            if(innerConsumer.consumes(product)) {
                                outer.dependants.add(inner);
                            }
                        }
                    }
                }
            }
        }

        return dependancySpecs;
    }

    private static void sortDependancyMap(List<DependencySpec> dependancySpecs, SortOrder sortOrder) {
        // We first iterate over the array continuously until we make no re-orderings.
        // This ordering process is very simple... for each entry in the list,
        // make sure it's higher up the list than it's dependants...
        boolean iterate = true;
        while(iterate) {
            iterate = applySort(dependancySpecs);
        }

        DependencySpec[] array = new DependencySpec[dependancySpecs.size()];
        dependancySpecs.toArray(array);

        // Now we apply another sort, which simply sorts by type, making sure
        // we have the following ordering:
        // - Producers (only), followed by...
        // - Producers/Consumers, followed by...
        // - Visitors (non Producers and non Consumers), followed by...
        // - Consumers (only)...
        Arrays.sort(array, new Comparator<DependencySpec>() {
            public int compare(DependencySpec left, DependencySpec right) {
                int leftScore = score(left);
                int rightScore = score(right);

                if(leftScore > rightScore) {
                    return -1;
                } else if(leftScore < rightScore) {
                    return 1;
                }
                return 0;
            }
            private int score(DependencySpec spec) {
                int score = 0;
                if(spec.visitor.getContentHandler() instanceof Producer) {
                    score += 2;
                }
                if(spec.visitor.getContentHandler() instanceof Consumer) {
                    score -= 1;
                }
                return score;
            }
        });

        dependancySpecs.clear();
        if(sortOrder == SortOrder.PRODUCERS_FIRST) {
            dependancySpecs.addAll(Arrays.asList(array));
        } else {
            // Add them in reverse order...
            for(DependencySpec spec : array) {
                dependancySpecs.add(0, spec);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean applySort(List<DependencySpec> dependencySpecs) {
        int specCount = dependencySpecs.size();

        for (int i = 0; i < specCount; i++) {
            DependencySpec dependancy = dependencySpecs.get(i);

            for (int ii = 0; ii < dependancy.dependants.size(); ii++) {
                List<DependencySpec> dependants = dependancy.dependants;
                DependencySpec dependant = dependants.get(ii);
                int dependantIndex = dependencySpecs.indexOf(dependant);

                if(dependantIndex < i) {
                    // Remove the dependancy from the list and re-add it
                    // in front of the dependant...
                    dependencySpecs.remove(i);
                    dependencySpecs.add(dependantIndex, dependancy);

                    // Return and start again...
                    return true;
                }
            }
        }

        // That's it... they're all sorted...
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T extends ContentHandler> void remapList(List<DependencySpec> dependancySpecs, List<ContentHandlerConfigMap<T>> visitors) {
        visitors.clear();

        for(DependencySpec dependancySpec : dependancySpecs) {
            visitors.add(dependancySpec.visitor);
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertNo2WayDependencies(List<DependencySpec> dependancySpecs) throws SmooksConfigurationException {
        Stack<DependencySpec> dependencyStack = new Stack<DependencySpec>();
        for(DependencySpec spec : dependancySpecs) {
            dependencyStack.push(spec);
            assertNo2WayDependencies(spec, spec.dependants, dependencyStack);
            dependencyStack.pop();
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertNo2WayDependencies(DependencySpec spec, List<DependencySpec> dependancySpecs, Stack<DependencySpec> dependencyStack) {
        for(DependencySpec dependancy : dependancySpecs) {
            dependencyStack.push(dependancy);
            if(dependancy.isDependant(spec)) {
                dependencyStack.push(spec);
                throw new SmooksConfigurationException("Invalid 2-Way/Circular Visitor Producer/Consumer dependency detected in configuration.\n" + getDependencyStackTrace(dependencyStack));
            }

            // Recurse down ...
            assertNo2WayDependencies(spec, dependancy.dependants, dependencyStack);
            dependencyStack.pop();
        }
    }

    private static class DependencySpec<T extends ContentHandler>  {

        private ContentHandlerConfigMap<T> visitor;

        private List<DependencySpec> dependants = new ArrayList<DependencySpec>();

        private DependencySpec(ContentHandlerConfigMap<T> visitor) {
            AssertArgument.isNotNull(visitor, "visitor");
            this.visitor = visitor;
        }

        private boolean isDependant(DependencySpec visitor) {
            if(visitor == this) {
                throw new IllegalStateException("Unexpected call to 'isDependant' with this Visitor.");
            }

            for(DependencySpec dependant : dependants) {
                if(dependant == visitor) {
                    return true;
                }
            }

            return false;
        }

    }

    private static String getDependencyStackTrace(Stack<DependencySpec> dependencyStack) {
        StringBuilder builder = new StringBuilder();
        int numTabs = 0;

        appendTabs(++numTabs, builder);
        builder.append(dependencyStack.pop().visitor.getResourceConfig());
        builder.append("\n");
        while(!dependencyStack.isEmpty()) {
            appendTabs(++numTabs, builder);
            builder.append("depends-on: ");
            builder.append(dependencyStack.pop().visitor.getResourceConfig());
            builder.append("\n");
        }

        return builder.toString();
    }

    private static void appendTabs(int count, StringBuilder builder) {
        for(int i = 0; i < count; i++) {
            builder.append('\t');
        }
    }
}
