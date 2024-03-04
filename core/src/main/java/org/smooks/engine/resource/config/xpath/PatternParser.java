/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * Copyright 2000-2002 bob mcwhirter & James Strachan.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   * Neither the name of the Jaxen Project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Jaxen Project and was originally
 * created by bob mcwhirter <bob@werken.com> and
 * James Strachan <jstrachan@apache.org>.  For more information on the
 * Jaxen Project, please see <http://www.jaxen.org/>.
 *
 * $Id$
 */
package org.smooks.engine.resource.config.xpath;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.DefaultAllNodeStep;
import org.jaxen.expr.DefaultCommentNodeStep;
import org.jaxen.expr.DefaultFilterExpr;
import org.jaxen.expr.DefaultNameStep;
import org.jaxen.expr.DefaultProcessingInstructionNodeStep;
import org.jaxen.expr.DefaultStep;
import org.jaxen.expr.DefaultTextNodeStep;
import org.jaxen.expr.DefaultXPathFactory;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.Step;
import org.jaxen.expr.TextNodeStep;
import org.jaxen.expr.UnionExpr;
import org.jaxen.pattern.LocationPathPattern;
import org.jaxen.pattern.NameTest;
import org.jaxen.pattern.NamespaceTest;
import org.jaxen.pattern.NodeTest;
import org.jaxen.pattern.NodeTypeTest;
import org.jaxen.pattern.Pattern;
import org.jaxen.pattern.PatternHandler;
import org.jaxen.pattern.TextNodeTest;
import org.jaxen.pattern.UnionPattern;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;


/**
 * <code>PatternParser</code> is a helper class for parsing
 * XSLT patterns
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @deprecated will be removed in Jaxen 2.0
 */
public class PatternParser {
    private static final boolean TRACE = false;
    private static final boolean USE_HANDLER = false;

    public static Pattern parse(String text) throws JaxenException, org.jaxen.saxpath.SAXPathException {
        if (USE_HANDLER) {
            XPathReader reader = XPathReaderFactory.createReader();
            PatternHandler handler = new PatternHandler();

            handler.setXPathFactory(new DefaultXPathFactory());
            reader.setXPathHandler(handler);
            reader.parse(text);

            return handler.getPattern();
        } else {
            XPathReader reader = XPathReaderFactory.createReader();
            JaxenHandler handler = new JaxenHandler();

            handler.setXPathFactory(new DefaultXPathFactory());
            reader.setXPathHandler(handler);
            reader.parse(text);

            Pattern pattern = convertExpr(handler.getXPathExpr().getRootExpr());
            return pattern.simplify();
        }
    }

    protected static Pattern convertExpr(Expr expr) throws JaxenException {
        if (TRACE) {
            System.out.println("Converting: " + expr + " into a pattern.");
        }

        if (expr instanceof LocationPath) {
            return convertExpr((LocationPath) expr);
        } else if (expr instanceof FilterExpr) {
            LocationPathPattern answer = new LocationPathPattern();
            answer.addFilter((FilterExpr) expr);
            return answer;
        } else if (expr instanceof UnionExpr) {
            UnionExpr unionExpr = (UnionExpr) expr;
            Pattern lhs = convertExpr(unionExpr.getLHS());
            Pattern rhs = convertExpr(unionExpr.getRHS());
            return new UnionPattern(lhs, rhs);
        } else {
            LocationPathPattern answer = new LocationPathPattern();
            answer.addFilter(new DefaultFilterExpr(expr,
                    new PredicateSet()));
            return answer;
        }
    }

    protected static LocationPathPattern convertExpr(LocationPath locationPath) throws JaxenException {
        LocationPathPattern answer = new LocationPathPattern();
        //answer.setAbsolute( locationPath.isAbsolute() );
        List steps = locationPath.getSteps();

        // go through steps backwards
        LocationPathPattern path = answer;
        boolean first = true;
        for (ListIterator iter = steps.listIterator(steps.size()); iter.hasPrevious(); ) {
            Step step = (Step) iter.previous();
            if (first) {
                first = false;
                path = convertStep(path, step);
            } else {
                if (navigationStep(step)) {
                    LocationPathPattern parent = new LocationPathPattern();
                    int axis = step.getAxis();
                    if (axis == Axis.DESCENDANT || axis == Axis.DESCENDANT_OR_SELF) {
                        path.setAncestorPattern(parent);
                    } else {
                        path.setParentPattern(parent);
                    }
                    path = parent;
                }
                path = convertStep(path, step);
            }
        }
        if (locationPath.isAbsolute()) {
            LocationPathPattern parent = new LocationPathPattern(NodeTypeTest.DOCUMENT_TEST);
            path.setParentPattern(parent);
        }
        return answer;
    }

    protected static LocationPathPattern convertStep(LocationPathPattern path, Step step) throws JaxenException {
        if (step instanceof DefaultAllNodeStep) {
            int axis = step.getAxis();
            if (axis == Axis.ATTRIBUTE) {
                path.setNodeTest(NodeTypeTest.ATTRIBUTE_TEST);
            } else {
                path.setNodeTest(NodeTypeTest.ELEMENT_TEST);
            }
        } else if (step instanceof DefaultCommentNodeStep) {
            path.setNodeTest(NodeTypeTest.COMMENT_TEST);
        } else if (step instanceof DefaultProcessingInstructionNodeStep) {
            path.setNodeTest(NodeTypeTest.PROCESSING_INSTRUCTION_TEST);
        } else if (step instanceof DefaultTextNodeStep) {
            path.setNodeTest(TextNodeTest.SINGLETON);
        } else if (step instanceof DefaultCommentNodeStep) {
            path.setNodeTest(NodeTypeTest.COMMENT_TEST);
        } else if (step instanceof DefaultNameStep) {
            DefaultNameStep nameStep = (DefaultNameStep) step;
            String localName = nameStep.getLocalName();
            String prefix = nameStep.getPrefix();
            int axis = nameStep.getAxis();
            short nodeType = Pattern.ELEMENT_NODE;
            if (axis == Axis.ATTRIBUTE) {
                nodeType = Pattern.ATTRIBUTE_NODE;
            }
            if (nameStep.isMatchesAnyName()) {
                if (prefix.length() == 0 || prefix.equals("*")) {
                    if (axis == Axis.ATTRIBUTE) {
                        path.setNodeTest(NodeTypeTest.ATTRIBUTE_TEST);
                    } else {
                        path.setNodeTest(NodeTypeTest.ELEMENT_TEST);
                    }
                } else {
                    path.setNodeTest(new PrefixNamespaceTest(prefix, nodeType));
                }
            } else {
                NameTest nameTest = new NameTest(localName, nodeType);
                NodeTest nodeTest;
                if (prefix.length() > 0) {
                    NamespaceTest namespaceTest = new PrefixNamespaceTest(prefix, nodeType);
                    nodeTest = new NameNamespaceCompositeTest(nameTest, namespaceTest);
                } else {
                    nodeTest = nameTest;
                }
                path.setNodeTest(nodeTest);
            }
            return convertDefaultStep(path, nameStep);
        } else if (step instanceof DefaultStep) {
            return convertDefaultStep(path, (DefaultStep) step);
        } else {
            throw new JaxenException("Cannot convert: " + step + " to a Pattern");
        }
        return path;
    }

    protected static LocationPathPattern convertDefaultStep(LocationPathPattern path, DefaultStep step) throws JaxenException {
        List predicates = step.getPredicates();
        if (!predicates.isEmpty()) {
            FilterExpr filter = new DefaultFilterExpr(new PredicateSet());
            for (Iterator iter = predicates.iterator(); iter.hasNext(); ) {
                Predicate predicate = (Predicate) iter.next();
                if (!(predicate.getExpr() instanceof NumberExpr) && !containsTextNode(predicate.getExpr())) {
                    filter.addPredicate(predicate);
                }
            }
            if (!filter.getPredicates().isEmpty()) {
                path.addFilter(filter);
            }
        }
        return path;
    }

    public static boolean containsTextNode(Expr expr) {
        if (expr instanceof LocationPath) {
            for (Object step : ((LocationPath) expr).getSteps()) {
                if (step instanceof TextNodeStep) {
                    return true;
                } else {
                    for (Object predicate : ((Step) step).getPredicates()) {
                        if (containsTextNode(((Predicate) predicate).getExpr())) {
                            return true;
                        }
                    }
                }
            }
        } else if (expr instanceof BinaryExpr) {
            return containsTextNode(((BinaryExpr) expr).getLHS()) || containsTextNode(((BinaryExpr) expr).getRHS());
        }

        return false;
    }

    protected static boolean navigationStep(Step step) {
        if (step instanceof DefaultNameStep) {
            return true;
        } else if (step.getClass().equals(DefaultStep.class)) {
            return !step.getPredicates().isEmpty();
        } else {
            return true;
        }
    }

    protected static class NameNamespaceCompositeTest extends NodeTest {
        private final NameTest nameTest;
        private final NamespaceTest namespaceTest;

        public NameNamespaceCompositeTest(NameTest nameTest, NamespaceTest namespaceTest) {
            this.nameTest = nameTest;
            this.namespaceTest = namespaceTest;
        }

        public boolean matches(Object node, Context context) throws JaxenException {
            return nameTest.matches(node, context) && namespaceTest.matches(node, context);
        }

        public String getText() {
            return namespaceTest.getText() + nameTest.getText();
        }
    }
}

