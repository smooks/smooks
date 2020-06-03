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
package org.smooks.cdr.xpath;

import org.jaxen.JaxenHandler;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.Step;
import org.jaxen.expr.XPathExpr;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksResourceConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * {@link SelectorStep} Builder class.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SelectorStepBuilder {

    private static SelectorStep[] SELECTOR_NONE_STEP;

    static {
        try {
            SELECTOR_NONE_STEP = _buildSteps(SmooksResourceConfiguration.SELECTOR_NONE);
        } catch (SAXPathException e) {
            throw new IllegalStateException("Unexpected exception while constructing the 'none' SelectorStep array.");
        }
    }

    /**
     * Construct a set of selector steps from the specified selector (ala XPath expresssion steps).
     * <p/>
     * This process does not configure the namespaces on the steps.  The {@link SelectorStep#setNamespaces(SelectorStep[],java.util.Properties)}
     * method needs to be called to configure the namespaces.
     *
     * @param selectorExpression The selector expression.
     * @return The set of selector steps.
     * @throws SAXPathException Error parsing expression.
     */
    public static SelectorStep[] buildSteps(String selectorExpression) throws SAXPathException {
        if(SmooksResourceConfiguration.SELECTOR_NONE.equals(selectorExpression)) {
            return SELECTOR_NONE_STEP;
        }

        return _buildSteps(selectorExpression);
    }

    /**
     * Construct a set of selector steps from the specified selector (ala XPath expresssion steps).
     * <p/>
     * This process does not configure the namespaces on the steps.  The {@link SelectorStep#setNamespaces(SelectorStep[],java.util.Properties)}
     * method needs to be called to configure the namespaces.
     *
     * @param selectorExpression The selector expression.
     * @return The set of selector steps.
     * @throws SAXPathException Error parsing expression.
     */
    @SuppressWarnings("unchecked")
    private static SelectorStep[] _buildSteps(String selectorExpression) throws SAXPathException {
        AssertArgument.isNotNull(selectorExpression, "selectorExpression");

        String xpathExpression = toXPathExpression(selectorExpression);
        XPathReader reader = XPathReaderFactory.createReader();
        JaxenHandler handler = new JaxenHandler();
        List<SelectorStep> selectorSteps = new ArrayList<SelectorStep>();
        boolean isRooted = false;
        boolean endsStarStar = false;

        if(xpathExpression.startsWith("/")) {
            isRooted = true;
        } else if(isEncodedToken(xpathExpression)) {
            String[] tokens = xpathExpression.split("/");

            selectorSteps.add(new SelectorStep(tokens[0]));

            StringBuilder reconstructedExpression = new StringBuilder();
            for(int i = 1; i < tokens.length; i++) {
                if(reconstructedExpression.length() > 0) {
                    reconstructedExpression.append('/');
                }
                reconstructedExpression.append(tokens[i]);
            }
            xpathExpression = reconstructedExpression.toString();
        }
        if(xpathExpression.endsWith("//")) {
            endsStarStar = true;
            xpathExpression = xpathExpression.substring(0, xpathExpression.length() - 2);
        }

        if(xpathExpression.trim().length() > 0) {
            reader.setXPathHandler(handler);
            reader.parse(xpathExpression);

            XPathExpr xpath = handler.getXPathExpr();
            Expr expr = xpath.getRootExpr();
            if (!(expr instanceof LocationPath)) {
                throw new SAXPathException("Invalid XPath expression '" + xpathExpression + "'.  Selector must be a LocationPath expression. Is '" + expr.getText() + "'.");
            }

            LocationPath path = (LocationPath) expr;
            List<Step> steps = path.getSteps();

            for (int i = 0; i < steps.size(); i++) {
                Step step = steps.get(i);

                try {
                    if(step.getAxis() == Axis.ATTRIBUTE && i < steps.size() - 1) {
                        // Attribute steps are only supported as the last step...
                        throw new SAXPathException("Attribute axis steps are only supported at the end of the expression.  '" + step.getText() + "' is not at the end.");
                    } else if(step.getAxis() == Axis.DESCENDANT_OR_SELF) {
                        selectorSteps.add(new SelectorStep(xpathExpression, "**"));
                    } else if(step.getAxis() != Axis.CHILD && step.getAxis() != Axis.ATTRIBUTE) {
                        throw new SAXPathException("XPath step '" + step.getText() + "' not supported.");
                    } else {
                        if(i == steps.size() - 2) {
                            Step nextStep = steps.get(i + 1);
                            if(nextStep.getAxis() == Axis.ATTRIBUTE) {
                                selectorSteps.add(new SelectorStep(xpathExpression, step, nextStep));
                                // We end here.  The next step is the last step and we've merged it into
                                // the last evaluator...
                                break;
                            } else {
                                selectorSteps.add(new SelectorStep(xpathExpression, step));
                            }
                        } else {
                            selectorSteps.add(new SelectorStep(xpathExpression, step));
                        }
                    }
                } catch (SAXPathException e) {
                    throw new SAXPathException("Error processing XPath selector expression '" + xpathExpression + "'.", e);
                } catch (Exception e) {
                    throw new SAXPathException("Error building step evaluator.", e);
                }
            }
        }

        if(isRooted) {
            if(selectorSteps.isEmpty()) {
                selectorSteps.add(new SelectorStep(xpathExpression, SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR));
            } else {
                selectorSteps.get(0).setRooted(true);
            }
        }

        if(endsStarStar) {
            selectorSteps.add(new SelectorStep(xpathExpression, "**"));
        }

        return selectorSteps.toArray(new SelectorStep[0]);
    }

    private static boolean isEncodedToken(String xpathExpression) {
        if(xpathExpression.startsWith("#") && !xpathExpression.startsWith(SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR)) {
            return true;
        }

        return xpathExpression.startsWith("$") && !xpathExpression.startsWith(SmooksResourceConfiguration.LEGACY_DOCUMENT_FRAGMENT_SELECTOR);
    }

    /**
     * Construct a set of selector steps from the specified selector (ala XPath expresssion steps).
     *
     * @param selectorExpression The selector expression.
     * @param namespaces The namespace prefix-to-uri mappings.
     * @return The set of selector steps.
     * @throws SAXPathException Error parsing expression.
     */
    @SuppressWarnings("WeakerAccess")
    public static SelectorStep[] buildSteps(String selectorExpression, Properties namespaces) throws SAXPathException {
        SelectorStep[] steps = buildSteps(selectorExpression);
        return SelectorStep.setNamespaces(steps, namespaces);
    }

    /**
     * Create a print friendly representation of the set of selector steps.
     * @param steps The selector steps.
     * @return A print friendly representation of the set of selector steps.
     */
    public static String toString(SelectorStep[] steps) {
        AssertArgument.isNotNull(steps, "steps");

        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < steps.length; i++) {
            if(steps[i].isRooted()) {
                stringBuilder.append("/");
            } else if(i > 0) {
                stringBuilder.append("/");
            }

            stringBuilder.append(steps[i]);
        }

        return stringBuilder.toString();
    }

    /**
     * Normalize the selector expression to an XPath expression.
     * <p/>
     * Historically, the Smooks selectors support some tokens not supported by the XPath spec.
     * This method normalizes a selector expression to be a valid XPath expression.
     *
     * @param selectorExpression A selector expression.
     * @return An XPath expression.
     */
    private static String toXPathExpression(final String selectorExpression) {
        // Need to remove all space characters where they don't
        // exist inside squaere brackets...
        StringBuilder xpathExpressionBuilder = new StringBuilder();
        boolean normalize = true;

        for(int i = 0; i < selectorExpression.length(); i++) {
            char character = selectorExpression.charAt(i);

            if(character == '[') {
                normalize = false;
            } else if(character == ']') {
                normalize = true;
            } else if(character == ' ' && normalize) {
                if(xpathExpressionBuilder.charAt(xpathExpressionBuilder.length() - 1) != '/') {
                    xpathExpressionBuilder.append('/');
                }
                continue;
            }
            xpathExpressionBuilder.append(character);
        }
        String xpathExpression = xpathExpressionBuilder.toString();

        // Handle the #document token. Just replace it with a leading slash..
        xpathExpression = xpathExpression.replace(SmooksResourceConfiguration.LEGACY_DOCUMENT_FRAGMENT_SELECTOR, SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR);
        if(xpathExpression.equals(SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR)) {
            xpathExpression = "/";
        } if(xpathExpression.startsWith(SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR + "/")) {
            xpathExpression = xpathExpression.substring(SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR.length());
        }

        // Replace the legacy ** token with the XPath descendant-or-self axis token...
        xpathExpression = xpathExpression.replace("/**/", "//");
        xpathExpression = xpathExpression.replace("/**", "//");
        xpathExpression = xpathExpression.replace("**/", "//");

        return xpathExpression;
    }

    public static String[] toContextualSelector(SelectorStep[] selectorSteps) {
        String targetAttribute = extractTargetAttribute(selectorSteps);
        String[] contextualSelector;

        if(targetAttribute != null) {
            contextualSelector = new String[selectorSteps.length + 1];
            contextualSelector[contextualSelector.length - 1] = "@" + targetAttribute;
        } else {
            contextualSelector = new String[selectorSteps.length];
        }

        for(int i = 0; i < selectorSteps.length; i++) {
            contextualSelector[i] = selectorSteps[i].getTargetElement().getLocalPart();
        }

        return contextualSelector;
    }

    @SuppressWarnings("unused")
    public static String extractTargetElement(SelectorStep[] selectorSteps) {
        return selectorSteps[selectorSteps.length - 1].getTargetElement().getLocalPart();
    }

    @SuppressWarnings("WeakerAccess")
    public static String extractTargetAttribute(SelectorStep[] selectorSteps) {
        QName targetAttribute = selectorSteps[selectorSteps.length - 1].getTargetAttribute();

        if(targetAttribute == null) {
            return null;
        }

        return targetAttribute.getLocalPart();
    }
}
