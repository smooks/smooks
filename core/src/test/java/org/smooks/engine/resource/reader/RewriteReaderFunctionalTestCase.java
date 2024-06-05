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
package org.smooks.engine.resource.reader;

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.io.FragmentWriter;
import org.smooks.io.payload.StringSource;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RewriteReaderFunctionalTestCase {

    public static class RewriteResource implements ElementVisitor {
        private final TypedKey<FragmentWriter> fragmentWriterTypedKey = TypedKey.of();

        @Override
        public void visitBefore(Element element, ExecutionContext executionContext) {
            FragmentWriter fragmentWriter = new FragmentWriter(executionContext, new NodeFragment(element), false);
            executionContext.put(fragmentWriterTypedKey, fragmentWriter);
            try {
                fragmentWriter.write("<bar>");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }

        @Override
        public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
            try {
                executionContext.get(fragmentWriterTypedKey).write(characterData.getParentNode().getNodeName());
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }

        @Override
        public void visitAfter(Element element, ExecutionContext executionContext) {
            try {
                executionContext.get(fragmentWriterTypedKey).write("</bar>");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }

        @Override
        public void visitChildElement(Element childElement, ExecutionContext executionContext) {

        }
    }
    
    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-rewrite.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        StringWriter result = new StringWriter();

        smooks.filterSource(execContext, new StringSource("<helloWorld>bar</helloWorld>"), new StreamResult(result));
        assertEquals("<bar>helloWorld</bar>", result.toString());
    }
}