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
package org.smooks.engine.delivery.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.event.VisitExecutionEvent;
import org.smooks.engine.delivery.event.VisitSequence;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.sax.ng.terminate.TerminateException;
import org.smooks.engine.lookup.GlobalParamsLookup;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.annotation.PostConstruct;
import org.w3c.dom.Node;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, DOMElementVisitor {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ExceptionInterceptor.class);
    protected static final char PATH_SEPARATOR = '/';
    protected static final String LINE_SEPARATOR = System.lineSeparator();
    protected static final String DIVIDER = LINE_SEPARATOR + "---------------------------------------------------------------------------------------------------------------------------------------" + LINE_SEPARATOR;

    protected boolean terminateOnVisitorException;
    protected String visitBeforeExceptionMessage;
    protected String visitAfterExceptionMessage;
    protected String visitChildTextExceptionMessage;
    protected String visitChildElementExceptionMessage;

    @PostConstruct
    public void postConstruct() {
        terminateOnVisitorException = Boolean.parseBoolean(applicationContext.getRegistry().lookup(new GlobalParamsLookup()).getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION));

        String errorContext = LINE_SEPARATOR + LINE_SEPARATOR + "Error Context" + DIVIDER;
        errorContext +=
                "Event => %s" + LINE_SEPARATOR +
                "Selector => " + getTarget().getResourceConfig().getSelectorPath().getSelector() + LINE_SEPARATOR +
                "Content handler => " + getTarget().getContentHandler().getClass().getName();

        visitBeforeExceptionMessage = "Error while processing start event" + errorContext;
        visitAfterExceptionMessage = "Error while processing end event" + errorContext;
        visitChildTextExceptionMessage = "Error while processing text event" + errorContext;
        visitChildElementExceptionMessage = "Error while processing child event" + errorContext;
    }

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        intercept(visitBeforeInvocation, executionContext, visitBeforeExceptionMessage, new NodeFragment(element), VisitSequence.BEFORE, element, executionContext);
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        intercept(visitAfterInvocation, executionContext, visitAfterExceptionMessage, new NodeFragment(element), VisitSequence.AFTER, element, executionContext);
    }

    @Override
    public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
        intercept(visitChildTextInvocation, executionContext, visitChildTextExceptionMessage, new NodeFragment(characterData), VisitSequence.AFTER, characterData, executionContext);
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        try {
            intercept(visitChildElementInvocation, childElement, executionContext);
        } catch (Throwable t) {
            processVisitorException(t, visitChildElementExceptionMessage, executionContext, new NodeFragment(childElement), VisitSequence.AFTER, visitorBinding);
        }
    }

    protected <T extends Visitor> void intercept(final Invocation<T> invocation, final ExecutionContext executionContext, final String exceptionMessage, final Fragment<?> fragment, final VisitSequence visitSequence, final Object... invocationArgs) {
        try {
            intercept(invocation, invocationArgs);
        } catch (Throwable t) {
            processVisitorException(t, exceptionMessage, executionContext, fragment, visitSequence, visitorBinding);
        }
    }

    protected void processVisitorException(final Throwable t, final String exceptionMessage, final ExecutionContext executionContext, final Fragment<?> fragment, final VisitSequence visitSequence, final ContentHandlerBinding<Visitor> visitorBinding) {
        for (ExecutionEventListener executionEventListener : executionContext.getContentDeliveryRuntime().getExecutionEventListeners()) {
            executionEventListener.onEvent(new VisitExecutionEvent<>(fragment, visitorBinding, visitSequence, executionContext, t));
        }

        if (t instanceof TerminateException) {
            throw (TerminateException) t;
        }

        executionContext.setTerminationError(t);
        String completeExceptionMessage = String.format(exceptionMessage, toPath((Node) fragment.unwrap()));
        if (terminateOnVisitorException) {
            if (t instanceof SmooksException) {
                throw (SmooksException) t;
            } else {
                throw new SmooksException(completeExceptionMessage + DIVIDER, t);
            }
        } else {
            LOGGER.error(completeExceptionMessage + LINE_SEPARATOR + LINE_SEPARATOR + "Stack Trace" + DIVIDER + stackTraceToString(t));
        }
    }

    protected String toPath(Node node) {
        StringBuilder path = new StringBuilder().append(PATH_SEPARATOR).append(node.getNodeName());
        node = node.getParentNode();
        while (node != null && !(node instanceof Document)) {
            path.insert(0, PATH_SEPARATOR + node.getNodeName());
            node = node.getParentNode();
        }

        return path.toString();
    }

    protected String stackTraceToString(Throwable e) {
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer, true);
        e.printStackTrace(printWriter);
        return writer.toString();
    }
}
