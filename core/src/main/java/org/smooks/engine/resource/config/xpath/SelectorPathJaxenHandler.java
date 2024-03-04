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
package org.smooks.engine.resource.config.xpath;

import org.jaxen.*;
import org.jaxen.expr.*;
import org.jaxen.saxpath.Axis;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.engine.resource.config.xpath.predicate.AttributePredicate;
import org.smooks.engine.resource.config.xpath.predicate.DefaultPredicateEvaluator;
import org.smooks.engine.resource.config.xpath.predicate.PositionPredicateEvaluator;
import org.smooks.engine.resource.config.xpath.step.AllSelectorStep;
import org.smooks.engine.resource.config.xpath.step.AttributeSelectorStep;
import org.smooks.engine.resource.config.xpath.step.DocumentSelectorStep;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;

import java.util.*;

public class SelectorPathJaxenHandler extends JaxenHandler {

    private final String selector;
    private final Properties namespaces;
    private SelectorPath selectorPath;
    private boolean isTextNode = false;
    private boolean isPredicate = false;

    public SelectorPathJaxenHandler(String selector, Properties namespaces) {
        super();
        this.selector = selector;
        this.namespaces = namespaces;
    }

    @Override
    public void setXPathFactory(XPathFactory xpathFactory) {
        super.setXPathFactory(xpathFactory);
    }

    @Override
    public XPathFactory getXPathFactory() {
        return super.getXPathFactory();
    }

    @Override
    public XPathExpr getXPathExpr() {
        return super.getXPathExpr();
    }

    @Override
    public XPathExpr getXPathExpr(boolean shouldSimplify) {
        return super.getXPathExpr(shouldSimplify);
    }

    @Override
    public void startXPath() {
        super.startXPath();
    }

    @Override
    public void endXPath() throws JaxenException {
        super.endXPath();
    }

    @Override
    public void startPathExpr() {
        super.startPathExpr();
    }

    @Override
    public void endPathExpr() throws JaxenException {
        super.endPathExpr();
    }

    @Override
    public void startAbsoluteLocationPath() throws JaxenException {
        selectorPath = new IndexedSelectorPath(selector);
        selectorPath.add(new DocumentSelectorStep());
        super.startAbsoluteLocationPath();
    }

    @Override
    public void endAbsoluteLocationPath() throws JaxenException {
        super.endAbsoluteLocationPath();
    }

    @Override
    public void startRelativeLocationPath() throws JaxenException {
        if (selectorPath == null) {
            selectorPath = new IndexedSelectorPath(selector);
        }
        super.startRelativeLocationPath();
    }

    @Override
    public void endRelativeLocationPath() throws JaxenException {
        super.endRelativeLocationPath();
    }

    @Override
    protected void endLocationPath() throws JaxenException {
        super.endLocationPath();
    }

    @Override
    protected void addSteps(LocationPath locationPath, Iterator stepIter) {
        super.addSteps(locationPath, stepIter);
    }

    @Override
    public void startNameStep(int axis, String prefix, String localName) throws JaxenException {
        if (axis == Axis.ATTRIBUTE) {
            if (isPredicate) {
                selectorPath.get(selectorPath.size() - 1).getPredicates().add(new AttributePredicate(namespaces.getProperty(prefix), localName));
            } else {
                selectorPath.add(new AttributeSelectorStep(namespaces.getProperty(prefix), localName, prefix));
            }
        } else if (!(selectorPath instanceof IndexedSelectorPath) ||
                !localName.equals("*") ||
                !(((IndexedSelectorPath) selectorPath).getTargetSelectorStep() instanceof DocumentSelectorStep)) {
            if (!isPredicate) {
                selectorPath.add(new ElementSelectorStep(namespaces.getProperty(prefix), localName, prefix));
            }
        }
        super.startNameStep(axis, prefix, localName);
    }

    @Override
    public void endNameStep() {
        super.endNameStep();
    }

    @Override
    public void startTextNodeStep(int axis) throws JaxenException {
        isTextNode = true;
        super.startTextNodeStep(axis);
    }

    @Override
    public void endTextNodeStep() {
        super.endTextNodeStep();
    }

    @Override
    public void startCommentNodeStep(int axis) throws JaxenException {
        super.startCommentNodeStep(axis);
    }

    @Override
    public void endCommentNodeStep() {
        super.endCommentNodeStep();
    }

    @Override
    public void startAllNodeStep(int axis) throws JaxenException {
        selectorPath.add(new AllSelectorStep());
        super.startAllNodeStep(axis);
    }

    @Override
    public void endAllNodeStep() {
        super.endAllNodeStep();
    }

    @Override
    public void startProcessingInstructionNodeStep(int axis, String name) throws JaxenException {
        super.startProcessingInstructionNodeStep(axis, name);
    }

    @Override
    public void endProcessingInstructionNodeStep() {
        super.endProcessingInstructionNodeStep();
    }

    @Override
    protected void endStep() {
        super.endStep();
    }

    @Override
    public void startPredicate() {
        this.isPredicate = true;
        super.startPredicate();
    }

    @Override
    public void endPredicate() throws JaxenException {
        Expr expr = (Expr) peekFrame().getFirst();
        SelectorStep currentSelectorStep = selectorPath.get(selectorPath.size() - 1);
        if (expr instanceof PathExpr) {
            if (((PathExpr) expr).getFilterExpr() != null) {
                if (((FilterExpr) ((PathExpr) expr).getFilterExpr()).getExpr() instanceof NumberExpr) {
                    selectorPath.get(selectorPath.size() - 1).getPredicates().add(new PositionPredicateEvaluator(expr, ((ElementSelectorStep) currentSelectorStep).getQName().getLocalPart(), ((ElementSelectorStep) currentSelectorStep).getQName().getNamespaceURI()));
                } else {
                    selectorPath.get(selectorPath.size() - 1).getPredicates().add(new DefaultPredicateEvaluator(expr));
                }
            }
        } else if (expr instanceof BinaryExpr) {
            if (isTextNode) {
                ((ElementSelectorStep) currentSelectorStep).setAccessesText(true);
                currentSelectorStep.getPredicates().add(new DefaultPredicateEvaluator(expr));
            }
        }
        isTextNode = false;
        isPredicate = false;
        super.endPredicate();
    }

    @Override
    public void startFilterExpr() {
        super.startFilterExpr();
    }

    @Override
    public void endFilterExpr() throws JaxenException {
        super.endFilterExpr();
    }

    @Override
    protected void addPredicates(Predicated obj, Iterator predIter) {
        super.addPredicates(obj, predIter);
    }

    @Override
    protected void returnExpr() {
        super.returnExpr();
    }

    @Override
    public void startOrExpr() {
        super.startOrExpr();
    }

    @Override
    public void endOrExpr(boolean create) throws JaxenException {
        super.endOrExpr(create);
    }

    @Override
    public void startAndExpr() {
        super.startAndExpr();
    }

    @Override
    public void endAndExpr(boolean create) throws JaxenException {
        super.endAndExpr(create);
    }

    @Override
    public void startEqualityExpr() {
        super.startEqualityExpr();
    }

    @Override
    public void endEqualityExpr(int operator) throws JaxenException {
        super.endEqualityExpr(operator);
    }

    @Override
    public void startRelationalExpr() {
        super.startRelationalExpr();
    }

    @Override
    public void endRelationalExpr(int operator) throws JaxenException {
        super.endRelationalExpr(operator);
    }

    @Override
    public void startAdditiveExpr() {
        super.startAdditiveExpr();
    }

    @Override
    public void endAdditiveExpr(int operator) throws JaxenException {
        super.endAdditiveExpr(operator);
    }

    @Override
    public void startMultiplicativeExpr() {
        super.startMultiplicativeExpr();
    }

    @Override
    public void endMultiplicativeExpr(int operator) throws JaxenException {
        super.endMultiplicativeExpr(operator);
    }

    @Override
    public void startUnaryExpr() {
        super.startUnaryExpr();
    }

    @Override
    public void endUnaryExpr(int operator) throws JaxenException {
        super.endUnaryExpr(operator);
    }

    @Override
    public void startUnionExpr() {
        super.startUnionExpr();
    }

    @Override
    public void endUnionExpr(boolean create) throws JaxenException {
        super.endUnionExpr(create);
    }

    @Override
    public void number(int number) throws JaxenException {
        super.number(number);
    }

    @Override
    public void number(double number) throws JaxenException {
        super.number(number);
    }

    @Override
    public void literal(String literal) throws JaxenException {
        super.literal(literal);
    }

    @Override
    public void variableReference(String prefix, String variableName) throws JaxenException {
        super.variableReference(prefix, variableName);
    }

    @Override
    public void startFunction(String prefix, String functionName) throws JaxenException {
        if (selectorPath == null) {
            selectorPath = new SimpleSelectorPath(selector);
        }
        super.startFunction(prefix, functionName);
    }

    @Override
    public void endFunction() {
        super.endFunction();
    }

    @Override
    protected void addParameters(FunctionCallExpr function, Iterator paramIter) {
        super.addParameters(function, paramIter);
    }

    @Override
    protected int stackSize() {
        return super.stackSize();
    }

    @Override
    protected void push(Object obj) {
        super.push(obj);
    }

    @Override
    protected Object pop() {
        return super.pop();
    }

    @Override
    protected boolean canPop() {
        return super.canPop();
    }

    @Override
    protected void pushFrame() {
        super.pushFrame();
    }

    @Override
    protected LinkedList popFrame() {
        return super.popFrame();
    }

    @Override
    protected LinkedList peekFrame() {
        return super.peekFrame();
    }

    public SelectorPath getSelectorPath() {
        return selectorPath;
    }

    public String getSelector() {
        return selector;
    }
}
