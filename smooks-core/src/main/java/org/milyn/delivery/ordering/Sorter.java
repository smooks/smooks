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
package org.milyn.delivery.ordering;

import org.milyn.commons.assertion.AssertArgument;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerConfigMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class Sorter {

    public static enum SortOrder {
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

    protected static <T extends ContentHandler> List<DependencySpec> buildDependencyMap(List<ContentHandlerConfigMap<T>> visitors) {
        List<DependencySpec> dependancySpecs = new ArrayList<DependencySpec>();

        for (ContentHandlerConfigMap<T> visitor : visitors) {
            dependancySpecs.add(new DependencySpec(visitor));
        }

        for (DependencySpec outer : dependancySpecs) {
            if (outer.visitor.getContentHandler() instanceof Producer) {
                Set<? extends Object> outerProducts = ((Producer) outer.visitor.getContentHandler()).getProducts();

                for (DependencySpec inner : dependancySpecs) {
                    if (inner != outer && inner.visitor.getContentHandler() instanceof Consumer) {
                        Consumer innerConsumer = (Consumer) inner.visitor.getContentHandler();
                        for (Object product : outerProducts) {
                            if (innerConsumer.consumes(product)) {
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
        while (iterate) {
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

                if (leftScore > rightScore) {
                    return -1;
                } else if (leftScore < rightScore) {
                    return 1;
                }
                return 0;
            }

            private int score(DependencySpec spec) {
                int score = 0;
                if (spec.visitor.getContentHandler() instanceof Producer) {
                    score += 2;
                }
                if (spec.visitor.getContentHandler() instanceof Consumer) {
                    score -= 1;
                }
                return score;
            }
        });

        dependancySpecs.clear();
        if (sortOrder == SortOrder.PRODUCERS_FIRST) {
            dependancySpecs.addAll(Arrays.asList(array));
        } else {
            // Add them in reverse order...
            for (DependencySpec spec : array) {
                dependancySpecs.add(0, spec);
            }
        }
    }

    private static boolean applySort(List<DependencySpec> dependencySpecs) {
        int specCount = dependencySpecs.size();

        for (int i = 0; i < specCount; i++) {
            DependencySpec dependancy = dependencySpecs.get(i);

            for (int ii = 0; ii < dependancy.dependants.size(); ii++) {
                List<DependencySpec> dependants = dependancy.dependants;
                DependencySpec dependant = dependants.get(ii);
                int dependantIndex = dependencySpecs.indexOf(dependant);

                if (dependantIndex < i) {
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

    private static <T extends ContentHandler> void remapList(List<DependencySpec> dependancySpecs, List<ContentHandlerConfigMap<T>> visitors) {
        visitors.clear();

        for (DependencySpec dependancySpec : dependancySpecs) {
            visitors.add(dependancySpec.visitor);
        }
    }

    private static void assertNo2WayDependencies(List<DependencySpec> dependancySpecs) throws SmooksConfigurationException {
        Stack<DependencySpec> dependencyStack = new Stack<DependencySpec>();
        for (DependencySpec spec : dependancySpecs) {
            dependencyStack.push(spec);
            assertNo2WayDependencies(spec, spec.dependants, dependencyStack);
            dependencyStack.pop();
        }
    }

    private static void assertNo2WayDependencies(DependencySpec spec, List<DependencySpec> dependancySpecs, Stack<DependencySpec> dependencyStack) {
        for (DependencySpec dependancy : dependancySpecs) {
            dependencyStack.push(dependancy);
            if (dependancy.isDependant(spec)) {
                dependencyStack.push(spec);
                throw new SmooksConfigurationException("Invalid 2-Way/Circular Visitor Producer/Consumer dependency detected in configuration.\n" + getDependencyStackTrace(dependencyStack));
            }

            // Recurse down ...
            assertNo2WayDependencies(spec, dependancy.dependants, dependencyStack);
            dependencyStack.pop();
        }
    }

    private static class DependencySpec<T extends ContentHandler> {

        private ContentHandlerConfigMap<T> visitor;

        private List<DependencySpec> dependants = new ArrayList<DependencySpec>();

        private DependencySpec(ContentHandlerConfigMap<T> visitor) {
            AssertArgument.isNotNull(visitor, "visitor");
            this.visitor = visitor;
        }

        private boolean isDependant(DependencySpec visitor) {
            if (visitor == this) {
                throw new IllegalStateException("Unexpected call to 'isDependant' with this Visitor.");
            }

            for (DependencySpec dependant : dependants) {
                if (dependant == visitor) {
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
        while (!dependencyStack.isEmpty()) {
            appendTabs(++numTabs, builder);
            builder.append("depends-on: ");
            builder.append(dependencyStack.pop().visitor.getResourceConfig());
            builder.append("\n");
        }

        return builder.toString();
    }

    private static void appendTabs(int count, StringBuilder builder) {
        for (int i = 0; i < count; i++) {
            builder.append('\t');
        }
    }
}
