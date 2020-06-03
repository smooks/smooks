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
package org.smooks.delivery;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.replay.EndElementEvent;
import org.smooks.delivery.replay.SAXEventReplay;
import org.smooks.delivery.replay.StartElementEvent;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.smooks.xml.NamespaceMappings;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Abstract SAX Content Handler.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class SmooksContentHandler extends DefaultHandler2 implements SAXEventReplay {

    private ExecutionContext executionContext;
    private SmooksContentHandler parentContentHandler;
    private SmooksContentHandler nestedContentHandler;
    private NamespaceDeclarationStack namespaceDeclarationStack;
    private boolean endReplayed = false;
    private SAXEventReplay lastEvent = null;
    private StartElementEvent startEvent = new StartElementEvent();
    private EndElementEvent endEvent = new EndElementEvent();
    private int depth = 0;

    public SmooksContentHandler(ExecutionContext executionContext, SmooksContentHandler parentContentHandler) {
        this.executionContext = executionContext;
        this.parentContentHandler = parentContentHandler;
        attachHandler();

        if(parentContentHandler != null) {
            parentContentHandler.nestedContentHandler = this;
        }
    }

    public NamespaceDeclarationStack getNamespaceDeclarationStack() {
        if(namespaceDeclarationStack == null) {
            namespaceDeclarationStack = NamespaceMappings.getNamespaceDeclarationStack(executionContext);
            if(namespaceDeclarationStack == null) {
                throw new IllegalStateException("NamespaceDeclarationStack instance not set on ExecutionContext.");
            }
        }
        return namespaceDeclarationStack;
    }

    public void replayStartElement() {
        // Replay the last sax event from the parent handler on this sax handler...
        parentContentHandler.replay(this);
    }

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        getNamespaceDeclarationStack().pushNamespaces(qName, uri, attributes);

        startEvent.set(uri, localName, qName, attributes);
        lastEvent = startEvent;

        depth++;
        startElement(startEvent);

        if(nestedContentHandler != null) {
            // Replay the start element event from the parent handler onto the nested handler...
            replay(nestedContentHandler);
        }
    }

    public abstract void startElement(StartElementEvent startEvent) throws SAXException;

    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            endEvent.set(uri, localName, qName);
            lastEvent = endEvent;

            endElement(endEvent);
            depth--;
        } finally {
            if(!endReplayed && depth == 0 && parentContentHandler != null) {
                endReplayed = true;
                // Replay the last sax event from this handler onto the parent handler ...
                replay(parentContentHandler);
                // Reinstate the parent handler on the XMLReader so all events are
                // forwarded to it again ...
                XMLReader xmlReader = AbstractParser.getXMLReader(executionContext);
                xmlReader.setContentHandler(parentContentHandler);
                // Remove the nested handler (this handler) form the parent handler...
                parentContentHandler.resetNestedContentHandler();
            }
        }
        getNamespaceDeclarationStack().popNamespaces();
    }

    public abstract void endElement(EndElementEvent endEvent) throws SAXException;

    public void replay(org.xml.sax.ContentHandler handler) throws SmooksException {
        if(lastEvent != null) {
            lastEvent.replay(handler);
        }
    }

    private void attachHandler() {
        executionContext.setAttribute(DefaultHandler2.class, this);
    }

    public static SmooksContentHandler getHandler(ExecutionContext executionContext) {
        return (SmooksContentHandler) executionContext.getAttribute(DefaultHandler2.class);
    }

    public void detachHandler() {
        executionContext.removeAttribute(DefaultHandler2.class);
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public SmooksContentHandler getParentContentHandler() {
        return parentContentHandler;
    }

    public SmooksContentHandler getNestedContentHandler() {
        return nestedContentHandler;
    }

    public void resetNestedContentHandler() {
        nestedContentHandler = null;
    }

    public abstract void cleanup();
}
