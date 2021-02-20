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
package org.smooks.engine.delivery.fragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.expression.ExecutionContextExpressionEvaluator;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.config.xpath.XPathExpressionEvaluator;

import javax.xml.namespace.QName;

public class SAXElementFragment implements Fragment<SAXElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SAXElementFragment.class);

    private final SAXElement saxElement;

    public SAXElementFragment(final SAXElement saxElement) {
        this.saxElement = saxElement;
    }
    
    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SAXElement unwrap() {
        return saxElement;
    }

    @Override
    public boolean reserve(long id, Object token) {
        return true;
    }
    
    @Override
    public boolean release(long id, Object token) {
        return true;
    }

    @Override
    public boolean isMatch(SelectorPath selectorPath, ExecutionContext executionContext) {
        if (selectorPath.getConditionEvaluator() != null && !assertConditionTrue(executionContext, selectorPath)) {
            return false;
        }
        
        if (selectorPath.getSelectorNamespaceURI() != null) {
            if (!isTargetedAtNamespace(saxElement.getName().getNamespaceURI(), selectorPath)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + saxElement.getName() + "].  Element not in namespace [" + selectorPath.getSelectorNamespaceURI() + "].");
                }
                return false;
            }
        } else {
            // We don't test the SelectorStep namespace if a namespace is configured on the
            // resource configuration.  This is why we have this code inside the else block.
            if (!selectorPath.getTargetSelectorStep().isTargetedAtNamespace(saxElement.getName().getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + saxElement.getName() + "].  Element not in namespace [" + selectorPath.getTargetSelectorStep().getElement().getNamespaceURI() + "].");
                }
                return false;
            }
        }

        XPathExpressionEvaluator evaluator = selectorPath.getTargetSelectorStep().getPredicatesEvaluator();
        if (evaluator == null) {
            LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
        } else if (!evaluator.evaluate(this, executionContext)) {
            return false;
        }

        if (selectorPath.size() > 1 && !isTargetedAtElementContext(saxElement, executionContext, selectorPath)) {
            // Note: If the selector is not contextual, there's no need to perform the
            // isTargetedAtElementContext check because we already know the visitor is targeted at the
            // element by name - because we looked it up by name in the 1st place (at least that's the assumption).
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not applying resource [" + this + "] to element [" + saxElement.getName() + "].  This resource is only targeted at '" + saxElement.getName().getLocalPart() + "' when in the following context '" + selectorPath.getSelector() + "'.");
            }
            return false;
        }

        return true;
    }

    /**
     * Is this resource configuration targeted at the specified SAX element
     * in context.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     * <p/>
     * Note this doesn't perform any namespace checking.
     *
     * @param saxElement          The element to check against.
     * @param executionContext The current execution context.
     * @return True if this resource configuration is targeted at the specified
     * element in context, otherwise false.
     */
    private boolean isTargetedAtElementContext(SAXElement saxElement, ExecutionContext executionContext, SelectorPath selectorPath) {
        SAXElement currentElement = saxElement;
        ContextIndex index = new ContextIndex(executionContext);

        index.i = selectorPath.size() - 1;

        // Unless it's **, start at the parent because the current element
        // has already been tested...
        if (!selectorPath.get(index.i).isStarStar()) {
            index.i = selectorPath.size() - 2;
            currentElement = saxElement.getParent();
        } else {
            // The target selector step is "**".  If the parent one is "#document" and we're at
            // the root now, then fail...
            if (selectorPath.size() == 2 && selectorPath.get(0).isRooted() && saxElement.getParent() == null) {
                return false;
            }
        }

        if (currentElement == null) {
            return false;
        }

        // Check the element name(s).
        while (index.i >= 0) {
            SAXElement parentElement = currentElement.getParent();

            if (!isTargetedAtElementContext(currentElement, parentElement, index, selectorPath)) {
                return false;
            }

            if (parentElement == null) {
                return true;
            }

            currentElement = parentElement;
        }

        return true;
    }

    private boolean isTargetedAtElementContext(SAXElement saxElement, SAXElement parentSaxElement, ContextIndex index, SelectorPath selectorPath) {
        if (selectorPath.get(index.i).isRooted() && parentSaxElement != null) {
            return false;
        } else if (selectorPath.get(index.i).isStar()) {
            index.i--;
        } else if (selectorPath.get(index.i).isStarStar()) {
            if (index.i == 0) {
                // No more tokens to match and ** matches everything
                return true;
            } else if (index.i == 1) {
                SelectorStep parentStep = selectorPath.get(0);

                if (parentSaxElement == null && parentStep.isRooted()) {
                    // we're at the root of the document and the only selector left is
                    // the document selector.  Pass..
                    return true;
                } else if (parentSaxElement == null) {
                    // we're at the root of the document, yet there are still
                    // unmatched tokens in the selector.  Fail...
                    return false;
                }
            } else if (parentSaxElement == null) {
                // we're at the root of the document, yet there are still
                // unmatched tokens in the selector.  Fail...
                return false;
            }

            SelectorStep parentStep = selectorPath.get(index.i - 1);

            if (isTargetedAtElement(parentSaxElement, selectorPath, parentStep)) {
                if (!parentStep.isStarStar()) {
                    XPathExpressionEvaluator evaluator = parentStep.getPredicatesEvaluator();
                    if (evaluator == null) {
                        LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                    } else if (!evaluator.evaluate(new SAXElementFragment(parentSaxElement), index.executionContext)) {
                        return false;
                    }
                }
                index.i--;
            }
        } else if (!isTargetedAtElement(saxElement, selectorPath, selectorPath.get(index.i))) {
            return false;
        } else {
            if (!selectorPath.get(index.i).isStarStar()) {
                XPathExpressionEvaluator evaluator = selectorPath.get(index.i).getPredicatesEvaluator();
                if (evaluator == null) {
                    LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                } else if (!evaluator.evaluate(new SAXElementFragment(saxElement), index.executionContext)) {
                    return false;
                }
            }
            index.i--;
        }

        if (parentSaxElement == null) {
            if (index.i >= 0 && !selectorPath.get(index.i).isStarStar()) {
                return selectorPath.get(index.i).isRooted();
            }
        }

        return true;
    }

    protected boolean isTargetedAtElement(SAXElement saxElement, SelectorPath selectorPath, SelectorStep selectorStep) {
        QName qname = saxElement.getName();

        if(selectorStep.isStar() || selectorStep.isStarStar()) {
            return true;
        }

        if(!qname.getLocalPart().equalsIgnoreCase(selectorStep.getElement().getLocalPart())) {
            return false;
        }

        return isTargetedAtNamespace(qname.getNamespaceURI(), selectorPath);
    }
    
    /**
     * Is this resource configuration targets at the same namespace as the
     * specified elemnt.
     *
     * @param namespace The element to check against.
     * @return True if this resource config is targeted at the element namespace,
     * or if the resource is not targeted at any namespace (i.e. not specified),
     * otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Deprecated
    protected boolean isTargetedAtNamespace(String namespace, SelectorPath selectorPath) {
        if (selectorPath.getSelectorNamespaceURI() != null) {
            return selectorPath.getSelectorNamespaceURI().equals(namespace);
        }

        return true;
    }
    
    protected boolean assertConditionTrue(ExecutionContext executionContext, SelectorPath selectorPath) {
        if (selectorPath.getConditionEvaluator() == null) {
            return true;
        }

        return ((ExecutionContextExpressionEvaluator) selectorPath.getConditionEvaluator()).eval(executionContext);
    }

    protected static class ContextIndex {
        private int i;
        private final ExecutionContext executionContext;

        public ContextIndex(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }
    }
}